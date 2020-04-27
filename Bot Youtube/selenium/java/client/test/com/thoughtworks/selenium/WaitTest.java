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

package com.thoughtworks.selenium;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.thoughtworks.selenium.Wait.WaitTimedOutException;

import org.junit.Before;
import org.junit.Test;

public class WaitTest {

  private long finished;
  private long now;
  private int tries = 0;

  @Before
  public void setUp() {
    now = System.currentTimeMillis();
  }

  @Test
  public void testUntil() {
    finished = now + 500l;
    new Wait() {
      @Override
      public boolean until() {
        tries++;
        return System.currentTimeMillis() > finished;
      }
    }.wait("clock stopped");
    assertTrue("didn't try enough times: " + tries, tries > 1);
  }

  @Test
  public void testUntilWithWaitTakingString() {
    finished = now + 500l;
    new Wait("a message to be shown if wait times out") {
      @Override
      public boolean until() {
        tries++;
        return System.currentTimeMillis() > finished;
      }
    };
    assertTrue("didn't try enough times: " + tries, tries > 1);
  }

  @Test
  public void testTimedOut() {
    finished = now + 5000l;
    try {
      new Wait() {
        @Override
        public boolean until() {
          tries++;
          return System.currentTimeMillis() > finished;
        }
      }.wait("timed out as expected", 500, 50);
      fail("expected timeout");
    } catch (WaitTimedOutException e) {
      long waited = System.currentTimeMillis() - now;
      assertTrue("didn't wait long enough:" + waited, waited >= 500);
      assertTrue("didn't try enough times: " + tries, tries > 7);
    }
  }

}
