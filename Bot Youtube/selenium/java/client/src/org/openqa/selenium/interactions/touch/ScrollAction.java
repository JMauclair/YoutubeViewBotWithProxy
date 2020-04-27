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

import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.interactions.internal.TouchAction;

/**
 * Creates a scroll gesture.
 */
@Deprecated
public class ScrollAction extends TouchAction implements Action {

  private final int xOffset;
  private final int yOffset;

  public ScrollAction(TouchScreen touchScreen, Locatable locationProvider, int x, int y) {
    super(touchScreen, locationProvider);
    xOffset = x;
    yOffset = y;
  }

  public ScrollAction(TouchScreen touchScreen, int xOffset, int yOffset) {
    super(touchScreen, null);
    this.xOffset = xOffset;
    this.yOffset = yOffset;
  }

  @Override
  public void perform() {
    touchScreen.scroll(getActionLocation(), xOffset, yOffset);
  }

}
