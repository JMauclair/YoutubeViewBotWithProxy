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

import com.google.common.collect.ImmutableMap;

import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.interactions.Mouse;

import java.util.HashMap;
import java.util.Map;

/**
 * Executes wire commands for mouse interaction.
 */
public class RemoteMouse implements Mouse {
  protected final ExecuteMethod executor;

  public RemoteMouse(ExecuteMethod executor) {
    this.executor = executor;
  }


  protected Map<String, Object> paramsFromCoordinates(Coordinates where) {
    Map<String, Object> params = new HashMap<>();

    if (where != null) {
      String id = (String) where.getAuxiliary();
      params.put("element", id);
    }

    return params;
  }

  protected void moveIfNeeded(Coordinates where) {
    if (where != null) {
      mouseMove(where);
    }
  }

  @Override
  public void click(Coordinates where) {
    moveIfNeeded(where);

    executor.execute(DriverCommand.CLICK, ImmutableMap.of("button", 0));
  }

  @Override
  public void contextClick(Coordinates where) {
    moveIfNeeded(where);

    executor.execute(DriverCommand.CLICK, ImmutableMap.of("button", 2));
  }

  @Override
  public void doubleClick(Coordinates where) {
    moveIfNeeded(where);

    executor.execute(DriverCommand.DOUBLE_CLICK, ImmutableMap.of());
  }

  @Override
  public void mouseDown(Coordinates where) {
    moveIfNeeded(where);

    executor.execute(DriverCommand.MOUSE_DOWN, ImmutableMap.of());
  }

  @Override
  public void mouseUp(Coordinates where) {
    moveIfNeeded(where);

    executor.execute(DriverCommand.MOUSE_UP, ImmutableMap.of());
  }

  @Override
  public void mouseMove(Coordinates where) {
    Map<String, Object> moveParams = paramsFromCoordinates(where);

    executor.execute(DriverCommand.MOVE_TO, moveParams);
  }

  @Override
  public void mouseMove(Coordinates where, long xOffset, long yOffset) {
    Map<String, Object> moveParams = paramsFromCoordinates(where);
    moveParams.put("xoffset", xOffset);
    moveParams.put("yoffset", yOffset);

    executor.execute(DriverCommand.MOVE_TO, moveParams);
  }
}
