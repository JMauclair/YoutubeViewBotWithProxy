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

package org.openqa.selenium.docker;

import org.openqa.selenium.remote.http.HttpHandler;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class Docker {

  private static final Logger LOG = Logger.getLogger(Docker.class.getName());
  protected final HttpHandler client;
  private volatile Optional<DockerProtocol> dockerClient;

  public Docker(HttpHandler client) {
    this.client = Objects.requireNonNull(client, "HTTP client to use must be set.");
    this.dockerClient = Optional.empty();
  }

  public boolean isSupported() {
    return getDocker().isPresent();
  }

  public String getVersion() {
    return getDocker().map(DockerProtocol::version).orElse("unsupported");
  }

  public Image getImage(String name) {
    Objects.requireNonNull(name, "Image name to get must be set.");

    LOG.info("Obtaining image: " + name);

    return getDocker()
      .map(protocol -> protocol.getImage(name))
      .orElseThrow(() -> new DockerException("Unable to get image " + name));
  }

  public Container create(ContainerInfo info) {
    Objects.requireNonNull(info, "Container info must be set.");

    LOG.info("Creating image from " + info);

    return getDocker()
      .map(protocol -> protocol.create(info))
      .orElseThrow(() -> new DockerException("Unable to create container: " + info));
  }

  private Optional<DockerProtocol> getDocker() {
    if (dockerClient.isPresent()) {
      return dockerClient;
    }

    synchronized (this) {
      if (!dockerClient.isPresent()) {
        dockerClient = new VersionCommand(client).getDockerProtocol();
      }
    }

    return dockerClient;
  }
}
