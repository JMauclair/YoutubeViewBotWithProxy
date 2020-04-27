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

package org.openqa.selenium.grid.sessionmap.remote;

import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.grid.config.Config;
import org.openqa.selenium.grid.data.Session;
import org.openqa.selenium.grid.log.LoggingOptions;
import org.openqa.selenium.grid.server.NetworkOptions;
import org.openqa.selenium.grid.sessionmap.SessionMap;
import org.openqa.selenium.grid.sessionmap.config.SessionMapOptions;
import org.openqa.selenium.grid.web.Values;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.tracing.HttpTracing;

import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;

import static org.openqa.selenium.remote.http.Contents.asJson;
import static org.openqa.selenium.remote.http.HttpMethod.DELETE;
import static org.openqa.selenium.remote.http.HttpMethod.GET;
import static org.openqa.selenium.remote.http.HttpMethod.POST;

public class RemoteSessionMap extends SessionMap {

  private final HttpClient client;

  public RemoteSessionMap(Tracer tracer, HttpClient client) {
    super(tracer);

    this.client = Objects.requireNonNull(client);
  }

  public static SessionMap create(Config config) {
    Tracer tracer = new LoggingOptions(config).getTracer();
    URI uri = new SessionMapOptions(config).getSessionMapUri();
    HttpClient.Factory clientFactory = new NetworkOptions(config).getHttpClientFactory(tracer);

    try {
      return new RemoteSessionMap(tracer, clientFactory.createClient(uri.toURL()));
    } catch (MalformedURLException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public boolean add(Session session) {
    Objects.requireNonNull(session, "Session must be set");

    HttpRequest request = new HttpRequest(POST, "/se/grid/session");
    request.setContent(asJson(session));

    return makeRequest(request, Boolean.class);
  }

  @Override
  public Session get(SessionId id) {
    Objects.requireNonNull(id, "Session ID must be set");

    Session session = makeRequest(new HttpRequest(GET, "/se/grid/session/" + id), Session.class);
    if (session == null) {
      throw new NoSuchSessionException("Unable to find session with ID: " + id);
    }
    return session;
  }

  @Override
  public URI getUri(SessionId id) throws NoSuchSessionException {
    Objects.requireNonNull(id, "Session ID must be set");

    URI value = makeRequest(new HttpRequest(GET, "/se/grid/session/" + id + "/uri"), URI.class);
    if (value == null) {
      throw new NoSuchSessionException("Unable to find session with ID: " + id);
    }
    return value;
  }

  @Override
  public void remove(SessionId id) {
    Objects.requireNonNull(id, "Session ID must be set");

    makeRequest(new HttpRequest(DELETE, "/se/grid/session/" + id), Void.class);
  }

  private <T> T makeRequest(HttpRequest request, Type typeOfT) {
    Span activeSpan = tracer.getCurrentSpan();
    HttpTracing.inject(tracer, activeSpan, request);

    HttpResponse response = client.execute(request);
    return Values.get(response, typeOfT);
  }
}
