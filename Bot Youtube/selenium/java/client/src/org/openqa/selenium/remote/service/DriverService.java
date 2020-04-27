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

package org.openqa.selenium.remote.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.openqa.selenium.Beta;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.net.UrlChecker;
import org.openqa.selenium.os.CommandLine;
import org.openqa.selenium.os.ExecutableFinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages the life and death of a native executable driver server.
 *
 * It is expected that the driver server implements the
 * <a href="https://github.com/SeleniumHQ/selenium/wiki/JsonWireProtocol">WebDriver Wire Protocol</a>.
 * In particular, it should implement /status command that is used to check if the server is alive.
 * In addition to this, it is supposed that the driver server implements /shutdown hook that is
 * used to stop the server.
 */
public class DriverService {
  protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);

  /**
   * The base URL for the managed server.
   */
  private final URL url;

  /**
   * Controls access to {@link #process}.
   */
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * A reference to the current child process. Will be {@code null} whenever this service is not
   * running. Protected by {@link #lock}.
   */
  protected CommandLine process = null;

  private final String executable;
  private final Duration timeout;
  private final List<String> args;
  private final Map<String, String> environment;
  private OutputStream outputStream = System.err;

  /**
  *
  * @param executable The driver executable.
  * @param port Which port to start the driver server on.
  * @param timeout Timeout waiting for driver server to start.
  * @param args The arguments to the launched server.
  * @param environment The environment for the launched server.
  * @throws IOException If an I/O error occurs.
  */
 protected DriverService(
     File executable,
     int port,
     Duration timeout,
     List<String> args,
     Map<String, String> environment) throws IOException {
   this.executable = executable.getCanonicalPath();
   this.timeout = timeout;
   this.args = args;
   this.environment = environment;

   this.url = getUrl(port);
 }

  protected List<String> getArgs() {
    return args;
  }

  protected Map<String, String> getEnvironment() {
   return environment;
 }

  protected URL getUrl(int port) throws IOException {
   return new URL(String.format("http://localhost:%d", port));
 }

  /**
   * @return The base URL for the managed driver server.
   */
  public URL getUrl() {
    return url;
  }

  /**
   *
   * @param exeName Name of the executable file to look for in PATH
   * @param exeProperty Name of a system property that specifies the path to the executable file
   * @param exeDocs The link to the driver documentation page
   * @param exeDownload The link to the driver download page
   *
   * @return The driver executable as a {@link File} object
   * @throws IllegalStateException If the executable not found or cannot be executed
   */
  protected static File findExecutable(
      String exeName,
      String exeProperty,
      String exeDocs,
      String exeDownload) {
    String defaultPath = new ExecutableFinder().find(exeName);
    String exePath = System.getProperty(exeProperty, defaultPath);
    checkState(exePath != null,
        "The path to the driver executable must be set by the %s system property;"
            + " for more information, see %s. "
            + "The latest version can be downloaded from %s",
            exeProperty, exeDocs, exeDownload);

    File exe = new File(exePath);
    checkExecutable(exe);
    return exe;
  }

  protected static void checkExecutable(File exe) {
    checkState(exe.exists(),
        "The driver executable does not exist: %s", exe.getAbsolutePath());
    checkState(!exe.isDirectory(),
        "The driver executable is a directory: %s", exe.getAbsolutePath());
    checkState(exe.canExecute(),
        "The driver is not executable: %s", exe.getAbsolutePath());
  }

  /**
   * Checks whether the driver child process is currently running.
   *
   * @return Whether the driver child process is still running.
   */
  public boolean isRunning() {
    lock.lock();
    try {
      return process != null && process.isRunning();
    } catch (IllegalThreadStateException e) {
      return true;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Starts this service if it is not already running. This method will block until the server has
   * been fully started and is ready to handle commands.
   *
   * @throws IOException If an error occurs while spawning the child process.
   * @see #stop()
   */
  public void start() throws IOException {
    lock.lock();
    try {
      if (process != null) {
        return;
      }
      process = new CommandLine(this.executable, args.toArray(new String[] {}));
      process.setEnvironmentVariables(environment);
      process.copyOutputTo(getOutputStream());
      process.executeAsync();

      CompletableFuture<Boolean> serverStarted = CompletableFuture.supplyAsync(() -> {
        waitUntilAvailable();
        return true;
      });

      CompletableFuture<Boolean> processFinished = CompletableFuture.supplyAsync(() -> {
        process.waitFor(getTimeout().toMillis());
        return false;
      });

      try {
        boolean started = (Boolean) CompletableFuture.anyOf(serverStarted, processFinished)
            .get(getTimeout().toMillis() * 2, TimeUnit.MILLISECONDS);
        if (!started) {
          process = null;
          throw new WebDriverException("Driver server process died prematurely.");
        }
      } catch (ExecutionException | TimeoutException e) {
        throw new WebDriverException("Timed out waiting for driver server to start.", e);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new WebDriverException("Timed out waiting for driver server to start.", e);
      }
    } finally {
      lock.unlock();
    }
  }

  protected Duration getTimeout() {
    return timeout;
  }

  protected void waitUntilAvailable() {
    try {
      URL status = new URL(url.toString() + "/status");
      new UrlChecker().waitUntilAvailable(getTimeout().toMillis(), TimeUnit.MILLISECONDS, status);
    } catch (MalformedURLException e) {
      throw new WebDriverException("Driver server status URL is malformed.", e);
    } catch (UrlChecker.TimeoutException e) {
      throw new WebDriverException("Timed out waiting for driver server to start.", e);
    }
  }

  /**
   * Stops this service if it is currently running. This method will attempt to block until the
   * server has been fully shutdown.
   *
   * @see #start()
   */
  public void stop() {
    lock.lock();

    WebDriverException toThrow = null;
    try {
      if (process == null) {
        return;
      }

      if (hasShutdownEndpoint()) {
        try {
          URL killUrl = new URL(url.toString() + "/shutdown");
          new UrlChecker().waitUntilUnavailable(3, SECONDS, killUrl);
        } catch (MalformedURLException e) {
          toThrow = new WebDriverException(e);
        } catch (UrlChecker.TimeoutException e) {
          toThrow = new WebDriverException("Timed out waiting for driver server to shutdown.", e);
        }
      }

      process.destroy();

      if (getOutputStream() instanceof FileOutputStream) {
        try {
          getOutputStream().close();
        } catch (IOException e) {
        }
      }
    } finally {
      process = null;
      lock.unlock();
    }

    if (toThrow != null) {
      throw toThrow;
    }
  }

  protected boolean hasShutdownEndpoint() {
    return true;
  }

  public void sendOutputTo(OutputStream outputStream) {
    this.outputStream = Preconditions.checkNotNull(outputStream);
  }

  protected OutputStream getOutputStream() {
    return outputStream;
  }

  public abstract static class Builder<DS extends DriverService, B extends Builder<?, ?>> {

    private int port = 0;
    private File exe = null;
    private Map<String, String> environment = ImmutableMap.of();
    private File logFile;
    private Duration timeout;

    /**
     * Provides a measure of how strongly this {@link DriverService} supports the given
     * {@code capabilities}. A score of 0 or less indicates that this {@link DriverService} does not
     * support instances of {@link org.openqa.selenium.WebDriver} that require {@code capabilities}.
     * Typically, the score is generated by summing the number of capabilities that the driver
     * service directly supports that are unique to the driver service (that is, things like
     * "{@code proxy}" don't tend to count to the score).
     */
    public abstract int score(Capabilities capabilities);

    /**
     * Sets which driver executable the builder will use.
     *
     * @param file The executable to use.
     * @return A self reference.
     */
    @SuppressWarnings("unchecked")
    public B usingDriverExecutable(File file) {
      checkNotNull(file);
      checkExecutable(file);
      this.exe = file;
      return (B) this;
    }

    /**
     * Sets which port the driver server should be started on. A value of 0 indicates that any
     * free port may be used.
     *
     * @param port The port to use; must be non-negative.
     * @return A self reference.
     */
    public B usingPort(int port) {
      checkArgument(port >= 0, "Invalid port number: %s", port);
      this.port = port;
      return (B) this;
    }

    protected int getPort() {
      return port;
    }

    /**
     * Configures the driver server to start on any available port.
     *
     * @return A self reference.
     */
    public B usingAnyFreePort() {
      this.port = 0;
      return (B) this;
    }

    /**
     * Defines the environment for the launched driver server. These
     * settings will be inherited by every browser session launched by the
     * server.
     *
     * @param environment A map of the environment variables to launch the
     *     server with.
     * @return A self reference.
     */
    @Beta
    public B withEnvironment(Map<String, String> environment) {
      this.environment = ImmutableMap.copyOf(environment);
      return (B) this;
    }

    /**
     * Configures the driver server to write log to the given file.
     *
     * @param logFile A file to write log to.
     * @return A self reference.
     */
    public B withLogFile(File logFile) {
      this.logFile = logFile;
      return (B) this;
    }

    protected File getLogFile() {
      return logFile;
    }

    /**
     * Configures the timeout waiting for driver server to start.
     *
     * @return A self reference.
     */
    public B withTimeout(Duration timeout) {
      this.timeout = timeout;
      return (B) this;
    }

    protected Duration getDefaultTimeout() {
      return DEFAULT_TIMEOUT;
    }

    /**
     * Creates a new service to manage the driver server. Before creating a new service, the
     * builder will find a port for the server to listen to.
     *
     * @return The new service object.
     */
    public DS build() {
      if (port == 0) {
        port = PortProber.findFreePort();
      }

      if (exe == null) {
        exe = findDefaultExecutable();
      }

      if (timeout == null) {
        timeout = getDefaultTimeout();
      }

      List<String> args = createArgs();

      DS service = createDriverService(exe, port, timeout, args, environment);
      port = 0; // reset port to allow reusing this builder

      return service;
    }

    protected abstract File findDefaultExecutable();

    protected abstract List<String> createArgs();

    protected abstract DS createDriverService(File exe, int port, Duration timeout, List<String> args,
        Map<String, String> environment);
  }
}
