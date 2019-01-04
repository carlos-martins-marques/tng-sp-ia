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

package sonata.kernel.vimadaptor.wrapper.openstack;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import sonata.kernel.vimadaptor.wrapper.openstack.javastackclient.JavaStackCore;
import sonata.kernel.vimadaptor.wrapper.openstack.javastackclient.JavaStackUtils;
import sonata.kernel.vimadaptor.wrapper.openstack.javastackclient.models.network.PoliciesData;
import sonata.kernel.vimadaptor.wrapper.openstack.javastackclient.models.network.PolicyProperties;
import sonata.kernel.vimadaptor.wrapper.openstack.javastackclient.models.network.RulesProperties;

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
    javaStack.setProjectId(null);
    javaStack.setAuthenticated(false);

    javaStack.authenticateClientV3(identityPort);

  }

  /**
   * Get the Qos Policies.
   *
   * @return the Qos Policies
   */
  public ArrayList<QosPolicy> getPolicies() {

    QosPolicy output_policy = null;
    String policyName = null;
    int cpu, ram, disk;

    ArrayList<QosPolicy> output_policies = new ArrayList<>();
    Logger.info("Getting qos policies");
    try {
      mapper = new ObjectMapper();
      String listPolicies =
          JavaStackUtils.convertHttpResponseToString(javaStack.listQosPolicies());
      System.out.println(listPolicies);
      PoliciesData inputPolicies = mapper.readValue(listPolicies, PoliciesData.class);
      System.out.println(inputPolicies.getPolicies());
      for (PolicyProperties input_policy : inputPolicies.getPolicies()) {
        System.out.println(input_policy.getId() + ": " + input_policy.getName());

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
      Logger.error("Runtime error getting openstack qos policies" + " error message: " + e.getMessage());
    }

    return output_policies;

  }


}