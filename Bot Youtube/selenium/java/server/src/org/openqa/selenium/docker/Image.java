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

import org.openqa.selenium.docker.internal.ImageSummary;
import org.openqa.selenium.json.Json;

import java.util.Objects;
import java.util.Set;

public class Image {

  private final ImageSummary summary;

  public Image(ImageSummary summary) {
    this.summary = Objects.requireNonNull(summary);
  }

  public String getName() {
    return summary.getRepoTags().stream()
        .findFirst()
        .orElseThrow(() -> new DockerException("Unable to find name"));
  }

  public ImageId getId() {
    return summary.getId();
  }

  public Set<String> getTags() {
    return summary.getRepoTags();
  }

  @Override
  public String toString() {
    new Json().toJson(summary);
    return "Image{" +
      "summary=" + summary +
      '}';
  }
}
