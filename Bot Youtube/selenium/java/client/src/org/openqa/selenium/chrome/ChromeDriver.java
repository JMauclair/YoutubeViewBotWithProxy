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

package org.openqa.selenium.chrome;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.chromium.ChromiumDriverCommandExecutor;
import org.openqa.selenium.html5.LocationContext;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * A {@link WebDriver} implementation that controls a Chrome browser running on the local machine.
 * This class is provided as a convenience for easily testing the Chrome browser. The control server
 * which each instance communicates with will live and die with the instance.
 *
 * To avoid unnecessarily restarting the ChromeDriver server with each instance, use a
 * {@link RemoteWebDriver} coupled with the desired {@link ChromeDriverService}, which is managed
 * separately. For example: <pre>{@code
 *
 * import static org.junit.Assert.assertEquals;
 *
 * import org.junit.*;
 * import org.junit.runner.RunWith;
 * import org.junit.runners.JUnit4;
 * import org.openqa.selenium.chrome.ChromeDriverService;
 * import org.openqa.selenium.remote.DesiredCapabilities;
 * import org.openqa.selenium.remote.RemoteWebDriver;
 *
 * {@literal @RunWith(JUnit4.class)}
 * public class ChromeTest extends TestCase {
 *
 *   private static ChromeDriverService service;
 *   private WebDriver driver;
 *
 *   {@literal @BeforeClass}
 *   public static void createAndStartService() {
 *     service = new ChromeDriverService.Builder()
 *         .usingDriverExecutable(new File("path/to/my/chromedriver.exe"))
 *         .usingAnyFreePort()
 *         .build();
 *     service.start();
 *   }
 *
 *   {@literal @AfterClass}
 *   public static void createAndStopService() {
 *     service.stop();
 *   }
 *
 *   {@literal @Before}
 *   public void createDriver() {
 *     driver = new RemoteWebDriver(service.getUrl(),
 *         DesiredCapabilities.chrome());
 *   }
 *
 *   {@literal @After}
 *   public void quitDriver() {
 *     driver.quit();
 *   }
 *
 *   {@literal @Test}
 *   public void testGoogleSearch() {
 *     driver.get("http://www.google.com");
 *     WebElement searchBox = driver.findElement(By.name("q"));
 *     searchBox.sendKeys("webdriver");
 *     searchBox.quit();
 *     assertEquals("webdriver - Google Search", driver.getTitle());
 *   }
 * }
 * }</pre>
 *
 * Note that unlike ChromeDriver, RemoteWebDriver doesn't directly implement
 * role interfaces such as {@link LocationContext} and {@link WebStorage}.
 * Therefore, to access that functionality, it needs to be
 * {@link org.openqa.selenium.remote.Augmenter augmented} and then cast
 * to the appropriate interface.
 *
 * @see ChromeDriverService#createDefaultService
 */
public class ChromeDriver extends ChromiumDriver {

  /**
   * Creates a new ChromeDriver using the {@link ChromeDriverService#createDefaultService default}
   * server configuration.
   *
   * @see #ChromeDriver(ChromeDriverService, ChromeOptions)
   */
  public ChromeDriver() {
    this(ChromeDriverService.createDefaultService(), new ChromeOptions());
  }

  /**
   * Creates a new ChromeDriver instance. The {@code service} will be started along with the driver,
   * and shutdown upon calling {@link #quit()}.
   *
   * @param service The service to use.
   * @see RemoteWebDriver#RemoteWebDriver(org.openqa.selenium.remote.CommandExecutor, Capabilities)
   */
  public ChromeDriver(ChromeDriverService service) {
    this(service, new ChromeOptions());
  }

  /**
   * Creates a new ChromeDriver instance. The {@code capabilities} will be passed to the
   * ChromeDriver service.
   *
   * @param capabilities The capabilities required from the ChromeDriver.
   * @see #ChromeDriver(ChromeDriverService, Capabilities)
   * @deprecated Use {@link ChromeDriver(ChromeOptions)} instead.
   */
  @Deprecated
  public ChromeDriver(Capabilities capabilities) {
    this(ChromeDriverService.createDefaultService(), capabilities);
  }

  /**
   * Creates a new ChromeDriver instance with the specified options.
   *
   * @param options The options to use.
   * @see #ChromeDriver(ChromeDriverService, ChromeOptions)
   */
  public ChromeDriver(ChromeOptions options) {
    this(ChromeDriverService.createDefaultService(), options);
  }

  /**
   * Creates a new ChromeDriver instance with the specified options. The {@code service} will be
   * started along with the driver, and shutdown upon calling {@link #quit()}.
   *
   * @param service The service to use.
   * @param options The options to use.
   */
  public ChromeDriver(ChromeDriverService service, ChromeOptions options) {
    this(service, (Capabilities) options);
  }

  /**
   * Creates a new ChromeDriver instance. The {@code service} will be started along with the
   * driver, and shutdown upon calling {@link #quit()}.
   *
   * @param service      The service to use.
   * @param capabilities The capabilities required from the ChromeDriver.
   * @deprecated Use {@link ChromeDriver(ChromeDriverService, ChromeOptions)} instead.
   */
  @Deprecated
  public ChromeDriver(ChromeDriverService service, Capabilities capabilities) {
    super(new ChromiumDriverCommandExecutor("goog", service), capabilities, ChromeOptions.CAPABILITY);
  }

}
