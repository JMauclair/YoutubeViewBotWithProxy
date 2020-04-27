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

package org.openqa.selenium.server.htmlrunner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Console;
import com.google.common.collect.ImmutableMap;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.grid.config.MapConfig;
import org.openqa.selenium.grid.server.BaseServerOptions;
import org.openqa.selenium.grid.server.Server;
import org.openqa.selenium.grid.web.NoHandler;
import org.openqa.selenium.grid.web.PathResource;
import org.openqa.selenium.grid.web.ResourceHandler;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.jre.server.JreServer;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.http.Route;
import org.openqa.selenium.safari.SafariDriver;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Runs HTML Selenium test suites.
 */
public class HTMLLauncher {

  //    java -jar selenium-server-standalone-<version-number>.jar -htmlSuite "*firefox"
  //    "http://www.google.com" "c:\absolute\path\to\my\HTMLSuite.html"
  //    "c:\absolute\path\to\my\results.html"
  private static Logger log = Logger.getLogger(HTMLLauncher.class.getName());

  private Server<?> server;

  /**
   * Launches a single HTML Selenium test suite.
   *
   * @param browser - the browserString ("*firefox", "*iexplore" or an executable path)
   * @param startURL - the start URL for the browser
   * @param suiteURL - the relative URL to the HTML suite
   * @param outputFile - The file to which we'll output the HTML results
   * @param timeoutInSeconds - the amount of time (in seconds) to wait for the browser to finish
   * @return PASS or FAIL
   * @throws IOException if we can't write the output file
   */
  public String runHTMLSuite(
    String browser,
    String startURL,
    String suiteURL,
    File outputFile,
    long timeoutInSeconds,
    String userExtensions) throws IOException {
    File parent = outputFile.getParentFile();
    if (parent != null && !parent.exists()) {
      parent.mkdirs();
    }
    if (outputFile.exists() && !outputFile.canWrite()) {
      throw new IOException("Can't write to outputFile: " + outputFile.getAbsolutePath());
    }
    long timeoutInMs = 1000L * timeoutInSeconds;
    if (timeoutInMs < 0) {
      log.warning("Looks like the timeout overflowed, so resetting it to the maximum.");
      timeoutInMs = Long.MAX_VALUE;
    }

    WebDriver driver = null;
    try {
      driver = createDriver(browser);
      URL suiteUrl = determineSuiteUrl(startURL, suiteURL);

      driver.get(suiteUrl.toString());
      Selenium selenium = new WebDriverBackedSelenium(driver, startURL);
      selenium.setTimeout(String.valueOf(timeoutInMs));
      if (userExtensions != null) {
        selenium.setExtensionJs(userExtensions);
      }
      List<WebElement> allTables = driver.findElements(By.id("suiteTable"));
      if (allTables.isEmpty()) {
        throw new RuntimeException("Unable to find suite table: " + driver.getPageSource());
      }
      Results results = new CoreTestSuite(suiteUrl.toString()).run(driver, selenium, new URL(startURL));

      HTMLTestResults htmlResults = results.toSuiteResult();
      try (Writer writer = Files.newBufferedWriter(outputFile.toPath())) {
        htmlResults.write(writer);
      }

      return results.isSuccessful() ? "PASSED" : "FAILED";
    } finally {
      if (server != null) {
        try {
          server.stop();
        } catch (Exception e) {
          // Nothing sane to do. Log the error and carry on
          log.log(Level.INFO, "Exception shutting down server. You may ignore this.", e);
        }
      }

      if (driver != null) {
        driver.quit();
      }
    }
  }

  private URL determineSuiteUrl(String startURL, String suiteURL) throws IOException {
    if (suiteURL.startsWith("https://") || suiteURL.startsWith("http://")) {
      return verifySuiteUrl(new URL(suiteURL));
    }

    // Is the suiteURL a file?
    Path path = Paths.get(suiteURL).toAbsolutePath();
    if (Files.exists(path)) {
      // Not all drivers can read files from the disk, so we need to host the suite somewhere.
      int port = PortProber.findFreePort();
      BaseServerOptions options = new BaseServerOptions(
        new MapConfig(ImmutableMap.of("server", ImmutableMap.of("port", port))));

      Json json = new Json();

      server = new JreServer(
        options,
        Route.prefix("/test")
          .to(Route.combine(new ResourceHandler(new PathResource(path.getParent()))))
          .fallbackTo(() -> new NoHandler(json)));

      PortProber.waitForPortUp(port, 15, SECONDS);

      URL serverUrl = server.getUrl();
      return new URL(serverUrl.getProtocol(), serverUrl.getHost(), serverUrl.getPort(),
                     "/tests/");
    }

    // Well then, it must be a URL relative to whatever the browserUrl. Probe and find out.
    URL browser = new URL(startURL);
    return verifySuiteUrl(new URL(browser, suiteURL));
  }

  private URL verifySuiteUrl(URL url) throws IOException {
    // Now probe.
    URLConnection connection = url.openConnection();
    if (!(connection instanceof HttpURLConnection)) {
      throw new IOException("The HTMLLauncher only supports relative HTTP URLs");
    }
    HttpURLConnection httpConnection = (HttpURLConnection) connection;
    httpConnection.setInstanceFollowRedirects(true);
    httpConnection.setRequestMethod("HEAD");
    int responseCode = httpConnection.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_OK) {
      throw new IOException("Invalid suite URL: " + url);
    }
    return url;
 }

  private static String printUsage(JCommander commander) {
    StringBuilder usage = new StringBuilder();
    commander.setConsole(new Console() {
      @Override
      public void print(String msg) {
        usage.append(msg);
      }

      @Override
      public void println(String msg) {
        usage.append(msg).append("\n");
      }

      @Override
      public char[] readPassword(boolean echoInput) {
        throw new UnsupportedOperationException("readPassword");
      }
    });
    commander.usage();

    return usage.toString();
  }

  public static int mainInt(String... args) throws Exception {
    Args processed = new Args();
    JCommander jCommander = new JCommander(processed);
    jCommander.setCaseSensitiveOptions(false);
    try {
      jCommander.parse(args);
    } catch (ParameterException ex) {
      System.err.print(ex.getMessage() + "\n" + printUsage(jCommander));
      return 0;
    }

    if (processed.help) {
      System.err.print(printUsage(jCommander));
      return 0;
    }

    warnAboutLegacyOptions(processed);

    Path resultsPath = Paths.get(processed.htmlSuite.get(3));
    Files.createDirectories(resultsPath);

    String suite = processed.htmlSuite.get(2);
    String startURL = processed.htmlSuite.get(1);
    String[] browsers = new String[] {processed.htmlSuite.get(0)};

    HTMLLauncher launcher = new HTMLLauncher();

    boolean passed = true;
    for (String browser : browsers) {
      // Turns out that Windows doesn't like "*" in a path name
      String reportFileName = browser.contains(" ") ? browser.substring(0, browser.indexOf(' ')) : browser;
      File results = resultsPath.resolve(reportFileName.substring(1) + ".results.html").toFile();
      String result = "FAILED";

      try {
        long timeout;
        try {
          timeout = Long.parseLong(processed.timeout);
        } catch (NumberFormatException e) {
          System.err.println("Timeout does not appear to be a number: " + processed.timeout);
          return -2;
        }
        result = launcher.runHTMLSuite(browser, startURL, suite, results, timeout, processed.userExtensions);
        passed &= "PASSED".equals(result);
      } catch (Throwable e) {
        log.log(Level.WARNING, "Test of browser failed: " + browser, e);
        passed = false;
      }
    }

    return passed ? 1 : 0;
  }

  private static void warnAboutLegacyOptions(Args processed) {
    if (processed.multiWindow) {
      System.err.println("Multi-window mode is no longer used as an option and will be ignored.");
    }

    if (processed.port != 0) {
      System.err.println("Port is no longer used as an option and will be ignored.");
    }

    if (processed.trustAllSSLCertificates) {
      System.err.println("Trusting all ssl certificates is no longer a user-settable option.");
    }
  }

  public static void main(String[] args) throws Exception {
    System.exit(mainInt(args));
  }

  private WebDriver createDriver(String browser) {
    String[] parts = browser.split(" ", 2);
    browser = parts[0];
    switch (browser) {
      case "*chrome":
      case "*firefox":
      case "*firefoxproxy":
      case "*firefoxchrome":
      case "*pifirefox":
        FirefoxOptions options = new FirefoxOptions().setLegacy(false);
        if (parts.length > 1) {
          options.setBinary(parts[1]);
        }
        return new FirefoxDriver(options);

      case "*iehta":
      case "*iexplore":
      case "*iexploreproxy":
      case "*piiexplore":
        return new InternetExplorerDriver();

      case "*googlechrome":
        return new ChromeDriver();

      case "*MicrosoftEdge":
        return new EdgeDriver();

      case "*opera":
      case "*operablink":
        return new OperaDriver();

      case "*safari":
      case "*safariproxy":
        return new SafariDriver();

      default:
        throw new RuntimeException("Unrecognized browser: " + browser);
    }
  }

  public static class Args {
    @Parameter(
      names = "-htmlSuite",
      required = true,
      arity = 4,
      description = "Run an HTML Suite: \"*browser\" \"http://baseUrl.com\" \"path\\to\\HTMLSuite.html\" \"path\\to\\report\\dir\"")
    private List<String> htmlSuite;

    @Parameter(
      names = "-timeout",
      description = "Timeout to use in seconds")
    private String timeout = "30";

    @Parameter(
      names = "-userExtensions",
      description = "User extensions to attempt to use."
    )
    private String userExtensions;

    @Parameter(
      names = "-multiwindow",
      hidden = true)
    private boolean multiWindow = true;

    @Parameter(
      names = "-port",
      hidden = true)
    private Integer port = 0;

    @Parameter(
      names = "-trustAllSSLCertificates",
      hidden = true)
    private boolean trustAllSSLCertificates;

    @Parameter(
      names = {"-help", "--help", "-h"},
      description = "This help message",
      help = true)
    private boolean help;
  }
}
