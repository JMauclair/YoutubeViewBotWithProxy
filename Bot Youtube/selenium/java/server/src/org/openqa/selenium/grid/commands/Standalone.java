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

package org.openqa.selenium.grid.commands;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import io.opentelemetry.trace.Tracer;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.cli.CliCommand;
import org.openqa.selenium.grid.TemplateGridCommand;
import org.openqa.selenium.events.EventBus;
import org.openqa.selenium.grid.config.Config;
import org.openqa.selenium.grid.distributor.Distributor;
import org.openqa.selenium.grid.distributor.local.LocalDistributor;
import org.openqa.selenium.grid.docker.DockerFlags;
import org.openqa.selenium.grid.docker.DockerOptions;
import org.openqa.selenium.grid.log.LoggingOptions;
import org.openqa.selenium.grid.node.Node;
import org.openqa.selenium.grid.node.config.NodeOptions;
import org.openqa.selenium.grid.node.local.LocalNode;
import org.openqa.selenium.grid.router.Router;
import org.openqa.selenium.grid.server.BaseServerFlags;
import org.openqa.selenium.grid.server.BaseServerOptions;
import org.openqa.selenium.grid.server.EventBusFlags;
import org.openqa.selenium.grid.server.EventBusOptions;
import org.openqa.selenium.grid.server.NetworkOptions;
import org.openqa.selenium.grid.server.Server;
import org.openqa.selenium.grid.sessionmap.SessionMap;
import org.openqa.selenium.grid.sessionmap.local.LocalSessionMap;
import org.openqa.selenium.grid.web.CombinedHandler;
import org.openqa.selenium.grid.web.RoutableHttpClientFactory;
import org.openqa.selenium.net.NetworkUtils;
import org.openqa.selenium.netty.server.NettyServer;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.Route;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.logging.Logger;

import static org.openqa.selenium.remote.http.Route.combine;

@AutoService(CliCommand.class)
public class Standalone extends TemplateGridCommand {

  private static final Logger LOG = Logger.getLogger("selenium");

  @Override
  public String getName() {
    return "standalone";
  }

  @Override
  public String getDescription() {
    return "The selenium server, running everything in-process.";
  }

  @Override
  protected Set<Object> getFlagObjects() {
    return ImmutableSet.of(
      new BaseServerFlags(),
      new DockerFlags(),
      new EventBusFlags(),
      new StandaloneFlags());
  }

  @Override
  protected String getSystemPropertiesConfigPrefix() {
    return "selenium";
  }

  @Override
  protected Config getDefaultConfig() {
    return new DefaultStandaloneConfig();
  }

  @Override
  protected void execute(Config config) {
    LoggingOptions loggingOptions = new LoggingOptions(config);
    Tracer tracer = loggingOptions.getTracer();

    EventBusOptions events = new EventBusOptions(config);
    EventBus bus = events.getEventBus();

    String hostName;
    try {
      hostName = new NetworkUtils().getNonLoopbackAddressOfThisMachine();
    } catch (WebDriverException e) {
      hostName = "localhost";
    }

    int port = config.getInt("server", "port")
      .orElseThrow(() -> new IllegalArgumentException("No port to use configured"));
    URI localhost;
    URL localhostURL;
    try {
      localhost = new URI("http", null, hostName, port, null, null, null);
      localhostURL = localhost.toURL();
    } catch (URISyntaxException | MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }

    NetworkOptions networkOptions = new NetworkOptions(config);
    CombinedHandler combinedHandler = new CombinedHandler();
    HttpClient.Factory clientFactory = new RoutableHttpClientFactory(
      localhostURL,
      combinedHandler,
      networkOptions.getHttpClientFactory(tracer));

    SessionMap sessions = new LocalSessionMap(tracer, bus);
    combinedHandler.addHandler(sessions);
    Distributor distributor = new LocalDistributor(tracer, bus, clientFactory, sessions, null);
    combinedHandler.addHandler(distributor);
    Router router = new Router(tracer, clientFactory, sessions, distributor);
    HttpHandler httpHandler = combine(router, Route.prefix("/wd/hub").to(combine(router)));

    LocalNode.Builder nodeBuilder = LocalNode.builder(
      tracer,
      bus,
      clientFactory,
      localhost,
      null)
      .maximumConcurrentSessions(Runtime.getRuntime().availableProcessors() * 3);

    new NodeOptions(config).configure(tracer, clientFactory, nodeBuilder);
    new DockerOptions(config).configure(tracer, clientFactory, nodeBuilder);

    Node node = nodeBuilder.build();
    combinedHandler.addHandler(node);
    distributor.add(node);

    Server<?> server = new NettyServer(new BaseServerOptions(config), httpHandler);
    server.start();

    BuildInfo info = new BuildInfo();
    LOG.info(String.format(
      "Started Selenium standalone %s (revision %s): %s",
      info.getReleaseLabel(),
      info.getBuildRevision(),
      server.getUrl()));
  }
}
