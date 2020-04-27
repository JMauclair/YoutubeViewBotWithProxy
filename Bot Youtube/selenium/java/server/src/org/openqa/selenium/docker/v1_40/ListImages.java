// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.docker.v1_40;

import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.docker.Image;
import org.openqa.selenium.docker.internal.ImageSummary;
import org.openqa.selenium.docker.internal.Reference;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.TypeToken;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.openqa.selenium.json.Json.JSON_UTF_8;
import static org.openqa.selenium.remote.http.Contents.string;
import static org.openqa.selenium.remote.http.HttpMethod.GET;

class ListImages {

  private static final Json JSON = new Json();
  private static final Type SET_OF_IMAGE_SUMMARIES = new TypeToken<Set<ImageSummary>>() {}.getType();

  private final HttpHandler client;

  public ListImages(HttpHandler client) {
    this.client = Objects.requireNonNull(client);
  }

  public Set<Image> apply(Reference reference) {
    Objects.requireNonNull(reference, "Reference to search for must be set");

    String familiarName = reference.getFamiliarName();
    Map<String, Object> filters = ImmutableMap.of("reference", ImmutableMap.of(familiarName, true));

    // https://docs.docker.com/engine/api/v1.40/#operation/ImageList
    HttpRequest req = new HttpRequest(GET, "/v1.40/images/json")
      .addHeader("Content-Length", "0")
      .addHeader("Content-Type", JSON_UTF_8)
      .addQueryParameter("filters", JSON.toJson(filters));

    HttpResponse response = DockerMessages.throwIfNecessary(
      client.execute(req),
    "Unable to list images for %s", reference);

    Set<ImageSummary> images =
      JSON.toType(string(response), SET_OF_IMAGE_SUMMARIES);

    return images.stream()
      .map(org.openqa.selenium.docker.Image::new)
      .collect(toImmutableSet());
  }
}
