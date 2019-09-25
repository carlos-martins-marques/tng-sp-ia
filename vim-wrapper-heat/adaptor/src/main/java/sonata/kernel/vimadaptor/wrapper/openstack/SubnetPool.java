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

package sonata.kernel.vimadaptor.wrapper.openstack;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class SubnetPool {

  private static final org.slf4j.Logger Logger =
          LoggerFactory.getLogger(SubnetPool.class);

  private String name;
  private String id;
  private ArrayList<String> prefixes;


  /**
   * Basic flavor constructor.
   *
   * @param name the name of this Subnet Pool
   * @param id the id of this Subnet Pool

   */
  public SubnetPool(String name, String id, ArrayList<String> prefixes) {
    super();
    this.name = name;
    this.id = id;
    this.prefixes = prefixes;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  public ArrayList<String> getPrefixes() {
    return prefixes;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setPrefixes(ArrayList<String> prefixes) {
    this.prefixes = prefixes;
  }

}