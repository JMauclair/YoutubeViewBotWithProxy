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

package org.openqa.selenium;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

public class CookieTest {

  @Test
  public void testCanCreateAWellFormedCookie() {
    new Cookie("Fish", "cod", "", "", null, false);
  }

  @Test
  public void testShouldThrowAnExceptionWhenSemiColonExistsInTheCookieAttribute() {
    Cookie cookie = new Cookie("hi;hi", "value", null, null, null, false);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(cookie::validate);
  }

  @Test
  public void testShouldThrowAnExceptionTheNameIsNull() {
    Cookie cookie = new Cookie(null, "value", null, null, null, false);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(cookie::validate);
  }

  @Test
  public void testCookiesShouldAllowSecureToBeSet() {
    Cookie cookie = new Cookie("name", "value", "", "/", new Date(), true);
    assertThat(cookie.isSecure()).isTrue();
  }

  @Test
  public void testSecureDefaultsToFalse() {
    Cookie cookie = new Cookie("name", "value");
    assertThat(cookie.isSecure()).isFalse();
  }

  @Test
  public void testCookiesShouldAllowHttpOnlyToBeSet() {
    Cookie cookie = new Cookie("name", "value", "", "/", new Date(), false, true);
    assertThat(cookie.isHttpOnly()).isTrue();
  }

  @Test
  public void testHttpOnlyDefaultsToFalse() {
    Cookie cookie = new Cookie("name", "value");
    assertThat(cookie.isHttpOnly()).isFalse();
  }

  @Test
  public void testCookiesShouldAllowSameSiteToBeSet() {
    Cookie cookie = new Cookie("name", "value", "", "/", new Date(), false, true, "Lax");
    assertThat(cookie.getSameSite()).isEqualTo("Lax");

    Cookie builderCookie = new Cookie.Builder("name", "value").sameSite("Lax").build();
    assertThat(builderCookie.getSameSite()).isEqualTo("Lax");
  }

  @Test
  public void testCookieSerializes() throws IOException, ClassNotFoundException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    Cookie cookieToSerialize = new Cookie("Fish", "cod", "", "", null, false, true, "Lax");

    objectOutputStream.writeObject(cookieToSerialize);
    byte[] serializedCookie = byteArrayOutputStream.toByteArray();

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedCookie);
    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
    Cookie deserializedCookie = (Cookie) objectInputStream.readObject();
    assertThat(cookieToSerialize).isEqualTo(deserializedCookie);
  }
}
