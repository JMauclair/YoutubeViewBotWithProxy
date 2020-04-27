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

package org.openqa.selenium.json;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.SessionId;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

import static java.lang.Integer.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.openqa.selenium.json.Json.MAP_TYPE;
import static org.openqa.selenium.logging.LogType.BROWSER;
import static org.openqa.selenium.logging.LogType.CLIENT;
import static org.openqa.selenium.logging.LogType.DRIVER;
import static org.openqa.selenium.logging.LogType.SERVER;

public class JsonOutputTest {

  @Test
  public void emptyObjectsLookNice() {
    String json = convert(emptyMap());

    assertThat(json).isEqualTo("{\n}");
  }

  @Test
  public void emptyCollectionsLookNice() {
    String json = convert(emptyList());

    assertThat(json).isEqualTo("[\n]");
  }

  @Test
  public void shouldBeAbleToConvertASimpleString() {
    String json = convert("cheese");

    assertThat(json).isEqualTo("\"cheese\"");
  }

  @Test
  public void shouldConvertAMapIntoAJsonObject() {
    Map<String, String> toConvert = new HashMap<>();
    toConvert.put("cheese", "cheddar");
    toConvert.put("fish", "nice bit of haddock");

    String json = convert(toConvert);

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();
    assertThat(converted.get("cheese").getAsString()).isEqualTo("cheddar");
  }

  @Test
  public void shouldConvertASimpleJavaBean() {
    String json = convert(new SimpleBean());

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();
    assertThat(converted.get("foo").getAsString()).isEqualTo("bar");
    assertThat(converted.get("simple").getAsBoolean()).isEqualTo(true);
    assertThat(converted.get("number").getAsDouble()).isEqualTo(123.456);
  }

  @Test
  public void shouldConvertArrays() {
    String json = convert(new BeanWithArray());

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();
    JsonArray allNames = converted.get("names").getAsJsonArray();
    assertThat(allNames).hasSize(3);
  }

  @Test
  public void shouldConvertCollections() {
    String json = convert(new BeanWithCollection());

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();
    JsonArray allNames = converted.get("something").getAsJsonArray();
    assertThat(allNames).hasSize(2);
  }

  @Test
  public void shouldConvertNumbersAsLongs() {
    String json = convert(new Exception());
    Map<?,?> map = new Json().toType(json, Map.class);

    List<?> stack = (List<?>) map.get("stackTrace");
    Map<?,?> line = (Map<?,?>) stack.get(0);

    Object o = line.get("lineNumber");
    assertThat(o).isInstanceOf(Long.class);
  }

  @Test
  public void shouldNotChokeWhenCollectionIsNull() {
    convert(new BeanWithNullCollection());
  }

  @Test
  public void testShouldConvertEnumsToStrings() {
    // If this doesn't hang indefinitely, we're all good
    convert(State.INDIFFERENT);
  }

  @Test
  public void testShouldConvertEnumsWithMethods() {
    // If this doesn't hang indefinitely, we're all good
    convert(WithMethods.CHEESE);
  }

  @Test
  public void nullAndAnEmptyStringAreEncodedDifferently() {
    String nullValue = convert(null);
    String emptyString = convert("");

    assertThat(emptyString).isNotEqualTo(nullValue);
  }

  @Test
  public void shouldBeAbleToConvertAPoint() {
    convert(new Point(65, 75));
  }

  @Test
  public void shouldEncodeClassNameAsClassProperty() {
    String json = convert(new SimpleBean());

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();

    assertThat(converted.get("class").getAsString()).isEqualTo(SimpleBean.class.getName());
  }

  @Test
  public void shouldBeAbleToConvertASessionId() {
    SessionId sessionId = new SessionId("some id");
    String json = convert(sessionId);

    JsonPrimitive converted = new JsonParser().parse(json).getAsJsonPrimitive();

    assertThat(converted.getAsString()).isEqualTo("some id");
  }

  @Test
  public void shouldBeAbleToConvertAJsonObject() {
    JsonObject obj = new JsonObject();
    obj.addProperty("key", "value");
    String json = convert(obj);

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();

    assertThat(converted.get("key").getAsString()).isEqualTo("value");
  }

  @Test
  public void shouldBeAbleToConvertACapabilityObject() {
    Capabilities caps = new ImmutableCapabilities("key", "alpha");

    String json = convert(caps);

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();

    assertThat(converted.get("key").getAsString()).isEqualTo("alpha");
  }

  @Test
  public void shouldConvertAProxyCorrectly() {
    Proxy proxy = new Proxy();
    proxy.setHttpProxy("localhost:4444");

    MutableCapabilities caps = new DesiredCapabilities("foo", "1", Platform.LINUX);
    caps.setCapability(CapabilityType.PROXY, proxy);
    Map<String, ?> asMap = ImmutableMap.of("desiredCapabilities", caps);
    Command command = new Command(new SessionId("empty"), DriverCommand.NEW_SESSION, asMap);

    String json = convert(command.getParameters());

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();
    JsonObject capsAsMap = converted.get("desiredCapabilities").getAsJsonObject();

    assertThat(capsAsMap.get(CapabilityType.PROXY).getAsJsonObject().get("httpProxy").getAsString())
        .isEqualTo(proxy.getHttpProxy());
  }

  @Test
  public void shouldCallToJsonMethodIfPresent() {
    String json = convert(new JsonAware("converted"));
    assertThat(json).isEqualTo("\"converted\"");
  }

  @Test
  public void shouldPreferToJsonMethodToToMapMethod() {
    String json = convert(new MappableJsonAware("converted"));
    assertThat(json).isEqualTo("\"converted\"");
  }

  @Test
  public void toJsonMethodCanConvertibleReturnedMap() {
    class ToJsonReturnsMap {
      @SuppressWarnings("unused")
      public Map<String, Object> toJson() {
        return ImmutableMap.of("cheese", "peas");
      }
    }

    String json = convert(new ToJsonReturnsMap());
    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();

    assertThat(converted.entrySet()).hasSize(1);
    assertThat(converted.get("cheese").getAsString()).isEqualTo("peas");
  }

  @Test
  public void toJsonMethodCanConvertReturnedCollection() {
    class ToJsonReturnsCollection {
      @SuppressWarnings("unused")
      public Set<String> toJson() {
        return ImmutableSortedSet.of("cheese", "peas");
      }
    }

    String json = convert(new ToJsonReturnsCollection());
    JsonArray converted = new JsonParser().parse(json).getAsJsonArray();

    assertThat(converted).hasSize(2);
    JsonArray expected = new JsonArray();
    expected.add(new JsonPrimitive("cheese"));
    expected.add(new JsonPrimitive("peas"));
    assertThat(converted).isEqualTo(expected);
  }

  @Test
  public void shouldCallAsMapMethodIfPresent() {
    String json = convert(new Mappable1("a key", "a value"));

    Map<String, Object> value = new Json().toType(json, MAP_TYPE);

    assertThat(value).isEqualTo(ImmutableMap.of("a key", "a value"));
  }

  @Test
  public void shouldCallToMapMethodIfPresent() {
    String json = convert(new Mappable2("a key", "a value"));

    Map<String, Object> value = new Json().toType(json, MAP_TYPE);

    assertThat(value).isEqualTo(ImmutableMap.of("a key", "a value"));
  }

  @Test
  public void toJsonDoesNotNeedToBePublic() {
    class PrivatelyMappable {
      private String toJson() {
        return "cheese";
      }
    }

    String json = convert(new PrivatelyMappable());

    assertThat(json).isEqualTo("\"cheese\"");
  }

  @Test
  public void convertsToJsonMethodResultToPrimitiveIfItIsNotJson() {
    // We want this parsed as a string primitive, but JsonParser will reject it
    // as malformed because of the slash.
    String raw = "gnu/linux";

    // Make sure that the parser does actually reject this so the test is
    // meaningful. If this stops failing, choose a different malformed JSON
    // string.
    assertThatExceptionOfType(JsonException.class)
        .isThrownBy(() -> new Json().toType(raw, String.class));

    String json = convert(new JsonAware(raw));

    // The JSON spec says that we should encode the forward stroke ("solidus"). Decode the string
    assertThat(json.startsWith("\"")).isTrue();
    assertThat(json.endsWith("\"")).isTrue();
    json = new JsonParser().parse(json).getAsString();

    assertThat(json).isEqualTo("gnu/linux");
  }

  private void verifyStackTraceInJson(String json, StackTraceElement[] stackTrace) {
    int posOfLastStackTraceElement = 0;
    for (StackTraceElement e : stackTrace) {
      if (e.getFileName() != null) {
        // Native methods may have null filenames
        assertThat(json).contains("\"fileName\": \"" + e.getFileName() + "\"");
      }
      assertThat(json)
          .contains("\"lineNumber\": " + e.getLineNumber() + "",
                    "\"class\": \"" + e.getClass().getName() + "\"",
                    "\"className\": \"" + e.getClassName() + "\"",
                    "\"methodName\": \"" + e.getMethodName() + "\"");

      int posOfCurrStackTraceElement = json.indexOf(e.getMethodName());
      assertThat(posOfCurrStackTraceElement).isGreaterThan(posOfLastStackTraceElement);
    }
  }

  @Test
  public void shouldBeAbleToConvertARuntimeException() {
    RuntimeException clientError = new RuntimeException("foo bar baz!");
    StackTraceElement[] stackTrace = clientError.getStackTrace();
    String json = convert(clientError);
    assertThat(json).contains("\"message\": \"foo bar baz!\"",
                              "\"class\": \"java.lang.RuntimeException\"",
                              "\"stackTrace\"");
    verifyStackTraceInJson(json, stackTrace);
  }

  @Test
  public void shouldBeAbleToConvertAWebDriverException() {
    RuntimeException clientError = new WebDriverException("foo bar baz!");
    StackTraceElement[] stackTrace = clientError.getStackTrace();
    String raw = convert(clientError);

    JsonObject converted = new JsonParser().parse(raw).getAsJsonObject();

    assertThat(converted.has("buildInformation")).isTrue();
    assertThat(converted.has("systemInformation")).isTrue();
    assertThat(converted.has("additionalInformation")).isTrue();

    assertThat(converted.has("message")).isTrue();
    assertThat(converted.get("message").getAsString()).contains("foo bar baz!");
    assertThat(converted.get("class").getAsString()).isEqualTo(WebDriverException.class.getName());

    assertThat(converted.has("stackTrace")).isTrue();
    verifyStackTraceInJson(raw, stackTrace);
  }

  @Test
  public void shouldConvertUnhandledAlertException() {
    RuntimeException clientError = new UnhandledAlertException("unhandled alert", "cheese!");
    Map<String, Object> obj = new Json().toType(new StringReader(convert(clientError)), Map.class);
    assertThat(obj).containsKey("alert");
    assertThat(obj.get("alert")).isEqualTo(ImmutableMap.of("text", "cheese!"));
  }


  @Test
  public void shouldConvertDatesToMillisecondsInUtcTime() {
    String jsonStr = convert(new Date(0));
    assertThat(valueOf(jsonStr).intValue()).isEqualTo(0);
  }

  @Test
  public void shouldConvertDateFieldsToSecondsSince1970InUtcTime() {
    class Bean {
      private final Date date;

      Bean(Date date) {
        this.date = date;
      }

      @SuppressWarnings("unused")
      public Date getDate() {
        return date;
      }
    }

    Date date = new Date(123456789L);
    Bean bean = new Bean(date);
    String jsonStr = convert(bean);

    JsonObject converted = new JsonParser().parse(jsonStr).getAsJsonObject();

    assertThat(converted.has("date")).isTrue();
    assertThat(converted.get("date").getAsLong()).isEqualTo(123456L);
  }

  @Test
  public void shouldBeAbleToConvertACookie() {
    Date expiry = new Date();
    Cookie cookie = new Cookie("name", "value", "domain", "/path", expiry, true, true);

    String jsonStr = convert(cookie);

    JsonObject converted = new JsonParser().parse(jsonStr).getAsJsonObject();

    assertThat(converted.get("name").getAsString()).isEqualTo("name");
    assertThat(converted.get("value").getAsString()).isEqualTo("value");
    assertThat(converted.get("domain").getAsString()).isEqualTo("domain");
    assertThat(converted.get("path").getAsString()).isEqualTo("/path");
    assertThat(converted.get("secure").getAsBoolean()).isTrue();
    assertThat(converted.get("httpOnly").getAsBoolean()).isTrue();
    assertThat(converted.get("expiry").getAsLong())
        .isEqualTo(MILLISECONDS.toSeconds(expiry.getTime()));
  }

  @Test
  public void unsetCookieFieldsAreUndefined() {
    Cookie cookie = new Cookie("name", "value");
    String jsonStr = convert(cookie);
    assertThat(jsonStr).doesNotContain("domain", "expiry");
  }

  @Test
  public void properlyConvertsNulls() {
    Map<String, Object> frameId = new HashMap<>();
    frameId.put("id", null);
    String payload = convert(frameId);

    Map<String, Object> result = new Json().toType(payload, MAP_TYPE);
    assertThat(result).containsKey("id");
    assertThat(result.get("id")).isNull();
  }

  @Test
  public void convertLoggingPreferencesToJson() {
    LoggingPreferences prefs = new LoggingPreferences();
    prefs.enable(LogType.BROWSER, Level.WARNING);
    prefs.enable(LogType.CLIENT, Level.FINE);
    prefs.enable(LogType.DRIVER, Level.ALL);
    prefs.enable(LogType.SERVER, Level.OFF);

    String json = convert(prefs);

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();

    assertThat(converted.get(BROWSER).getAsString()).isEqualTo("WARNING");
    assertThat(converted.get(CLIENT).getAsString()).isEqualTo("DEBUG");
    assertThat(converted.get(DRIVER).getAsString()).isEqualTo("ALL");
    assertThat(converted.get(SERVER).getAsString()).isEqualTo("OFF");
  }

  @Test
  public void convertsLogEntryToJson() {
    String raw = convert(new LogEntry(Level.OFF, 17, "foo"));

    JsonObject converted = new JsonParser().parse(raw).getAsJsonObject();

    assertThat(converted.get("message").getAsString()).isEqualTo("foo");
    assertThat(converted.get("timestamp").getAsLong()).isEqualTo(17);
    assertThat(converted.get("level").getAsString()).isEqualTo("OFF");
  }

  @Test
  public void convertLogEntriesToJson() {
    long timestamp = new Date().getTime();
    final LogEntry entry1 = new LogEntry(Level.OFF, timestamp, "entry1");
    final LogEntry entry2 = new LogEntry(Level.WARNING, timestamp, "entry2");
    LogEntries entries = new LogEntries(asList(entry1, entry2));

    String json = convert(entries);

    JsonArray converted = new JsonParser().parse(json).getAsJsonArray();

    JsonObject obj1 = converted.get(0).getAsJsonObject();
    JsonObject obj2 = converted.get(1).getAsJsonObject();
    assertThat(obj1.get("level").getAsString()).isEqualTo("OFF");
    assertThat(obj1.get("timestamp").getAsLong()).isEqualTo(timestamp);
    assertThat(obj1.get("message").getAsString()).isEqualTo("entry1");
    assertThat(obj2.get("level").getAsString()).isEqualTo("WARNING");
    assertThat(obj2.get("timestamp").getAsLong()).isEqualTo(timestamp);
    assertThat(obj2.get("message").getAsString()).isEqualTo("entry2");
  }

  @Test
  public void shouldBeAbleToConvertACommand() {
    SessionId sessionId = new SessionId("some id");
    String commandName = "some command";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("param1", "value1");
    parameters.put("param2", "value2");
    Command command = new Command(sessionId, commandName, parameters);

    String json = convert(command);

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();

    assertThat(converted.has("sessionId")).isTrue();
    JsonPrimitive sid = converted.get("sessionId").getAsJsonPrimitive();
    assertThat(sid.getAsString()).isEqualTo(sessionId.toString());

    assertThat(commandName).isEqualTo(converted.get("name").getAsString());

    assertThat(converted.has("parameters")).isTrue();
    JsonObject pars = converted.get("parameters").getAsJsonObject();
    assertThat(pars.entrySet()).hasSize(2);
    assertThat(pars.get("param1").getAsString()).isEqualTo(parameters.get("param1"));
    assertThat(pars.get("param2").getAsString()).isEqualTo(parameters.get("param2"));
  }

  @Test
  public void shouldConvertAUrlToAString() throws MalformedURLException {
    URL url = new URL("http://example.com/cheese?type=edam");
    Map<String, URL> toConvert = ImmutableMap.of("url", url);

    String seen = new Json().toJson(toConvert);
    JsonObject converted = new JsonParser().parse(seen).getAsJsonObject();

    assertThat(converted.get("url").getAsString()).isEqualTo(url.toExternalForm());
  }

  @Test
  public void shouldNotIncludePropertiesFromJavaLangObjectOtherThanClass() {
    String json = convert(new SimpleBean());

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();

    Stream.of(SimplePropertyDescriptor.getPropertyDescriptors(Object.class))
        .filter(pd -> !"class".equals(pd.getName()))
        .map(SimplePropertyDescriptor::getName)
        .forEach(name -> assertThat(converted.keySet()).contains(name));
  }

  @Test
  public void shouldAllowValuesToBeStreamedToACollection() {
    StringBuilder builder = new StringBuilder();

    try (JsonOutput jsonOutput = new Json().newOutput(builder)) {
      jsonOutput.beginArray()
          .write("brie")
          .write("peas")
          .endArray();
    }

    assertThat((Object) new Json().toType(builder.toString(), Object.class))
        .isEqualTo(Arrays.asList("brie", "peas"));
  }

  @Test
  public void shouldAllowValuesToBeStreamedToAnObject() {
    StringBuilder builder = new StringBuilder();

    try (JsonOutput jsonOutput = new Json().newOutput(builder)) {
      jsonOutput.beginObject()
          .name("cheese").write("brie")
          .name("vegetable").write("peas")
          .endObject();
    }

    assertThat((Object) new Json().toType(builder.toString(), MAP_TYPE))
        .isEqualTo(ImmutableMap.of("cheese", "brie", "vegetable", "peas"));
  }

  @Test
  public void whenConvertingObjectsContainingClassesDoNotBeNoisy() {
    String json = convert(ImmutableMap.of("thing", SimpleBean.class));

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();

    assertThat(converted.size()).isEqualTo(1);
    assertThat(converted.getAsJsonPrimitive("thing").getAsString())
        .isEqualTo(SimpleBean.class.getName());
  }

  @Test
  public void canDisablePrettyPrintingToGetSingleLineOutput() {
    Map<String, Object> toEncode = ImmutableMap.of(
        "ary", Arrays.asList("one", "two"),
        "map", ImmutableMap.of("cheese", "cheddar"),
        "string", "This has a \nnewline in it");

    StringBuilder json = new StringBuilder();
    try (JsonOutput out = new Json().newOutput(json)) {
      out.setPrettyPrint(false);

      out.write(toEncode);
    }

    assertThat(json.indexOf("\n")).isEqualTo(-1);
  }

  @Test
  public void shouldEncodeLogLevelsAsStrings() {
    String converted = convert(Level.INFO);

    assertThat(converted).isEqualTo("\"INFO\"");
  }

  @Test
  public void shouldNotWriteOptionalFieldsThatAreEmptyInAMap() {
    String json = convert(ImmutableMap.of("there", Optional.of("cheese"), "notThere", Optional.empty()));

    JsonObject converted = new JsonParser().parse(json).getAsJsonObject();

    assertThat(converted.has("notThere")).isFalse();
    assertThat(converted.get("there").getAsString()).isEqualTo("cheese");
  }

  @Test
  public void shouldNotWriteOptionalsThatAreNotPresentToAList() {
    String json = convert(Arrays.asList(Optional.of("cheese"), Optional.empty()));

    JsonArray converted = new JsonParser().parse(json).getAsJsonArray();

    assertThat(converted.size()).isEqualTo(1);
    assertThat(converted.get(0).getAsString()).isEqualTo("cheese");
  }

  private String convert(Object toConvert) {
    try (Writer writer = new StringWriter();
         JsonOutput jsonOutput = new Json().newOutput(writer)) {
      jsonOutput.write(toConvert);
      return writer.toString();
    } catch (IOException e) {
      throw new JsonException(e);
    }
  }

  @SuppressWarnings("unused")
  private static class SimpleBean {

    public String getFoo() {
      return "bar";
    }

    public boolean isSimple() {
      return true;
    }

    public double getNumber() {
      return 123.456;
    }
  }

  @SuppressWarnings("unused")
  private static class BeanWithArray {
    public String[] getNames() {
      return new String[] {"peter", "paul", "mary"};
    }
  }

  private static class BeanWithCollection {

    @SuppressWarnings("unused")
    public Set<?> getSomething() {
      Set<Integer> integers = new HashSet<>();
      integers.add(1);
      integers.add(43);
      return integers;
    }
  }

  private static class BeanWithNullCollection {

    @SuppressWarnings("unused")
    public List<?> getList() {
      return null;
    }
  }

  public enum State {

    GOOD,
    BAD,
    INDIFFERENT
  }

  public enum WithMethods {

    CHEESE() {
      @Override
      public void eat(String foodStuff) {
        // Does nothing
      }
    },
    EGGS() {
      @Override
      public void eat(String foodStuff) {
        // Does nothing too
      }
    };

    public abstract void eat(String foodStuff);
  }

  public class JsonAware {
    private String convertedValue;

    public JsonAware(String convertedValue) {
      this.convertedValue = convertedValue;
    }

    public String toJson() {
      return convertedValue;
    }
  }

  public class MappableJsonAware {
    private String convertedValue;

    public MappableJsonAware(String convertedValue) {
      this.convertedValue = convertedValue;
    }

    public String toJson() {
      return convertedValue;
    }

    public Map<String, Object> asMap() {
      return ImmutableMap.of("key", "value");
    }
  }

  public class Mappable1 {
    private String key;
    private Object value;

    public Mappable1(String key, Object value) {
      this.key = key;
      this.value = value;
    }

    public Map<String, Object> asMap() {
      return ImmutableMap.of(key, value);
    }
  }

  public class Mappable2 {
    private String key;
    private Object value;

    public Mappable2(String key, Object value) {
      this.key = key;
      this.value = value;
    }

    public Map<String, Object> toMap() {
      return ImmutableMap.of(key, value);
    }
  }
}
