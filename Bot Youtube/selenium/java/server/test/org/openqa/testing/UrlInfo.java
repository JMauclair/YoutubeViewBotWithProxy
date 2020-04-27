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

package org.openqa.testing;

public class UrlInfo {

  private final String baseUrl;
  private final String contextPath;
  private final String pathInfo;
  private final String queryString;

  public UrlInfo(String baseUrl, String contextPath, String pathInfo) {
    this(baseUrl, contextPath, pathInfo, "");
  }

  public UrlInfo(String baseUrl, String contextPath, String pathInfo, String queryString) {
    this.baseUrl = baseUrl;
    this.contextPath = contextPath;
    this.pathInfo = pathInfo;
    this.queryString = queryString;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getServletPath() {
    return "";
  }

  public String getContextPath() {
    return contextPath;
  }

  public String getPathInfo() {
    return pathInfo;
  }

  public String getQueryString() {
    return queryString;
  }

  @Override
  public String toString() {
    return baseUrl + contextPath + pathInfo;
  }
}
