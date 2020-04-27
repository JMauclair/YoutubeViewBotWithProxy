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
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.interactions.internal.TouchAction;

/**
 * Creates a move gesture.
 */
@Deprecated
public class MoveAction extends TouchAction implements Action {

  private final int x;
  private final int y;

  public MoveAction(TouchScreen touchScreen, int x, int y) {
    super(touchScreen, null);
    this.x = x;
    this.y = y;
  }

  @Override
  public void perform() {
    touchScreen.move(x, y);
  }

}
