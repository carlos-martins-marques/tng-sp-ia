/*
 * Copyright (c) 2015 SONATA-NFV, UCL, NOKIA, NCSR Demokritos ALL RIGHTS RESERVED.
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

package sonata.kernel.adaptor.commons;

import java.util.ArrayList;


import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigureWanPayload {

  @JsonProperty("service_instance_id")
  private String serviceInstanceId;

  @JsonProperty("wim_uuid")
  private String wimUuid;

  @JsonProperty("vl_id")
  private String vlId;

  private NapObject ingress;

  private NapObject egress;

//  private QosObject qos;

  private Boolean bidirectional;


  public String getServiceInstanceId() {
    return serviceInstanceId;
  }

  public String getWimUuid() {
    return wimUuid;
  }

  public String getVlId() {
    return vlId;
  }

  public NapObject getIngress() {
    return ingress;
  }

  public NapObject getEgress() {
    return egress;
  }

/*  public QosObject getQos() {
    return qos;
  }*/

  public Boolean getBidirectional() {
    return bidirectional;
  }


  public void setServiceInstanceId(String serviceInstanceId) {
    this.serviceInstanceId = serviceInstanceId;
  }

  public void setWimUuid(String wimUuid) {
    this.wimUuid = wimUuid;
  }

  public void setVlId(String vlId) {
    this.vlId = vlId;
  }

  public void setIngress(NapObject ingress) {
    this.ingress = ingress;
  }

  public void setEgress(NapObject egress) {
    this.egress = egress;
  }

/*  public void setQos(QosObject qos) {
    this.qos = qos;
  }*/

  public void setBidirectional(Boolean bidirectional) {
    this.bidirectional = bidirectional;
  }


}
