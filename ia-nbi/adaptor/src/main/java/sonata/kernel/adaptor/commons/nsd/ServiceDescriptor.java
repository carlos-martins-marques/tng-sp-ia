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


package sonata.kernel.adaptor.commons.nsd;

import com.fasterxml.jackson.annotation.JsonProperty;

import sonata.kernel.adaptor.commons.vnfd.AutoScalePolicy;

import java.util.ArrayList;

public class ServiceDescriptor {

  private String author;
  @JsonProperty("auto_scale_policy")
  private AutoScalePolicy autoScalePolicy;
  @JsonProperty("connection_points")
  private ArrayList<ConnectionPoint> connectionPoints;
  @JsonProperty("created_at")
  private String createdAt;
  private String description;
  @JsonProperty("descriptor_version")
  private String descriptorVersion;
  @JsonProperty("forwarding_graphs")
  private ArrayList<ForwardingGraph> forwardingGraphs;
  @JsonProperty("instance_uuid")
  private String instanceUuid;
  @JsonProperty("lifecycle_events")
  private LifeCycleEvent lifecycleEvents;
  @JsonProperty("monitoring_parameters")
  private ArrayList<MonitoringParameter> monitoringParameters;
  private String name;
  @JsonProperty("network_functions")
  private ArrayList<NetworkFunction> networkFunctions;
  @JsonProperty("network_services")
  private ArrayList<String> networkServices;
  @JsonProperty("services_dependency")
  private ArrayList<String> servicesDependency;
  private String status;
  @JsonProperty("updated_at")
  private String updatedAt;
  private String uuid;
  private String vendor;
  private String version;
  @JsonProperty("virtual_links")
  private ArrayList<VirtualLink> virtualLinks;
  @JsonProperty("vnf_depencency")
  private ArrayList<String> vnfDepencency;


  public String getAuthor() {
    return author;
  }

  public AutoScalePolicy getAutoScalePolicy() {
    return autoScalePolicy;
  }

  public ArrayList<ConnectionPoint> getConnectionPoints() {
    return connectionPoints;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getDescription() {
    return description;
  }

  public String getDescriptorVersion() {
    return descriptorVersion;
  }

  public ArrayList<ForwardingGraph> getForwardingGraphs() {
    return forwardingGraphs;
  }

  public String getInstanceUuid() {
    return instanceUuid;
  }

  public LifeCycleEvent getLifecycleEvents() {
    return lifecycleEvents;
  }

  public ArrayList<MonitoringParameter> getMonitoringParameters() {
    return monitoringParameters;
  }

  public String getName() {
    return name;
  }

  public ArrayList<NetworkFunction> getNetworkFunctions() {
    return networkFunctions;
  }

  public ArrayList<String> getNetworkServices() {
    return networkServices;
  }

  public ArrayList<String> getServicesDependency() {
    return servicesDependency;
  }

  public String getStatus() {
    return status;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public String getUuid() {
    return uuid;
  }

  public String getVendor() {
    return vendor;
  }

  public String getVersion() {
    return version;
  }

  public ArrayList<VirtualLink> getVirtualLinks() {
    return virtualLinks;
  }

  public ArrayList<String> getVnfDepencency() {
    return vnfDepencency;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setAutoScalePolicy(AutoScalePolicy autoScalePolicy) {
    this.autoScalePolicy = autoScalePolicy;
  }

  public void setConnectionPoints(ArrayList<ConnectionPoint> connectionPoints) {
    this.connectionPoints = connectionPoints;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setDescriptorVersion(String descriptorVersion) {
    this.descriptorVersion = descriptorVersion;
  }

  public void setForwardingGraphs(ArrayList<ForwardingGraph> forwardingGraphs) {
    this.forwardingGraphs = forwardingGraphs;
  }

  public void setInstanceUuid(String instanceUuid) {
    this.instanceUuid = instanceUuid;
  }

  public void setLifecycleEvents(LifeCycleEvent lifecycleEvents) {
    this.lifecycleEvents = lifecycleEvents;
  }

  public void setMonitoringParameters(ArrayList<MonitoringParameter> monitoringParameters) {
    this.monitoringParameters = monitoringParameters;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNetworkFunctions(ArrayList<NetworkFunction> networkFunctions) {
    this.networkFunctions = networkFunctions;
  }

  public void setNetworkServices(ArrayList<String> networkServices) {
    this.networkServices = networkServices;
  }

  public void setServicesDependency(ArrayList<String> servicesDependency) {
    this.servicesDependency = servicesDependency;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setVirtualLinks(ArrayList<VirtualLink> virtualLinks) {
    this.virtualLinks = virtualLinks;
  }

  public void setVnfDepencency(ArrayList<String> vnfDepencency) {
    this.vnfDepencency = vnfDepencency;
  }



}
