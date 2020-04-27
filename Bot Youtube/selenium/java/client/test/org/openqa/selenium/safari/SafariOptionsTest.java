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

package org.openqa.selenium.safari;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.remote.CapabilityType;

import java.util.HashMap;
import java.util.Map;

public class SafariOptionsTest {

  @Test
  public void roundTrippingToCapabilitiesAndBackWorks() {
    SafariOptions expected = new SafariOptions().setUseTechnologyPreview(true);

    // Convert to a Map so we can create a standalone capabilities instance, which we then use to
    // create a new set of options. This is the round trip, ladies and gentlemen.
    SafariOptions seen = new SafariOptions(new ImmutableCapabilities(expected.asMap()));

    assertThat(seen).isEqualTo(expected);
  }

  @Test
  public void canConstructFromCapabilities() {
    Map<String, Object> embeddedOptions = new HashMap<>();
    embeddedOptions.put("technologyPreview", true);

    SafariOptions options = new SafariOptions();
    assertThat(options.getUseTechnologyPreview()).isFalse();

    options = new SafariOptions(new ImmutableCapabilities(SafariOptions.CAPABILITY, embeddedOptions));
    assertThat(options.getUseTechnologyPreview()).isTrue();

    embeddedOptions.put("technologyPreview", false);
    options = new SafariOptions(new ImmutableCapabilities(SafariOptions.CAPABILITY, embeddedOptions));
    assertThat(options.getUseTechnologyPreview()).isFalse();

    options = new SafariOptions(new ImmutableCapabilities(CapabilityType.BROWSER_NAME, "Safari Technology Preview"));
    assertThat(options.getUseTechnologyPreview()).isTrue();

    options = new SafariOptions(new ImmutableCapabilities(CapabilityType.BROWSER_NAME, "safari"));
    assertThat(options.getUseTechnologyPreview()).isFalse();
  }

  @Test
  public void newerStyleCapabilityWinsOverOlderStyle() {
    SafariOptions options = new SafariOptions(new ImmutableCapabilities(
        CapabilityType.BROWSER_NAME, "Safari Technology Preview",
        SafariOptions.CAPABILITY, singletonMap("technologyPreview", false)));

    assertThat(options.getUseTechnologyPreview()).isTrue();
  }

  @Test
  public void canSetAutomaticInspection() {
    SafariOptions options = new SafariOptions().setAutomaticInspection(true);
    assertThat(options.getAutomaticInspection()).isTrue();
  }

  @Test
  public void canSetAutomaticProfiling() {
    SafariOptions options = new SafariOptions().setAutomaticProfiling(true);
    assertThat(options.getAutomaticProfiling()).isTrue();
  }

  @Test
  public void settingTechnologyPreviewModeAlsoChangesBrowserName() {
    SafariOptions options = new SafariOptions();
    assertThat(options.getBrowserName()).isEqualTo("safari");

    options.setUseTechnologyPreview(true);
    assertThat(options.getBrowserName()).isEqualTo("Safari Technology Preview");

    options.setUseTechnologyPreview(false);
    assertThat(options.getBrowserName()).isEqualTo("safari");
  }

  @Test
  public void optionsAsMapShouldBeImmutable() {
    Map<String, Object> options = new SafariOptions().asMap();
    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> options.put("browserType", "chrome"));

    Map<String, Object> safariOptions = (Map<String, Object>) options.get(SafariOptions.CAPABILITY);
    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> safariOptions.put("x", true));
  }


}
