/*
 * Copyright (c) 2015 SONATA-NFV, UCL, NOKIA, THALES, NCSR Demokritos ALL RIGHTS RESERVED. <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at <p>
 * http://www.apache.org/licenses/LICENSE-2.0 <p> Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License. <p> Neither the name of the
 * SONATA-NFV, UCL, NOKIA, THALES NCSR Demokritos nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * <p> This work has been performed in the framework of the SONATA project, funded by the European
 * Commission under Grant number 671517 through the Horizon 2020 and 5G-PPP programmes. The authors
 * would like to acknowledge the contributions of their colleagues of the SONATA partner consortium
 * (www.sonata-nfv.eu).
 *
 * @author Dario Valocchi (Ph.D.), UCL
 * 
 * @author Bruno Vidalenc (Ph.D.), THALES
 */

package sonata.kernel.adaptor.wrapper.vpnaas;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import sonata.kernel.adaptor.wrapper.vpnaas.javastackclient.JavaStackCore;
import sonata.kernel.adaptor.wrapper.vpnaas.javastackclient.JavaStackUtils;
import sonata.kernel.adaptor.wrapper.vpnaas.javastackclient.models.network.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class wraps a Nova Client written in python when instantiated the onnection details of the
 * OpenStack instance should be provided.
 *
 */
public class OpenStackNeutronClient {

  private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(OpenStackNeutronClient.class);

  private JavaStackCore javaStack; // instance for calling OpenStack APIs

  private ObjectMapper mapper;

  /**
   * Construct a new Openstack Neutron Client.
   *
   * @param url of the OpenStack endpoint
   * @param userName to log into the OpenStack service
   * @param password to log into the OpenStack service
   * @param domain to log into the OpenStack service
   * @param tenantName to log into the OpenStack service
   * @throws IOException if the authentication process fails
   */
  public OpenStackNeutronClient(String url, String userName, String password, String domain, String tenantName,
                                String identityPort) throws IOException {
    Logger.debug(
        "URL:" + url + "|User:" + userName + "|Project:" + tenantName + "|Pass:" + password + "|Domain:" + domain + "|");

    javaStack = JavaStackCore.getJavaStackCore();

    javaStack.setEndpoint(url);
    javaStack.setUsername(userName);
    javaStack.setPassword(password);
    javaStack.setDomain(domain);
    javaStack.setProjectName(tenantName);
    javaStack.setProjectId(null);
    javaStack.setAuthenticated(false);

    javaStack.authenticateClientV3(identityPort);

  }

  /**
   * Get the Qos Policies.
   *
   * @return the Qos Policies
   */
/*  public ArrayList<QosPolicy> getPolicies() {

    QosPolicy output_policy = null;
    String policyName = null;
    int cpu, ram, disk;

    ArrayList<QosPolicy> output_policies = new ArrayList<>();
    Logger.info("Getting qos policies");
    try {
      mapper = new ObjectMapper();
      String listPolicies =
          JavaStackUtils.convertHttpResponseToString(javaStack.listQosPolicies());
      Logger.info(listPolicies);
      PoliciesData inputPolicies = mapper.readValue(listPolicies, PoliciesData.class);
      Logger.info(inputPolicies.getPolicies().toString());
      for (PolicyProperties input_policy : inputPolicies.getPolicies()) {
        Logger.info(input_policy.getId() + ": " + input_policy.getName());

        policyName = input_policy.getName();
        ArrayList<QosRule> qosRules = new ArrayList<>();
        for (RulesProperties input_rule : input_policy.getRules()) {
          qosRules.add(new QosRule(input_rule.getId(),input_rule.getType(),input_rule.getDirection(),
                  input_rule.getMaxKbps(),input_rule.getMinKbps()));
        }

        output_policy = new QosPolicy(policyName, qosRules);
        output_policies.add(output_policy);
      }

    } catch (Exception e) {
      Logger.warn("Warning: Runtime error getting openstack qos policies" + " error message: " + e.getMessage());
    }

    return output_policies;

  }

  */
  /**
   * Get the External Networks.
   *
   * @return the External Networks
   */
  public ArrayList<ExtNetwork> getNetworks() {

    ExtNetwork output_network = null;

    ArrayList<ExtNetwork> output_networks = new ArrayList<>();
    Logger.info("Getting external networks");
    try {
      mapper = new ObjectMapper();
      String listNetworks =
          JavaStackUtils.convertHttpResponseToString(javaStack.listNetworks());
      Logger.info(listNetworks);
      NetworksData inputNetworks = mapper.readValue(listNetworks, NetworksData.class);
      Logger.info(inputNetworks.getNetworks().toString());
      for (NetworksProperties input_network : inputNetworks.getNetworks()) {
        Logger.info(input_network.getId() + ": " + input_network.getName());

        output_network = new ExtNetwork(input_network.getName(), input_network.getId());
        output_networks.add(output_network);
      }

    } catch (Exception e) {
      Logger.error("Runtime error getting openstack external networks" + " error message: " + e.getMessage());
    }

    return output_networks;

  }

  /**
   * Get the Routers for a specific External Network.
   *
   * @return the Routers
   */
  public ArrayList<Router> getRouters(String network) {

    Router output_router = null;

    ArrayList<Router> output_routers = new ArrayList<>();
    Logger.info("Getting routers");
    try {
      mapper = new ObjectMapper();
      String listRouters =
          JavaStackUtils.convertHttpResponseToString(javaStack.listRouters());
      Logger.info(listRouters);
      RoutersData inputRouters = mapper.readValue(listRouters, RoutersData.class);
      Logger.info(inputRouters.getRouters().toString());
      for (RoutersProperties input_router : inputRouters.getRouters()) {
        if (input_router.getExternalGatewayInfo() != null) {
          Logger.info(input_router.getId() + ": " + input_router.getName() + ": " + input_router.getExternalGatewayInfo().getNetworkId());

          if (input_router.getExternalGatewayInfo().getNetworkId().equals(network)) {
            output_router = new Router(input_router.getName(), input_router.getId(),null);
            output_routers.add(output_router);
          }
        }
      }

    } catch (Exception e) {
      Logger.error("Runtime error getting openstack routers" + " error message: " + e.getMessage());
    }

    if (output_routers.isEmpty()) {
      return null;
    } else {
      return output_routers;
    }

  }

  /**
   * Get the Router for the given router ID.
   *
   * @return the Router
   */
  public Router getRouter(String routerId) {

    Router output_router = null;

    Logger.info("Getting router");
    try {
      mapper = new ObjectMapper();
      String routerString =
          JavaStackUtils.convertHttpResponseToString(javaStack.getRouter(routerId));
      Logger.info(routerString);
      RouterData inputRouter = mapper.readValue(routerString, RouterData.class);
      Logger.info(inputRouter.getRouter().toString());
      RoutersProperties inputRouterProp = inputRouter.getRouter();
      Logger.info(inputRouterProp.getId() + ": " + inputRouterProp.getName() + ": " + inputRouterProp.getExternalGatewayInfo().getExternalFixedIps().get(0).getIpAddress());

      output_router = new Router(inputRouterProp.getName(), inputRouterProp.getId(),inputRouterProp.getExternalGatewayInfo().getExternalFixedIps().get(0).getIpAddress());

    } catch (Exception e) {
      Logger.error("Runtime error getting openstack routers" + " error message: " + e.getMessage());
    }

    return output_router;

  }

  /**
   * Get the Subnet for the given floating IP.
   *
   * @return the Subnet
   */
  public Subnet getSubnet(String fIp) {

    Subnet output_subnet = null;

    Logger.info("Getting Subnet");
    try {
      mapper = new ObjectMapper();
      String FloatingIpString =
          JavaStackUtils.convertHttpResponseToString(javaStack.getFloatingIp(fIp));
      Logger.info(FloatingIpString);
      FloatingIpData inputFloatingIp = mapper.readValue(FloatingIpString, FloatingIpData.class);
      Logger.info(inputFloatingIp.getFloatingIp().toString());
      String networkId = inputFloatingIp.getFloatingIp().get(0).getPortDetails().getNetworkId();

      String SubnetString =
          JavaStackUtils.convertHttpResponseToString(javaStack.getSubnet(networkId));
      Logger.info(SubnetString);
      SubnetData inputSubnet = mapper.readValue(SubnetString, SubnetData.class);
      Logger.info(inputSubnet.getSubnet().toString());

      SubnetProperties inputSubnetProp = inputSubnet.getSubnet().get(0);
      Logger.info(inputSubnetProp.getId() + ": " + inputSubnetProp.getNetworkId() + ": " + inputSubnetProp.getCidr());

      output_subnet = new Subnet(inputSubnetProp.getNetworkId(), inputSubnetProp.getId(),inputSubnetProp.getCidr());

    } catch (Exception e) {
      Logger.error("Runtime error getting openstack subnet" + " error message: " + e.getMessage());
    }

    return output_subnet;

  }

  /**
   * Get the Subnet Pools
   *
   * @return the Subnet Pools
   */
  public ArrayList<SubnetPool> getSubnetPools() {

    SubnetPool outputSubnetPool = null;

    ArrayList<SubnetPool> outputSubnetPools = new ArrayList<>();
    Logger.info("Getting subnet pools");
    try {
      mapper = new ObjectMapper();
      String listSubnetPools =
          JavaStackUtils.convertHttpResponseToString(javaStack.listSubnetPools());
      Logger.info(listSubnetPools);
      SubnetPoolsData inputSubnetPools = mapper.readValue(listSubnetPools, SubnetPoolsData.class);
      Logger.info(inputSubnetPools.getSubnetPools().toString());
      for (SubnetPoolProperties inputSubnetPool : inputSubnetPools.getSubnetPools()) {
        Logger.info(inputSubnetPool.getId() + ": " + inputSubnetPool.getName());

        outputSubnetPool = new SubnetPool(inputSubnetPool.getName(), inputSubnetPool.getId(), inputSubnetPool.getPrefixes());
        outputSubnetPools.add(outputSubnetPool);
      }

    } catch (Exception e) {
      Logger.error("Runtime error getting openstack subnet pools" + " error message: " + e.getMessage());
    }

    return outputSubnetPools;

  }

  /**
   * Create a Subnet Pool
   *
   * @param name - the name
   * @param prefixes - A list of subnet prefixes to assign to the subnet pool
   * @param defaultPrefixlen - The size of the prefix to allocate when you create the subnet
   * @return - the uuid of the created subnet pool, if the process failed the returned value is null
   */
  public String createSubnetPool(String name, ArrayList<String> prefixes, String defaultPrefixlen) {
    String uuid = null;

    Logger.info("Creating subnet pool: " + name);

    try {
      mapper = new ObjectMapper();
      String createSubnetPoolResponse =
          JavaStackUtils.convertHttpResponseToString(javaStack.createSubnetPool(name,prefixes,defaultPrefixlen));
      Logger.info(createSubnetPoolResponse);
      SubnetPoolData inputSubnetPool = mapper.readValue(createSubnetPoolResponse, SubnetPoolData.class);
       uuid = inputSubnetPool.getSubnetPool().getId();

    } catch (Exception e) {
      Logger.error(
          "Runtime error creating subnet pool : " + name + " error message: " + e.getMessage());
      return null;
    }

    return uuid;
  }

  /**
   * Update a Subnet Pool
   *
   * @param id - the id
   * @param prefixes - A list of subnet prefixes to assign to the subnet pool
   * @return - the uuid of the created subnet pool, if the process failed the returned value is null
   */
  public String updateSubnetPool(String id, ArrayList<String> prefixes) {
    String uuid = null;

    Logger.info("Updating subnet pool id: " + id);

    try {
      mapper = new ObjectMapper();
      String updateSubnetPoolResponse =
          JavaStackUtils.convertHttpResponseToString(javaStack.updateSubnetPool(id,prefixes));
      Logger.info(updateSubnetPoolResponse);
      SubnetPoolData inputSubnetPool = mapper.readValue(updateSubnetPoolResponse, SubnetPoolData.class);
      uuid = inputSubnetPool.getSubnetPool().getId();

    } catch (Exception e) {
      Logger.error(
          "Runtime error updating subnet pool id: " + id + " error message: " + e.getMessage());
      return null;
    }

    return uuid;
  }

  /**
   * Get the Ike Policy for the given name.
   *
   * @return the Ike Policy Id
   */
  public String getIkePolicy(String name) {

    String outputIkePolicyId = null;

    Logger.info("Getting Ike Policy");
    try {
      mapper = new ObjectMapper();
      String ikePolicyString =
          JavaStackUtils.convertHttpResponseToString(javaStack.getIkePolicy(name));
      Logger.info(ikePolicyString);
      IkePoliciesData inputIkePolicy = mapper.readValue(ikePolicyString, IkePoliciesData.class);
      Logger.info(inputIkePolicy.getIkePolicy().toString());

      IkePolicyProperties inputIkePolicyProp = inputIkePolicy.getIkePolicy().get(0);
      Logger.info(inputIkePolicyProp.getId() + ": " + inputIkePolicyProp.getName());

      outputIkePolicyId = inputIkePolicyProp.getId();

    } catch (Exception e) {
      Logger.error("Runtime error getting openstack ike policy" + " error message: " + e.getMessage());
    }

    return outputIkePolicyId;

  }

  /**
   * Create an Ike Policy
   *
   * @param name - the name
   * @param lifetime - Ike lifetime in seconds
   * @return - the uuid of the created Ike Policy, if the process failed the returned value is null
   */
  public String createIkePolicy(String name, String lifetime) {
    String uuid = null;

    Logger.info("Creating Ike Policy: " + name);

    try {
      mapper = new ObjectMapper();
      String createIkePolicy =
          JavaStackUtils.convertHttpResponseToString(javaStack.createIkePolicy(name,lifetime));
      Logger.info(createIkePolicy);
      IkePolicyData inputIkePolicy = mapper.readValue(createIkePolicy, IkePolicyData.class);
      uuid = inputIkePolicy.getIkePolicy().getId();

    } catch (Exception e) {
      Logger.error(
          "Runtime error creating Ike Policy : " + name + " error message: " + e.getMessage());
      return null;
    }

    return uuid;
  }

  /**
   * Delete the Ike Policy for the given name.
   *
   * @return the Response
   */
  public String deleteIkePolicy(String name) {

    String outputResponse = null;

    Logger.info("Delete Ike Policy");
    try {
      mapper = new ObjectMapper();
      String ikePolicyString =
          JavaStackUtils.convertHttpResponseToString(javaStack.deleteIkePolicy(getIkePolicy(name)));
      Logger.info(ikePolicyString);
      outputResponse = ikePolicyString;

    } catch (Exception e) {
      Logger.error("Runtime error deleting openstack ike policy" + " error message: " + e.getMessage());
    }

    return outputResponse;

  }

  /**
   * Get the Ipsec Policy for the given name.
   *
   * @return the Ipsec Policy Id
   */
  public String getIpsecPolicy(String name) {

    String outputIpsecPolicyId = null;

    Logger.info("Getting Ipsec Policy");
    try {
      mapper = new ObjectMapper();
      String ipsecPolicyString =
          JavaStackUtils.convertHttpResponseToString(javaStack.getIpsecPolicy(name));
      Logger.info(ipsecPolicyString);
      IpsecPoliciesData inputIpsecPolicy = mapper.readValue(ipsecPolicyString, IpsecPoliciesData.class);
      Logger.info(inputIpsecPolicy.getIpsecPolicy().toString());

      IpsecPolicyProperties inputIpsecPolicyProp = inputIpsecPolicy.getIpsecPolicy().get(0);
      Logger.info(inputIpsecPolicyProp.getId() + ": " + inputIpsecPolicyProp.getName());

      outputIpsecPolicyId = inputIpsecPolicyProp.getId();

    } catch (Exception e) {
      Logger.error("Runtime error getting openstack ipsec policy" + " error message: " + e.getMessage());
    }

    return outputIpsecPolicyId;

  }

  /**
   * Create an Ipsec Policy
   *
   * @param name - the name
   * @param lifetime - Ike lifetime in seconds
   * @return - the uuid of the created Ipsec Policy, if the process failed the returned value is null
   */
  public String createIpsecPolicy(String name, String lifetime) {
    String uuid = null;

    Logger.info("Creating Ipsec Policy: " + name);

    try {
      mapper = new ObjectMapper();
      String createIpsecPolicy =
          JavaStackUtils.convertHttpResponseToString(javaStack.createIpsecPolicy(name,lifetime));
      Logger.info(createIpsecPolicy);
      IpsecPolicyData inputIpsecPolicy = mapper.readValue(createIpsecPolicy, IpsecPolicyData.class);
      uuid = inputIpsecPolicy.getIpsecPolicy().getId();

    } catch (Exception e) {
      Logger.error(
          "Runtime error creating Ipsec Policy : " + name + " error message: " + e.getMessage());
      return null;
    }

    return uuid;
  }

  /**
   * Delete the Ipsec Policy for the given name.
   *
   * @return the Response
   */
  public String deleteIpsecPolicy(String name) {

    String outputResponse = null;

    Logger.info("Delete Ipsec Policy");
    try {
      mapper = new ObjectMapper();
      String ipsecPolicyString =
          JavaStackUtils.convertHttpResponseToString(javaStack.deleteIpsecPolicy(getIpsecPolicy(name)));
      Logger.info(ipsecPolicyString);
      outputResponse = ipsecPolicyString;

    } catch (Exception e) {
      Logger.error("Runtime error deleting openstack ipsec policy" + " error message: " + e.getMessage());
    }

    return outputResponse;

  }

  /**
   * Get the Vpn Service for the given name.
   *
   * @return the Vpn Service Id
   */
  public String getVpnService(String name) {

    String outputVpnServiceId = null;

    Logger.info("Getting Vpn Service");
    try {
      mapper = new ObjectMapper();
      String vpnServiceString =
          JavaStackUtils.convertHttpResponseToString(javaStack.getVpnService(name));
      Logger.info(vpnServiceString);
      VpnServicesData inputVpnService = mapper.readValue(vpnServiceString, VpnServicesData.class);
      Logger.info(inputVpnService.getVpnService().toString());

      VpnServiceProperties inputVpnServiceProp = inputVpnService.getVpnService().get(0);
      Logger.info(inputVpnServiceProp.getId() + ": " + inputVpnServiceProp.getName());

      outputVpnServiceId = inputVpnServiceProp.getId();

    } catch (Exception e) {
      Logger.error("Runtime error getting openstack vpn service" + " error message: " + e.getMessage());
    }

    return outputVpnServiceId;

  }

  /**
   * Create an Vpn Service
   *
   * @param name - the name
   * @param routerId - router id
   * @return - the uuid of the created Vpn Service, if the process failed the returned value is null
   */
  public String createVpnService(String name, String routerId) {
    String uuid = null;

    Logger.info("Creating Vpn Service: " + name);

    try {
      mapper = new ObjectMapper();
      String createVpnService =
          JavaStackUtils.convertHttpResponseToString(javaStack.createVpnService(name,routerId));
      Logger.info(createVpnService);
      VpnServiceData inputVpnService = mapper.readValue(createVpnService, VpnServiceData.class);
      uuid = inputVpnService.getVpnService().getId();

    } catch (Exception e) {
      Logger.error(
          "Runtime error creating vpn service : " + name + " error message: " + e.getMessage());
      return null;
    }

    return uuid;
  }

  /**
   * Delete the Vpn Service for the given name.
   *
   * @return the Response
   */
  public String deleteVpnService(String name) {

    String outputResponse = null;

    Logger.info("Delete Vpn Service");
    try {
      mapper = new ObjectMapper();
      String vpnServiceString =
          JavaStackUtils.convertHttpResponseToString(javaStack.deleteVpnService(getVpnService(name)));
      Logger.info(vpnServiceString);
      outputResponse = vpnServiceString;

    } catch (Exception e) {
      Logger.error("Runtime error deleting openstack vpn service" + " error message: " + e.getMessage());
    }

    return outputResponse;

  }


  /**
   * Get the Endpoint Group for the given name.
   *
   * @return the Endpoint Group Id
   */
  public String getEndpointGroup(String name) {

    String outputEndpointGroupId = null;

    Logger.info("Getting Endpoint Group");
    try {
      mapper = new ObjectMapper();
      String endpointGroupString =
          JavaStackUtils.convertHttpResponseToString(javaStack.getEndpointGroup(name));
      Logger.info(endpointGroupString);
      EndpointGroupsData inputEndpointGroup = mapper.readValue(endpointGroupString, EndpointGroupsData.class);
      Logger.info(inputEndpointGroup.getEndpointGroups().toString());

      EndpointGroupProperties inputEndpointGroupProp = inputEndpointGroup.getEndpointGroups().get(0);
      Logger.info(inputEndpointGroupProp.getId() + ": " + inputEndpointGroupProp.getName());

      outputEndpointGroupId = inputEndpointGroupProp.getId();

    } catch (Exception e) {
      Logger.error("Runtime error getting openstack Endpoint Group" + " error message: " + e.getMessage());
    }

    return outputEndpointGroupId;

  }

  /**
   * Create an Endpoint Group
   *
   * @param name - the name
   * @param type - type
   * @param endpoints - endpoints
   * @return - the uuid of the created Endpoint Group, if the process failed the returned value is null
   */
  public String createEndpointGroup(String name, String type, ArrayList<String> endpoints) {
    String uuid = null;

    Logger.info("Creating Endpoint Group: " + name);

    try {
      mapper = new ObjectMapper();
      String createEndpointGroup =
          JavaStackUtils.convertHttpResponseToString(javaStack.createEndpointGroup(name,type,endpoints));
      Logger.info(createEndpointGroup);
      EndpointGroupData inputEndpointGroup = mapper.readValue(createEndpointGroup, EndpointGroupData.class);
      uuid = inputEndpointGroup.getEndpointGroup().getId();

    } catch (Exception e) {
      Logger.error(
          "Runtime error creating Endpoint Group : " + name + " error message: " + e.getMessage());
      return null;
    }

    return uuid;
  }

  /**
   * Delete the Endpoint Group for the given name.
   *
   * @return the Response
   */
  public String deleteEndpointGroup(String name) {

    String outputResponse = null;

    Logger.info("Delete Endpoint Group");
    try {
      mapper = new ObjectMapper();
      String endpointGroupString =
          JavaStackUtils.convertHttpResponseToString(javaStack.deleteEndpointGroup(getEndpointGroup(name)));
      Logger.info(endpointGroupString);
      outputResponse = endpointGroupString;

    } catch (Exception e) {
      Logger.error("Runtime error deleting openstack Endpoint Group" + " error message: " + e.getMessage());
    }

    return outputResponse;

  }

  /**
   * Get the Ipsec Connection for the given name.
   *
   * @return the Ipsec Connection Id
   */
  public String getIpsecConnection(String name) {

    String outputIpsecConnectionId = null;

    Logger.info("Getting Ipsec Connection");
    try {
      mapper = new ObjectMapper();
      String ipsecConnectionString =
          JavaStackUtils.convertHttpResponseToString(javaStack.getIpsecConnection(name));
      Logger.info(ipsecConnectionString);
      IpsecConnectionsData inputIpsecConnection = mapper.readValue(ipsecConnectionString, IpsecConnectionsData.class);
      Logger.info(inputIpsecConnection.getIpsecConnections().toString());

      IpsecConnectionProperties inputIpsecConnectionProp = inputIpsecConnection.getIpsecConnections().get(0);
      Logger.info(inputIpsecConnectionProp.getId() + ": " + inputIpsecConnectionProp.getName());

      outputIpsecConnectionId = inputIpsecConnectionProp.getId();

    } catch (Exception e) {
      Logger.error("Runtime error getting openstack Ipsec Connection" + " error message: " + e.getMessage());
    }

    return outputIpsecConnectionId;

  }

  /**
   * Create an Ipsec Connection
   *
   * @param name - the name
   * @param vpnServiceId - Vpn Service Id
   * @param ikePolicyId - Ike Policy Id
   * @param ipsecPolicyId - Ipsec Policy Id
   * @param peerAddress - Peer Address
   * @param peerId - Peer Id
   * @param psk - psk
   * @param subnetEpGroupId - subnet Endpoint Group Id
   * @param cidrEpGroupId - cidr Endpoint Group Id
   * @return - the uuid of the created Ipsec Connection, if the process failed the returned value is null
   */
  public String createIpsecConnection(String name, String vpnServiceId, String ikePolicyId,
                                      String ipsecPolicyId, String peerAddress, String peerId, String psk,
                                      String subnetEpGroupId, String cidrEpGroupId) {
    String uuid = null;

    Logger.info("Creating Ipsec Connection: " + name);

    try {
      mapper = new ObjectMapper();
      String createIpsecConnection =
          JavaStackUtils.convertHttpResponseToString(javaStack.createIpsecConnection(name, vpnServiceId, ikePolicyId,
              ipsecPolicyId, peerAddress, peerId, psk, subnetEpGroupId, cidrEpGroupId));
      Logger.info(createIpsecConnection);
      IpsecConnectionData inputIpsecConnection = mapper.readValue(createIpsecConnection, IpsecConnectionData.class);
      uuid = inputIpsecConnection.getIpsecConnection().getId();

    } catch (Exception e) {
      Logger.error(
          "Runtime error creating Ipsec Connection : " + name + " error message: " + e.getMessage());
      return null;
    }

    return uuid;
  }

  /**
   * Delete the Ipsec Connection for the given name.
   *
   * @return the Response
   */
  public String deleteIpsecConnection(String name) {

    String outputResponse = null;

    Logger.info("Delete Ipsec Connection");
    try {
      mapper = new ObjectMapper();
      String ipsecConnectionString =
          JavaStackUtils.convertHttpResponseToString(javaStack.deleteIpsecConnection(getIpsecConnection(name)));
      Logger.info(ipsecConnectionString);
      outputResponse = ipsecConnectionString;

    } catch (Exception e) {
      Logger.error("Runtime error deleting openstack Ipsec Connection" + " error message: " + e.getMessage());
    }

    return outputResponse;

  }
  
}
