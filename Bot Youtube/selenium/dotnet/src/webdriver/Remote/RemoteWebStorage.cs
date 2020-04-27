﻿// <copyright file="RemoteWebStorage.cs" company="WebDriver Committers">
// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements. See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership. The SFC licenses this file
// to you under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// </copyright>

using OpenQA.Selenium.Html5;

namespace OpenQA.Selenium.Remote
{
    /// <summary>
    /// Provides remote access to the <see cref="IWebStorage"/> API.
    /// </summary>
    public class RemoteWebStorage : IWebStorage
    {
        private RemoteWebDriver driver;

        /// <summary>
        /// Initializes a new instance of the <see cref="RemoteWebStorage"/> class.
        /// </summary>
        /// <param name="driver">The driver instance.</param>
        public RemoteWebStorage(RemoteWebDriver driver)
        {
            this.driver = driver;
        }

        /// <summary>
        /// Gets the local storage for the site currently opened in the browser.
        /// </summary>
        public ILocalStorage LocalStorage
        {
            get
            {
                return new RemoteLocalStorage(this.driver);
            }
        }

        /// <summary>
        /// Gets the session storage for the site currently opened in the browser.
        /// </summary>
        public ISessionStorage SessionStorage
        {
            get
            {
                return new RemoteSessionStorage(this.driver);
            }
        }
    }
}
