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

package org.openqa.selenium.grid.session;

import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.http.HttpHandler;

import java.util.Map;

public interface ActiveSession extends HttpHandler, WrapsDriver {

  SessionId getId();

  Dialect getUpstreamDialect();

  Dialect getDownstreamDialect();

  /**
   * Describe the current webdriver session's capabilities.
   */
  Map<String, Object> getCapabilities();

  TemporaryFilesystem getFileSystem();

  void stop();
}
