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
package org.openqa.selenium.net;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.list;

public class NetworkInterface {

  private final String name;
  private java.net.NetworkInterface networkInterface;
  private final Iterable<InetAddress> inetAddresses;
  private Boolean isLoopback;

  public NetworkInterface(java.net.NetworkInterface networkInterface) {
    this(networkInterface.getName(), list(networkInterface.getInetAddresses()));
    this.networkInterface = networkInterface;
  }

  NetworkInterface(String name, Iterable<InetAddress> inetAddresses) {
    this.name = name;
    this.inetAddresses = StreamSupport.stream(inetAddresses.spliterator(), false)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  NetworkInterface(String name, InetAddress... inetAddresses) {
    this(name, Arrays.asList(inetAddresses));
    this.isLoopback = isLoopBackFromINetAddresses(this.inetAddresses);
  }

  public boolean isIp4AddressBindingOnly() {
    return getIp6Address() == null;
  }

  public boolean isLoopBack() {
    if (isLoopback == null && networkInterface != null) {
      try {
        // Issue 1181 : determine whether this NetworkInterface instance is loopback
        // from java.net.NetworkInterface API
        isLoopback = networkInterface.isLoopback();
      } catch (SocketException ex) {
        Logger.getLogger(NetworkInterface.class.getName()).log(Level.WARNING, null, ex);
        // If a SocketException is caught, determine whether this NetworkInterface
        // instance is loopback from computation from its inetAddresses
        isLoopback = isLoopBackFromINetAddresses(list(networkInterface.getInetAddresses()));
      }
    }
    // 'isLoopback != null' to avoid an unboxing NPE
    return isLoopback != null && isLoopback;
  }

  private boolean isLoopBackFromINetAddresses(Iterable<InetAddress> inetAddresses) {
    // Let's hope there's no such thing as network interfaces with mixed addresses ;)
    Iterator<InetAddress> iterator = inetAddresses.iterator();
    return iterator.hasNext() && iterator.next().isLoopbackAddress();
  }

  InetAddress getIp4LoopbackOnly() {
    // Goes by the wildly unscientific assumption that if there are more than one set of
    // loopback addresses, firefox will bind to the last one we get.
    // An alternate theory if this fails is that firefox prefers 127.0.0.1
    // Most "normal" boxes don't have multiple addresses so we'll just refine this
    // algorithm until it works.
    // See NetworkUtilsTest#testOpenSuseBoxIssue1181
    // Issue 1181
    if (!isLoopBack()) {
      return null;
    }
    InetAddress lastFound = null;
    for (InetAddress inetAddress : inetAddresses) {
      if (inetAddress.isLoopbackAddress() && !isIpv6(inetAddress)) {
        lastFound = inetAddress;
      }
    }
    return lastFound;
  }

  static boolean isIpv6(InetAddress address) {
    return address instanceof Inet6Address;
  }

  public InetAddress getIp4NonLoopBackOnly() {
    for (InetAddress inetAddress : inetAddresses) {
      if (!inetAddress.isLoopbackAddress() && !isIpv6(inetAddress)) {
        return inetAddress;
      }
    }
    return null;
  }

  public InetAddress getIp6Address() {
    for (InetAddress inetAddress : inetAddresses) {
      if (isIpv6(inetAddress)) {
        return inetAddress;
      }
    }
    return null;
  }

  public Iterable<InetAddress> getInetAddresses() {
    return inetAddresses;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", NetworkInterface.class.getSimpleName() + "[", "]")
        .add("name='" + name + "'")
        .add("networkInterface=" + networkInterface)
        .add("inetAddresses=" + inetAddresses)
        .add("isLoopback=" + isLoopback)
        .toString();
  }
}
