/**
 * Copyright 2016 IBM Corp. All Rights Reserved.
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
 */
package net.bluemix.tutorial.vision;

import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * From the Watson Java SDK, but adapted to fix https://github.com/watson-developer-cloud/java-sdk/issues/371
 */
public class PatchedCredentialUtils {

  /** The Constant WATSON_VISUAL_RECOGNITION. */
  private static final String WATSON_VISUAL_RECOGNITION = "watson_vision_combined";

  /** The Constant APIKEY. */
  private static final String APIKEY = "api_key";

  /** The Constant CREDENTIALS. */
  private static final String CREDENTIALS = "credentials";

  /** The Constant log. */
  private static final Logger log = Logger.getLogger(PatchedCredentialUtils.class.getName());

  /** The Constant PLAN. */
  private static final String PLAN = "plan";

  /** The services. */
  private static String services;

  /**
   * Gets the <b>VCAP_SERVICES</b> environment variable and return it as a
   * {@link JsonObject}.
   * 
   * @return the VCAP_SERVICES as a {@link JsonObject}.
   */
  private static JsonObject getVCAPServices() {
    final String envServices = services != null ? services : System.getenv("VCAP_SERVICES");
    if (envServices == null)
      return null;

    JsonObject vcapServices = null;

    try {
      final JsonParser parser = new JsonParser();
      vcapServices = (JsonObject) parser.parse(envServices);
    } catch (final JsonSyntaxException e) {
      log.log(Level.INFO, "Error parsing VCAP_SERVICES", e);
    }
    return vcapServices;
  }

  /**
   * Returns the apiKey from the VCAP_SERVICES or null if doesn't exists.
   * 
   * @param serviceName
   *          the service name
   * @return the API key or null if the service cannot be found.
   */
  public static String getAPIKey(String serviceName) {
    return getAPIKey(serviceName, null);
  }

  /**
   * Returns the apiKey from the VCAP_SERVICES or null if doesn't exists. If
   * plan is specified, then only credentials for the given plan will be
   * returned.
   * 
   * @param serviceName
   *          the service name
   * @param plan
   *          the service plan: standard, free or experimental
   * @return the API key
   */
  public static String getAPIKey(String serviceName, String plan) {
    if (serviceName == null || serviceName.isEmpty())
      return null;

    final JsonObject services = getVCAPServices();
    if (services == null)
      return null;

    for (final Entry<String, JsonElement> entry : services.entrySet()) {
      final String key = entry.getKey();
      if (key.startsWith(serviceName)) {
        final JsonArray servInstances = services.getAsJsonArray(key);
        for (final JsonElement instance : servInstances) {
          final JsonObject service = instance.getAsJsonObject();
          final String instancePlan = service.get(PLAN).getAsString();
          if (plan == null || plan.equalsIgnoreCase(instancePlan)) {
            final JsonObject credentials = instance.getAsJsonObject().getAsJsonObject(CREDENTIALS);
            if (serviceName.equalsIgnoreCase(WATSON_VISUAL_RECOGNITION)) {
              return credentials.get(APIKEY).getAsString();
            }
          }
        }
      }
    }
    return null;
  }

}
