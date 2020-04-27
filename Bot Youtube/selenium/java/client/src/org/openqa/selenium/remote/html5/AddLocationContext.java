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

package org.openqa.selenium.remote.html5;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.html5.LocationContext;
import org.openqa.selenium.remote.AugmenterProvider;
import org.openqa.selenium.remote.InterfaceImplementation;

import java.lang.reflect.InvocationTargetException;

public class AddLocationContext implements AugmenterProvider {

  @Override
  public Class<?> getDescribedInterface() {
    return LocationContext.class;
  }

  @Override
  public InterfaceImplementation getImplementation(Object value) {
    return (executeMethod, self, method, args) -> {
      LocationContext context = new RemoteLocationContext(executeMethod);
      try {
        return method.invoke(context, args);
      } catch (IllegalAccessException e) {
        throw new WebDriverException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e.getCause());
      }
    };
  }

}
