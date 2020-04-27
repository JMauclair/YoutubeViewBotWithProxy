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

package org.openqa.grid.internal;

public class RemoteProxySlowSetup {
//
//  private RemoteProxy p1;
//  private RemoteProxy p2;
//
//  private GridRegistry registry;
//
//  @Before
//  public void setup() {
//    registry = DefaultGridRegistry.newInstance(new Hub(new GridHubConfiguration()));
//    // create 2 proxy that are equal and have a slow onRegistration
//    // p1.equals(p2) = true
//    GridNodeConfiguration config1 = new GridNodeConfiguration();
//    RegistrationRequest req1 = new RegistrationRequest(config1);
//    p1 = new SlowRemoteSetup(req1,registry);
//
//    GridNodeConfiguration config2 = new GridNodeConfiguration();
//    RegistrationRequest req2 = new RegistrationRequest(config2);
//    p2 = new SlowRemoteSetup(req2,registry);
//  }
//
//  // the first onRegistration should be executed, but the 2nd shouldn't.
//  @Test
//  public void addDup() throws InterruptedException {
//    new Thread(new Runnable() { // Thread safety reviewed
//      @Override
//      public void run() {
//        registry.add(p1);
//      }
//    }).start();
//    new Thread(new Runnable() { // Thread safety reviewed
//      @Override
//      public void run() {
//        registry.add(p2);
//      }
//    }).start();
//    Thread.sleep(1500);
//
//    // check that the beforeRegistration has only been called once.
//    assertFalse(SlowRemoteSetup.error);
//    // and there is only 1 proxy registered at the end.
//    assertEquals(1, registry.getAllProxies().size());
//
//  }
//
//  @After
//  public void teardown() {
//    registry.stop();
//  }
//
//  private static class SlowRemoteSetup extends BaseRemoteProxy implements RegistrationListener {
//
//    private boolean flag = false;
//    private static boolean error = false;
//
//    // update flag to true. It should happen only once, so if flag is already
//    // true, set error to true.
//    private synchronized void updateFlag() {
//      if (flag) {
//        error = true;
//      }
//      flag = true;
//    }
//
//    public SlowRemoteSetup(RegistrationRequest req,GridRegistry registry) {
//      super(req, registry);
//    }
//
//    @Override
//    public void beforeRegistration() {
//      try {
//        updateFlag();
//        Thread.sleep(1000);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//      return true;
//    }
//
//    @Override
//    public int hashCode() {
//      return 42;
//    }
//
//  }
}
