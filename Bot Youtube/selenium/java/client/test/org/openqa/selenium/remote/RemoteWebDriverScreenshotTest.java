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

package org.openqa.selenium.remote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.openqa.selenium.OutputType.BASE64;
import static org.openqa.selenium.testing.drivers.Browser.HTMLUNIT;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.testing.Ignore;
import org.openqa.selenium.testing.JUnit4TestBase;
import org.openqa.selenium.testing.drivers.WebDriverBuilder;

@Ignore(HTMLUNIT)
public class RemoteWebDriverScreenshotTest extends JUnit4TestBase {

  @Test
  @Ignore
  public void testShouldBeAbleToGrabASnapshotOnException() {
    if (!(driver instanceof RemoteWebDriver)) {
      System.out.println("Skipping test: driver is not a remote webdriver");
      return;
    }

    driver.get(pages.simpleTestPage);

    assertThatExceptionOfType(NoSuchElementException.class)
        .isThrownBy(() -> driver.findElement(By.id("doesnayexist")))
        .satisfies(e -> assertThat(
            ((ScreenshotException) e.getCause()).getBase64EncodedScreenshot().length()).isGreaterThan(0));
  }

  @Test
  public void testCanAugmentWebDriverInstanceIfNecessary() {
    if (!(driver instanceof RemoteWebDriver)) {
      System.out.println("Skipping test: driver is not a remote webdriver");
      return;
    }

    RemoteWebDriver remote = (RemoteWebDriver) driver;
    Boolean screenshots = (Boolean) remote.getCapabilities()
        .getCapability(CapabilityType.TAKES_SCREENSHOT);
    if (screenshots == null || !screenshots) {
      System.out.println("Skipping test: remote driver cannot take screenshots");
    }

    driver.get(pages.formPage);
    WebDriver toUse = new Augmenter().augment(driver);
    String screenshot = ((TakesScreenshot) toUse).getScreenshotAs(BASE64);

    assertThat(screenshot.length()).isGreaterThan(0);
  }

  @Test
  public void testShouldBeAbleToDisableSnapshotOnException() {
    if (!(driver instanceof RemoteWebDriver)) {
      System.out.println("Skipping test: driver is not a remote webdriver");
      return;
    }

    Capabilities caps = new ImmutableCapabilities("webdriver.remote.quietExceptions", true);

    WebDriver noScreenshotDriver = new WebDriverBuilder().get(caps);

    noScreenshotDriver.get(pages.simpleTestPage);

    assertThatExceptionOfType(NoSuchElementException.class)
        .isThrownBy(() -> noScreenshotDriver.findElement(By.id("doesnayexist")))
        .satisfies(e -> {
          Throwable t = e;
          while (t != null) {
            assertThat(t).isNotInstanceOf(ScreenshotException.class);
            t = t.getCause();
          }
        });
  }

}
