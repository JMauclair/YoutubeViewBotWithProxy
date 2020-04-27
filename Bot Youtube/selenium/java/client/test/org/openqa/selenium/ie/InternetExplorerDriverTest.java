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

package org.openqa.selenium.ie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.WaitingConditions.elementTextToEqual;
import static org.openqa.selenium.ie.InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.testing.JUnit4TestBase;
import org.openqa.selenium.testing.NeedsLocalEnvironment;
import org.openqa.selenium.testing.NoDriverAfterTest;
import org.openqa.selenium.testing.NoDriverBeforeTest;
import org.openqa.selenium.testing.drivers.WebDriverBuilder;

import java.awt.Robot;

@NeedsLocalEnvironment(reason = "Requires local browser launching environment")
public class InternetExplorerDriverTest extends JUnit4TestBase {

  @Test
  @NoDriverBeforeTest
  public void canRestartTheIeDriverInATightLoop() {
    for (int i = 0; i < 5; i++) {
      WebDriver driver = newIeDriver();
      driver.quit();
    }
  }

  @Test
  @NoDriverBeforeTest
  public void canStartMultipleIeDriverInstances() {
    WebDriver firstDriver = newIeDriver();
    WebDriver secondDriver = newIeDriver();
    try {
      firstDriver.get(pages.xhtmlTestPage);
      secondDriver.get(pages.formPage);
      assertThat(firstDriver.getTitle()).isEqualTo("XHTML Test Page");
      assertThat(secondDriver.getTitle()).isEqualTo("We Leave From Here");
    } finally {
      firstDriver.quit();
      secondDriver.quit();
    }
  }

  @NoDriverBeforeTest
  @NoDriverAfterTest
  @NeedsLocalEnvironment
  @Test
  public void testPersistentHoverCanBeTurnedOff() throws Exception {
    createNewDriver(new ImmutableCapabilities(ENABLE_PERSISTENT_HOVERING, false));

    driver.get(pages.javascriptPage);
    // Move to a different element to make sure the mouse is not over the
    // element with id 'item1' (from a previous test).
    new Actions(driver).moveToElement(driver.findElement(By.id("keyUp"))).build().perform();
    WebElement element = driver.findElement(By.id("menu1"));

    final WebElement item = driver.findElement(By.id("item1"));
    assertThat(item.getText()).isEqualTo("");

    ((JavascriptExecutor) driver).executeScript("arguments[0].style.background = 'green'", element);
    new Actions(driver).moveToElement(element).build().perform();

    // Move the mouse somewhere - to make sure that the thread firing the events making
    // hover persistent is not active.
    Robot robot = new Robot();
    robot.mouseMove(50, 50);

    // Intentionally wait to make sure hover DOES NOT persist.
    Thread.sleep(1000);

    wait.until(elementTextToEqual(item, ""));

    assertThat(item.getText()).isEqualTo("");
  }

  private WebDriver newIeDriver() {
    return new WebDriverBuilder().get();
  }
}
