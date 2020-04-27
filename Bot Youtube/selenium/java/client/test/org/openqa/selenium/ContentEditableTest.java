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
import static org.openqa.selenium.testing.drivers.Browser.EDGE;
import static org.openqa.selenium.testing.drivers.Browser.IE;
import static org.openqa.selenium.testing.drivers.Browser.MARIONETTE;
import static org.openqa.selenium.testing.drivers.Browser.SAFARI;
import static org.openqa.selenium.testing.TestUtilities.getEffectivePlatform;
import static org.openqa.selenium.testing.TestUtilities.isFirefox;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.testing.JUnit4TestBase;
import org.openqa.selenium.testing.NotYetImplemented;

public class ContentEditableTest extends JUnit4TestBase {

  @After
  public void switchToDefaultContent() {
    driver.switchTo().defaultContent();
  }

  @Test
  @NotYetImplemented(value = MARIONETTE)
  public void testTypingIntoAnIFrameWithContentEditableOrDesignModeSet() {
    driver.get(pages.richTextPage);

    driver.switchTo().frame("editFrame");
    WebElement element = driver.switchTo().activeElement();
    element.sendKeys("Fishy");

    driver.switchTo().defaultContent();
    WebElement trusted = driver.findElement(By.id("istrusted"));
    WebElement id = driver.findElement(By.id("tagId"));

    assertThat(trusted.getText()).isIn("[true]", "[n/a]", "[]");
    assertThat(id.getText()).isIn("[frameHtml]", "[theBody]");
  }

  @Test
  @NotYetImplemented(value = MARIONETTE)
  @NotYetImplemented(SAFARI)
  public void testNonPrintableCharactersShouldWorkWithContentEditableOrDesignModeSet() {
    assumeFalse("FIXME: Fails in Firefox on Linux with synthesized events",
                isFirefox(driver) &&
                (getEffectivePlatform(driver).is(Platform.LINUX) ||
                 getEffectivePlatform(driver).is(Platform.MAC)));

    driver.get(pages.richTextPage);

    driver.switchTo().frame("editFrame");
    WebElement element = driver.switchTo().activeElement();
    element.sendKeys("Dishy", Keys.BACK_SPACE, Keys.LEFT, Keys.LEFT);
    element.sendKeys(Keys.LEFT, Keys.LEFT, "F", Keys.DELETE, Keys.END, "ee!");

    assertThat(element.getText()).isEqualTo("Fishee!");
  }

  @Test
  public void testShouldBeAbleToTypeIntoEmptyContentEditableElement() {
    driver.get(pages.readOnlyPage);
    WebElement editable = driver.findElement(By.id("content-editable-blank"));

    editable.sendKeys("cheese");

    assertThat(editable.getText()).isEqualTo("cheese");
  }

  @Test
  @NotYetImplemented(SAFARI)
  @NotYetImplemented(EDGE)
  @NotYetImplemented(value = MARIONETTE, reason = "https://github.com/mozilla/geckodriver/issues/667")
  public void testShouldBeAbleToTypeIntoContentEditableElementWithExistingValue() {
    driver.get(pages.readOnlyPage);
    WebElement editable = driver.findElement(By.id("content-editable"));

    String initialText = editable.getText();
    editable.sendKeys(", edited");

    assertThat(editable.getText()).isEqualTo(initialText + ", edited");
  }

  @Test
  public void testShouldBeAbleToTypeIntoTinyMCE() {
    driver.get(appServer.whereIs("tinymce.html"));
    driver.switchTo().frame("mce_0_ifr");

    WebElement editable = driver.findElement(By.id("tinymce"));

    editable.clear();
    editable.sendKeys("cheese"); // requires focus on OS X

    assertThat(editable.getText()).isEqualTo("cheese");
  }

  @Test
  @NotYetImplemented(value = IE, reason = "Prepends text")
  @NotYetImplemented(value = SAFARI, reason = "Prepends text")
  @NotYetImplemented(value = MARIONETTE, reason = "https://github.com/mozilla/geckodriver/issues/667")
  @NotYetImplemented(EDGE)
  public void testShouldAppendToTinyMCE() {
    driver.get(appServer.whereIs("tinymce.html"));
    driver.switchTo().frame("mce_0_ifr");

    WebElement editable = driver.findElement(By.id("tinymce"));

    editable.sendKeys(" and cheese"); // requires focus on OS X

    assertThat(editable.getText()).isEqualTo("Initial content and cheese");
  }

  @Test
  @NotYetImplemented(value = MARIONETTE, reason = "Doesn't write anything")
  @NotYetImplemented(value = SAFARI, reason = "Prepends text")
  public void appendsTextToEndOfContentEditableWithMultipleTextNodes() {
    driver.get(appServer.whereIs("content-editable.html"));
    WebElement input = driver.findElement(By.id("editable"));
    input.sendKeys(", world!");
    assertThat(input.getText()).isEqualTo("Why hello, world!");
  }
}
