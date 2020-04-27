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

package org.openqa.selenium.devtools;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.devtools.target.model.ReceivedMessageFromTarget;
import org.openqa.selenium.devtools.target.model.SessionID;
import org.openqa.selenium.devtools.target.model.TargetCrashed;
import org.openqa.selenium.devtools.target.model.TargetID;
import org.openqa.selenium.devtools.target.model.TargetInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.devtools.target.Target.activateTarget;
import static org.openqa.selenium.devtools.target.Target.attachToTarget;
import static org.openqa.selenium.devtools.target.Target.attachedToTarget;
import static org.openqa.selenium.devtools.target.Target.closeTarget;
import static org.openqa.selenium.devtools.target.Target.createTarget;
import static org.openqa.selenium.devtools.target.Target.getTargetInfo;
import static org.openqa.selenium.devtools.target.Target.getTargets;
import static org.openqa.selenium.devtools.target.Target.receivedMessageFromTarget;
import static org.openqa.selenium.devtools.target.Target.sendMessageToTarget;
import static org.openqa.selenium.devtools.target.Target.setDiscoverTargets;
import static org.openqa.selenium.devtools.target.Target.targetCrashed;
import static org.openqa.selenium.devtools.target.Target.targetCreated;
import static org.openqa.selenium.devtools.target.Target.targetDestroyed;
import static org.openqa.selenium.devtools.target.Target.targetInfoChanged;

public class ChromeDevToolsTargetTest extends DevToolsTestBase {

  private final int id = 123;

  @Test
  public void getTargetActivateAndAttach() {
    devTools.addListener(attachedToTarget(), Assert::assertNotNull);

    driver.get(appServer.whereIs("devToolsConsoleTest.html"));
    List<TargetInfo> allTargets = devTools.send(getTargets());

    for (TargetInfo target : allTargets) {
      validateTarget(target);
      devTools.send(activateTarget(target.getTargetId()));
      SessionID sessionId =
          devTools.send(attachToTarget(target.getTargetId(), Optional.of(Boolean.FALSE)));
      validateSession(sessionId);
      TargetInfo infods = devTools.send(getTargetInfo(Optional.of(target.getTargetId())));
      validateTargetInfo(infods);
    }
  }

  @Test
  public void getTargetAndSendMessageToTarget() {
    List<TargetInfo> allTargets = null;
    SessionID sessionId = null;
    TargetInfo targetInfo = null;
    driver.get(appServer.whereIs("devToolsConsoleTest.html"));
    devTools.addListener(receivedMessageFromTarget(), this::validateMessage);
    allTargets = devTools.send(getTargets());
    validateTargetsInfos(allTargets);
    ArrayList<TargetInfo> listTargets = new ArrayList<>(allTargets);
    validateTarget(listTargets.get(0));
    targetInfo = listTargets.get(0);
    devTools.send(activateTarget(targetInfo.getTargetId()));
    sessionId = devTools.send(attachToTarget(targetInfo.getTargetId(), Optional.of(false)));
    validateSession(sessionId);
    devTools.send(
        sendMessageToTarget(
            "{\"id\":" + id + ",\"method\":\"Page.bringToFront\"}",
            Optional.of(sessionId),
            Optional.of(targetInfo.getTargetId())));
  }

  @Test
  public void createAndContentLifeCycle() {
    devTools.addListener(targetCreated(), this::validateTargetInfo);
    devTools.addListener(targetCrashed(), this::validateTargetCrashed);
    devTools.addListener(targetDestroyed(), this::validateTargetId);
    devTools.addListener(targetInfoChanged(), this::validateTargetInfo);

    TargetID target =
        devTools.send(
            createTarget(
                appServer.whereIs("devToolsConsoleTest.html"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(Boolean.TRUE),
                Optional.of(Boolean.FALSE)));
    validateTargetId(target);
    devTools.send(setDiscoverTargets(true));
    Boolean isClosed = devTools.send(closeTarget(target));
    assertNotNull(isClosed);
    assertTrue(isClosed);
  }

  private void validateTargetCrashed(TargetCrashed targetCrashed) {
    assertNotNull(targetCrashed);
    assertNotNull(targetCrashed.getStatus());
    assertNotNull(targetCrashed.getTargetId());
  }

  private void validateTargetId(TargetID targetId) {
    assertNotNull(targetId);
  }

  private void validateMessage(ReceivedMessageFromTarget messageFromTarget) {
    assertNotNull(messageFromTarget);
    assertNotNull(messageFromTarget.getMessage());
    assertNotNull(messageFromTarget.getSessionId());
    assertNotNull(messageFromTarget.getMessage());
    assertEquals("{\"id\":" + id + ",\"result\":{}}", messageFromTarget.getMessage());
  }

  private void validateTargetInfo(TargetInfo targetInfo) {
    assertNotNull(targetInfo);
    assertNotNull(targetInfo.getTargetId());
    assertNotNull(targetInfo.getTitle());
    assertNotNull(targetInfo.getType());
    assertNotNull(targetInfo.getUrl());
  }

  private void validateTargetsInfos(List<TargetInfo> targets) {
    assertNotNull(targets);
    assertFalse(targets.isEmpty());
  }

  private void validateTarget(TargetInfo targetInfo) {
    assertNotNull(targetInfo);
    assertNotNull(targetInfo.getTargetId());
    assertNotNull(targetInfo.getTitle());
    assertNotNull(targetInfo.getType());
    assertNotNull(targetInfo.getUrl());
  }

  private void validateSession(SessionID sessionId) {
    assertNotNull(sessionId);
  }
}
