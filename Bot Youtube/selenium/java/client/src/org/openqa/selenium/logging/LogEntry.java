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

package org.openqa.selenium.logging;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Represents a single log statement.
 */
public class LogEntry {

  private final Level level;
  private final long timestamp;
  private final String message;

  /**
   * @param level     the severity of the log entry
   * @param timestamp UNIX Epoch timestamp at which this log entry was created
   * @param message ew  the log entry's message
   */
  public LogEntry(Level level, long timestamp, String message) {
    this.level = level;
    this.timestamp = timestamp;
    this.message = message;
  }

  /**
   * Gets the logging entry's severity.
   *
   * @return severity of log statement
   */
  public Level getLevel() {
    return level;
  }

  /**
   * Gets the timestamp of the log statement in milliseconds since UNIX Epoch.
   *
   * @return timestamp as UNIX Epoch
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Gets the log entry's message.
   *
   * @return the log statement
   */
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return String.format(
        "[%s] [%s] %s",
        DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp)), level, message);
  }

  public Map<String, Object> toJson() {
    Map<String, Object> map = new HashMap<>();
    map.put("timestamp", timestamp);
    map.put("level", level);
    map.put("message", message);
    return map;
  }

}
