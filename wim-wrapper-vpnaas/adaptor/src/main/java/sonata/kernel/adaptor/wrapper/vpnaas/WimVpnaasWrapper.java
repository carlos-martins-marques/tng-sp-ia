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

      //TODO
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

      //Check if ike policy already exist
      // Create the ike policy or get the id of the existent (lifetime 60s)

      //Check if ipsec policy already exist
      // Create the ipsec policy or get the id of the existent (lifetime 60s)

      // Create the endpoint group for subnet (use subnet_id Left)

      // Create the endpoint group for cidr (use cidr Right)

      // create the ipsec connection (use router_ip Right and the ids of the resources created)


      //// Create the VPN connection in the VIM Right (use of instance_id/vl_id in the names of resources)
      //Check if vpn service already exist
      // Create the vpn service or get the id of the existent (use router_id Right)

      //Check if ike policy already exist
      // Create the ike policy or get the id of the existent (lifetime 60s)

      //Check if ipsec policy already exist
      // Create the ipsec policy or get the id of the existent (lifetime 60s)

      // Create the endpoint group for subnet (use subnet_id Right)

      // Create the endpoint group for cidr (use cidr Left)

      // create the ipsec connection (use router_ip Left and the ids of the resources created)
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
      String vimLeft = serviceConfiguration.getIngress();
      String vimRight = serviceConfiguration.getEgress();

      //Remove entry from DB
      WrapperBay.getInstance().getWimRepo().removeServiceInstanceEntry(instanceId, vlId);

      //TODO
      //// Remove the VPN connection in the VIM Left (use of instance_id/vl_id in the names of resources)
      // Delete the ipsec connection

      // Delete the endpoint group for cidr

      // Delete the endpoint group for subnet

      //Check if ipsec policy already used for other connection
      // Delete the ipsec policy

      //Check if ike policy already used for other connection
      // Delete the ike policy

      //Check if vpn service already used for other connection
      // Delete the vpn service

      //// Remove the VPN connection in the VIM Right (use of instance_id/vl_id in the names of resources)
      // Delete the ipsec connection

      // Delete the endpoint group for cidr

      // Delete the endpoint group for subnet

      //Check if ipsec policy already used for other connection
      // Delete the ipsec policy

      //Check if ike policy already used for other connection
      // Delete the ike policy

      //Check if vpn service already used for other connection
      // Delete the vpn service
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

}
