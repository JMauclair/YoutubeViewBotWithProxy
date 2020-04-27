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

package org.openqa.selenium.build;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.os.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class BazelBuild {
  private static Logger log = Logger.getLogger(BazelBuild.class.getName());

  public void build(String target) {
    Path projectRoot = InProject.findProjectRoot();

    if (!Files.exists(projectRoot.resolve("Rakefile"))) {
      // we're not in dev mode
      return;
    }

    if (target == null || "".equals(target)) {
      throw new IllegalStateException("No targets specified");
    }
    log.info("\nBuilding " + target + " ...");

    CommandLine commandLine = new CommandLine("bazel", "build", target);
    commandLine.setWorkingDirectory(projectRoot.toAbsolutePath().toString());
    commandLine.copyOutputTo(System.err);
    commandLine.execute();

    if (!commandLine.isSuccessful()) {
      throw new WebDriverException("Build failed! " + target + "\n" + commandLine.getStdOut());
    }
  }
}
