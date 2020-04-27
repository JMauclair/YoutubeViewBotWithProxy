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

public class SessionTimesOutTest {

//  private RegistrationRequest req = new RegistrationRequest();
//  private Map<String, Object> app1 = new HashMap<>();
//
//  // create a request for a proxy that times out after 0.5 sec.
//  @Before
//  public void setup() {
//    app1.put(CapabilityType.APPLICATION_NAME, "app1");
//    req.getConfiguration().capabilities.add(new DesiredCapabilities(app1));
//    // a test is timed out is inactive for more than 1 sec.
//    req.getConfiguration().timeout = 1;
//    // every 0.1 sec, the proxy check is something has timed out.
//    req.getConfiguration().cleanUpCycle = 100;
//    req.getConfiguration().host = "localhost";
//  }
//
//  class MyRemoteProxyTimeout extends BaseRemoteProxy implements TimeoutListener {
//
//    public MyRemoteProxyTimeout(RegistrationRequest request, GridRegistry registry) {
//      super(request, registry);
//    }
//
//    @Override
//    public void beforeRelease(TestSession session) {
//    }
//  }
//
//  /**
//   * check that the proxy is freed after it times out.
//   */
//  @Test(timeout = 10000)
//  public void testTimeout() throws InterruptedException {
//
//    GridRegistry registry = DefaultGridRegistry.newInstance(new Hub(new GridHubConfiguration()));
//    RemoteProxy p1 = new MyRemoteProxyTimeout(req, registry);
//    p1.setupTimeoutListener();
//
//    try {
//      registry.add(p1);
//      RequestHandler newSessionRequest = GridHelper.createNewSessionHandler(registry, app1);
//
//      newSessionRequest.process();
//      TestSession session = newSessionRequest.getSession();
//      // wait for a timeout
//      Thread.sleep(1000);
//
//      RequestHandler newSessionRequest2 = GridHelper.createNewSessionHandler(registry, app1);
//      newSessionRequest2.process();
//      TestSession session2 = newSessionRequest2.getSession();
//      assertNotNull(session2);
//      assertNotSame(session, session2);
//    } finally {
//      registry.stop();
//    }
//  }
//
//  private static boolean timeoutDone = false;
//
//  class MyRemoteProxyTimeoutSlow extends BaseRemoteProxy implements TimeoutListener {
//
//    public MyRemoteProxyTimeoutSlow(RegistrationRequest request, GridRegistry registry) {
//      super(request, registry);
//    }
//
//    @Override
//    public void beforeRelease(TestSession session) {
//      try {
//        Thread.sleep(1000);
//        timeoutDone = true;
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//    }
//  }
//
//  @Ignore(value = "flaky in travis CI")
//  @Test(timeout = 20000)
//  public void testTimeoutSlow() throws InterruptedException {
//    GridRegistry registry = DefaultGridRegistry.newInstance(new Hub(new GridHubConfiguration()));
//    registry.getHub().getConfiguration().timeout = 1800;
//    registry.getHub().getConfiguration().cleanUpCycle = null;
//    RemoteProxy p1 = new MyRemoteProxyTimeoutSlow(req, registry);
//    p1.setupTimeoutListener();
//
//    try {
//      registry.add(p1);
//
//      RequestHandler newSessionRequest = GridHelper.createNewSessionHandler(registry, app1);
//      newSessionRequest.process();
//      TestSession session = newSessionRequest.getSession();
//      // timeout cleanup will start
//      Thread.sleep(1100);
//      // but the session finishes before the timeout cleanup finishes
//      registry.terminate(session, SessionTerminationReason.CLIENT_STOPPED_SESSION);
//
//      // wait to have the slow time out process finished
//      int i = 0;
//      while (!timeoutDone) {
//        if (i >= 4) {
//          throw new RuntimeException("should be true");
//        }
//        Thread.sleep(250);
//      }
//      assertTrue(timeoutDone);
//
//      RequestHandler newSessionRequest2 = GridHelper.createNewSessionHandler(registry, app1);
//      newSessionRequest2.process();
//      TestSession session2 = newSessionRequest2.getSession();
//      assertNotNull(session2);
//      assertNotEquals(session2, session);
//
//    } finally {
//      registry.stop();
//    }
//  }
//
//  class MyBuggyRemoteProxyTimeout extends BaseRemoteProxy implements TimeoutListener {
//
//    public MyBuggyRemoteProxyTimeout(RegistrationRequest request, GridRegistry registry) {
//      super(request, registry);
//    }
//
//    @Override
//    public void beforeRelease(TestSession session) {
//      throw new NullPointerException();
//    }
//  }
//
//  // a proxy throwing an exception will end up not releasing the resources.
//  @Test(timeout = 5000)
//  public void testTimeoutBug() throws InterruptedException {
//    final GridRegistry registry =
//        DefaultGridRegistry.newInstance(new Hub(new GridHubConfiguration()));
//    RemoteProxy p1 = new MyBuggyRemoteProxyTimeout(req, registry);
//    p1.setupTimeoutListener();
//
//    try {
//      registry.add(p1);
//
//      RequestHandler newSessionRequest = GridHelper.createNewSessionHandler(registry, app1);
//      newSessionRequest.process();
//
//      final RequestHandler newSessionRequest2 =
//          GridHelper.createNewSessionHandler(registry, app1);
//      new Thread(new Runnable() {  // Thread safety reviewed
//        @Override
//        public void run() {
//          // the request should never be processed because the
//          // resource is not released by the buggy proxy
//          newSessionRequest2.process();
//        }
//      }).start();
//
//      // wait for a timeout
//      Thread.sleep(500);
//      // the request has not been processed yet.
//      assertNull(newSessionRequest2.getServerSession());
//    } finally {
//      registry.stop();
//    }
//  }
//
//  class MyStupidConfig extends BaseRemoteProxy implements TimeoutListener {
//
//    public MyStupidConfig(RegistrationRequest request, GridRegistry registry) {
//      super(request, registry);
//    }
//
//    @Override
//    public void beforeRelease(TestSession session) {
//      session.put("FLAG", true);
//      session.put("MustSupportNullValue", null);
//      session.put(null, "MustSupportNullKey");
//    }
//  }
//
//  @Test(timeout = 10000)
//  public void stupidConfig() throws InterruptedException {
//    Object[][] configs = new Object[][]{
//        // correct config, just to check something happens
//        {1, 5},
//        // and invalid ones
//        {-1, 5}, {5, -1}, {-1, -1}, {0, 0}};
//    java.util.List<GridRegistry> registryList = new ArrayList<>();
//    try {
//      for (Object[] c : configs) {
//        // timeout is in seconds
//        int timeout = (Integer) c[0];
//        int cycle = (Integer) c[1];
//        GridRegistry registry =
//            DefaultGridRegistry.newInstance(new Hub(new GridHubConfiguration()));
//        registryList.add(registry);
//
//        RegistrationRequest req = new RegistrationRequest();
//        Map<String, Object> app1 = new HashMap<>();
//        app1.put(CapabilityType.APPLICATION_NAME, "app1");
//        req.getConfiguration().capabilities.add(new DesiredCapabilities(app1));
//        req.getConfiguration().timeout = timeout;
//        req.getConfiguration().cleanUpCycle = cycle;
//        req.getConfiguration().host = "localhost";
//
//        registry.getHub().getConfiguration().cleanUpCycle = cycle;
//        registry.getHub().getConfiguration().timeout = timeout;
//
//        final MyStupidConfig proxy = new MyStupidConfig(req, registry);
//        proxy.setupTimeoutListener();
//        registry.add(proxy);
//        RequestHandler newSessionRequest = GridHelper.createNewSessionHandler(registry, app1);
//        newSessionRequest.process();
//        TestSession session = newSessionRequest.getSession();
//        // wait -> timed out and released.
//        Thread.sleep(Math.max(1010, Math.min(0, timeout * 1000 + cycle)));
//        boolean shouldTimeout = timeout > 0 && cycle > 0;
//
//        if (shouldTimeout) {
//          System.out.println(String.format("Should timeout with this set timeout: %d seconds, cleanUpCycle: %d ms", timeout, cycle));
//          assertEquals(session.get("FLAG"), true);
//          assertNull(session.getSlot().getSession());
//        } else {
//          assertNull(session.get("FLAG"));
//          assertNotNull(session.getSlot().getSession());
//        }
//      }
//    } finally {
//      for (GridRegistry registry : registryList) {
//        registry.stop();
//      }
//    }
//
//  }

}
