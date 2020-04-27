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

package org.openqa.selenium.ie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.openqa.selenium.ie.InternetExplorerDriver.INITIAL_BROWSER_URL;
import static org.openqa.selenium.ie.InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS;
import static org.openqa.selenium.ie.InternetExplorerOptions.IE_OPTIONS;

import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Map;

public class InternetExplorerOptionsTest {

  @Test
  public void shouldAllowACapabilityToBeSet() {
    InternetExplorerOptions options = new InternetExplorerOptions();
    options.setCapability("cheese", "cake");

    assertThat(options.asMap()).containsEntry("cheese", "cake");
  }

  @Test
  public void shouldMirrorCapabilitiesForIeProperly() {
    String expected = "http://cheese.example.com";
    InternetExplorerOptions options = new InternetExplorerOptions()
        .withInitialBrowserUrl(expected);

    Map<String, Object> map = options.asMap();

    assertThat(map).containsEntry(INITIAL_BROWSER_URL, expected);
    assertThat(map).containsKey("se:ieOptions");
    assertThat(map.get("se:ieOptions")).asInstanceOf(MAP)
        .containsEntry(INITIAL_BROWSER_URL, expected);
  }

  @Test
  public void shouldMirrorCapabilitiesFromPassedInIeOptions() {
    InternetExplorerOptions toMirror = new InternetExplorerOptions()
        .introduceFlakinessByIgnoringSecurityDomains();

    // This is damn weird.
    InternetExplorerOptions options = new InternetExplorerOptions();
    options.setCapability("se:ieOptions", toMirror);

    assertThat(options.is(INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS)).isTrue();
  }

  @Test
  public void shouldPopulateIeOptionsFromExistingCapabilitiesWhichLackThem() {
    Capabilities caps = new ImmutableCapabilities(
        INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);

    InternetExplorerOptions options = new InternetExplorerOptions(caps);

    assertThat(options.is(INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS)).isTrue();
    assertThat(options.getCapability("se:ieOptions")).asInstanceOf(MAP)
        .containsEntry(INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
  }

  @Test
  public void shouldSurviveASerializationRoundTrip() {
    InternetExplorerOptions options = new InternetExplorerOptions()
        .withInitialBrowserUrl("http://www.cheese.com")
        .addCommandSwitches("--cake");

    String json = new Json().toJson(options);
    Capabilities capabilities = new Json().toType(json, Capabilities.class);

    assertThat(capabilities).isEqualTo(options);

    InternetExplorerOptions freshOptions = new InternetExplorerOptions(capabilities);

    assertThat(freshOptions).isEqualTo(options);
  }

  @Test
  public void shouldSetIeOptionsCapabilityWhenConstructedFromExistingCapabilities() {
    InternetExplorerOptions expected = new InternetExplorerOptions();
    expected.setCapability("requireWindowFocus", true);

    DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
    desiredCapabilities.setPlatform(Platform.WINDOWS);
    InternetExplorerOptions seen = new InternetExplorerOptions(desiredCapabilities);
    seen.setCapability("requireWindowFocus", true);

    assertThat(seen.getCapability(IE_OPTIONS)).isEqualTo(expected.getCapability(IE_OPTIONS));
  }
}
