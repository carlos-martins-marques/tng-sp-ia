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

package sonata.kernel.vimadaptor.commons;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HeatRequestPayload {

  private String uuid;
  private String type;
  private String endpoint;
  @JsonProperty("username")
  private String userName;
  private String password;
  @JsonProperty("authkey")
  private String authKey;
  private String tenant;
  private String domain;
  @JsonProperty("external_network_id")
  private String externalNetworkId;
  //@JsonProperty("external_router_id")
  //private String externalRouterId;



  public String getUuid() {
    return this.uuid;
  }

  public String getType() {
    return this.type;
  }


  public String getEndpoint() {
    return endpoint;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  public String getAuthKey() {
    return authKey;
  }

  public String getTenant() {
    return tenant;
  }

  public String getDomain() {
    return domain;
  }

  public String getExternalNetworkId() {
    return externalNetworkId;
  }

  //public String getExternalRouterId() {
    //return externalRouterId;
  //}


  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setAuthKey(String authKey) {
    this.authKey = authKey;
  }

  public void setTenant(String tenant) {
    this.tenant = tenant;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public void setExternalNetworkId(String externalNetworkId) {
    this.externalNetworkId = externalNetworkId;
  }

  //public void setExternalRouterId(String externalRouterId) {
    //this.externalRouterId = externalRouterId;
  //}

}
