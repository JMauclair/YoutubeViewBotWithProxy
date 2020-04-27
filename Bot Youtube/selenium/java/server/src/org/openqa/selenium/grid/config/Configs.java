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

package org.openqa.selenium.grid.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Configs {

  private Configs() {
    // This class is not intended to be instantiated
  }

  public static Config from(Path path) {
    Objects.requireNonNull(path, "Path to read must be set.");

    if (!Files.exists(path)) {
      throw new ConfigException("Path to read from does not exist: " + path);
    }

    String fileName = path.getFileName().toString();
    if (fileName.endsWith(".tml") || fileName.endsWith(".toml")) {
      return TomlConfig.from(path);
    }

    if (fileName.endsWith(".json")) {
      return JsonConfig.from(path);
    }

    throw new ConfigException(
      "Unable to determine file type. The file extension must be one of '.toml' or '.json' " + path);
  }
}
