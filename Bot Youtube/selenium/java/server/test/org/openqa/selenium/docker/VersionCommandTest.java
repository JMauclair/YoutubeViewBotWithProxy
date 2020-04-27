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

import org.junit.Test;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.HttpResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.remote.http.Contents.utf8String;

// https://docs.docker.com/engine/api/v1.40/#operation/SystemVersion
public class VersionCommandTest {

  @Test
  public void ifDockerIsDownReturnEmpty() {
    HttpHandler handler = req -> { throw new UncheckedIOException(new IOException("Eep")); };

    Optional<DockerProtocol> maybeDocker = new VersionCommand(handler).getDockerProtocol();

    assertThat(maybeDocker).isEqualTo(Optional.empty());
  }

  @Test
  public void shouldReturnEmptyIfServerReturnsAnUnsuccessfulResponseStatus() {
    HttpHandler handler = req -> new HttpResponse().setStatus(HTTP_INTERNAL_ERROR);

    Optional<DockerProtocol> maybeDocker = new VersionCommand(handler).getDockerProtocol();

    assertThat(maybeDocker).isEqualTo(Optional.empty());
  }

  @Test
  public void shouldReturnEmptyIfServerDoesNotSupportOurVersionOfTheDockerApi() {
    // We only support v1.40+
    HttpHandler handler = req -> new HttpResponse()
      .addHeader("Content-Type", "application/json")
      .setContent(utf8String("{\"ApiVersion\":\"1.12\",\"MinAPIVersion\":\"1.2\"}"));

    Optional<DockerProtocol> maybeDocker = new VersionCommand(handler).getDockerProtocol();

    assertThat(maybeDocker).isEqualTo(Optional.empty());
  }

  @Test
  public void shouldReturnEmptyIfServerVersionOfDockerApiIsHigherThanAnyWeSupport() {
    // I sincerely hope that there is no version "9999999" of the docker protocol.
    HttpHandler handler = req -> new HttpResponse()
      .addHeader("Content-Type", "application/json")
      .setContent(utf8String("{\"ApiVersion\":\"9999999.12\",\"MinAPIVersion\":\"9999999.0\"}"));

    Optional<DockerProtocol> maybeDocker = new VersionCommand(handler).getDockerProtocol();

    assertThat(maybeDocker).isEqualTo(Optional.empty());
  }

  @Test
  public void shouldReturnADockerInstanceIfTheVersionOfTheApiSupportedIsOneSeleniumAlsoSupports() {
    HttpHandler handler = req -> new HttpResponse()
      .addHeader("Content-Type", "application/json")
      // Note: the version here does not exactly match any we claim to provide
      .setContent(utf8String("{\"ApiVersion\":\"1.41\",\"MinAPIVersion\":\"1.12\"}"));

    Optional<DockerProtocol> maybeDocker = new VersionCommand(handler).getDockerProtocol();

    assertThat(maybeDocker).isNotEqualTo(Optional.empty());
    assertThat(maybeDocker.get().version()).isEqualTo("1.40");
  }
}
