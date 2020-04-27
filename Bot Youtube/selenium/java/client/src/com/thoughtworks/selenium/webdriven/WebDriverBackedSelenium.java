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

import com.thoughtworks.selenium.DefaultSelenium;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;

import java.util.function.Supplier;

public class WebDriverBackedSelenium extends DefaultSelenium
    implements HasCapabilities, WrapsDriver {
  public WebDriverBackedSelenium(Supplier<WebDriver> maker, String baseUrl) {
    super(new WebDriverCommandProcessor(baseUrl, maker));
  }

  public WebDriverBackedSelenium(WebDriver baseDriver, String baseUrl) {
    super(new WebDriverCommandProcessor(baseUrl, baseDriver));
  }

  @Override
  public WebDriver getWrappedDriver() {
    return ((WrapsDriver) commandProcessor).getWrappedDriver();
  }

  @Override
  public Capabilities getCapabilities() {
    WebDriver driver = getWrappedDriver();
    if (driver instanceof HasCapabilities) {
      return ((HasCapabilities) driver).getCapabilities();
    }

    return null;
  }
}
