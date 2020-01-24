/*
 * Copyright (c) 2015 SONATA-NFV, UCL, NOKIA, THALES, NCSR Demokritos ALL RIGHTS RESERVED.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Neither the name of the SONATA-NFV, UCL, NOKIA, NCSR Demokritos nor the names of its contributors
 * may be used to endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * This work has been performed in the framework of the SONATA project, funded by the European
 * Commission under Grant number 671517 through the Horizon 2020 and 5G-PPP programmes. The authors
 * would like to acknowledge the contributions of their colleagues of the SONATA partner consortium
 * (www.sonata-nfv.eu).
 *
 * @author Dario Valocchi (Ph.D.), UCL
 * 
 */

package sonata.kernel.adaptor.wrapper.vpnaas;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;
import sonata.kernel.adaptor.commons.NapObject;
import sonata.kernel.adaptor.commons.QosObject;
import sonata.kernel.adaptor.wrapper.*;

import java.io.IOException;
import java.util.ArrayList;

public class WimVpnaasWrapper extends WimWrapper {

  private static final org.slf4j.Logger Logger =
      LoggerFactory.getLogger(WimVpnaasWrapper.class);

  public WimVpnaasWrapper(WimWrapperConfiguration config) {
    super(config);
  }

  @Override
  public boolean configureNetwork(String instanceId, String vlId, NapObject ingress, NapObject egress, QosObject qos, Boolean bidirectional) {

    boolean out = true;
    //Check if already exist this vpn connection
    WimServiceConfiguration serviceConfiguration = WrapperBay.getInstance().getWimRepo().getServiceConfigurationFromInstance(instanceId,vlId);
    if (serviceConfiguration == null) {
      // Add entry to DB
      WrapperBay.getInstance().getWimRepo().writeServiceInstanceEntry(instanceId, vlId, ingress.getLocation(), egress.getLocation(), this.getWimConfig().getUuid());

      //// Get information from ingress side (VIM Left)
      // Get router_id from VIM DB for ingress location (VIM Left)
      String routerIdLeft = null;
      VimWrapperConfiguration vimConfigLeft = WrapperBay.getInstance().getVimRepo().getVimConfig(ingress.getLocation());
      if (vimConfigLeft.getConfiguration() != null) {
        //If is heat, parse configuration json
        if (vimConfigLeft.getVimVendor() == ComputeVimVendor.getByName("heat")) {
          JSONTokener tokener = new JSONTokener(vimConfigLeft.getConfiguration());
          JSONObject object = (JSONObject) tokener.nextValue();
          if (object.has("tenant_ext_router")) {
            routerIdLeft = object.getString("tenant_ext_router");
          }
        }
      }
      if (routerIdLeft == null) {
        Logger.error("Router id for vim uuid " + ingress.getLocation() + " not exist in DB.");
        out = false;
        return out;
      }

      // Get subnet_id from Openstack VIM Left for the ingress NAP (FIP Left)
      Subnet subnetLeft = null;
      String subnetIdLeft = null;
      subnetLeft = getSubnet(vimConfigLeft,ingress.getNap());
      if (subnetLeft != null) {
        subnetIdLeft = subnetLeft.getId();
      }
      if (subnetIdLeft == null) {
        Logger.error("Subnet id for vim uuid " + ingress.getLocation() + " and floating ip " + ingress.getNap() +
            " not exist in OpenStack.");
        out = false;
        return out;
      }
      // Get cidr from Openstack VIM Left for the ingress NAP (FIP Left)
      String subnetCidrLeft = null;
      if (subnetLeft != null) {
        subnetCidrLeft = subnetLeft.getCidr();
      }
      if (subnetCidrLeft == null) {
        Logger.error("Subnet cidr for vim uuid " + ingress.getLocation() + " and floating ip " + ingress.getNap() +
            " not exist in OpenStack.");
        out = false;
        return out;
      }

      // Get router_ip from Openstack VIM Left
      Router routerLeft = null;
      String routerIpLeft = null;
      routerLeft = getRouter(vimConfigLeft, routerIdLeft);
      if (routerLeft != null) {
        routerIpLeft = routerLeft.getIp();
      }
      if (routerIpLeft == null) {
        Logger.error("Router ip for vim uuid " + ingress.getLocation() + " not exist in OpenStack.");
        out = false;
        return out;
      }

      Logger.info("Left=>  router_id: " + routerIdLeft + " subnet_id: " + subnetIdLeft + " cidr: " + subnetCidrLeft + " router_ip: " + routerIpLeft);


      //// Get information from egress side (VIM Right)
      // Get router_id from VIM DB for egress location (VIM Right)
      String routerIdRight = null;
      VimWrapperConfiguration vimConfigRight = WrapperBay.getInstance().getVimRepo().getVimConfig(egress.getLocation());
      if (vimConfigRight.getConfiguration() != null) {
        //If is heat, parse configuration json
        if (vimConfigRight.getVimVendor() == ComputeVimVendor.getByName("heat")) {
          JSONTokener tokener = new JSONTokener(vimConfigRight.getConfiguration());
          JSONObject object = (JSONObject) tokener.nextValue();
          if (object.has("tenant_ext_router")) {
            routerIdRight = object.getString("tenant_ext_router");
          }
        }
      }
      if (routerIdRight == null) {
        Logger.error("Router id for vim uuid " + egress.getLocation() + " not exist in DB.");
        out = false;
        return out;
      }

      // Get subnet_id from Openstack VIM Right for the egress NAP (FIP Right)
      Subnet subnetRight = null;
      String subnetIdRight = null;
      subnetRight = getSubnet(vimConfigRight,egress.getNap());
      if (subnetRight != null) {
        subnetIdRight = subnetRight.getId();
      }
      if (subnetIdRight == null) {
        Logger.error("Subnet id for vim uuid " + egress.getLocation() + " and floating ip " + egress.getNap() +
            " not exist in OpenStack.");
        out = false;
        return out;
      }
      // Get cidr from Openstack VIM Right for the egress NAP (FIP Right)
      String subnetCidrRight = null;
      if (subnetRight != null) {
        subnetCidrRight = subnetRight.getCidr();
      }
      if (subnetCidrRight == null) {
        Logger.error("Subnet cidr for vim uuid " + egress.getLocation() + " and floating ip " + egress.getNap() +
            " not exist in OpenStack.");
        out = false;
        return out;
      }

      // Get router_ip from Openstack VIM Right
      Router routerRight = null;
      String routerIpRight = null;
      routerRight = getRouter(vimConfigRight, routerIdRight);
      if (routerRight != null) {
        routerIpRight = routerRight.getIp();
      }
      if (routerIpRight == null) {
        Logger.error("Router ip for vim uuid " + egress.getLocation() + " not exist in OpenStack.");
        out = false;
        return out;
      }

      Logger.info("Right=>  router_id: " + routerIdRight + " subnet_id: " + subnetIdRight + " cidr: " + subnetCidrRight + " router_ip: " + routerIpRight);


      //// Create the VPN connection in the VIM Left (use of instance_id/vl_id in the names of resources)
      //Check if vpn service already exist
      // Create the vpn service or get the id of the existent (use router_id Left)
      String vpnServiceIdLeft = getVpnService(vimConfigLeft,"vpn_5");
      if (vpnServiceIdLeft == null) {
        vpnServiceIdLeft = createVpnService(vimConfigLeft, "vpn_5", routerIdLeft);
      }
      if (vpnServiceIdLeft == null) {
        Logger.error("Failed creating Vpn Service for vim uuid " + ingress.getLocation());
        out = false;
        return out;
      }

      //Check if ike policy already exist
      // Create the ike policy or get the id of the existent (lifetime 60s)
      String ikePolicyIdLeft = getIkePolicy(vimConfigLeft,"ikepolicy_5");
      if (ikePolicyIdLeft == null) {
        ikePolicyIdLeft = createIkePolicy(vimConfigLeft, "ikepolicy_5", "60");
      }
      if (ikePolicyIdLeft == null) {
        Logger.error("Failed creating Ike Policy for vim uuid " + ingress.getLocation());
        out = false;
        return out;
      }

      //Check if ipsec policy already exist
      // Create the ipsec policy or get the id of the existent (lifetime 60s)
      String ipsecPolicyIdLeft = getIpsecPolicy(vimConfigLeft,"ipsecpolicy_5");
      if (ipsecPolicyIdLeft == null) {
        ipsecPolicyIdLeft = createIpsecPolicy(vimConfigLeft, "ipsecpolicy_5", "60");
      }
      if (ipsecPolicyIdLeft == null) {
        Logger.error("Failed creating Ipsec Policy for vim uuid " + ingress.getLocation());
        out = false;
        return out;
      }
      // Create the endpoint group for subnet (use subnet_id Left)
      String subnetEndpointGroupIdLeft = getEndpointGroup(vimConfigLeft,"subnet_5");
      if (subnetEndpointGroupIdLeft == null) {
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(subnetIdLeft);
        subnetEndpointGroupIdLeft = createEndpointGroup(vimConfigLeft, "subnet_5", "subnet", endpoints);
      }
      if (subnetEndpointGroupIdLeft == null) {
        Logger.error("Failed creating Endpoint Group subnet for vim uuid " + ingress.getLocation());
        out = false;
        return out;
      }

      // Create the endpoint group for cidr (use cidr Right)
      String cidrEndpointGroupIdLeft = getEndpointGroup(vimConfigLeft,"cidr_5");
      if (cidrEndpointGroupIdLeft == null) {
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(subnetCidrRight);
        cidrEndpointGroupIdLeft = createEndpointGroup(vimConfigLeft, "cidr_5", "cidr", endpoints);
      }
      if (cidrEndpointGroupIdLeft == null) {
        Logger.error("Failed creating Endpoint Group cidr for vim uuid " + ingress.getLocation());
        out = false;
        return out;
      }

      // create the ipsec connection (use router_ip Right and the ids of the resources created)
      String ipsecConnectionIdLeft = getIpsecConnection(vimConfigLeft,"vpnconnection_5");
      if (ipsecConnectionIdLeft == null) {
        ipsecConnectionIdLeft = createIpsecConnection(vimConfigLeft, "vpnconnection_5", vpnServiceIdLeft,
            ikePolicyIdLeft, ipsecPolicyIdLeft, routerIpRight, routerIpRight, "secret", subnetEndpointGroupIdLeft,
            cidrEndpointGroupIdLeft);
      }
      if (ipsecConnectionIdLeft == null) {
        Logger.error("Failed creating Ipsec Connection for vim uuid " + ingress.getLocation());
        out = false;
        return out;
      }
      Logger.info("Left=>  vpn_service_id: " + vpnServiceIdLeft + " ike_policy_id: " + ikePolicyIdLeft +
          " ipsec_policy_id: " + ipsecPolicyIdLeft + " subnet_endpoint_group_id: " + subnetEndpointGroupIdLeft
          + " cidr_endpoint_group_id: " + cidrEndpointGroupIdLeft + " ipsec_connection_id: " + ipsecConnectionIdLeft);

      //// Create the VPN connection in the VIM Right (use of instance_id/vl_id in the names of resources)
      //Check if vpn service already exist
      // Create the vpn service or get the id of the existent (use router_id Right)
      String vpnServiceIdRight = getVpnService(vimConfigRight,"vpn_5");
      if (vpnServiceIdRight == null) {
        vpnServiceIdRight = createVpnService(vimConfigRight, "vpn_5", routerIdRight);
      }
      if (vpnServiceIdRight == null) {
        Logger.error("Failed creating Vpn Service for vim uuid " + egress.getLocation());
        out = false;
        return out;
      }
      //Check if ike policy already exist
      // Create the ike policy or get the id of the existent (lifetime 60s)
      String ikePolicyIdRight = getIkePolicy(vimConfigRight,"ikepolicy_5");
      if (ikePolicyIdRight == null) {
        ikePolicyIdRight = createIkePolicy(vimConfigRight, "ikepolicy_5", "60");
      }
      if (ikePolicyIdRight == null) {
        Logger.error("Failed creating Ike Policy for vim uuid " + egress.getLocation());
        out = false;
        return out;
      }

      //Check if ipsec policy already exist
      // Create the ipsec policy or get the id of the existent (lifetime 60s)
      String ipsecPolicyIdRight = getIpsecPolicy(vimConfigRight,"ipsecpolicy_5");
      if (ipsecPolicyIdRight == null) {
        ipsecPolicyIdRight = createIpsecPolicy(vimConfigRight, "ipsecpolicy_5", "60");
      }
      if (ipsecPolicyIdRight == null) {
        Logger.error("Failed creating Ipsec Policy for vim uuid " + egress.getLocation());
        out = false;
        return out;
      }

      // Create the endpoint group for subnet (use subnet_id Right)
      String subnetEndpointGroupIdRight = getEndpointGroup(vimConfigRight,"subnet_5");
      if (subnetEndpointGroupIdRight == null) {
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(subnetIdRight);
        subnetEndpointGroupIdRight = createEndpointGroup(vimConfigRight, "subnet_5", "subnet", endpoints);
      }
      if (subnetEndpointGroupIdRight == null) {
        Logger.error("Failed creating Endpoint Group subnet for vim uuid " + egress.getLocation());
        out = false;
        return out;
      }

      // Create the endpoint group for cidr (use cidr Left)
      String cidrEndpointGroupIdRight = getEndpointGroup(vimConfigRight,"cidr_5");
      if (cidrEndpointGroupIdRight == null) {
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(subnetCidrLeft);
        cidrEndpointGroupIdRight = createEndpointGroup(vimConfigRight, "cidr_5", "cidr", endpoints);
      }
      if (cidrEndpointGroupIdRight == null) {
        Logger.error("Failed creating Endpoint Group cidr for vim uuid " + egress.getLocation());
        out = false;
        return out;
      }

      // create the ipsec connection (use router_ip Left and the ids of the resources created)
      String ipsecConnectionIdRight = getIpsecConnection(vimConfigRight,"vpnconnection_5");
      if (ipsecConnectionIdRight == null) {
        ipsecConnectionIdRight = createIpsecConnection(vimConfigRight, "vpnconnection_5", vpnServiceIdRight,
            ikePolicyIdRight, ipsecPolicyIdRight, routerIpLeft, routerIpLeft, "secret", subnetEndpointGroupIdRight, cidrEndpointGroupIdRight);
      }
      if (ipsecConnectionIdRight == null) {
        Logger.error("Failed creating Ipsec Connection for vim uuid " + egress.getLocation());
        out = false;
        return out;
      }

      Logger.info("Right=>  vpn_service_id: " + vpnServiceIdRight + " ike_policy_id: " + ikePolicyIdRight +
          " ipsec_policy_id: " + ipsecPolicyIdRight + " subnet_endpoint_group_id: " + subnetEndpointGroupIdRight
          + " cidr_endpoint_group_id: " + cidrEndpointGroupIdRight + " ipsec_connection_id: " + ipsecConnectionIdRight);

      Logger.info("WAN Configured");
    } else {
      Logger.warn("VPN for the instance id " + instanceId + " and vl id " + vlId + " already exist.");
      out = false;
    }
    return out;
  }

  @Override
  public boolean removeNetConfiguration(String instanceId, String vlId) {

    boolean out = true;
    //Check if exist this vpn connection
    WimServiceConfiguration serviceConfiguration = WrapperBay.getInstance().getWimRepo().getServiceConfigurationFromInstance(instanceId,vlId);
    if (serviceConfiguration != null) {
      //Get info from DB (Vim Left and VIM Right)
      NapObject ingress = new NapObject(serviceConfiguration.getIngress(),null);
      NapObject egress = new NapObject(serviceConfiguration.getEgress(),null);

      //Remove entry from DB
      WrapperBay.getInstance().getWimRepo().removeServiceInstanceEntry(instanceId, vlId);

      //// Remove the VPN connection in the VIM Left (use of instance_id/vl_id in the names of resources)
      VimWrapperConfiguration vimConfigLeft = WrapperBay.getInstance().getVimRepo().getVimConfig(ingress.getLocation());
      // Delete the ipsec connection
      String ipsecConnectionIdLeft = getIpsecConnection(vimConfigLeft,"vpnconnection_5");
      if (ipsecConnectionIdLeft != null) {
        deleteIpsecConnection(vimConfigLeft, "vpnconnection_5");
      }

      // Delete the endpoint group for cidr
      String cidrEndpointGroupIdLeft = getEndpointGroup(vimConfigLeft,"cidr_5");
      if (cidrEndpointGroupIdLeft != null) {
        deleteEndpointGroup(vimConfigLeft, "cidr_5");
      }

      // Delete the endpoint group for subnet
      String subnetEndpointGroupIdLeft = getEndpointGroup(vimConfigLeft,"subnet_5");
      if (subnetEndpointGroupIdLeft != null) {
        deleteEndpointGroup(vimConfigLeft, "subnet_5");
      }

      //Check if ipsec policy already used for other connection
      // Delete the ipsec policy
      String ipsecPolicyIdLeft = getIpsecPolicy(vimConfigLeft,"ipsecpolicy_5");
      if (ipsecPolicyIdLeft != null) {
        deleteIpsecPolicy(vimConfigLeft, "ipsecpolicy_5");
      }

      //Check if ike policy already used for other connection
      // Delete the ike policy
      String ikePolicyIdLeft = getIkePolicy(vimConfigLeft,"ikepolicy_5");
      if (ikePolicyIdLeft != null) {
        deleteIkePolicy(vimConfigLeft, "ikepolicy_5");
      }
      //Check if vpn service already used for other connection
      // Delete the vpn service
      String vpnServiceIdLeft = getVpnService(vimConfigLeft,"vpn_5");
      if (vpnServiceIdLeft != null) {
        deleteVpnService(vimConfigLeft, "vpn_5");
      }

      Logger.info("Left=>  vpn_service_id: " + vpnServiceIdLeft + " ike_policy_id: " + ikePolicyIdLeft +
          " ipsec_policy_id: " + ipsecPolicyIdLeft + " subnet_endpoint_group_id: " + subnetEndpointGroupIdLeft
          + " cidr_endpoint_group_id: " + cidrEndpointGroupIdLeft + " ipsec_connection_id: " + ipsecConnectionIdLeft);

      //// Remove the VPN connection in the VIM Right (use of instance_id/vl_id in the names of resources)
      VimWrapperConfiguration vimConfigRight = WrapperBay.getInstance().getVimRepo().getVimConfig(egress.getLocation());
      // Delete the ipsec connection
      String ipsecConnectionIdRight = getIpsecConnection(vimConfigRight,"vpnconnection_5");
      if (ipsecConnectionIdRight != null) {
        deleteIpsecConnection(vimConfigRight, "vpnconnection_5");
      }

      // Delete the endpoint group for cidr
      String cidrEndpointGroupIdRight = getEndpointGroup(vimConfigRight,"cidr_5");
      if (cidrEndpointGroupIdRight != null) {
        deleteEndpointGroup(vimConfigRight, "cidr_5");
      }
      // Delete the endpoint group for subnet
      String subnetEndpointGroupIdRight = getEndpointGroup(vimConfigRight,"subnet_5");
      if (subnetEndpointGroupIdRight != null) {
        deleteEndpointGroup(vimConfigRight, "subnet_5");
      }

      //Check if ipsec policy already used for other connection
      // Delete the ipsec policy
      String ipsecPolicyIdRight = getIpsecPolicy(vimConfigRight,"ipsecpolicy_5");
      if (ipsecPolicyIdRight != null) {
        deleteIpsecPolicy(vimConfigRight, "ipsecpolicy_5");
      }

      //Check if ike policy already used for other connection
      // Delete the ike policy
      String ikePolicyIdRight = getIkePolicy(vimConfigRight,"ikepolicy_5");
      if (ikePolicyIdRight != null) {
        deleteIkePolicy(vimConfigRight, "ikepolicy_5");
      }

      //Check if vpn service already used for other connection
      // Delete the vpn service
      String vpnServiceIdRight = getVpnService(vimConfigRight,"vpn_5");
      if (vpnServiceIdRight != null) {
        deleteVpnService(vimConfigRight, "vpn_5");
      }

      Logger.info("Right=>  vpn_service_id: " + vpnServiceIdRight + " ike_policy_id: " + ikePolicyIdRight +
          " ipsec_policy_id: " + ipsecPolicyIdRight + " subnet_endpoint_group_id: " + subnetEndpointGroupIdRight
          + " cidr_endpoint_group_id: " + cidrEndpointGroupIdRight + " ipsec_connection_id: " + ipsecConnectionIdRight);

      Logger.info("WAN configuration removed");
    } else {
      Logger.warn("VPN for the instance id " + instanceId + " and vl id " + vlId + " not exist.");
      out = false;
    }

    return out;
  }


  public Router getRouter(VimWrapperConfiguration vimConfig, String routerId) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    Router output = null;
    Logger.info("OpenStack wrapper - Getting router ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.getRouter(routerId);

      Logger.info("OpenStack wrapper - Router retrieved.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]getRouters-time: " + (stop - start) + " ms");
    return output;
  }

  public Subnet getSubnet(VimWrapperConfiguration vimConfig, String fIp) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    Subnet output = null;
    Logger.info("OpenStack wrapper - Getting subnet ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.getSubnet(fIp);

      Logger.info("OpenStack wrapper - Subnet retrieved.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]getSubnet-time: " + (stop - start) + " ms");
    return output;
  }

  public String getIkePolicy(VimWrapperConfiguration vimConfig, String name) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Getting Ike Policy ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.getIkePolicy(name);

      Logger.info("OpenStack wrapper - Ike Policy retrieved.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]getIkePolicy-time: " + (stop - start) + " ms");
    return output;
  }

  public String createIkePolicy(VimWrapperConfiguration vimConfig, String name, String lifetime) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Creating Ike Policy ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.createIkePolicy(name,lifetime);

      Logger.info("OpenStack wrapper - Ike Policy created.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]createIkePolicy-time: " + (stop - start) + " ms");
    return output;
  }

  public String deleteIkePolicy(VimWrapperConfiguration vimConfig, String name) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Deleting Ike Policy ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.deleteIkePolicy(name);

      Logger.info("OpenStack wrapper - Ike Policy deleted.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]deleteIkePolicy-time: " + (stop - start) + " ms");
    return output;
  }


  public String getIpsecPolicy(VimWrapperConfiguration vimConfig, String name) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Getting Ipsec Policy ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.getIpsecPolicy(name);

      Logger.info("OpenStack wrapper - Ipsec Policy retrieved.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]getIpsecPolicy-time: " + (stop - start) + " ms");
    return output;
  }

  public String createIpsecPolicy(VimWrapperConfiguration vimConfig, String name, String lifetime) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Creating Ipsec Policy ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.createIpsecPolicy(name,lifetime);

      Logger.info("OpenStack wrapper - Ipsec Policy created.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]createIpsecPolicy-time: " + (stop - start) + " ms");
    return output;
  }

  public String deleteIpsecPolicy(VimWrapperConfiguration vimConfig, String name) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Deleting Ipsec Policy ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.deleteIpsecPolicy(name);

      Logger.info("OpenStack wrapper - Ipsec Policy deleted.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]deleteIpsecPolicy-time: " + (stop - start) + " ms");
    return output;
  }


  public String getVpnService(VimWrapperConfiguration vimConfig, String name) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Getting Vpn Service ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.getVpnService(name);

      Logger.info("OpenStack wrapper - Vpn Service retrieved.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]getVpnService-time: " + (stop - start) + " ms");
    return output;
  }

  public String createVpnService(VimWrapperConfiguration vimConfig, String name, String routerId) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Creating Vpn Service ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.createVpnService(name,routerId);

      Logger.info("OpenStack wrapper - Vpn Service created.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]createVpnService-time: " + (stop - start) + " ms");
    return output;
  }

  public String deleteVpnService(VimWrapperConfiguration vimConfig, String name) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Deleting Vpn Service ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.deleteVpnService(name);

      Logger.info("OpenStack wrapper - Vpn Service deleted.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]deleteVpnService-time: " + (stop - start) + " ms");
    return output;
  }

  public String getEndpointGroup(VimWrapperConfiguration vimConfig, String name) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Getting Endpoint Group ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.getEndpointGroup(name);

      Logger.info("OpenStack wrapper - Endpoint Group retrieved.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]getEndpointGroup-time: " + (stop - start) + " ms");
    return output;
  }

  public String createEndpointGroup(VimWrapperConfiguration vimConfig, String name, String type, ArrayList<String> endpoints) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Creating Endpoint Group ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.createEndpointGroup(name,type,endpoints);

      Logger.info("OpenStack wrapper - Endpoint Group created.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]createEndpointGroup-time: " + (stop - start) + " ms");
    return output;
  }

  public String deleteEndpointGroup(VimWrapperConfiguration vimConfig, String name) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Deleting Endpoint Group ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.deleteEndpointGroup(name);

      Logger.info("OpenStack wrapper - Endpoint Group deleted.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]deleteEndpointGroup-time: " + (stop - start) + " ms");
    return output;
  }

  public String getIpsecConnection(VimWrapperConfiguration vimConfig, String name) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Getting Ipsec Connection ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.getIpsecConnection(name);

      Logger.info("OpenStack wrapper - Ipsec Connection retrieved.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]getIpsecConnection-time: " + (stop - start) + " ms");
    return output;
  }

  public String createIpsecConnection(VimWrapperConfiguration vimConfig, String name, String vpnServiceId,
                                      String ikePolicyId, String ipsecPolicyId, String peerAddress, String peerId,
                                      String psk, String subnetEpGroupId, String cidrEpGroupId) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Creating Ipsec Connection ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.createIpsecConnection(name, vpnServiceId, ikePolicyId,
          ipsecPolicyId, peerAddress, peerId, psk, subnetEpGroupId, cidrEpGroupId);

      Logger.info("OpenStack wrapper - IpsecConnection created.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]createIpsecConnection-time: " + (stop - start) + " ms");
    return output;
  }

  public String deleteIpsecConnection(VimWrapperConfiguration vimConfig, String name) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(vimConfig.getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    String output = null;
    Logger.info("OpenStack wrapper - Deleting Ipsec Connection ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(vimConfig.getVimEndpoint().toString(),
          vimConfig.getAuthUserName(), vimConfig.getAuthPass(), vimConfig.getDomain(), tenant, identityPort);

      output = neutronClient.deleteIpsecConnection(name);

      Logger.info("OpenStack wrapper - Ipsec Connection deleted.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]deleteIpsecConnection-time: " + (stop - start) + " ms");
    return output;
  }

}
