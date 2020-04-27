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

package com.thoughtworks.selenium.webdriven;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VariableDeclarationTest {

  private static final String REPLACEMENT = "selenium.browserbot = {};";

  private VariableDeclaration declaration;

  @Before
  public void setUp() {
    declaration = new VariableDeclaration(
        "selenium.browserbot", REPLACEMENT);
  }

  @Test
  public void testShouldLeaveThingsWellAloneIfNotNeeded() {
    StringBuilder builder = new StringBuilder();
    declaration.mutate("I like cheese", builder);

    // We don't expect the variable declaration to be written
    assertEquals(builder.toString(), "", builder.toString());
  }

  @Test
  public void testShouldAddDeclarationIfNecesssary() {
    StringBuilder builder = new StringBuilder();
    declaration.mutate("selenium.browserbot.findElement", builder);

    assertEquals(REPLACEMENT, builder.toString());
  }

  @Test
  public void testReplacementStillHappensWithStrangeSpacing() {
    StringBuilder builder = new StringBuilder();
    declaration.mutate("selenium   \n\n\n .browserbot .findCheese", builder);

    assertEquals(REPLACEMENT, builder.toString());
  }
}
