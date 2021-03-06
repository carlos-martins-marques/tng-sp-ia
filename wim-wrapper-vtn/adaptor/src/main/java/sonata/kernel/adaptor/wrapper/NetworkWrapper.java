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

import sonata.kernel.adaptor.commons.NetworkConfigurePayload;


public abstract class NetworkWrapper extends AbstractWrapper implements Wrapper {



  public NetworkWrapper(VimWrapperConfiguration config) {

    this.setType(WrapperType.NETWORK);
    this.setVimConfig(config);
  }

  // /**
  // * Configure the SFC and networking aspects of the service
  // *
  // * @param data the service deployment descriptors
  // * @param composition the composition of the deployed service
  // * @throws Exception
  // *
  // */
  // @Deprecated
  // public abstract void configureNetworking(ServiceDeployPayload data, StackComposition
  // composition)
  // throws Exception;

  /**
   * Configure a given SFC chain into a specific NFVi-PoP.
   * 
   * @param data the payload representing the configuration to apply
   * 
   */
  public abstract void configureNetworking(NetworkConfigurePayload data) throws Exception;


  /**
   * Deconfigure a given SFC chain into a specific NFVi-PoP.
   * 
   * @param instanceId the service instance ID which identifies the network service
   * 
   */
  public abstract void deconfigureNetworking(String instanceId) throws Exception;


}
