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

module Selenium
  module WebDriver
    module SpecSupport
      module Helpers
        def driver
          GlobalTestEnv.driver_instance
        end

        def reset_driver!(time = 0)
          GlobalTestEnv.reset_driver!(time)
        end

        def quit_driver
          GlobalTestEnv.quit_driver
        end

        def create_driver!(**opts, &block)
          GlobalTestEnv.create_driver!(opts, &block)
        end

        def ensure_single_window
          GlobalTestEnv.ensure_single_window
        end

        def url_for(filename)
          GlobalTestEnv.url_for filename
        end

        def fix_windows_path(path)
          return path unless WebDriver::Platform.windows?

          if GlobalTestEnv.browser == :ie
            path = path[%r{file://(.*)}, 1]
            path = WebDriver::Platform.windows_path(path)

            "file://#{path}"
          else
            path.sub(%r[file:/{0,2}], 'file:///')
          end
        end

        def long_wait
          @long_wait ||= Wait.new(timeout: 30)
        end

        def short_wait
          @short_wait ||= Wait.new(timeout: 3)
        end

        def wait_for_alert
          wait = Wait.new(timeout: 5, ignore: Error::NoSuchAlertError)
          wait.until { driver.switch_to.alert }
        end

        def wait_for_no_alert
          wait = Wait.new(timeout: 5, ignore: Error::UnexpectedAlertOpenError)
          wait.until { driver.title }
        end

        def wait_for_element(locator)
          wait = Wait.new(timeout: 25, ignore: Error::NoSuchElementError)
          wait.until { driver.find_element(locator) }
        end

        def wait(timeout = 10)
          Wait.new(timeout: timeout)
        end
      end # Helpers
    end # SpecSupport
  end # WebDriver
end # Selenium
