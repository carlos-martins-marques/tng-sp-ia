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
 * Neither the name of the SONATA-NFV, UCL, NOKIA, THALES, NCSR Demokritos nor the names of its
 * contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This work has been performed in the framework of the SONATA project, funded by the European
 * Commission under Grant number 671517 through the Horizon 2020 and 5G-PPP programmes. The authors
 * would like to acknowledge the contributions of their colleagues of the SONATA partner consortium
 * (www.sonata-nfv.eu).
 *
 * @author Bruno Vidalenc (Ph.D.), Thales
 * 
 * @author Dario Valocchi (Ph.D.), UCL
 * 
 */

package sonata.kernel.adaptor.wrapper.vpnaas;

import org.slf4j.LoggerFactory;

public class Subnet {

  private static final org.slf4j.Logger Logger =
          LoggerFactory.getLogger(Subnet.class);

  private String networkId;

  private String id;

  private String cidr;


  /**
   * Basic subnet constructor.
   *
   * @param networkId the id of the network
   * @param id the subnet
   * @param cidr the subnet

   */
  public Subnet(String networkId, String id, String cidr) {
    super();
    this.networkId = networkId;
    this.id = id;
    this.cidr = cidr;

  }

  public String getNetworkId() {
    return networkId;
  }

  public String getId() {
    return id;
  }

  public String getCidr() {
    return cidr;
  }


  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setCidr(String cidr) {
    this.cidr = cidr;
  }

}