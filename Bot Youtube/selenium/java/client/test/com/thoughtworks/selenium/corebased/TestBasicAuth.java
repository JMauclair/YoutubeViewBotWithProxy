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

package com.thoughtworks.selenium.corebased;

import static org.junit.Assume.assumeFalse;

import com.thoughtworks.selenium.InternalSelenseTestBase;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.environment.GlobalTestEnvironment;
import org.openqa.selenium.environment.webserver.AppServer;
import org.openqa.selenium.testing.TestUtilities;

import java.net.MalformedURLException;
import java.net.URL;

@Ignore("Browsers don't allow basic auth through URL params any more")
public class TestBasicAuth extends InternalSelenseTestBase {
  @Test
  public void testBasicAuth() throws Exception {
    assumeFalse(
        "Geckodriver does not support basic auth without user interaction.",
        selenium instanceof WrapsDriver &&
        TestUtilities.isFirefox(((WrapsDriver) selenium).getWrappedDriver()));
    selenium.open(getUrl());
    assertEquals(selenium.getTitle(), "Welcome");
  }

  private String getUrl() throws MalformedURLException {
    AppServer appServer = GlobalTestEnvironment.get().getAppServer();
    URL url = new URL(appServer.whereIs("basicAuth/index.html"));

    return String.format("%s://alice:foo@%s:%d%s",
        url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
  }
}
