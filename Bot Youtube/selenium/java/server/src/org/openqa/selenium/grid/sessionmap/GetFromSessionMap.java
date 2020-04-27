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

package org.openqa.selenium.grid.sessionmap;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import org.openqa.selenium.grid.data.Session;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

import java.util.Objects;

import static org.openqa.selenium.remote.RemoteTags.CAPABILITIES;
import static org.openqa.selenium.remote.RemoteTags.SESSION_ID;
import static org.openqa.selenium.remote.http.Contents.asJson;
import static org.openqa.selenium.remote.tracing.HttpTags.HTTP_REQUEST;
import static org.openqa.selenium.remote.tracing.HttpTracing.newSpanAsChildOf;

class GetFromSessionMap implements HttpHandler {

  private final Tracer tracer;
  private final SessionMap sessions;
  private final SessionId id;

  GetFromSessionMap(Tracer tracer, SessionMap sessions, SessionId id) {
    this.tracer = Objects.requireNonNull(tracer);
    this.sessions = Objects.requireNonNull(sessions);
    this.id = Objects.requireNonNull(id);
  }

  @Override
  public HttpResponse execute(HttpRequest req) {
    Span span = newSpanAsChildOf(tracer, req, "sessions.get_session").startSpan();

    try (Scope scope = tracer.withSpan(span)) {
      HTTP_REQUEST.accept(span, req);

      Session session = sessions.get(id);

      SESSION_ID.accept(span, session.getId());
      CAPABILITIES.accept(span, session.getCapabilities());
      span.setAttribute("session.uri", session.getUri().toString());

      return new HttpResponse().setContent(asJson(ImmutableMap.of("value", session)));
    } finally {
      span.end();
    }
  }
}
