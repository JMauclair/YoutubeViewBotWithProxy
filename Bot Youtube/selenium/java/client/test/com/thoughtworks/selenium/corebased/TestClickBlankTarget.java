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

import com.thoughtworks.selenium.InternalSelenseTestBase;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("Switching to the null window appears to fail")
public class TestClickBlankTarget extends InternalSelenseTestBase {
  @Test
  public void testClickBlankTarget() {
    selenium.open("Frames.html");
    selenium.selectFrame("bottomFrame");
    selenium.click("changeBlank");
    selenium.waitForPopUp("_blank", "10000");
    selenium.selectWindow("_blank");
    selenium.click("changeSpan");
    selenium.close();
    selenium.selectWindow("null");
    selenium.click("changeBlank");
    selenium.waitForPopUp("_blank", "10000");
    selenium.selectWindow("_blank");
    selenium.click("changeSpan");
    selenium.close();
    selenium.selectWindow("null");
    selenium.selectFrame("bottomFrame");
    selenium.submit("formBlank");
    selenium.waitForPopUp("_blank", "10000");
    selenium.selectWindow("_blank");
    selenium.click("changeSpan");
    selenium.close();
    selenium.selectWindow("null");
    selenium.open("test_select_window.html");
    selenium.click("popupBlank");
    selenium.waitForPopUp("_blank", "10000");
    selenium.selectWindow("_blank");
    System.out.println("At the end");
    verifyEquals(selenium.getTitle(), "Select Window Popup");
    selenium.close();
    selenium.selectWindow("null");
  }
}
