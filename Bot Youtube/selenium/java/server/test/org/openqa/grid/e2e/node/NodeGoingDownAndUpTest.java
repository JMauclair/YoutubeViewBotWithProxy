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

package org.openqa.grid.e2e.node;

public class NodeGoingDownAndUpTest {

//  private Hub hub;
//  private GridRegistry registry;
//  private SelfRegisteringRemote remote;
//  private Wait<Object> wait = new FluentWait<Object>("").withTimeout(Duration.ofSeconds(30));
//
//  @Before
//  public void prepare() {
//    hub = GridTestHelper.getHub();
//    registry = hub.getRegistry();
//
//    remote = GridTestHelper.getRemoteWithoutCapabilities(hub.getUrl(), GridRole.NODE);
//
//    // check if the node is up every 900 ms
//    remote.getConfiguration().nodePolling = 900;
//    // unregister the proxy is it's down for more than 10 sec in a row.
//    remote.getConfiguration().unregisterIfStillDownAfter = 10000;
//    // mark as down after 3 tries
//    remote.getConfiguration().downPollingLimit = 3;
//    // limit connection and socket timeout for node alive check up to
//    remote.getConfiguration().nodeStatusCheckTimeout = 100;
//    // add browser
//    remote.addBrowser(GridTestHelper.getDefaultBrowserCapability(), 1);
//
//    remote.setRemoteServer(new SeleniumServer(remote.getConfiguration()));
//    remote.startRemoteServer();
//    remote.sendRegistrationRequest();
//    RegistryTestHelper.waitForNode(registry, 1);
//  }
//
//  @Test
//  public void markdown() {
//    // should be up
//    for (RemoteProxy proxy : registry.getAllProxies()) {
//      wait.until(isUp((DefaultRemoteProxy) proxy));
//    }
//
//    // killing the nodes
//    remote.stopRemoteServer();
//
//    // should be down
//    for (RemoteProxy proxy : registry.getAllProxies()) {
//      wait.until(isDown((DefaultRemoteProxy) proxy));
//    }
//
//    // and back up
//    remote.setRemoteServer(new SeleniumServer(remote.getConfiguration()));
//    remote.startRemoteServer();
//
//    // should be up
//    for (RemoteProxy proxy : registry.getAllProxies()) {
//      wait.until(isUp((DefaultRemoteProxy) proxy));
//    }
//  }
//
//  private Function<Object, Boolean> isUp(final DefaultRemoteProxy proxy) {
//    return input -> !proxy.isDown();
//  }
//
//  private Function<Object, Boolean> isDown(final DefaultRemoteProxy proxy) {
//    return input -> proxy.isDown();
//  }
//
//  @After
//  public void stop() {
//    hub.stop();
//    remote.stopRemoteServer();
//  }
}
