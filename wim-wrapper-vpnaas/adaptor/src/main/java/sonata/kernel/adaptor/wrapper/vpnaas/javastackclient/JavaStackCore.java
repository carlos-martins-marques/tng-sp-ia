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
 * @author Adel Zaalouk (Ph.D.), NEC
 * 
 * @author Dario Valocchi (Ph.D.), UCL
 */

package sonata.kernel.adaptor.wrapper.vpnaas.javastackclient;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sun.xml.internal.xsom.impl.scd.Iterators;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import sonata.kernel.adaptor.wrapper.vpnaas.javastackclient.models.authentication.AuthenticationData;
import sonata.kernel.adaptor.wrapper.vpnaas.javastackclient.models.authenticationv3.AuthenticationDataV3;
import sonata.kernel.adaptor.wrapper.vpnaas.javastackclient.models.authenticationv3.CatalogItem;
import sonata.kernel.adaptor.wrapper.vpnaas.javastackclient.models.authenticationv3.EndpointItem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;


public class JavaStackCore {

  public enum Constants {
    AUTH_PORT("5000"), AUTH_URI_V2("/v2.0/tokens"), AUTH_URI_V3(
        "/v3/auth/tokens"), AUTHTOKEN_HEADER("X-AUTH-TOKEN");

    private final String constantValue;

    Constants(String constantValue) {
      this.constantValue = constantValue;
    }

    @Override
    public String toString() {
      return this.constantValue;
    }
  }
  private static class Compute {
    static String PORT;
    static String VERSION;

    public static String getPORT() {
      return PORT;
    }

    public static String getVERSION() {
      return VERSION;
    }

    public static void setPORT(String PORT) {
      Compute.PORT = PORT;
    }

    public static void setVERSION(String VERSION) {
      Compute.VERSION = VERSION;
    }
  }
  private static class Identity {
    static String PORT;
    static String VERSION;

    public static String getPORT() {
      return PORT;
    }

    public static String getVERSION() {
      return VERSION;
    }

    public static void setPORT(String PORT) {
      Identity.PORT = PORT;
    }

    public static void setVERSION(String VERSION) {
      Identity.VERSION = VERSION;
    }
  }
  private static class Image {
    static String PORT;
    static String VERSION;

    public static String getPORT() {
      return PORT;
    }

    public static String getVERSION() {
      return VERSION;
    }

    public static void setPORT(String PORT) {
      Image.PORT = PORT;
    }

    public static void setVERSION(String VERSION) {
      Image.VERSION = VERSION;
    }
  }
  private static class Orchestration {
    static String PORT;
    static String VERSION;

    public static String getPORT() {
      return PORT;
    }

    public static String getVERSION() {
      return VERSION;
    }

    public static void setPORT(String PORT) {
      Orchestration.PORT = PORT;
    }

    public static void setVERSION(String VERSION) {
      Orchestration.VERSION = VERSION;
    }
  }

  private static class Network {
    static String PORT;
    static String VERSION;

    public static String getPORT() {
      return PORT;
    }

    public static String getVERSION() {
      return VERSION;
    }

    public static void setPORT(String PORT) {
      Network.PORT = PORT;
    }

    public static void setVERSION(String VERSION) {
      Network.VERSION = VERSION;
    }
  }

  // private static class SingeltonJavaStackCoreHelper {
  // private static final JavaStackCore _javaStackCore = new JavaStackCore();
  // }

  private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(JavaStackCore.class);

  public static JavaStackCore getJavaStackCore() {
    return new JavaStackCore();
  }

  private String endpoint;

  // private String image_id;
  private boolean isAuthenticated = false;


  private ObjectMapper mapper;

  private String password;

  private String projectId;

  private String projectName;



  private String token_id;

  private String username;
  
  private String domain;

  private JavaStackCore() {}

  /**
   * Authenticate Client (v2 of the Identity API for backward compatability)
   *
   * @param
   * @throws IOException
   */
  public void authenticateClientV2() throws IOException {

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost post;
    HttpResponse response = null;

    if (!isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Constants.AUTH_PORT.toString());
      buildUrl.append(Constants.AUTH_URI_V2.toString());

      post = new HttpPost(buildUrl.toString());
      String body = String.format(
          "{\"auth\": {\"tenantName\": \"%s\", \"passwordCredentials\": {\"username\": \"%s\", \"password\": \"%s\"}}}",
          this.projectName, this.username, this.password);

      Logger.debug("[JavaStack] Authenticating client...");
      Logger.debug("[JavaStack] " + post.toString());
      Logger.debug("[JavaStack] " + body);

      post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
      response = httpClient.execute(post);

      Logger.debug("[JavaStack] Authentication response:");
      Logger.debug(response.toString());

      mapper = new ObjectMapper();

      String httpResponseString = JavaStackUtils.convertHttpResponseToString(response);
      Logger.debug("[JavaStack] Authentication response body:");
      Logger.debug(httpResponseString);
      AuthenticationData auth = mapper.readValue(httpResponseString, AuthenticationData.class);

      this.token_id = auth.getAccess().getToken().getId();
      this.projectId = auth.getAccess().getToken().getTenant().getId();
      this.isAuthenticated = true;

    } else {
      Logger.info("You are already authenticated");
    }
  }

  /**
   * Authenticate Client (v3 of the Identity API) and fetches information about endpoints e.g.,
   * ports and version
   *
   * @throws IOException
   */
  public synchronized void authenticateClientV3(String customIdentityPort) throws IOException {
    RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000).build();
    HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    HttpPost post;
    HttpResponse response = null;
    HashMap<String, String> endpoint_details = new HashMap<>();
    String identityPort =
        (customIdentityPort == null) ? Constants.AUTH_PORT.toString() : customIdentityPort;
    if (!isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(identityPort);
      buildUrl.append(Constants.AUTH_URI_V3.toString());

      post = new HttpPost(buildUrl.toString());
      String body = String.format(
          "{\n" + "    \"auth\": {\n"
              + "        \"identity\": {\n"
              + "            \"methods\": [\n"
              + "                \"password\"\n"
              + "            ],\n"
              + "            \"password\": {\n"
              + "                \"user\": {\n"
              + "                    \"name\": \"%s\",\n"
              + "                    \"domain\": {\n"
              + "                        \"name\": \"%s\"\n"
              + "                    },\n"
              + "                    \"password\": \"%s\"\n"
              + "                }\n"
              + "            }\n"
              + "        },\n"
              + "        \"scope\": {\n"
              + "            \"project\": {\n"
              + "                \"domain\": {\n"
              + "                    \"name\": \"%s\"\n"
              + "                },\n"
              + "                \"name\": \"%s\"\n"
              + "            }\n"
              + "        }\n"
              + "    }\n"
              + "}",
          this.username, this.domain, this.password, this.domain, this.projectName);

      post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));

      Logger.debug("[JavaStack] Authenticating client...");
      Logger.debug("[JavaStack] " + post.toString());
      // Logger.debug("[JavaStack] " + body);

      response = httpClient.execute(post);

      Logger.debug("[JavaStack] Authentication response:");
      Logger.debug(response.toString());

      if (response.containsHeader("X-Subject-Token")) {
        this.token_id = response.getFirstHeader("X-Subject-Token").getValue();
      }

      mapper = new ObjectMapper();
      String httpResponseString = JavaStackUtils.convertHttpResponseToString(response);
      Logger.debug("[JavaStack] Authentication response body:");
      Logger.debug(httpResponseString);
      AuthenticationDataV3 auth = mapper.readValue(httpResponseString, AuthenticationDataV3.class);

      ArrayList<CatalogItem> catalogItems = auth.getToken().getCatalog();
      for (CatalogItem catalogItem : catalogItems) {
        String type = catalogItem.getType();
        String id = catalogItem.getId();

        for (EndpointItem endpointItem : catalogItem.getEndpoints()) {
          if (endpointItem.getIface().equals("public")) {
            String[] path_port = endpointItem.getUrl().split(":");;
            String[] path = path_port[2].split("/");
            String version = "";
	    String port;

            switch (type) {
              case "identity":
                port = path[0];
		if (path.length > 1)
                version = path[1];
                Identity.setPORT(port);
                Identity.setVERSION(version);
                break;

              case "orchestration":
                port = path[0];
		if (path.length > 1)
                version = path[1];
                Orchestration.setPORT(port);
                Orchestration.setVERSION(version);
                break;

              case "image":
                port = path[0];
                version = "v2";
                Image.setPORT(port);
                Image.setVERSION(version);
                break;

              case "compute":
                port = path[0];
		if (path.length > 1)
                version = path[1];
                Compute.setPORT(port);
                Compute.setVERSION(version);
                break;

              case "network":
                port = path[0];
                version = "v2.0";
                Network.setPORT(port);
                Network.setVERSION(version);
                break;

              case "cloudformation":
                break;

              default:
                Logger.warn("[JavaStack]Unhandled endpoint type: " + type + ". skipping");
            }
          }
        }
      }

      if (auth.getToken().getProject() == null) {
        throw new IOException(
            "Authentication response doesn't contain Project ID. SONATA VIM-Adaptor can't work with this Keystone configuration.");
      }
      this.projectId = auth.getToken().getProject().getId();
      Logger.debug("[JavaStack] ProjectId set to " + projectId);
      this.isAuthenticated = true;

    } else {
      Logger.info("You are already authenticated");
    }
  }

  /**
   * GLANCE Method to create an Image
   *
   * @param template
   * @param containerFormat
   * @param diskFormat
   * @param name
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse createImage(String template, String containerFormat,
      String diskFormat, String name) throws IOException {
    HttpPost createImage;
    HttpClient httpClient = HttpClientBuilder.create().build();

    if (this.isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Image.getPORT());
      buildUrl.append(String.format("/%s/images", Image.getVERSION()));

      createImage = new HttpPost(buildUrl.toString());
      String requestBody =
          String.format("{ \"container_format\": \"bare\"," + "\"disk_format\": \"raw\","
              + " \"name\": \"%s\"" + ",\"visibility\":\"private\"" + "}", name);

      createImage.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
      createImage.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
    return httpClient.execute(createImage);
  }

  /**
   * HEAT method to create a stack using a template
   *
   * @param template
   * @param stackName
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse createStack(String template, String stackName)
      throws IOException {

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();
    HttpPost createStack = null;
    HttpResponse response = null;

    String jsonTemplate = JavaStackUtils.convertYamlToJson(template);
    JSONObject modifiedObject = new JSONObject();
    modifiedObject.put("stack_name", stackName);
    modifiedObject.put("template", new JSONObject(jsonTemplate));

    if (this.isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Orchestration.getPORT());
      buildUrl.append(String.format("/%s/%s/stacks", Orchestration.getVERSION(), this.projectId));

      Logger.debug(buildUrl.toString());
      createStack = new HttpPost(buildUrl.toString());
      createStack
          .setEntity(new StringEntity(modifiedObject.toString(), ContentType.APPLICATION_JSON));
      // Logger.debug(this.token_id);
      createStack.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("Request: " + createStack.toString());
      // Logger.debug("Request body: " + modifiedObject.toString());

      response = httpClient.execute(createStack);
      int statusCode = response.getStatusLine().getStatusCode();
      String responsePhrase = response.getStatusLine().getReasonPhrase();

      Logger.debug("Response: " + response.toString());
      // Logger.debug("Response body:");
      //
      // if (statusCode != 201) {
      // BufferedReader in =
      // new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      // String line = null;
      //
      // while ((line = in.readLine()) != null)
      // Logger.debug(line);
      // }


      return (statusCode == 201)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode,
              responsePhrase + ". Create Failed with Status: " + statusCode), null);
    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
  }

  /**
   * HEAT method to delete a stack
   *
   * @param stackName
   * @param stackId
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse deleteStack(String stackName, String stackId)
      throws IOException {

    HttpDelete deleteStack;
    HttpClient httpClient = HttpClientBuilder.create().build();
    if (this.isAuthenticated) {

      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Orchestration.getPORT());
      buildUrl.append(String.format("/%s/%s/stacks/%s/%s", Orchestration.getVERSION(), projectId,
          stackName, stackId));
      deleteStack = new HttpDelete(buildUrl.toString());
      deleteStack.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      return httpClient.execute(deleteStack);
    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
  }

  /**
   * HEAT Method to find a stack
   *
   * @param stackIdentity
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse findStack(String stackIdentity) throws IOException {
    HttpGet findStack;
    HttpClient httpClient = HttpClientBuilder.create().build();

    if (this.isAuthenticated) {

      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Orchestration.getPORT());
      buildUrl.append(String.format("/%s/%s/stacks/%s", Orchestration.getVERSION(), this.projectId,
          stackIdentity));

      // Logger.debug("URL: " + buildUrl);
      // Logger.debug("Token: " + this.token_id);

      findStack = new HttpGet(buildUrl.toString());
      findStack.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      return httpClient.execute(findStack);

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }

  }

  public String getEndpoint() {
    return endpoint;
  }

  public String getPassword() {
    return this.password;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getProjectName() {
    return projectName;
  }

  /**
   * HEAT method to get the HOT template
   *
   * @param stackName
   * @param stackId
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  public synchronized HttpResponse getStackTemplate(String stackName, String stackId)
      throws IOException, URISyntaxException {

    HttpResponseFactory factory = new DefaultHttpResponseFactory();
    HttpClient httpclient = HttpClientBuilder.create().build();
    HttpGet getStackTemplate = null;
    HttpResponse response = null;

    if (isAuthenticated) {

      URIBuilder builder = new URIBuilder();
      String path = String.format("/%s/%s/stacks/%s/%s/template", Orchestration.getVERSION(),
          this.projectId, stackName, stackId);

      builder.setScheme("http").setHost(endpoint).setPort(Integer.parseInt(Orchestration.getPORT()))
          .setPath(path);

      URI uri = builder.build();

      getStackTemplate = new HttpGet(uri);
      getStackTemplate.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("Request: " + getStackTemplate.toString());

      response = httpclient.execute(getStackTemplate);
      int status_code = response.getStatusLine().getStatusCode();

      Logger.debug("Response: " + response.toString());

      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
              "Get Template Failed with Status: " + status_code), null);

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }

  }

  public String getTokenId() {
    return this.token_id;
  }

  public String getUsername() {
    return this.username;
  }

  public String getDomain() {
    return this.domain;
  }

  /**
   * NOVA method to list compute flavors
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse listComputeFlavors() throws IOException {
    HttpGet getFlavors = null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Compute.getPORT());
      buildUrl.append(String.format("/%s/%s/flavors/detail", Compute.getVERSION(), this.projectId));

      // Logger.debug("[JavaStack] Authenticating client...");
      getFlavors = new HttpGet(buildUrl.toString());
      getFlavors.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getFlavors.toString());

      response = httpClient.execute(getFlavors);
      Logger.debug("[JavaStack] GET Flavor gresponse:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
              "List Flavors  Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * NOVA method to list compute limits
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse listComputeLimits() throws IOException {
    HttpGet getLimits = null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Compute.getPORT());
      buildUrl.append(String.format("/%s/%s/limits", Compute.getVERSION(), this.projectId));

      getLimits = new HttpGet(buildUrl.toString());
      getLimits.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("[JavaStack] Getting limit request:");
      Logger.debug(getLimits.toString());

      response = httpClient.execute(getLimits);

      Logger.debug("[JavaStack] Getting limit request:");
      Logger.debug(getLimits.toString());

      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
              "List Limits Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * GLANCE method to list images
   *
   * @return
   * @throws IOException
   */
  public HttpResponse listImages() throws IOException {

    Logger.debug("RESTful request to glance image list");
    HttpGet listImages = null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {

      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Image.getPORT());
      buildUrl.append(String.format("/%s/images", Image.getVERSION()));
      buildUrl.append("?limit=100");

      listImages = new HttpGet(buildUrl.toString());
      listImages.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("URL request:");
      Logger.debug(buildUrl.toString());

      Logger.debug("HTTP request:");
      Logger.debug(listImages.toString());

      response = httpClient.execute(listImages);
      Logger.debug("HTTP response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
              "Listing Images Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * HEAT method to list stack resources
   *
   * @param stackName
   * @param stackId
   * @param resources
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  public synchronized HttpResponse listStackResources(String stackName, String stackId,
      ArrayList<String> resources) throws IOException, URISyntaxException {
    HttpResponseFactory factory = new DefaultHttpResponseFactory();
    HttpClient httpclient = HttpClientBuilder.create().build();
    HttpGet listResources = null;
    HttpResponse response = null;

    if (isAuthenticated) {
      URIBuilder builder = new URIBuilder();
      String path = String.format("/%s/%s/stacks/%s/%s/resources", Orchestration.getVERSION(),
          this.projectId, stackName, stackId);

      builder.setScheme("http").setHost(endpoint).setPort(Integer.parseInt(Orchestration.getPORT()))
          .setPath(path);

      URI uri = builder.build();

      listResources = new HttpGet(uri);
      listResources.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);


      response = httpclient.execute(listResources);
      int status_code = response.getStatusLine().getStatusCode();

      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
              "List Failed with Status: " + status_code), null);

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
  }

  /**
   * HEAT Method to list stacks
   *
   * @param endpoint
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse listStacks(String endpoint) throws IOException {


    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();
    HttpResponse response = null;
    HttpGet listStacks = null;

    if (this.isAuthenticated) {

      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Orchestration.getPORT());
      buildUrl.append(String.format("/%s/%s/stacks", Orchestration.getVERSION(), this.projectId));

      Logger.info(buildUrl.toString());
      Logger.info(this.token_id);

      listStacks = new HttpGet(buildUrl.toString());
      listStacks.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      response = httpClient.execute(listStacks);
      int status_code = response.getStatusLine().getStatusCode();

      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
              "List Failed with Status: " + status_code), null);

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }

  }

  public void setAuthenticated(boolean isAuthenticated) {
    this.isAuthenticated = isAuthenticated;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * HEAT Method to show resource details
   *
   * @param stackName
   * @param stackId
   * @param resourceName
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  public synchronized HttpResponse showResourceData(String stackName, String stackId,
      String resourceName) throws IOException, URISyntaxException {
    HttpResponseFactory factory = new DefaultHttpResponseFactory();
    HttpClient httpclient = HttpClientBuilder.create().build();
    HttpGet showResourceData = null;
    HttpResponse response = null;

    if (isAuthenticated) {
      URIBuilder builder = new URIBuilder();
      String path = String.format("/%s/%s/stacks/%s/%s/resources/%s", Orchestration.getVERSION(),
          this.projectId, stackName, stackId, resourceName);

      builder.setScheme("http").setHost(endpoint).setPort(Integer.parseInt(Orchestration.getPORT()))
          .setPath(path);

      URI uri = builder.build();

      showResourceData = new HttpGet(uri);
      showResourceData.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      response = httpclient.execute(showResourceData);
      int status_code = response.getStatusLine().getStatusCode();

      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
              "List Failed with Status: " + status_code), null);

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
  }

  /**
   * HEAT method to update a stack
   *
   * @param stackName
   * @param stackUuid
   * @param template
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse updateStack(String stackName, String stackUuid, String template)
      throws IOException {

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();
    HttpPatch updateStack = null;
    HttpResponse response = null;

    String jsonTemplate = JavaStackUtils.convertYamlToJson(template);
    JSONObject modifiedObject = new JSONObject();
    modifiedObject.put("stack_name", stackName);
    modifiedObject.put("template", new JSONObject(jsonTemplate));

    if (this.isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Orchestration.getPORT());
      buildUrl.append(String.format("/%s/%s/stacks/%s/%s", Orchestration.getVERSION(), projectId,
          stackName, stackUuid));

      // Logger.debug(buildUrl.toString());
      updateStack = new HttpPatch(buildUrl.toString());
      updateStack
          .setEntity(new StringEntity(modifiedObject.toString(), ContentType.APPLICATION_JSON));
      // Logger.debug(this.token_id);
      updateStack.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("Request: " + updateStack.toString());
      Logger.debug("Request body: " + modifiedObject.toString());

      response = httpClient.execute(updateStack);
      int statusCode = response.getStatusLine().getStatusCode();
      String responsePhrase = response.getStatusLine().getReasonPhrase();

      Logger.debug("Response: " + response.toString());
      // Logger.debug("Response body:");
      //
      //
      // if (statusCode != 202) {
      // BufferedReader in =
      // new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      // String line = null;
      //
      // while ((line = in.readLine()) != null)
      // Logger.debug(line);
      // }

      return (statusCode == 202)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode,
              responsePhrase + ". Create Failed with Status: " + statusCode), null);
    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
  }

  /**
   * GLANCE method to upload an Image
   *
   * @param endpoint
   * @param imageId
   * @param binaryImageLocalFilePath
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse uploadBinaryImageData(String endpoint, String imageId,
      String binaryImageLocalFilePath) throws IOException {

    HttpPut uploadImage;
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response;
    if (this.isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Image.getPORT());
      buildUrl.append(String.format("/%s/images/%s/file", Image.getVERSION(), imageId));

      uploadImage = new HttpPut(buildUrl.toString());
      uploadImage.setHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      uploadImage.setHeader("Content-Type", "application/octet-stream");
      uploadImage.setEntity(new FileEntity(new File(binaryImageLocalFilePath)));
      response = httpClient.execute(uploadImage);
      Logger.debug("[JavaStackCore] Response of binary Image upload");
      Logger.debug(response.toString());
    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }

    return response;
  }

  /**
   * NEUTRON method to list qos policies
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse listQosPolicies() throws IOException {
    HttpGet getQosPolicies= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/qos/policies?tenant_id=%s", Network.getVERSION(), this.projectId));

      // Logger.debug("[JavaStack] Authenticating client...");
      getQosPolicies = new HttpGet(buildUrl.toString());
      getQosPolicies.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getQosPolicies.toString());

      response = httpClient.execute(getQosPolicies);
      Logger.debug("[JavaStack] GET Qos Policies response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
              ? response
              : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
              "List Qos Policies  Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * NEUTRON method to list external networks
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse listNetworks() throws IOException {
    HttpGet getNetworks= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/networks?router:external=true", Network.getVERSION()));

      // Logger.debug("[JavaStack] Authenticating client...");
      getNetworks = new HttpGet(buildUrl.toString());
      getNetworks.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getNetworks.toString());

      response = httpClient.execute(getNetworks);
      Logger.debug("[JavaStack] GET Networks response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
          "List Networks  Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * NEUTRON method to list routers
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse listRouters() throws IOException {
    HttpGet getRouters= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/routers", Network.getVERSION()));

      // Logger.debug("[JavaStack] Authenticating client...");
      getRouters = new HttpGet(buildUrl.toString());
      getRouters.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getRouters.toString());

      response = httpClient.execute(getRouters);
      Logger.debug("[JavaStack] GET Routers response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
          "List Routers  Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * NEUTRON method to get router by given routerId
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse getRouter(String routerId) throws IOException {
    HttpGet getRouters= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/routers/%s", Network.getVERSION(), routerId));

      // Logger.debug("[JavaStack] Authenticating client...");
      getRouters = new HttpGet(buildUrl.toString());
      getRouters.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getRouters.toString());

      response = httpClient.execute(getRouters);
      Logger.debug("[JavaStack] GET Routers response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
          "List Routers  Failed with Status: " + status_code), null);
    }
    return response;
  }


  /**
   * NEUTRON method to list Subnet Pools
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse listSubnetPools() throws IOException {
    HttpGet getSubnetPools= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/subnetpools", Network.getVERSION()));

      // Logger.debug("[JavaStack] Authenticating client...");
      getSubnetPools = new HttpGet(buildUrl.toString());
      getSubnetPools.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getSubnetPools.toString());

      response = httpClient.execute(getSubnetPools);
      Logger.debug("[JavaStack] GET Subnet Pools response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
          "List Subnet Pools Failed with Status: " + status_code), null);
    }
    return response;
  }


  /**
   * NEUTRON Method to create an Subnet Pool
   *
   * @param name
   * @param prefixes
   * @param defaultPrefixlen
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse createSubnetPool(String name, ArrayList<String> prefixes, String defaultPrefixlen) throws IOException {
    HttpPost createSubnetPool;
    HttpClient httpClient = HttpClientBuilder.create().build();

    if (this.isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/subnetpools", Network.getVERSION()));

      createSubnetPool = new HttpPost(buildUrl.toString());

      String prefixes_json = "";
      for (String prefix : prefixes) {
        if (!prefixes_json.equals("")) {
          prefixes_json = prefixes_json + ",";
        }
        prefixes_json = prefixes_json + "\""+prefix+"\"";
      }

      String requestBody =
          String.format("{ \"subnetpool\": { \"name\": \"%s\",\"prefixes\": [%s], \"default_prefixlen\": %s }}", name, prefixes_json, defaultPrefixlen);

      createSubnetPool.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
      createSubnetPool.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("[JavaStack] body " + requestBody);
      Logger.debug("[JavaStack] " + createSubnetPool.toString());

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
    return httpClient.execute(createSubnetPool);
  }

  /**
   * NEUTRON Method to update an Subnet Pool
   *
   * @param id
   * @param prefixes
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse updateSubnetPool(String id, ArrayList<String> prefixes) throws IOException {
    HttpPut createSubnetPool;
    HttpClient httpClient = HttpClientBuilder.create().build();

    if (this.isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/subnetpools/%s", Network.getVERSION(),id));

      createSubnetPool = new HttpPut(buildUrl.toString());

      String prefixes_json = "";
      for (String prefix : prefixes) {
        if (!prefixes_json.equals("")) {
          prefixes_json = prefixes_json + ",";
        }
        prefixes_json = prefixes_json + "\""+prefix+"\"";
      }

      String requestBody =
          String.format("{ \"subnetpool\": { \"prefixes\": [%s] }}", prefixes_json);

      createSubnetPool.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
      createSubnetPool.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("[JavaStack] body " + requestBody);
      Logger.debug("[JavaStack] " + createSubnetPool.toString());

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
    return httpClient.execute(createSubnetPool);
  }

  /**
   * NEUTRON method to get floating IP details by given FIP
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse getFloatingIp(String fIp) throws IOException {
    HttpGet getFloatingIp= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/floatingips?floating_ip_address=%s", Network.getVERSION(), fIp));

      // Logger.debug("[JavaStack] Authenticating client...");
      getFloatingIp = new HttpGet(buildUrl.toString());
      getFloatingIp.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getFloatingIp.toString());

      response = httpClient.execute(getFloatingIp);
      Logger.debug("[JavaStack] GET Floating IP response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
          "Get Floating IP  Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * NEUTRON method to get subnet details by given network id
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse getSubnet(String network_id) throws IOException {
    HttpGet getNetworkId= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/subnets?network_id=%s", Network.getVERSION(), network_id));

      // Logger.debug("[JavaStack] Authenticating client...");
      getNetworkId = new HttpGet(buildUrl.toString());
      getNetworkId.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getNetworkId.toString());

      response = httpClient.execute(getNetworkId);
      Logger.debug("[JavaStack] GET Subnet response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
          "Get Subnet  Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * NEUTRON method to get Ike Policy details by given name
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse getIkePolicy(String name) throws IOException {
    HttpGet getIkePolicy= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/vpn/ikepolicies?name=%s", Network.getVERSION(), name));

      // Logger.debug("[JavaStack] Authenticating client...");
      getIkePolicy = new HttpGet(buildUrl.toString());
      getIkePolicy.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getIkePolicy.toString());

      response = httpClient.execute(getIkePolicy);
      Logger.debug("[JavaStack] GET Ike Policy response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
          "Get Ike Policy Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * NEUTRON Method to create an Ike Policy
   *
   * @param name
   * @param lifetime in seconds
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse createIkePolicy(String name, String lifetime) throws IOException {
    HttpPost createIkePolicy;
    HttpClient httpClient = HttpClientBuilder.create().build();

    if (this.isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/vpn/ikepolicies", Network.getVERSION()));

      createIkePolicy = new HttpPost(buildUrl.toString());

      String requestBody =
          String.format("{ \"ikepolicy\": { \"name\": \"%s\",\"lifetime\": {\"units\": \"seconds\", \"value\": %s} }}", name, lifetime);

      createIkePolicy.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
      createIkePolicy.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("[JavaStack] body " + requestBody);
      Logger.debug("[JavaStack] " + createIkePolicy.toString());

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
    return httpClient.execute(createIkePolicy);
  }

  /**
   * NEUTRON method to delete an Ike Policy
   *
   * @param id
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse deleteIkePolicy(String id)
      throws IOException {

    HttpDelete deleteIkePolicy;
    HttpClient httpClient = HttpClientBuilder.create().build();
    if (this.isAuthenticated) {

      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Orchestration.getPORT());
      buildUrl.append(String.format("/%s/vpn/ikepolicies/%s", Network.getVERSION(),id));

      deleteIkePolicy = new HttpDelete(buildUrl.toString());
      deleteIkePolicy.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      return httpClient.execute(deleteIkePolicy);
    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
  }

  /**
   * NEUTRON method to get Ipsec Policy details by given name
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse getIpsecPolicy(String name) throws IOException {
    HttpGet getIpsecPolicy= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/vpn/ipsecpolicies?name=%s", Network.getVERSION(), name));

      // Logger.debug("[JavaStack] Authenticating client...");
      getIpsecPolicy = new HttpGet(buildUrl.toString());
      getIpsecPolicy.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getIpsecPolicy.toString());

      response = httpClient.execute(getIpsecPolicy);
      Logger.debug("[JavaStack] GET Ipsec Policy response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
          "Get Ipsec Policy Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * NEUTRON Method to create an Ipsec Policy
   *
   * @param name
   * @param lifetime in seconds
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse createIpsecPolicy(String name, String lifetime) throws IOException {
    HttpPost createIpsecPolicy;
    HttpClient httpClient = HttpClientBuilder.create().build();

    if (this.isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/vpn/ipsecpolicies", Network.getVERSION()));

      createIpsecPolicy = new HttpPost(buildUrl.toString());

      String requestBody =
          String.format("{ \"ipsecpolicy\": { \"name\": \"%s\",\"lifetime\": {\"units\": \"seconds\", \"value\": %s} }}", name, lifetime);

      createIpsecPolicy.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
      createIpsecPolicy.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("[JavaStack] body " + requestBody);
      Logger.debug("[JavaStack] " + createIpsecPolicy.toString());

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
    return httpClient.execute(createIpsecPolicy);
  }

  /**
   * NEUTRON method to delete an Ipsec Policy
   *
   * @param id
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse deleteIpsecPolicy(String id)
      throws IOException {

    HttpDelete deleteIpsecPolicy;
    HttpClient httpClient = HttpClientBuilder.create().build();
    if (this.isAuthenticated) {

      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Orchestration.getPORT());
      buildUrl.append(String.format("/%s/vpn/ipsecpolicies/%s", Network.getVERSION(),id));

      deleteIpsecPolicy = new HttpDelete(buildUrl.toString());
      deleteIpsecPolicy.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      return httpClient.execute(deleteIpsecPolicy);
    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
  }

  /**
   * NEUTRON method to get Vpn Service details by given name
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse getVpnService(String name) throws IOException {
    HttpGet getVpnServices= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/vpn/vpnservices?name=%s", Network.getVERSION(), name));

      // Logger.debug("[JavaStack] Authenticating client...");
      getVpnServices = new HttpGet(buildUrl.toString());
      getVpnServices.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getVpnServices.toString());

      response = httpClient.execute(getVpnServices);
      Logger.debug("[JavaStack] GET Vpn Service response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
          "Get Vpn Service Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * NEUTRON Method to create an Vpn Service
   *
   * @param name
   * @param routerId
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse createVpnService(String name, String routerId) throws IOException {
    HttpPost createVpnService;
    HttpClient httpClient = HttpClientBuilder.create().build();

    if (this.isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/vpn/vpnservices", Network.getVERSION()));

      createVpnService = new HttpPost(buildUrl.toString());

      String requestBody =
          String.format("{ \"vpnservice\": { \"name\": \"%s\",\"router_id\": \"%s\" }}", name, routerId);

      createVpnService.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
      createVpnService.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("[JavaStack] body " + requestBody);
      Logger.debug("[JavaStack] " + createVpnService.toString());

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
    return httpClient.execute(createVpnService);
  }

  /**
   * NEUTRON method to delete an Vpn Services
   *
   * @param id
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse deleteVpnService(String id)
      throws IOException {

    HttpDelete deleteVpnService;
    HttpClient httpClient = HttpClientBuilder.create().build();
    if (this.isAuthenticated) {

      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Orchestration.getPORT());
      buildUrl.append(String.format("/%s/vpn/vpnservices/%s", Network.getVERSION(),id));

      deleteVpnService = new HttpDelete(buildUrl.toString());
      deleteVpnService.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      return httpClient.execute(deleteVpnService);
    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
  }


  /**
   * NEUTRON method to get Endpoint Group details by given name
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse getEndpointGroup(String name) throws IOException {
    HttpGet getEndpointGroup= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/vpn/endpoint-groups?name=%s", Network.getVERSION(), name));

      // Logger.debug("[JavaStack] Authenticating client...");
      getEndpointGroup = new HttpGet(buildUrl.toString());
      getEndpointGroup.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getEndpointGroup.toString());

      response = httpClient.execute(getEndpointGroup);
      Logger.debug("[JavaStack] GET Endpoint Group response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
          "Get Endpoint Group Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * NEUTRON Method to create an Endpoint Group
   *
   * @param name
   * @param type
   * @param endpoints
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse createEndpointGroup(String name, String type, ArrayList<String> endpoints) throws IOException {
    HttpPost createEndpointGroup;
    HttpClient httpClient = HttpClientBuilder.create().build();

    if (this.isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/vpn/endpoint-groups", Network.getVERSION()));

      createEndpointGroup = new HttpPost(buildUrl.toString());

      String endpoints_json = "";
      for (String endpoint : endpoints) {
        if (!endpoints_json.equals("")) {
          endpoints_json = endpoints_json + ",";
        }
        endpoints_json = endpoints_json + "\""+endpoint+"\"";
      }

      String requestBody =
          String.format("{ \"endpoint_group\": { \"name\": \"%s\",\"type\": \"%s\",  \"endpoints\": [%s] }}", name, type, endpoints_json);

      createEndpointGroup.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
      createEndpointGroup.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("[JavaStack] body " + requestBody);
      Logger.debug("[JavaStack] " + createEndpointGroup.toString());

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
    return httpClient.execute(createEndpointGroup);
  }

  /**
   * NEUTRON method to delete an Endpoint Group
   *
   * @param id
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse deleteEndpointGroup(String id)
      throws IOException {

    HttpDelete deleteEndpointGroup;
    HttpClient httpClient = HttpClientBuilder.create().build();
    if (this.isAuthenticated) {

      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Orchestration.getPORT());
      buildUrl.append(String.format("/%s/vpn/endpoint-groups/%s", Network.getVERSION(),id));

      deleteEndpointGroup = new HttpDelete(buildUrl.toString());
      deleteEndpointGroup.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      return httpClient.execute(deleteEndpointGroup);
    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
  }

  /**
   * NEUTRON method to get Ipsec Connection details by given name
   *
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse getIpsecConnection(String name) throws IOException {
    HttpGet getIpsecConnection= null;
    HttpResponse response = null;

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponseFactory factory = new DefaultHttpResponseFactory();

    if (isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/vpn/ipsec-site-connections?name=%s", Network.getVERSION(), name));

      // Logger.debug("[JavaStack] Authenticating client...");
      getIpsecConnection = new HttpGet(buildUrl.toString());
      getIpsecConnection.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
      Logger.debug("[JavaStack] " + getIpsecConnection.toString());

      response = httpClient.execute(getIpsecConnection);
      Logger.debug("[JavaStack] GET Ipsec Connection response:");
      Logger.debug(response.toString());
      int status_code = response.getStatusLine().getStatusCode();
      return (status_code == 200)
          ? response
          : factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, status_code,
          "Get Ipsec Connection Failed with Status: " + status_code), null);
    }
    return response;
  }

  /**
   * NEUTRON Method to create an Ipsec Connection
   *
   * @param name
   * @param vpnServiceId
   * @param ikePolicyId
   * @param ipsecPolicyId
   * @param peerAddress
   * @param peerId
   * @param psk
   * @param subnetEpGroupId
   * @param cidrEpGroupId
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse createIpsecConnection(String name, String vpnServiceId, String ikePolicyId,
                                          String ipsecPolicyId, String peerAddress, String peerId, String psk,
                                          String subnetEpGroupId, String cidrEpGroupId) throws IOException {
    HttpPost createIpsecConnection;
    HttpClient httpClient = HttpClientBuilder.create().build();

    if (this.isAuthenticated) {
      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Network.getPORT());
      buildUrl.append(String.format("/%s/vpn/ipsec-site-connections", Network.getVERSION()));

      createIpsecConnection = new HttpPost(buildUrl.toString());

      String requestBody =
          String.format("{ \"endpoint_group\": { \"name\": \"%s\",\"vpnservice_id\": \"%s\" }," +
              "\"ikepolicy_id\": \"%s\" },\"ipsecpolicy_id\": \"%s\" },\"peer_address\": \"%s\" }" +
              ",\"peer_id\": \"%s\" },\"psk\": \"%s\" },\"local_ep_group_id\": \"%s\" }" +
              ",\"peer_ep_group_id\": \"%s\" }}", name, vpnServiceId, ikePolicyId, ipsecPolicyId,
              peerAddress, peerId, psk, subnetEpGroupId, cidrEpGroupId);

      createIpsecConnection.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
      createIpsecConnection.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      Logger.debug("[JavaStack] body " + requestBody);
      Logger.debug("[JavaStack] " + createIpsecConnection.toString());

    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
    return httpClient.execute(createIpsecConnection);
  }

  /**
   * NEUTRON method to delete an Ipsec Connection
   *
   * @param id
   * @return
   * @throws IOException
   */
  public synchronized HttpResponse deleteIpsecConnection(String id)
      throws IOException {

    HttpDelete deleteIpsecConnection;
    HttpClient httpClient = HttpClientBuilder.create().build();
    if (this.isAuthenticated) {

      StringBuilder buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.endpoint);
      buildUrl.append(":");
      buildUrl.append(Orchestration.getPORT());
      buildUrl.append(String.format("/%s/vpn/ipsec-site-connections/%s", Network.getVERSION(),id));

      deleteIpsecConnection = new HttpDelete(buildUrl.toString());
      deleteIpsecConnection.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

      return httpClient.execute(deleteIpsecConnection);
    } else {
      throw new IOException(
          "You must Authenticate before issuing this request, please re-authenticate. ");
    }
  }

}
