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

import org.slf4j.LoggerFactory;
import sonata.kernel.adaptor.commons.NapObject;
import sonata.kernel.adaptor.commons.QosObject;
import sonata.kernel.adaptor.wrapper.WimServiceConfiguration;
import sonata.kernel.adaptor.wrapper.WrapperBay;
import sonata.kernel.adaptor.wrapper.WimWrapper;
import sonata.kernel.adaptor.wrapper.WimWrapperConfiguration;

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
      //// Get information for create the VPN in the ingress side (VIM Left)
      // Get router_id from VIM DB for ingress location (VIM Left)

      // Get subnet_id from Openstack VIM Left for the ingress NAP (FIP Left)

      // Get cidr from Openstack VIM Right for the egress NAP (FIP Right)

      // Get router_ip from Openstack VIM Right


      //// Get information for create the VPN in the egress side (VIM Right)
      // Get router_id from VIM DB for egress location (VIM Right)

      // Get subnet_id from Openstack VIM Right for the egress NAP (FIP Right)

      // Get cidr from Openstack VIM Left for the ingress NAP (FIP Left)

      // Get router_ip from Openstack VIM Left


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

}
