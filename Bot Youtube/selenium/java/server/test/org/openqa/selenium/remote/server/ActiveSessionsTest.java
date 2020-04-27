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

package org.openqa.selenium.remote.server;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.grid.session.ActiveSession;
import org.openqa.selenium.remote.SessionId;

public class ActiveSessionsTest {

  @Test
  public void shouldReturnNullWhenSessionIsNotPresent() {
    ActiveSessions sessions = new ActiveSessions(10, MINUTES);
    ActiveSession session = sessions.get(new SessionId("1234567890"));

    assertNull(session);
  }

  @Test
  public void canAddNewSessionAndRetrieveById() {
    ActiveSessions sessions = new ActiveSessions(10, MINUTES);

    ActiveSession session = Mockito.mock(ActiveSession.class);
    SessionId id = new SessionId("1234567890");
    Mockito.when(session.getId()).thenReturn(id);

    sessions.put(session);

    assertEquals(session, sessions.get(id));
  }

  @Test
  public void shouldBeAbleToInvalidateASession() {
    ActiveSessions sessions = new ActiveSessions(10, MINUTES);

    ActiveSession session = Mockito.mock(ActiveSession.class);
    SessionId id = new SessionId("1234567890");
    Mockito.when(session.getId()).thenReturn(id);

    sessions.put(session);
    sessions.invalidate(id);

    assertNull(sessions.get(id));
  }
}