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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.openqa.selenium.json.Json.MAP_TYPE;
import static org.openqa.selenium.json.JsonType.BOOLEAN;
import static org.openqa.selenium.json.JsonType.END_COLLECTION;
import static org.openqa.selenium.json.JsonType.END_MAP;
import static org.openqa.selenium.json.JsonType.NAME;
import static org.openqa.selenium.json.JsonType.NULL;
import static org.openqa.selenium.json.JsonType.NUMBER;
import static org.openqa.selenium.json.JsonType.START_COLLECTION;
import static org.openqa.selenium.json.JsonType.START_MAP;
import static org.openqa.selenium.json.JsonType.STRING;
import static org.openqa.selenium.json.PropertySetting.BY_NAME;

import org.junit.Test;

import java.io.StringReader;
import java.util.Map;

public class JsonInputTest {

  @Test
  public void shouldParseBooleanValues() {
    JsonInput input = newInput("true");
    assertThat(input.peek()).isEqualTo(BOOLEAN);
    assertThat(input.nextBoolean()).isTrue();

    input = newInput("false");
    assertThat(input.peek()).isEqualTo(BOOLEAN);
    assertThat(input.nextBoolean()).isFalse();
  }

  @Test
  public void shouldParseNonDecimalNumbersAsLongs() {
    JsonInput input = newInput("42");
    assertThat(input.peek()).isEqualTo(NUMBER);
    assertThat(input.nextNumber()).isEqualTo(42L);
  }

  @Test
  public void shouldParseDecimalNumbersAsDoubles() {
    JsonInput input = newInput("42.0");
    assertThat(input.peek()).isEqualTo(NUMBER);
    assertThat((Double) input.nextNumber()).isEqualTo(42.0d);
  }

  @Test
  public void shouldHandleNullValues() {
    JsonInput input = newInput("null");
    assertThat(input.peek()).isEqualTo(NULL);
    assertThat(input.nextNull()).isNull();
  }

  @Test
  public void shouldBeAbleToReadAString() {
    JsonInput input = newInput("\"cheese\"");
    assertThat(input.peek()).isEqualTo(STRING);
    assertThat(input.nextString()).isEqualTo("cheese");
  }

  @Test
  public void shouldBeAbleToReadTheEmptyString() {
    JsonInput input = newInput("\"\"");
    assertThat(input.peek()).isEqualTo(STRING);
    assertThat(input.nextString()).isEqualTo("");
  }

  @Test
  public void anEmptyArrayHasNoContents() {
    JsonInput input = newInput("[]");
    assertThat(input.peek()).isEqualTo(START_COLLECTION);
    input.beginArray();
    assertThat(input.hasNext()).isFalse();
    assertThat(input.peek()).isEqualTo(END_COLLECTION);
    input.endArray();
  }

  @Test
  public void anArrayWithASingleElementHasNextButOnlyOneValue() {
    JsonInput input = newInput("[ \"peas\"]");
    input.beginArray();
    assertThat(input.nextString()).isEqualTo("peas");
    input.endArray();
  }

  @Test
  public void anArrayWithMultipleElementsReturnsTrueFromHasNextMoreThanOnce() {
    JsonInput input = newInput("[\"brie\", \"cheddar\"]");
    input.beginArray();
    assertThat(input.hasNext()).isTrue();
    assertThat(input.nextString()).isEqualTo("brie");
    assertThat(input.hasNext()).isTrue();
    assertThat(input.nextString()).isEqualTo("cheddar");
    assertThat(input.hasNext()).isFalse();
    input.endArray();
  }

  @Test
  public void callingHasNextWhenNotInAnArrayOrMapIsAnError() {
    JsonInput input = newInput("\"cheese\"");
    assertThatExceptionOfType(JsonException.class)
        .isThrownBy(input::hasNext);
  }

  @Test
  public void anEmptyMapHasNoContents() {
    JsonInput input = newInput("{      }");
    assertThat(input.peek()).isEqualTo(START_MAP);
    input.beginObject();
    assertThat(input.hasNext()).isFalse();
    assertThat(input.peek()).isEqualTo(END_MAP);
    input.endObject();
  }

  @Test
  public void canReadAMapWithASingleEntry() {
    JsonInput input = newInput("{\"cheese\": \"feta\"}");
    input.beginObject();
    assertThat(input.hasNext()).isTrue();
    assertThat(input.peek()).isEqualTo(NAME);
    assertThat(input.nextName()).isEqualTo("cheese");
    assertThat(input.peek()).isEqualTo(STRING);
    assertThat(input.nextString()).isEqualTo("feta");
    assertThat(input.hasNext()).isFalse();
    input.endObject();
  }

  @Test
  public void canReadAMapWithManyEntries() {
    JsonInput input = newInput("{" +
        "\"cheese\": \"stilton\"," +
        "\"vegetable\": \"peas\"," +
        "\"random\": 42}");

    assertThat(input.peek()).isEqualTo(START_MAP);
    input.beginObject();
    assertThat(input.hasNext()).isTrue();
    assertThat(input.peek()).isEqualTo(NAME);
    assertThat(input.nextName()).isEqualTo("cheese");
    assertThat(input.nextString()).isEqualTo("stilton");
    assertThat(input.hasNext()).isTrue();
    assertThat(input.peek()).isEqualTo(NAME);
    assertThat(input.nextName()).isEqualTo("vegetable");
    assertThat(input.nextString()).isEqualTo("peas");
    assertThat(input.hasNext()).isTrue();
    assertThat(input.peek()).isEqualTo(NAME);
    assertThat(input.nextName()).isEqualTo("random");
    assertThat(input.nextNumber()).isEqualTo(42L);
    assertThat(input.hasNext()).isFalse();
    assertThat(input.peek()).isEqualTo(END_MAP);
    input.endObject();
  }

  @Test
  public void nestedMapIsFine() {
    JsonInput input = newInput("{\"map\": {\"child\": [\"hello\",\"world\"]}}");

    input.beginObject();
    assertThat(input.hasNext()).isTrue();
    assertThat(input.nextName()).isEqualTo("map");
    input.beginObject();
    assertThat(input.hasNext()).isTrue();
    assertThat(input.nextName()).isEqualTo("child");
    input.beginArray();
    assertThat(input.hasNext()).isTrue();
    assertThat(input.nextString()).isEqualTo("hello");
    assertThat(input.hasNext()).isTrue();
    assertThat(input.nextString()).isEqualTo("world");
    assertThat(input.hasNext()).isFalse();
    input.endArray();
    assertThat(input.hasNext()).isFalse();
    input.endObject();
    assertThat(input.hasNext()).isFalse();
    input.endObject();
  }

  @Test
  public void shouldDecodeUnicodeEscapesProperly() {
    String raw = "{\"text\": \"\\u003Chtml\"}";

    try (JsonInput in = new JsonInput(new StringReader(raw), new JsonTypeCoercer(), BY_NAME)) {
      Map<String, Object> map = in.read(MAP_TYPE);

      assertThat(map.get("text")).isEqualTo("<html");
    }
  }

  @Test
  public void shouldCallFromJsonWithJsonInputParameter() {
    String raw = "{\"message\": \"Cheese!\"}";

    try (JsonInput in = new JsonInput(new StringReader(raw), new JsonTypeCoercer(), BY_NAME)) {
      HasFromJsonWithJsonInputParameter obj = in.read(HasFromJsonWithJsonInputParameter.class);

      assertThat(obj.getMessage()).isEqualTo("Cheese!");
    }
  }

  private JsonInput newInput(String raw) {
    StringReader reader = new StringReader(raw);
    return new JsonInput(reader, new JsonTypeCoercer(), BY_NAME);
  }

  public static class HasFromJsonWithJsonInputParameter {

    private final String message;

    public HasFromJsonWithJsonInputParameter(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

    private static HasFromJsonWithJsonInputParameter fromJson(JsonInput input) {
      input.beginObject();
      input.nextName();
      String message = input.nextString();
      input.endObject();

      return new HasFromJsonWithJsonInputParameter(message);
    }
  }
}
