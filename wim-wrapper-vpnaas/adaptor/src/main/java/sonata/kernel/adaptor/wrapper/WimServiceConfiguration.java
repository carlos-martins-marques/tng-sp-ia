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

package sonata.kernel.adaptor.wrapper;

public class WimServiceConfiguration {

  private String instanceUuid;
  private String vlId;
  private String wimUuid;
  private String ingress;
  private String egress;

  public String getInstanceUuid() {
    return instanceUuid;
  }

  public String getVlId() {
    return vlId;
  }

  public String getWimUuid() {
    return this.wimUuid;
  }

  public String getIngress() {
    return ingress;
  }

  public String getEgress() {
    return egress;
  }


  public void setInstanceUuid(String instanceUuid) {
    this.instanceUuid = instanceUuid;
  }

  public void setVlId(String vlId) {
    this.vlId = vlId;
  }

  public void setWimUuid(String wimUuid) {
    this.wimUuid = wimUuid;
  }

  public void setIngress(String ingress) {
    this.ingress = ingress;
  }

  public void setEgress(String egress) {
    this.egress = egress;
  }

  @Override
  public String toString() {
    String out = "";

    out += "wimUuid: " + wimUuid + "\n\r";
    out += "instanceUuid: " + instanceUuid + "\n\r";
    out += "vlId: " + vlId + "\n\r";
    out += "ingress: " + ingress + "\n\r";
    out += "egress: " + egress + "\n\r";
    return out;
  }

}
