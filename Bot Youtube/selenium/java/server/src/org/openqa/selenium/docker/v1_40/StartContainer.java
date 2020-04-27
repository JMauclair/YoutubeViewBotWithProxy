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

import org.openqa.selenium.docker.ContainerId;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.HttpRequest;

import java.util.Objects;

import static org.openqa.selenium.docker.v1_40.DockerMessages.throwIfNecessary;
import static org.openqa.selenium.remote.http.HttpMethod.POST;

class StartContainer {
  private final HttpHandler client;

  public StartContainer(HttpHandler client) {
    this.client = Objects.requireNonNull(client);
  }

  public void apply(ContainerId id) {
    Objects.requireNonNull(id);

    throwIfNecessary(
      client.execute(
        new HttpRequest(POST, String.format("/v1.40/containers/%s/start", id))
          .addHeader("Content-Length", "0")
          .addHeader("Content-Type", "text/plain")),
      "Unable to start container: %s",
      id);
  }
}
