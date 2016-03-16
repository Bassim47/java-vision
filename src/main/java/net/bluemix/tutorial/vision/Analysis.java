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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wink.common.model.multipart.BufferedInMultiPart;
import org.apache.wink.common.model.multipart.InPart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyVision;
import com.ibm.watson.developer_cloud.alchemy.v1.model.ImageFace;
import com.ibm.watson.developer_cloud.alchemy.v1.model.ImageKeyword;

@Path("analysis")
public class Analysis {

  private static Logger LOGGER = Logger.getLogger(Analysis.class.getName());
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private AlchemyVision vision;

  public Analysis() {
    // Alchemy API key is automatically retrieved from VCAP_SERVICES by the
    // Watson SDK
    vision = new AlchemyVision();
    
    // Allow a developer running locally to override the Alchemy API key with an environment variable.
    // When working with Liberty, it can be defined in server.env.
    String apiKey = System.getenv("ALCHEMY_API_KEY");
    if (apiKey != null) {
      vision.setApiKey(apiKey);
    }
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("image")
  public Response image(BufferedInMultiPart file) throws Exception {
    InPart filePart = file.getParts().get(0);

    // write it to disk
    InputStream fileInput = filePart.getInputStream();
    File tmpFile = File.createTempFile("vision-", ".jpg");
    tmpFile.deleteOnExit();

    LOGGER.info("Analyzing a binary file " + tmpFile);
    Files.copy(fileInput, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

    Result result = new Result();
    LOGGER.info("Calling Face Detection...");
    result.faces = vision.recognizeFaces(tmpFile, true).getImageFaces();

    LOGGER.info("Calling Image Keyword...");
    result.keywords = vision.getImageKeywords(tmpFile, true, true).getImageKeywords();

    return Response.ok(gson.toJson(result), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @POST
  @Path("url")
  public Response url(@FormParam("url") String urlText) throws Exception {
    URL url = new URL(urlText);

    Result result = new Result();
    result.url = urlText;
    
    LOGGER.info("Calling Face Detection...");
    result.faces = vision.recognizeFaces(url, true).getImageFaces();

    LOGGER.info("Calling Image Keyword...");
    result.keywords = vision.getImageKeywords(url, true, true).getImageKeywords();

    return Response.ok(gson.toJson(result), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @SuppressWarnings("unused")
  private static class Result {
    String url;
    List<ImageFace> faces;
    List<ImageKeyword> keywords;
  }

}
