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

package sonata.kernel.adaptor.wrapper;

import org.slf4j.LoggerFactory;
import sonata.kernel.adaptor.commons.VimResources;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class ResourceRepo {

  private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(ResourceRepo.class);
  private static ResourceRepo myInstance = null;

  /**
   * Singleton method to get the instance of the wrapperbay.
   *
   * @return the instance of the wrapperbay
   */
  public static ResourceRepo getInstance() {
    if (myInstance == null) {
      myInstance = new ResourceRepo();
    }
    return myInstance;
  }

  private ConcurrentHashMap<String, ConcurrentHashMap<ComputeVimVendor, ArrayList<VimResources>>> ResourceRepoMap;


  private ResourceRepo() {
    ResourceRepoMap = new ConcurrentHashMap<>();
  }



  /**
   * Return the number of vendors stored for a specific request id
   *
   * @param requestId The id of the request
   *
   * @return an integer with the number of vendors stored for a specific request id
   */
  public Integer getVendorsNumberForRequestId(String requestId) {
    if (ResourceRepoMap.containsKey(requestId)) {
      return ResourceRepoMap.get(requestId).size();
    } else {
      return 0;
    }

  }


  /**
   * Store the content from vendor and for specific request id
   *
   * @param requestId The id of the request
   * @param vendor The vendor name
   * @param content The resource content
   *
   * @return True
   */
  public Boolean putResourcesForRequestIdAndVendor(String requestId, ComputeVimVendor vendor, ArrayList<VimResources> content) {
    ConcurrentHashMap<ComputeVimVendor, ArrayList<VimResources>> resourceMap;
    if (ResourceRepoMap.containsKey(requestId)) {
      resourceMap = ResourceRepoMap.get(requestId);
    } else {
      resourceMap = new ConcurrentHashMap<>();
      ResourceRepoMap.put(requestId,resourceMap);

    }
    resourceMap.put(vendor,content);
    return true;
  }

  /**
   * Return the content stored for a specific request id as a array list
   *
   * @param requestId The id of the request
   *
   * @return ArrayList with the content for a specific request id
   */
  public ArrayList<VimResources> getResourcesFromRequestId(String requestId) {
    ArrayList<VimResources> resourceForRequestId = new ArrayList<>();
    if (ResourceRepoMap.containsKey(requestId)) {
      ConcurrentHashMap<ComputeVimVendor, ArrayList<VimResources>> resourceMap = ResourceRepoMap.get(requestId);
      for (ArrayList<VimResources> value : resourceMap.values()) {
        resourceForRequestId.addAll(value);
      }
    } else {
      resourceForRequestId = null;
    }
    return resourceForRequestId;
  }

  /**
   * Remove the map stored for a specific request id
   *
   * @param requestId The id of the request
   *
   * @return True
   */
  public Boolean removeResourcesFromRequestId(String requestId) {
    if (ResourceRepoMap.containsKey(requestId)) {
      ResourceRepoMap.remove(requestId);
    }
    return true;
  }


}
