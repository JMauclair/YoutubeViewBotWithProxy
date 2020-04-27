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

package org.openqa.selenium.firefox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

public class PreferencesTest {

  private static final String emptyDefaults = "{\"mutable\": {}, \"frozen\": {}}";
  private StringReader defaults;

  @Before
  public void setUp() {
    defaults = new StringReader(emptyDefaults);
  }

  @Test
  public void stringifyVsStringFormat() {
    assertThat(String.format("\"%s\"", "stringifyMe")).isEqualTo("\"stringifyMe\"");
  }

  @Test
  public void stringFormatOfStringify() {
    assertThat(String.format("\"%s\"", "\"stringifyMe\"")).isEqualTo("\"\"stringifyMe\"\"");
  }

  @Test
  public void detectStringification() {
    Preferences a = new Preferences(defaults);

    assertThat(canSet(a, "\"\"")).as("Empty String").isFalse();
    assertThat(canSet(a, ("\"Julian\""))).as("Valid stringified string").isFalse();
    assertThat(canSet(a, ("\"StartOnly"))).as("Only start is stringified").isTrue();
    assertThat(canSet(a, ("EndOnly\""))).as("Only end is stringified").isTrue();
    assertThat(canSet(a, (String.format("\"%s\"", "FormatMe"))))
        .as("Using String.format(\"%%s\")").isFalse();

    assertThat(canSet(a, ("\"Julian\" \"TestEngineer\" Harty.\"")))
        .as("\"Stringified string containing extra double-quotes\"").isFalse();
  }

  @Test
  public void parsePreferences_boolean() {
    StringReader lines = new StringReader("user_pref(\"extensions.update.notifyUser\", false);");
    Preferences prefs = new Preferences(defaults, lines);

    assertThat(prefs.getPreference("extensions.update.notifyUser")).isEqualTo(false);
  }

  @Test
  public void parsePreferences_integer() {
    StringReader lines = new StringReader("user_pref(\"dom.max_script_run_time\", 34);");
    Preferences prefs = new Preferences(defaults, lines);

    assertThat(prefs.getPreference("dom.max_script_run_time")).isEqualTo(34);
  }

  @Test
  public void parsePreferences_string() {
    String prefWithComma = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_1 like Mac OS X; en-us) "
        + "AppleWebKit/532.9 (KHTML, like Gecko)";
    String prefWithQuotes = "lpr ${MOZ_PRINTER_NAME:+-P\"$MOZ_PRINTER_NAME\"}";

    Reader lines = new StringReader(
        "user_pref(\"general.useragent.override\", \"" + prefWithComma + "\");\n" +
            "user_pref(\"print.print_command\", \"" + prefWithQuotes + "\");");
    Preferences prefs = new Preferences(defaults, lines);

    assertThat(prefs.getPreference("general.useragent.override")).isEqualTo(prefWithComma);
    assertThat(prefs.getPreference("print.print_command")).isEqualTo(prefWithQuotes);
  }

  @Test
  public void parsePreferences_multiline() {
    Reader lines = new StringReader(
        "user_pref(\"extensions.update.notifyUser\", false);\n" +
            "user_pref(\"dom.max_script_run_time\", 32);");
    Preferences prefs = new Preferences(defaults, lines);

    assertThat(prefs.getPreference("extensions.update.notifyUser")).isEqualTo(false);
    assertThat(prefs.getPreference("dom.max_script_run_time")).isEqualTo(32);
  }

  @Test
  public void cannotOverrideAFozenPrefence() {
    StringReader reader = new StringReader("{\"frozen\": {\"frozen.pref\": true }, \"mutable\": {}}");
    Preferences preferences = new Preferences(reader);
    preferences.setPreference("frozen.pref", false);

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(preferences::checkForChangesInFrozenPreferences)
        .withMessage("Preference frozen.pref may not be overridden: frozen value=true, requested value=false");
  }

  @Test
  public void canOverrideAFrozenPreferenceWithTheFrozenValue() {
    StringReader reader = new StringReader("{\"frozen\": {\"frozen.pref\": true }, \"mutable\": {}}");
    Preferences preferences = new Preferences(reader);

    preferences.setPreference("frozen.pref", true);

    assertThat(preferences.getPreference("frozen.pref")).isEqualTo(true);
  }

  @Test
  public void canOverrideMaxScriptRuntimeIfGreaterThanDefaultValueOrSetToInfinity() {
    Preferences preferences = new Preferences(defaults);
    preferences.setPreference("dom.max_script_run_time", 29);

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(preferences::checkForChangesInFrozenPreferences)
        .withMessage("dom.max_script_run_time must be == 0 || >= 30");

    preferences.setPreference("dom.max_script_run_time", 31);
    preferences.setPreference("dom.max_script_run_time", 0);
  }

  private boolean canSet(Preferences pref, String value) {
    try {
      pref.setPreference("key", value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
