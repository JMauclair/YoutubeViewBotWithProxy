# frozen_string_literal: true

# Licensed to the Software Freedom Conservancy (SFC) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The SFC licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

require File.expand_path('../spec_helper', __dir__)

module Selenium
  module WebDriver
    module Remote
      describe Capabilities do
        it 'has default capabilities for Chrome' do
          caps = Capabilities.chrome
          expect(caps.browser_name).to eq('chrome')
        end

        it 'has default capabilities for Edge' do
          caps = Capabilities.edge
          expect(caps.browser_name).to eq('MicrosoftEdge')
        end

        it 'has default capabilities for Firefox' do
          caps = Capabilities.firefox
          expect(caps.browser_name).to eq('firefox')
        end

        it 'has default capabilities for HtmlUnit' do
          caps = Capabilities.htmlunit
          expect(caps.browser_name).to eq('htmlunit')
        end

        it 'has default capabilities for Internet Explorer' do
          caps = Capabilities.internet_explorer
          expect(caps.browser_name).to eq('internet explorer')
        end

        it 'converts noProxy from string to array' do
          proxy = Proxy.new(no_proxy: 'proxy_url, localhost')
          caps = Capabilities.new(proxy: proxy)
          expect(caps.as_json['proxy']['noProxy']).to eq(%w[proxy_url localhost])
        end

        it 'does not convert noProxy if it is already array' do
          proxy = Proxy.new(no_proxy: ['proxy_url'])
          caps = Capabilities.new(proxy: proxy)
          expect(caps.as_json['proxy']['noProxy']).to eq(['proxy_url'])
        end

        it 'should default to no proxy' do
          expect(Capabilities.new.proxy).to be_nil
        end

        it 'can set and get standard capabilities' do
          caps = Capabilities.new

          caps.browser_name = 'foo'
          expect(caps.browser_name).to eq('foo')

          caps.page_load_strategy = :eager
          expect(caps.page_load_strategy).to eq(:eager)
        end

        it 'can set and get arbitrary capabilities' do
          caps = Capabilities.chrome
          caps['chrome'] = :foo
          expect(caps['chrome']).to eq(:foo)
        end

        it 'should set the given proxy' do
          proxy = Proxy.new(http: 'proxy_url')
          capabilities = Capabilities.new(proxy: proxy)

          expect(capabilities.as_json).to eq('proxy' => {'proxyType' => 'manual',
                                                         'httpProxy' => 'proxy_url'})
        end

        it 'should accept a Hash' do
          capabilities = Capabilities.new(proxy: {http: 'foo:123'})
          expect(capabilities.proxy.http).to eq('foo:123')
        end

        it 'should not contain proxy hash when no proxy settings' do
          capabilities_hash = Capabilities.new.as_json
          expect(capabilities_hash).not_to have_key('proxy')
        end

        it 'can merge capabilities' do
          a = Capabilities.chrome
          b = Capabilities.firefox
          a.merge!(b)

          expect(a.browser_name).to eq('firefox')
        end

        it 'can be serialized and deserialized to JSON' do
          caps = Capabilities.new(browser_name: 'firefox', 'extension:customCapability': true)
          expect(caps).to eq(Capabilities.json_create(caps.as_json))
        end
      end
    end # Remote
  end # WebDriver
end # Selenium
