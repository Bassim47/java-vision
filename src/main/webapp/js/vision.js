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
Dropzone.autoDiscover = false;

(function () {
  $(document).ready(function () {

    var imageCount = 0;
    var thumbnailTemplate = Handlebars.compile($("#image-results").html());

    Handlebars.registerHelper('formatPercent', function (float) {
      return Math.round(float * 100);
    });

    $("#uploadZone").dropzone({
      parallelUploads: 1,
      maxFiles: 5,
      maxFilesize: 1, //MB
      uploadMultiple: false,
      acceptedFiles: "image/*",
      dictDefaultMessage: "Drop an image to analyze here",
      init: function () {

        this.on("maxfilesexceeded", function (file) {
          this.removeFile(file);
        });

        this.on("thumbnail", function (file, dataUrl) {
          file.imageId = "image-" + (imageCount++);
          file.dataUrl = dataUrl;
        });

        this.on("error", function (file, errorMessage) {
          //
        });

        this.on("success", function (file, response) {
          var context = {
            file: file,
            faces: response.faces,
            keywords: response.keywords
          }
          $("#results").prepend(thumbnailTemplate(context));
        });

        this.on("complete", function (file) {
          this.removeFile(file);
        });
      }
    });
  });
})();
