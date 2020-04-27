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

package org.openqa.selenium.grid.sessionmap.httpd;

import com.google.common.collect.ImmutableMap;

import org.openqa.selenium.grid.config.MapConfig;

class DefaultSessionMapConfig extends MapConfig {

  public DefaultSessionMapConfig() {
    super(ImmutableMap.of(
      "events", ImmutableMap.of(
        "publish", "tcp://*:4442",
        "subscribe", "tcp://*:4443"),
      "sessions", ImmutableMap.of(
        "implementation", "org.openqa.selenium.grid.sessionmap.local.LocalSessionMap"),
      "server", ImmutableMap.of(
        "port", 5556)));
  }
}
