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

package sonata.kernel.vimadaptor.commons;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WimRecord {

  private String uuid;
  private String name;
  @JsonProperty("attached_vims")
  private ArrayList<String> attachedVims = new ArrayList<String>();
  
  public String getUuid() {
    return uuid;
  }
  public String getName() {
    return name;
  }
  public ArrayList<String> getAttachedVims() {
    return attachedVims;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setAttachedVims(ArrayList<String> attachedVims) {
    this.attachedVims = attachedVims;
  }
  
  @Override
  public String toString(){
    String out = "uuid: "+this.uuid+"\n"+
        "name: "+this.name+"\n"+
        "attachedVim: "+this.attachedVims.toString();
        
    return out;
  }
  
  
}
