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

package org.openqa.selenium.grid.commands;

import com.beust.jcommander.Parameter;

import org.openqa.selenium.grid.config.ConfigValue;

public class StandaloneFlags {

  @Parameter(
      names = {"--detect-drivers"},
      description = "Autodetect which drivers are available on the current system, and add them to the node.",
      arity = 1)
  @ConfigValue(section = "node", name = "detect-drivers")
  public boolean autoconfigure = true;

  @ConfigValue(section = "server", name = "port")
  public int port = 4444;
}
