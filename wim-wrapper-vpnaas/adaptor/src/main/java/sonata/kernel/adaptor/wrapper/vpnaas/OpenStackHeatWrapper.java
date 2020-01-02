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
 * @author Dario Valocchi(Ph.D.), UCL
 *
 * @author Guy Paz, Nokia
 *
 */

package sonata.kernel.adaptor.wrapper.vpnaas;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;

import sonata.kernel.adaptor.AdaptorCore;
import sonata.kernel.adaptor.commons.*;
import sonata.kernel.adaptor.commons.nsd.ConnectionPoint;
import sonata.kernel.adaptor.commons.nsd.ConnectionPointRecord;
import sonata.kernel.adaptor.commons.nsd.ConnectionPointType;
import sonata.kernel.adaptor.commons.nsd.InterfaceRecord;
import sonata.kernel.adaptor.commons.nsd.NetworkFunction;
import sonata.kernel.adaptor.commons.nsd.ServiceDescriptor;
import sonata.kernel.adaptor.commons.nsd.VirtualLink;
import sonata.kernel.adaptor.commons.vnfd.VirtualDeploymentUnit;
import sonata.kernel.adaptor.commons.vnfd.VnfDescriptor;
import sonata.kernel.adaptor.commons.vnfd.VnfVirtualLink;
import sonata.kernel.adaptor.wrapper.*;
import sonata.kernel.adaptor.wrapper.vpnaas.heat.HeatModel;
import sonata.kernel.adaptor.wrapper.vpnaas.heat.HeatPort;
import sonata.kernel.adaptor.wrapper.vpnaas.heat.HeatResource;
import sonata.kernel.adaptor.wrapper.vpnaas.heat.HeatServer;
import sonata.kernel.adaptor.wrapper.vpnaas.heat.HeatTemplate;
import sonata.kernel.adaptor.wrapper.vpnaas.heat.ServerPortsComposition;
import sonata.kernel.adaptor.wrapper.vpnaas.javastackclient.models.Image.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.Map.Entry;

import static com.sun.tools.doclint.Entity.or;

public class OpenStackHeatWrapper extends ComputeWrapper {

  private static final org.slf4j.Logger Logger =
      LoggerFactory.getLogger(OpenStackHeatWrapper.class);

  private String myPool;

  /**
   * Standard constructor for an Compute Wrapper of an OpenStack VIM using Heat.
   *
   * @param config the config object for this Compute Wrapper
   */
  public OpenStackHeatWrapper( VimWrapperConfiguration config) {
    super(config);
    String configuration = getVimConfig().getConfiguration();
    Logger.debug("Wrapper specific configuration: " + configuration);
    JSONTokener tokener = new JSONTokener(configuration);
    JSONObject object = (JSONObject) tokener.nextValue();
    // String tenant = object.getString("tenant");
    String tenantCidr = null;
    if (object.has("tenant_private_net_id")) {
      String tenantNetId = object.getString("tenant_private_net_id");
      int tenantNetLength = object.getInt("tenant_private_net_length");
      tenantCidr = tenantNetId + "/" + tenantNetLength;
    } else {
      tenantCidr = "10.0.0.0/8";
    }

    this.myPool = "tango-subnet-pool";
    // If vim not exist or cidr change


  }

  /*
   * (non-Javadoc)
   *
   * @see
   * sonata.kernel.adaptor.wrapper.ComputeWrapper#removeFunction(sonata.kernel.adaptor.commons
   * .FunctionRemovePayload, java.lang.String)
   */
  @Override
  public synchronized void removeFunction(FunctionRemovePayload data, String sid) {
    Long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(getVimConfig().getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }
    // String tenantExtNet = object.getString("tenant_ext_net");
    // String tenantExtRouter = object.getString("tenant_ext_router");
    // END COMMENT

    OpenStackHeatClient client = null;


    try {
      client = new OpenStackHeatClient(getVimConfig().getVimEndpoint().toString(),
          getVimConfig().getAuthUserName(), getVimConfig().getAuthPass(), getVimConfig().getDomain(), tenant, identityPort);

    } catch (IOException e) {
      Logger.error("OpenStackHeat wrapper - Unable to connect to the VIM");
      this.setChanged();
      WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(sid, "ERROR", e.getMessage());
      this.notifyObservers(errorUpdate);
      return;
    }

    Logger.debug(
        "Getting VIM stack name and UUID for service instance ID " + data.getServiceInstanceId());
    String stackUuid = WrapperBay.getInstance().getVimRepo()
        .getServiceInstanceVimUuid(data.getServiceInstanceId(), this.getVimConfig().getUuid());
    String stackName = WrapperBay.getInstance().getVimRepo()
        .getServiceInstanceVimName(data.getServiceInstanceId(), this.getVimConfig().getUuid());

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
    mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    mapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
    mapper.setSerializationInclusion(Include.NON_NULL);

    HeatTemplate template = client.getStackTemplate(stackName, stackUuid);
    if (template == null) {
      Logger.error("Error retrieving the stack template.");
      WrapperStatusUpdate update =
          new WrapperStatusUpdate(sid, "ERROR", "Cannot retrieve service stack from VIM.");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }
    //locate resources that should be removed
    ArrayList<String> keysToRemove = new ArrayList<String>();
    for (Entry<String, Object> e: template.getResources().entrySet()) {
      if (e.getKey().contains(data.getVnfUuid())) {
        keysToRemove.add(e.getKey());
      }
    }
    //remove the resources
    for (String key: keysToRemove) {
      template.removeResource(key);
    }
    Logger.info("Updated stack for VNF removal created.");
    Logger.info("Serializing updated stack...");
    String stackString = null;
    try {
      stackString = mapper.writeValueAsString(template);
    } catch (JsonProcessingException e) {
      Logger.error(e.getMessage());
      WrapperStatusUpdate update =
          new WrapperStatusUpdate(sid, "ERROR", "Exception during VNF Deployment");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }
    Logger.debug(stackString);
    try {
      client.updateStack(stackName, stackUuid, stackString);
    } catch (Exception e) {
      Logger.error(e.getMessage());
      WrapperStatusUpdate update =
          new WrapperStatusUpdate(sid, "ERROR", "Exception during VNF Deployment");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }
    int counter = 0;
    int wait = 1000;
    int maxCounter = 50;
    int maxWait = 5000;
    String status = null;
    while ((status == null || !status.equals("UPDATE_COMPLETE") || !status.equals("UPDATE_FAILED"))
        && counter < maxCounter) {
      status = client.getStackStatus(stackName, stackUuid);
      Logger.info("Status of stack " + stackUuid + ": " + status);
      if (status != null && (status.equals("UPDATE_COMPLETE") || status.equals("UPDATE_FAILED"))) {
        break;
      }
      try {
        Thread.sleep(wait);
      } catch (InterruptedException e) {
        Logger.error(e.getMessage(), e);
      }
      counter++;
      wait = Math.min(wait * 2, maxWait);

    }

    if (status == null) {
      Logger.error("unable to contact the VIM to check the update status");
      WrapperStatusUpdate update = new WrapperStatusUpdate(sid, "ERROR",
          "Function deployment process failed. Can't get update status.");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }
    if (status.equals("UPDATE_FAILED")) {
      Logger.error("Heat Stack update process failed on the VIM side.");
      WrapperStatusUpdate update = new WrapperStatusUpdate(sid, "ERROR",
          "Function deployment process failed on the VIM side.");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }

    Logger.info("Creating function remove response");
    FunctionRemoveResponse response = new FunctionRemoveResponse();
    response.setRequestStatus("COMPLETED");
    response.setMessage("");

    String body = null;
    try {
      body = mapper.writeValueAsString(response);
    } catch (JsonProcessingException e) {
      Logger.error(e.getMessage());
      WrapperStatusUpdate update =
          new WrapperStatusUpdate(sid, "ERROR", "Exception during VNF Deployment");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }
    Logger.info("Response created");
    Logger.info("body");

    //WrapperBay.getInstance().getVimRepo().removeFunctionInstanceEntry(data.getVnfUuid(), this.getVimConfig().getUuid());
    WrapperStatusUpdate update = new WrapperStatusUpdate(sid, "SUCCESS", body);
    this.markAsChanged();
    this.notifyObservers(update);
    long stop = System.currentTimeMillis();

    Logger.info("[OpenStackWrapper]FunctionRemove-time: " + (stop - start) + " ms");

  }

  /*
   * (non-Javadoc)
   *
   * @see
   * sonata.kernel.adaptor.wrapper.ComputeWrapper#deployFunction(sonata.kernel.adaptor.commons
   * .FunctionDeployPayload, java.lang.String)
   */
  @Override
  public synchronized void deployFunction(FunctionDeployPayload data, String sid) {
    Long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(getVimConfig().getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }
    // String tenantExtNet = object.getString("tenant_ext_net");
    // String tenantExtRouter = object.getString("tenant_ext_router");
    // END COMMENT

    OpenStackHeatClient client = null;
    OpenStackNeutronClient neutronClient = null;

    try {
      client = new OpenStackHeatClient(getVimConfig().getVimEndpoint().toString(),
          getVimConfig().getAuthUserName(), getVimConfig().getAuthPass(), getVimConfig().getDomain(), tenant, identityPort);
      neutronClient = new OpenStackNeutronClient(getVimConfig().getVimEndpoint().toString(),
          getVimConfig().getAuthUserName(), getVimConfig().getAuthPass(), getVimConfig().getDomain(), tenant, identityPort);
    } catch (IOException e) {
      Logger.error("OpenStackHeat wrapper - Unable to connect to the VIM");
      this.setChanged();
      WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(sid, "ERROR", e.getMessage());
      this.notifyObservers(errorUpdate);
      return;
    }

    Logger.debug(
        "Getting VIM stack name and UUID for service instance ID " + data.getServiceInstanceId());
    String stackUuid = WrapperBay.getInstance().getVimRepo()
        .getServiceInstanceVimUuid(data.getServiceInstanceId(), this.getVimConfig().getUuid());
    String stackName = WrapperBay.getInstance().getVimRepo()
        .getServiceInstanceVimName(data.getServiceInstanceId(), this.getVimConfig().getUuid());


    HeatModel stackAddition;

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
    mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    mapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
    mapper.setSerializationInclusion(Include.NON_NULL);

    HeatTemplate template = client.getStackTemplate(stackName, stackUuid);
    if (template == null) {
      Logger.error("Error retrieving the stack template.");
      WrapperStatusUpdate update =
          new WrapperStatusUpdate(sid, "ERROR", "Cannot retrieve service stack from VIM.");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }
    try {
      stackAddition =
          translate(data.getVnfd(), template.getResources().keySet(), data.getServiceInstanceId(), data.getPublicKey());
    } catch (Exception e) {
      Logger.error("Error: " + e.getMessage());
      e.printStackTrace();
      WrapperStatusUpdate update =
          new WrapperStatusUpdate(sid, "ERROR", "Exception during VNFD translation.");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }
    for (HeatResource resource : stackAddition.getResources()) {
      template.putResource(resource.getResourceName(), resource);
    }

    Logger.info("Updated stack for VNF deployment created.");
    Logger.info("Serializing updated stack...");
    String stackString = null;
    try {
      stackString = mapper.writeValueAsString(template);
    } catch (JsonProcessingException e) {
      Logger.error(e.getMessage());
      WrapperStatusUpdate update =
          new WrapperStatusUpdate(sid, "ERROR", "Exception during VNF Deployment");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }
    Logger.debug(stackString);
    try {
      client.updateStack(stackName, stackUuid, stackString);
    } catch (Exception e) {
      Logger.error(e.getMessage());
      WrapperStatusUpdate update =
          new WrapperStatusUpdate(sid, "ERROR", "Exception during VNF Deployment");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }
    int counter = 0;
    int wait = 1000;
    int maxCounter = 50;
    int maxWait = 5000;
    String status = null;
    while ((status == null || !status.equals("UPDATE_COMPLETE") || !status.equals("UPDATE_FAILED"))
        && counter < maxCounter) {
      status = client.getStackStatus(stackName, stackUuid);
      Logger.info("Status of stack " + stackUuid + ": " + status);
      if (status != null && (status.equals("UPDATE_COMPLETE") || status.equals("UPDATE_FAILED"))) {
        break;
      }
      try {
        Thread.sleep(wait);
      } catch (InterruptedException e) {
        Logger.error(e.getMessage(), e);
      }
      counter++;
      wait = Math.min(wait * 2, maxWait);

    }

    if (status == null) {
      Logger.error("unable to contact the VIM to check the update status");
      WrapperStatusUpdate update = new WrapperStatusUpdate(sid, "ERROR",
          "Function deployment process failed. Can't get update status.");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }
    if (status.equals("UPDATE_FAILED")) {
      Logger.error("Heat Stack update process failed on the VIM side.");
      WrapperStatusUpdate update = new WrapperStatusUpdate(sid, "ERROR",
          "Function deployment process failed on the VIM side.");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }

    // counter = 0;
    // wait = 1000;
    // StackComposition composition = null;
    // while (composition == null && counter < maxCounter) {
    //   Logger.info("Getting composition of stack " + stackUuid);
    //   composition = client.getStackComposition(stackName, stackUuid);
    //   try {
    //     Thread.sleep(wait);
    //   } catch (InterruptedException e) {
    //     Logger.error(e.getMessage(), e);
    //   }
    //   counter++;
    //   wait = Math.min(wait * 2, maxWait);
    // }

    // if (composition == null) {
    //   Logger.error("unable to contact the VIM to get the stack composition");
    //   WrapperStatusUpdate update =
    //       new WrapperStatusUpdate(sid, "ERROR", "Unable to get updated stack composition");
    //   this.markAsChanged();
    //   this.notifyObservers(update);
    //   return;
    // }

    Logger.info("Creating function deploy response");
    // Aux data structures for efficient mapping
    Hashtable<String, VirtualDeploymentUnit> vduTable =
        new Hashtable<String, VirtualDeploymentUnit>();
    Hashtable<String, VduRecord> vdurTable = new Hashtable<String, VduRecord>();

    // Create the response

    FunctionDeployResponse response = new FunctionDeployResponse();
    VnfDescriptor vnfd = data.getVnfd();
    response.setRequestStatus("COMPLETED");
    response.setInstanceVimUuid(stackUuid);
    response.setInstanceName(stackName);
    response.setVimUuid(this.getVimConfig().getUuid());
    response.setMessage("");


    VnfRecord vnfr = new VnfRecord();
    vnfr.setDescriptorVersion("vnfr-schema-01");
    vnfr.setId(vnfd.getInstanceUuid());
    vnfr.setDescriptorReference(vnfd.getUuid());
    vnfr.setStatus(Status.offline);
    // vnfr.setDescriptorReferenceName(vnf.getName());
    // vnfr.setDescriptorReferenceVendor(vnf.getVendor());
    // vnfr.setDescriptorReferenceVersion(vnf.getVersion());

    for (VirtualDeploymentUnit vdu : vnfd.getVirtualDeploymentUnits()) {
      Logger.debug("Inspecting VDU " + vdu.getId());
      VduRecord vdur = new VduRecord();
      vdur.setId(vdu.getId());
      vdur.setNumberOfInstances(1);
      vdur.setVduReference(vnfd.getName() + ":" + vdu.getId());
      vdur.setVmImage(vdu.getVmImage());
      vdurTable.put(vdur.getVduReference(), vdur);
      vnfr.addVdu(vdur);
      Logger.debug("VDU table created: " + vduTable.toString());

      HeatServer server = client.getServerComposition(stackName, stackUuid, vdu.getId());

      String[] identifiers = server.getServerName().split("\\.");
      String vnfName = identifiers[0];
      String vduName = identifiers[1];
      // String instanceId = identifiers[2];
      String vnfcIndex = identifiers[3];
      if (vdu.getId().equals(vduName)) {
        VnfcInstance vnfc = new VnfcInstance();
        vnfc.setId(vnfcIndex);
        vnfc.setVimId(data.getVimUuid());
        vnfc.setVcId(server.getServerId());
        //vnfc.setHostId(server.getHostId());
        ArrayList<ConnectionPointRecord> cpRecords = new ArrayList<ConnectionPointRecord>();
        ServerPortsComposition ports = client.getServerPortsComposition(stackName, stackUuid, vdu.getId());
        for (ConnectionPoint cp : vdu.getConnectionPoints()) {
          Logger.debug("Mapping CP " + cp.getId());
          Logger.debug("Looking for port " + vnfd.getName() + "." + vdu.getId() + "." + cp.getId()
              + "." + vnfd.getInstanceUuid());
          ConnectionPointRecord cpr = new ConnectionPointRecord();
          cpr.setId(cp.getId());


          // add each composition.ports information in the response. The IP, the netmask (and
          // maybe MAC address)
          boolean found = false;
          for (HeatPort port : ports.getPorts()) {
            Logger.debug("port " + port.getPortName());
            if (port.getPortName().equals(vnfd.getName() + "." + vdu.getId() + "." + cp.getId()
                + "." + vnfd.getInstanceUuid())) {
              found = true;
              Logger.debug("Found! Filling VDUR parameters");
              InterfaceRecord ip = new InterfaceRecord();
              if (port.getFloatinIp() != null) {
                ip.setAddress(port.getFloatinIp());
                ip.setHardwareAddress(port.getMacAddress());
                IpMapping ipMap = new IpMapping();
                ipMap.setFloatingIp(port.getFloatinIp());
                ipMap.setInternalIp(port.getIpAddress());
                response.addIp(ipMap);
                // Logger.info("Port:" + port.getPortName() + "- Addr: " +
                // port.getFloatingIp());
              } else {
                ip.setAddress(port.getIpAddress());
                ip.setHardwareAddress(port.getMacAddress());
                // Logger.info("Port:" + port.getPortName() + "- Addr: " +
                // port.getFloatingIp());
                ip.setNetmask("255.255.255.248");

              }
              cpr.setInterface(ip);
              cpr.setType(cp.getType());
              break;
            }
          }
          if (!found) {
            Logger.error("Can't find the VIM port that maps to this CP");
          }
          cpRecords.add(cpr);
        }
        vnfc.setConnectionPoints(cpRecords);
        VduRecord referenceVdur = vdurTable.get(vnfd.getName() + ":" + vdu.getId());
        referenceVdur.addVnfcInstance(vnfc);

      }

    }

    response.setVnfr(vnfr);
    String body = null;
    try {
      body = mapper.writeValueAsString(response);
    } catch (JsonProcessingException e) {
      Logger.error(e.getMessage());
      WrapperStatusUpdate update =
          new WrapperStatusUpdate(sid, "ERROR", "Exception during VNF Deployment");
      this.markAsChanged();
      this.notifyObservers(update);
      return;
    }
    Logger.info("Response created");
    // Logger.info("body");

    WrapperBay.getInstance().getVimRepo().writeFunctionInstanceEntry(vnfd.getInstanceUuid(),
        data.getServiceInstanceId(), this.getVimConfig().getUuid());
    WrapperStatusUpdate update = new WrapperStatusUpdate(sid, "SUCCESS", body);
    this.markAsChanged();
    this.notifyObservers(update);
    long stop = System.currentTimeMillis();

    Logger.info("[OpenStackWrapper]FunctionDeploy-time: " + (stop - start) + " ms");
  }



  /*
   * (non-Javadoc)
   *
   * @see sonata.kernel.adaptor.wrapper.ComputeWrapper#networkCreate(java.lang.String,
   * ArrayList<VirtualLink> virtualLinks)
   */
  @Override
  public boolean networkCreate(String instanceId, ArrayList<VirtualLink> virtualLinks) throws Exception {
    Long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(getVimConfig().getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }
    // String tenantExtNet = object.getString("tenant_ext_net");
    // String tenantExtRouter = object.getString("tenant_ext_router");
    // END COMMENT

    OpenStackHeatClient client = null;
    OpenStackNeutronClient neutronClient = null;

    try {
      client = new OpenStackHeatClient(getVimConfig().getVimEndpoint().toString(),
          getVimConfig().getAuthUserName(), getVimConfig().getAuthPass(), getVimConfig().getDomain(), tenant, identityPort);
      neutronClient = new OpenStackNeutronClient(getVimConfig().getVimEndpoint().toString(),
          getVimConfig().getAuthUserName(), getVimConfig().getAuthPass(), getVimConfig().getDomain(), tenant, identityPort);
    } catch (IOException e) {
      Logger.error("OpenStackHeat wrapper - Unable to connect to the VIM");
      return false;
    }

     HeatModel stackAddition;

    ObjectMapper mapper_y = new ObjectMapper(new YAMLFactory());
    mapper_y.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
    mapper_y.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    mapper_y.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
    mapper_y.setSerializationInclusion(Include.NON_NULL);

    try {
      stackAddition =  translateNetwork(instanceId, virtualLinks);
    } catch (Exception e) {
      Logger.error("Error: " + e.getMessage());
      e.printStackTrace();
      return false;
    }

    String stackUuid = WrapperBay.getInstance().getVimRepo()
        .getServiceInstanceVimUuid(instanceId, this.getVimConfig().getUuid());
    String stackName = WrapperBay.getInstance().getVimRepo()
        .getServiceInstanceVimName(instanceId, this.getVimConfig().getUuid());

    if (stackUuid != null && stackName != null) {
      Logger.info("Update the stack.");

      HeatTemplate template = client.getStackTemplate(stackName, stackUuid);
      if (template == null) {
        Logger.error("Error retrieving the stack template.");
        return false;
      }
      for (HeatResource resource : stackAddition.getResources()) {
        template.putResource(resource.getResourceName(), resource);
      }

      Logger.info("Updated stack for VNF network create created.");
      Logger.info("Serializing updated stack...");
      String stackString = null;
      try {
        stackString = mapper_y.writeValueAsString(template);
      } catch (JsonProcessingException e) {
        Logger.error(e.getMessage());
        return false;
      }
      Logger.debug(stackString);
      try {
        client.updateStack(stackName, stackUuid, stackString);
      } catch (Exception e) {
        Logger.error(e.getMessage());
        return false;
      }

      int counter = 0;
      int wait = 1000;
      int maxCounter = 50;
      int maxWait = 5000;
      String status = null;
      while ((status == null || !status.equals("UPDATE_COMPLETE") || !status.equals("UPDATE_FAILED"))
          && counter < maxCounter) {
        status = client.getStackStatus(stackName, stackUuid);
        Logger.info("Status of stack " + stackUuid + ": " + status);
        if (status != null && (status.equals("UPDATE_COMPLETE") || status.equals("UPDATE_FAILED"))) {
          break;
        }
        try {
          Thread.sleep(wait);
        } catch (InterruptedException e) {
          Logger.error(e.getMessage(), e);
        }
        counter++;
        wait = Math.min(wait * 2, maxWait);

      }

      if (status == null) {
        Logger.error("unable to contact the VIM to check the update status");
        return false;
      }
      if (status.equals("UPDATE_FAILED")) {
        Logger.error("Heat Stack update process failed on the VIM side.");
        return false;
      }

      Logger.info("VIM updated successfully.");

    } else {
      Logger.info("Deploying new stack.");
      ObjectMapper mapper = SonataManifestMapper.getSonataMapper();

      HeatTemplate template = new HeatTemplate();
      for (HeatResource resource : stackAddition.getResources()) {
        template.putResource(resource.getResourceName(), resource);
      }

      Logger.info("Serializing stack...");
      try {
        String stackString = mapper.writeValueAsString(template);
        Logger.debug(stackString);
        stackName = "SonataService-" + instanceId;
        Logger.info("Pushing stack to Heat...");
        stackUuid = client.createStack(stackName, stackString);

        if (stackUuid == null) {
          Logger.error("unable to contact the VIM to instantiate the service");
          return false;
        }
        int counter = 0;
        int wait = 1000;
        int maxWait = 15000;
        int maxCounter = 50;
        String status = null;
        while ((status == null || !status.equals("CREATE_COMPLETE")
            || !status.equals("CREATE_FAILED")) && counter < maxCounter) {
          status = client.getStackStatus(stackName, stackUuid);
          Logger.info("Status of stack " + stackUuid + ": " + status);
          if (status != null
              && (status.equals("CREATE_COMPLETE") || status.equals("CREATE_FAILED"))) {
            break;
          }
          try {
            Thread.sleep(wait);
          } catch (InterruptedException e) {
            Logger.error(e.getMessage(), e);
          }
          counter++;
          wait = Math.min(wait * 2, maxWait);
        }

        if (status == null) {
          Logger.error("unable to contact the VIM to check the instantiation status");
          return false;
        }
        if (status.equals("CREATE_FAILED")) {
          Logger.error("Heat Stack creation process failed on the VIM side.");
          return false;
        }
        Logger.info("VIM prepared successfully. Creating record in Infra Repo.");
        WrapperBay.getInstance().getVimRepo().writeServiceInstanceEntry(instanceId, stackUuid,
            stackName, this.getVimConfig().getUuid());

      } catch (Exception e) {
        Logger.error("Error during stack creation.");
        Logger.error(e.getMessage());
        return false;
      }
    }

    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]NetworkCreate-time: " + (stop - start) + " ms");
    return true;
  }


  /*
   * (non-Javadoc)
   *
   * @see sonata.kernel.adaptor.wrapper.ComputeWrapper#networkDelete(java.lang.String,
   * ArrayList<VirtualLink> virtualLinks)
   */
  @Override
  public boolean networkDelete(String instanceId, ArrayList<VirtualLink> virtualLinks) throws Exception {
    Long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(getVimConfig().getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }
    // String tenantExtNet = object.getString("tenant_ext_net");
    // String tenantExtRouter = object.getString("tenant_ext_router");
    // END COMMENT

    OpenStackHeatClient client = null;

    try {
      client = new OpenStackHeatClient(getVimConfig().getVimEndpoint().toString(),
          getVimConfig().getAuthUserName(), getVimConfig().getAuthPass(), getVimConfig().getDomain(), tenant, identityPort);
    } catch (IOException e) {
      Logger.error("OpenStackHeat wrapper - Unable to connect to the VIM");
      return false;
    }

    Logger.debug("Getting VIM stack name and UUID for service instance ID " + instanceId);
    String stackUuid = WrapperBay.getInstance().getVimRepo()
        .getServiceInstanceVimUuid(instanceId, this.getVimConfig().getUuid());
    String stackName = WrapperBay.getInstance().getVimRepo()
        .getServiceInstanceVimName(instanceId, this.getVimConfig().getUuid());

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
    mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    mapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
    mapper.setSerializationInclusion(Include.NON_NULL);


    HeatTemplate template = client.getStackTemplate(stackName, stackUuid);
    if (template == null) {
      Logger.error("Error retrieving the stack template.");
      return false;
    }

    //locate resources that should be removed
    ArrayList<String> keysToRemove = new ArrayList<String>();
    for (Entry<String, Object> e: template.getResources().entrySet()) {
      for (VirtualLink link : virtualLinks) {
        if (e.getKey().contains(link.getId())) {
          keysToRemove.add(e.getKey());
          break;
        }
      }
    }
    //remove the resources
    for (String key: keysToRemove) {
      template.removeResource(key);
    }

    if (!template.getResources().isEmpty()) {

      Logger.info("Updated stack for VNF network delete created.");
      Logger.info("Serializing updated stack...");
      String stackString = null;
      try {
        stackString = mapper.writeValueAsString(template);
      } catch (JsonProcessingException e) {
        Logger.error(e.getMessage());
        return false;
      }
      Logger.debug(stackString);
      try {
        client.updateStack(stackName, stackUuid, stackString);
      } catch (Exception e) {
        Logger.error(e.getMessage());
        return false;
      }

      int counter = 0;
      int wait = 1000;
      int maxCounter = 50;
      int maxWait = 5000;
      String status = null;
      while ((status == null || !status.equals("UPDATE_COMPLETE") || !status.equals("UPDATE_FAILED"))
          && counter < maxCounter) {
        status = client.getStackStatus(stackName, stackUuid);
        Logger.info("Status of stack " + stackUuid + ": " + status);
        if (status != null && (status.equals("UPDATE_COMPLETE") || status.equals("UPDATE_FAILED"))) {
          break;
        }
        try {
          Thread.sleep(wait);
        } catch (InterruptedException e) {
          Logger.error(e.getMessage(), e);
        }
        counter++;
        wait = Math.min(wait * 2, maxWait);
      }

      if (status == null) {
        Logger.error("unable to contact the VIM to check the update status");
        return false;
      }
      if (status.equals("UPDATE_FAILED")) {
        Logger.error("Heat Stack update process failed on the VIM side.");
        return false;
      }

      Logger.info("VIM updated successfully.");

    } else {

      try {
        String output = client.deleteStack(stackName, stackUuid);

        if (output.equals("DELETED")) {
          int counter = 0;
          int wait = 1000;
          int maxCounter = 20;
          int maxWait = 5000;
          String status = null;
          while (counter < maxCounter) {
            status = client.getStackStatus(stackName, stackUuid);
            Logger.info("Status of stack " + stackUuid + ": " + status);
            if (status == null || (status.equals("DELETE_COMPLETE") || status.equals("DELETE_FAILED"))) {
              break;
            }
            try {
              Thread.sleep(wait);
            } catch (InterruptedException e) {
              Logger.error(e.getMessage(), e);
            }
            counter++;
            wait = Math.min(wait * 2, maxWait);

          }

          if (status != null && status.equals("DELETE_FAILED")) {
            Logger.error("Heat Stack delete process failed on the VIM side.");
            return false;
          }

          WrapperBay.getInstance().getVimRepo().removeServiceInstanceEntry(instanceId, this.getVimConfig().getUuid());
        }
      } catch (Exception e) {
        Logger.error(e.getMessage());
        return false;
      }
      Logger.info("Stack deleted successfully.");
    }

    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]NetworkDelete-time: " + (stop - start) + " ms");
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see sonata.kernel.adaptor.wrapper.ComputeWrapper#removeImage(java.lang.String)
   */
  @Override
  public void removeImage(VnfImage image) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   *
   * @see
   * sonata.kernel.adaptor.wrapper.ComputeWrapper#removeService(sonata.kernel.adaptor.commons
   * .ServiceRemovePayload, java.lang.String)
   */
  @Override
  public void removeService(ServiceRemovePayload data, String callSid) {
    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(getVimConfig().getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }
    // String tenantExtNet = object.getString("tenant_ext_net");
    // String tenantExtRouter = object.getString("tenant_ext_router");
    // END COMMENT
    VimRepo repo = WrapperBay.getInstance().getVimRepo();
    Logger.info("Trying to remove NS instance: " + data.getServiceInstanceId());
    String stackName = repo.getServiceInstanceVimName(data.getServiceInstanceId(), this.getVimConfig().getUuid());
    String stackUuid = repo.getServiceInstanceVimUuid(data.getServiceInstanceId(), this.getVimConfig().getUuid());
    Logger.info("NS instance mapped to stack name: " + stackName);
    Logger.info("NS instance mapped to stack uuid: " + stackUuid);

    OpenStackHeatClient client = null;

    try {
      client = new OpenStackHeatClient(getVimConfig().getVimEndpoint().toString(),
          getVimConfig().getAuthUserName(), getVimConfig().getAuthPass(), getVimConfig().getDomain(), tenant, identityPort);
    } catch (IOException e) {
      Logger.error("OpenStackHeat wrapper - Unable to connect to the VIM");
      this.setChanged();
      WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(callSid, "ERROR", e.getMessage());
      this.notifyObservers(errorUpdate);
    }

    try {
      String output = client.deleteStack(stackName, stackUuid);

      if (output.equals("DELETED")) {
        int counter = 0;
        int wait = 1000;
        int maxCounter = 20;
        int maxWait = 5000;
        String status = null;
        while (counter < maxCounter) {
          status = client.getStackStatus(stackName, stackUuid);
          Logger.info("Status of stack " + stackUuid + ": " + status);
          if (status == null || (status.equals("DELETE_COMPLETE") || status.equals("DELETE_FAILED"))) {
            break;
          }
          try {
            Thread.sleep(wait);
          } catch (InterruptedException e) {
            Logger.error(e.getMessage(), e);
          }
          counter++;
          wait = Math.min(wait * 2, maxWait);

        }

        if (status != null && status.equals("DELETE_FAILED")) {
          Logger.error("Heat Stack delete process failed on the VIM side.");
          WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(callSid, "ERROR",
              "Remove service process failed on the VIM side.");
          this.setChanged();
          this.notifyObservers(errorUpdate);
          return;
        }

        repo.removeServiceInstanceEntry(data.getServiceInstanceId(), this.getVimConfig().getUuid());
        this.setChanged();
        String body =
            "{\"status\":\"COMPLETED\",\"wrapper_uuid\":\"" + this.getVimConfig().getUuid() + "\"}";
        WrapperStatusUpdate update = new WrapperStatusUpdate(callSid, "SUCCESS", body);
        this.notifyObservers(update);
      }
    } catch (Exception e) {
      e.printStackTrace();
      this.setChanged();
      WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(callSid, "ERROR", e.getMessage());
      this.notifyObservers(errorUpdate);
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]RemoveService-time: " + (stop - start) + " ms");
  }




  @Override
  public ArrayList<ExtNetwork> getNetworks() {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(getVimConfig().getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }
    // String tenantExtNet = object.getString("tenant_ext_net");
    // String tenantExtRouter = object.getString("tenant_ext_router");
    // END COMMENT

    ArrayList<ExtNetwork> output = null;
    Logger.info("OpenStack wrapper - Getting networks ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(getVimConfig().getVimEndpoint().toString(),
          getVimConfig().getAuthUserName(), getVimConfig().getAuthPass(), getVimConfig().getDomain(), tenant, identityPort);

      output = neutronClient.getNetworks();

      Logger.info("OpenStack wrapper - Networks retrieved.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]getNetworks-time: " + (stop - start) + " ms");
    return output;
  }

  @Override
  public ArrayList<Router> getRouters() {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(getVimConfig().getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }
    String tenantExtNet = object.getString("tenant_ext_net");
    // String tenantExtRouter = object.getString("tenant_ext_router");
    // END COMMENT

    ArrayList<Router> output = null;
    Logger.info("OpenStack wrapper - Getting routers ...");
    try {
      OpenStackNeutronClient neutronClient = new OpenStackNeutronClient(getVimConfig().getVimEndpoint().toString(),
          getVimConfig().getAuthUserName(), getVimConfig().getAuthPass(), getVimConfig().getDomain(), tenant, identityPort);

      output = neutronClient.getRouters(tenantExtNet);

      Logger.info("OpenStack wrapper - Routers retrieved.");
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      output = null;
    }
    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]getRouters-time: " + (stop - start) + " ms");
    return output;
  }

  public Boolean createOrUpdateSubnetPools(String name, String prefix, String defaultPrefixlen) {

    long start = System.currentTimeMillis();
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(getVimConfig().getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    String tenant = object.getString("tenant");
    String identityPort = null;
    if (object.has("identity_port")) {
      identityPort = object.getString("identity_port");
    }

    ArrayList<String> prefixes = new ArrayList<>();
    String id = null;

    boolean output = false;
    OpenStackNeutronClient neutronClient = null;
    try {
      neutronClient = new OpenStackNeutronClient(getVimConfig().getVimEndpoint().toString(),
          getVimConfig().getAuthUserName(), getVimConfig().getAuthPass(), getVimConfig().getDomain(), tenant, identityPort);
    } catch (IOException e) {
      Logger.error("OpenStack wrapper - Unable to connect to PoP.");;
      long stop = System.currentTimeMillis();
      Logger.info("[OpenStackWrapper]createOrUpdateSubnetPools-time: " + (stop - start) + " ms");
      return output;
    }
    Logger.info("OpenStack wrapper - List Subnet Pools ...");
    ArrayList<SubnetPool> subnetPools = null;
    try {
      subnetPools = neutronClient.getSubnetPools();

      Logger.info("OpenStack wrapper - Subnet Pools Listed.");
    } catch (Exception e) {
      Logger.error("OpenStack wrapper - Unable to list subnet. ERROR:"+e.getMessage());;
      output = false;
    }

    if (subnetPools != null) {
      for (SubnetPool inputSubnetPool : subnetPools) {
        if (inputSubnetPool.getName().equals(name)) {
          id = inputSubnetPool.getId();
          prefixes = inputSubnetPool.getPrefixes();
          break;
        }
      }
    }

    if (id == null) {
      // create
      prefixes.add(prefix);
      Logger.info("OpenStack wrapper - Creating Subnet Pool ...");
      try {
        String uuid = neutronClient.createSubnetPool(name, prefixes, defaultPrefixlen);
        if (uuid != null) {
          output = true;
          Logger.info("OpenStack wrapper - Subnet Pool Created.");
        }
      } catch (Exception e) {
        Logger.error("OpenStack wrapper - Unable to create subnet. ERROR:"+e.getMessage());;
        output = false;
      }

    } else {
      // update if prefix not exist
      if (!prefixes.contains(prefix)) {
        prefixes.add(prefix);
        Logger.info("OpenStack wrapper - Updating Subnet Pool ...");
        try {
          String uuid = neutronClient.updateSubnetPool(id, prefixes);
          if (uuid != null) {
            output = true;
            Logger.info("OpenStack wrapper - Subnet Pool Updated.");
          }
        } catch (Exception e) {
          Logger.error("OpenStack wrapper - Unable to update subnet. ERROR:"+e.getMessage());;
          output = false;
        }
      } else {
        // already exist with this prefix
        output = true;
      }
    }

    long stop = System.currentTimeMillis();
    Logger.info("[OpenStackWrapper]createOrUpdateSubnetPools-time: " + (stop - start) + " ms");
    return output;
  }





  private String getTenant() {
    JSONTokener tokener = new JSONTokener(getVimConfig().getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    return object.getString("tenant");
  }



  private boolean searchImageByChecksum(String imageChecksum, ArrayList<Image> glanceImages) {
    Logger.debug("Image lookup based on image checksum...");
    for (Image glanceImage : glanceImages) {
      if (glanceImage.getName() == null) continue;
      Logger.debug("Checking " + glanceImage.getName());
      if (glanceImage.getChecksum() == null) continue;
      if (glanceImage.getChecksum().equals(imageChecksum)) {
        return true;
      }
    }
    return false;
  }

  private boolean searchImageByName(String imageName, ArrayList<Image> glanceImages) {
    Logger.debug("Image lookup based on image name...");
    for (Image glanceImage : glanceImages) {
      if (glanceImage.getName() == null) continue;
      Logger.debug("Checking " + glanceImage.getName());
      if (glanceImage.getName().equals(imageName)) {
        return true;
      }
    }
    return false;
  }


  private HeatModel translate(VnfDescriptor vnfd, Set<String> resources, String serviceInstanceUuid,
                              String publicKey) throws Exception {
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(getVimConfig().getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    // String tenant = object.getString("tenant");
    String tenantExtNet = object.getString("tenant_ext_net");
    String tenantExtRouter = object.getString("tenant_ext_router");
    // END COMMENT
    HeatModel model = new HeatModel();
    ArrayList<String> publicPortNames = new ArrayList<String>();

    ArrayList<HashMap<String,Object>> configList = new ArrayList<HashMap<String, Object>>();

    ArrayList<VnfVirtualLink> NewVnfVirtualLinks = new ArrayList<VnfVirtualLink>();

    boolean hasPubKey = (publicKey != null);

    if (hasPubKey) {
      HeatResource keypair = new HeatResource();
      keypair.setType("OS::Nova::KeyPair");
      keypair.setName(vnfd.getName() + "_" + vnfd.getInstanceUuid() + "_keypair");
      keypair.putProperty("name", vnfd.getName() + "_" + vnfd.getInstanceUuid() + "_keypair");
      keypair.putProperty("save_private_key", "false");
      keypair.putProperty("public_key", publicKey);
      model.addResource(keypair);

      HashMap<String, Object> userMap = new HashMap<String, Object>();
      userMap.put("name", "sonatamano");
      userMap.put("gecos", "SONATA MANO admin user");
      String[] userGroups = {"adm", "audio", "cdrom", "dialout", "dip", "floppy", "netdev",
          "plugdev", "sudo", "video"};
      userMap.put("groups", userGroups);
      userMap.put("shell", "/bin/bash");
      String[] keys = {publicKey};
      userMap.put("ssh-authorized-keys", keys);
      userMap.put("home", "/home/sonatamano");
      Object[] usersList = {"default", userMap};

      HashMap<String, Object> keyCloudConfigMap = new HashMap<String, Object>();
      keyCloudConfigMap.put("users", usersList);

      HeatResource keycloudConfigObject = new HeatResource();
      keycloudConfigObject.setType("OS::Heat::CloudConfig");
      keycloudConfigObject.setName(vnfd.getName() + "_" + vnfd.getInstanceUuid() + "_keyCloudConfig");
      keycloudConfigObject.putProperty("cloud_config", keyCloudConfigMap);
      model.addResource(keycloudConfigObject);

      HashMap<String, Object> keyInitMap = new HashMap<String, Object>();
      keyInitMap.put("get_resource", vnfd.getName() + "_" + vnfd.getInstanceUuid() + "_keyCloudConfig");

      HashMap<String,Object> partMap1 = new HashMap<String, Object>();
      partMap1.put("config", keyInitMap);

      configList.add(partMap1);
    }

    // addSpAddressCloudConfigObject(vnfd, instanceUuid, model);

    // HashMap<String, Object> spAddressInitMap = new HashMap<String, Object>();
    // spAddressInitMap.put("get_resource", vnfd.getName() + "_" + instanceUuid + "_spAddressCloudConfig");

    // HashMap<String,Object> partMap2 = new HashMap<String, Object>();
    // partMap2.put("config", spAddressInitMap);

    // configList.add(partMap2);

    for (VirtualDeploymentUnit vdu : vnfd.getVirtualDeploymentUnits()) {
      Logger.debug("Each VDU goes into a resource group with a number of Heat Server...");
      HeatResource resourceGroup = new HeatResource();
      resourceGroup.setType("OS::Heat::ResourceGroup");
      resourceGroup.setName(vnfd.getName() + "." + vdu.getId() + "." + vnfd.getInstanceUuid());
      resourceGroup.putProperty("count", new Integer(1));
      String image =
          vnfd.getVendor() + "_" + vnfd.getName() + "_" + vnfd.getVersion() + "_" + vdu.getId();


      Logger.debug("image selected:" + image);
      HeatResource server = new HeatResource();
      server.setType("OS::Nova::Server");
      server.setName(null);
      server.putProperty("name",
          vnfd.getName() + "." + vdu.getId() + "." + vnfd.getInstanceUuid() + ".instance%index%");
      server.putProperty("image", image);

      String userData = vdu.getUserData();
      Logger.debug("User data for this vdu:" + userData);

      boolean vduHasUserData = (vdu.getUserData() != null);
      ArrayList<HashMap<String,Object>> newConfigList = new ArrayList<HashMap<String, Object>>(configList);

      if (vduHasUserData) {
        Logger.debug("Adding cloud-init resource");
        server.putProperty("config_drive", true);
        if (configList.isEmpty()){
          server.putProperty("user_data", userData);
          server.putProperty("user_data_format", "RAW");

        } else {
          HeatResource userDataObject = new HeatResource();
          userDataObject.setType("OS::Heat::SoftwareConfig");
          userDataObject.setName(vdu.getId() + "_" + vnfd.getInstanceUuid() + "_cloudInitConfig");
          userDataObject.putProperty("group", "ungrouped");
          userDataObject.putProperty("config", vdu.getUserData());
          model.addResource(userDataObject);

          HashMap<String, Object> cloudInitMap = new HashMap<String, Object>();
          cloudInitMap.put("get_resource", vdu.getId() + "_" + vnfd.getInstanceUuid() + "_cloudInitConfig");

          HashMap<String,Object> partMap3 = new HashMap<String, Object>();
          partMap3.put("config", cloudInitMap);
          newConfigList.add(partMap3);
        }
      }

      for (HashMap config : configList){
        Logger.debug(config.toString());
      }
      if (!newConfigList.isEmpty()){
        HeatResource serverInitObject = new HeatResource();
        serverInitObject.setType("OS::Heat::MultipartMime");
        serverInitObject.setName(vdu.getId() + "_" + vnfd.getInstanceUuid() + "_serverInit");
        serverInitObject.putProperty("parts", newConfigList);
        model.addResource(serverInitObject);

        HashMap<String, Object> userDataMap = new HashMap<String, Object>();
        userDataMap.put("get_resource", vdu.getId() + "_" + vnfd.getInstanceUuid() + "_serverInit");
        server.putProperty("user_data", userDataMap);
        server.putProperty("user_data_format", "SOFTWARE_CONFIG");
      }


      ArrayList<HashMap<String, Object>> net = new ArrayList<HashMap<String, Object>>();
      for (ConnectionPoint vduCp : vdu.getConnectionPoints()) {
        // create the port resource
        HeatResource port = new HeatResource();
        port.setType("OS::Neutron::Port");
        String cpQualifiedName =
            vnfd.getName() + "." + vdu.getId() + "." + vduCp.getId() + "." + vnfd.getInstanceUuid();
        port.setName(cpQualifiedName);
        port.putProperty("name", cpQualifiedName);
        HashMap<String, Object> netMap = new HashMap<String, Object>();
        Logger.debug("Mapping CP Type to the relevant network");

        String netId = null;
        // Already exist network

        if (netId != null) {
          if (resources.contains(netId)) {
            netMap.put("get_resource", netId);
            port.putProperty("network", netMap);
          } else {
            port.putProperty("network", netId);
          }
          if (netId.equals(tenantExtNet)) {
            publicPortNames.remove(cpQualifiedName);
          }
        } else {
          port.putProperty("network", netMap);
        }


        model.addResource(port);

        // add the port to the server
        HashMap<String, Object> n1 = new HashMap<String, Object>();
        HashMap<String, Object> portMap = new HashMap<String, Object>();
        portMap.put("get_resource", cpQualifiedName);
        n1.put("port", portMap);
        net.add(n1);
      }
      server.putProperty("networks", net);
      resourceGroup.putProperty("resource_def", server);
      model.addResource(resourceGroup);
    }

    for (String portName : publicPortNames) {
      // allocate floating IP
      HeatResource floatingIp = new HeatResource();
      floatingIp.setType("OS::Neutron::FloatingIP");
      floatingIp.setName("floating." + portName);


      floatingIp.putProperty("floating_network_id", tenantExtNet);

      HashMap<String, Object> floatMapPort = new HashMap<String, Object>();
      floatMapPort.put("get_resource", portName);
      floatingIp.putProperty("port_id", floatMapPort);
      model.addResource(floatingIp);
    }
    model.prepare();
    return model;
  }

  private HeatModel translateNetwork(String instanceId, ArrayList<VirtualLink> virtualLinks) throws Exception {
    // TODO This values should be per User, now they are per VIM. This should be re-designed once
    // user management is in place.
    JSONTokener tokener = new JSONTokener(this.getVimConfig().getConfiguration());
    JSONObject object = (JSONObject) tokener.nextValue();
    // String tenant = object.getString("tenant");
    // String tenantExtNet = object.getString("tenant_ext_net");
    String tenantExtRouter = object.getString("tenant_ext_router");
    // END COMMENT

    HeatModel model = new HeatModel();

    for (VirtualLink link : virtualLinks) {

      HeatResource network = new HeatResource();
      network.setType("OS::Neutron::Net");
      network.setName(link.getId());
      network.putProperty("name", link.getId());


      model.addResource(network);
      HeatResource subnet = new HeatResource();
      subnet.setType("OS::Neutron::Subnet");
      subnet.setName("subnet." + link.getId());
      subnet.putProperty("name", "subnet." + link.getId());
      {
        subnet.putProperty("subnetpool", myPool);
      }
      {
        subnet.putProperty("enable_dhcp", link.isDhcp());
      }
      String[] dnsArray = {"8.8.8.8"};
      subnet.putProperty("dns_nameservers", dnsArray);
      HashMap<String, Object> netMap = new HashMap<String, Object>();
      netMap.put("get_resource", link.getId());
      subnet.putProperty("network", netMap);
      model.addResource(subnet);

      if (link.isAccess()) {
        // internal router interface for network
        HeatResource routerInterface = new HeatResource();
        routerInterface.setType("OS::Neutron::RouterInterface");
        routerInterface.setName("routerInterface." + link.getId());
        HashMap<String, Object> subnetMapInt = new HashMap<String, Object>();
        subnetMapInt.put("get_resource", "subnet." + link.getId());
        routerInterface.putProperty("subnet", subnetMapInt);
        routerInterface.putProperty("router", tenantExtRouter);
        model.addResource(routerInterface);
      }
    }

    model.prepare();

    Logger.debug("Created " + model.getResources().size() + " resources.");

    return model;
  }

  private void addSpAddressCloudConfigObject(VnfDescriptor vnfd, String instanceUuid,
                                             HeatModel model) {


    String sonataSpAddress = (String)AdaptorCore.getInstance().getSystemParameter("sonata_sp_address");

    HashMap<String, Object> fileToWrite = new HashMap<String,Object>();
    fileToWrite.put("path", "/etc/sonata_sp_address.conf");
    fileToWrite.put("content", "SP_ADDRESS="+sonataSpAddress+"\n");

    ArrayList<HashMap<String, Object>> filesToWrite = new ArrayList<HashMap<String, Object>>();
    filesToWrite.add(fileToWrite);

    HashMap<String, Object> spAddressCloudConfigMap = new HashMap<String, Object>();
    spAddressCloudConfigMap.put("write_files", filesToWrite);

    HeatResource spAddressCloudConfigObject = new HeatResource();
    spAddressCloudConfigObject.setType("OS::Heat::CloudConfig");
    spAddressCloudConfigObject.setName(vnfd.getName() + "_" + instanceUuid + "_spAddressCloudConfig");
    spAddressCloudConfigObject.putProperty("cloud_config", spAddressCloudConfigMap);
    model.addResource(spAddressCloudConfigObject);
  }

  @Override
  @Deprecated
  public boolean deployService(ServiceDeployPayload data, String callSid) {
    return true;
  }

  @Override
  public ResourceUtilisation getResourceUtilisation() {
    ResourceUtilisation output = null;
    return output;
  }

  @Override
  public boolean isImageStored(VnfImage image, String callSid) {
    return true;
  }

  @Deprecated
  public boolean prepareService(String instanceId, ArrayList<VirtualLink> virtualLinks) throws Exception {
    return true;
  }

  @Override
  public void scaleFunction(FunctionScalePayload data, String sid) {

  }

  @Override
  public void uploadImage(VnfImage image) throws IOException {

  }

}
