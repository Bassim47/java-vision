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

    // create a placeholder for the result
    function addResultPending(imageId, imageTitle, imageUrl) {
      var context = {
        id: imageId,
        title: imageTitle,
        imageUrl: imageUrl,
        faces: [],
        keywords: [],
        pending: true
      };
      $("#results").prepend(thumbnailTemplate(context));
    }

    // when results are received
    function onResultReceived(imageId, context, response) {
      $("#" + imageId).replaceWith(thumbnailTemplate(context));
    }

    $("#uploadZone").dropzone({
      parallelUploads: 1,
      maxFiles: 1,
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

          addResultPending(file.imageId, "...", file.dataUrl);
        });

        this.on("error", function (file, errorMessage) {
          //
        });

        this.on("success", function (file, response) {
          var context = {
            id: file.imageId,
            title: file.name,
            imageUrl: file.dataUrl,
            faces: response.faces,
            keywords: response.keywords
          }
          onResultReceived(file.imageId, context, response);
        });

        this.on("complete", function (file) {
          this.removeFile(file);
        });
      }
    });


    function processUrl(imageUrl) {
      var imageId = "image-" + (imageCount++);
      addResultPending(imageId, imageUrl, imageUrl);

      $.ajax({
        url: "api/analysis/url",
        type: "POST",
        data: {
          url: imageUrl
        },
        success: function (response) {
          var context = {
            id: imageId,
            title: response.url,
            imageUrl: response.url,
            faces: response.faces,
            keywords: response.keywords
          };
          onResultReceived(imageId, context, response);
        }
      });
    }

    $(".sample-image").on("click", function (e) {
      processUrl($(this).attr("src"));
    });

    $("#analyze-url-form").submit(function (e) {
      e.preventDefault();
      processUrl($("#image-url").val());
    });
  });
})();
