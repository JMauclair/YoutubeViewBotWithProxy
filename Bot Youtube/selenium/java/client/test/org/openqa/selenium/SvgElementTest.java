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

package org.openqa.selenium;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.openqa.selenium.WaitingConditions.elementTextToEqual;
import static org.openqa.selenium.testing.drivers.Browser.HTMLUNIT;
import static org.openqa.selenium.testing.drivers.Browser.MARIONETTE;
import static org.openqa.selenium.testing.drivers.Browser.SAFARI;
import static org.openqa.selenium.testing.TestUtilities.isOldIe;

import org.junit.Test;
import org.openqa.selenium.testing.Ignore;
import org.openqa.selenium.testing.JUnit4TestBase;
import org.openqa.selenium.testing.NotYetImplemented;

import java.util.List;

public class SvgElementTest extends JUnit4TestBase {

  @Test
  @Ignore(value = HTMLUNIT, reason="test should enable JavaScript")
  @NotYetImplemented(value = MARIONETTE, reason = "https://bugzilla.mozilla.org/show_bug.cgi?id=1415068")
  @NotYetImplemented(SAFARI)
  public void testShouldClickOnGraphVisualElements() {
    assumeFalse("IE version < 9 doesn't support SVG", isOldIe(driver));

    driver.get(pages.svgPage);
    WebElement svg = driver.findElement(By.cssSelector("svg"));

    List<WebElement> groupElements = svg.findElements(By.cssSelector("g"));
    assertThat(groupElements).hasSize(5);

    groupElements.get(1).click();
    WebElement resultElement = driver.findElement(By.id("result"));

    wait.until(elementTextToEqual(resultElement, "slice_red"));
    assertThat(resultElement.getText()).isEqualTo("slice_red");

    groupElements.get(2).click();
    resultElement = driver.findElement(By.id("result"));

    wait.until(elementTextToEqual(resultElement, "slice_green"));
    assertThat(resultElement.getText()).isEqualTo("slice_green");
  }

  private static WebElement findAppleElement(List<WebElement> textElements) {
    for (WebElement currentElement : textElements) {
      if (currentElement.getText().contains("Apple")) {
        return currentElement;
      }
    }

    return null;
  }

  @Test
  @Ignore(value = HTMLUNIT, reason="test should enable JavaScript")
  public void testShouldClickOnGraphTextElements() {
    assumeFalse("IE version < 9 doesn't support SVG", isOldIe(driver));

    driver.get(pages.svgPage);
    WebElement svg = driver.findElement(By.cssSelector("svg"));
    List<WebElement> textElements = svg.findElements(By.cssSelector("text"));

    WebElement appleElement = findAppleElement(textElements);
    assertThat(appleElement).isNotNull();

    appleElement.click();
    WebElement resultElement = driver.findElement(By.id("result"));
    wait.until(elementTextToEqual(resultElement, "text_apple"));
    assertThat(resultElement.getText()).isEqualTo("text_apple");
  }

}
