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

package org.openqa.selenium.grid.data;

import static org.openqa.selenium.json.Json.MAP_TYPE;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.json.JsonInput;
import org.openqa.selenium.remote.Dialect;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CreateSessionRequest {

  private final Set<Dialect> downstreamDialects;
  private final Capabilities capabilities;
  private final Map<String, Object> metadata;

  public CreateSessionRequest(
      Set<Dialect> downstreamDialects,
      Capabilities capabilities,
      Map<String, Object> metadata) {
    this.downstreamDialects = ImmutableSet.copyOf(Objects.requireNonNull(downstreamDialects));
    this.capabilities = ImmutableCapabilities.copyOf(Objects.requireNonNull(capabilities));
    this.metadata = ImmutableMap.copyOf(Objects.requireNonNull(metadata));
  }

  public Set<Dialect> getDownstreamDialects() {
    return downstreamDialects;
  }

  public Capabilities getCapabilities() {
    return capabilities;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  private static CreateSessionRequest fromJson(JsonInput input) {
    Set<Dialect> downstreamDialects = null;
    Capabilities capabilities = null;
    Map<String, Object> metadata = null;

    input.beginObject();
    while (input.hasNext()) {
      switch (input.nextName()) {
        case "capabilities":
          capabilities = input.read(Capabilities.class);
          break;

        case "downstreamDialects":
          downstreamDialects = input.read(new TypeToken<Set<Dialect>>(){}.getType());
          break;

        case "metadata":
          metadata = input.read(MAP_TYPE);
          break;

        default:
          input.skipValue();
      }
    }
    input.endObject();

    return new CreateSessionRequest(downstreamDialects, capabilities, metadata);
  }

  public String toString() {
    return String.format("<CreateSessionRequest with %s>", capabilities);
  }
}