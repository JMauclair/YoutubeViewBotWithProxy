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

package org.openqa.selenium.grid.node;

import org.openqa.selenium.grid.data.CreateSessionRequest;
import org.openqa.selenium.grid.data.CreateSessionResponse;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Objects;

import static org.openqa.selenium.remote.http.Contents.asJson;
import static org.openqa.selenium.remote.http.Contents.string;

class NewNodeSession implements HttpHandler {

  private final Node node;
  private final Json json;

  NewNodeSession(Node node, Json json) {
    this.node = Objects.requireNonNull(node);
    this.json = Objects.requireNonNull(json);
  }

  @Override
  public HttpResponse execute(HttpRequest req) throws UncheckedIOException {
    CreateSessionRequest incoming = json.toType(string(req), CreateSessionRequest.class);

    CreateSessionResponse sessionResponse = node.newSession(incoming).orElse(null);

    HashMap<String, Object> value = new HashMap<>();
    value.put("value", sessionResponse);

    return new HttpResponse().setContent(asJson(value));
  }
}
