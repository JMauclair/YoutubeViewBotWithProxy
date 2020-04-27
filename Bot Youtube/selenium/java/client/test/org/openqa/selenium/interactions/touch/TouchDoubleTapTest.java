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

package org.openqa.selenium.interactions.touch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Locatable;

/**
 * Tests the basic double tap operations.
 */
public class TouchDoubleTapTest extends TouchTestBase {

  private TouchActions getBuilder(WebDriver driver) {
    return new TouchActions(driver);
  }

  @Test
  public void testCanDoubleTapOnAnImageAndAlterLocationOfElementsInScreen() {
    driver.get(pages.longContentPage);

    WebElement image = driver.findElement(By.id("imagestart"));
    int y = ((Locatable) image).getCoordinates().inViewPort().y;
    // The element is located at a certain point, after double tapping,
    // the y coordinate must change.
    assertThat(y).isGreaterThan(100);

    Action doubleTap = getBuilder(driver).doubleTap(image).build();
    doubleTap.perform();

    y = ((Locatable) image).getCoordinates().inViewPort().y;
    assertThat(y).isLessThan(50);
  }

}
