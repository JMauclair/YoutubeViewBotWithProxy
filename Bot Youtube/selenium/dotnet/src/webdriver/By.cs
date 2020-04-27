// <copyright file="By.cs" company="WebDriver Committers">
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

using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Globalization;
using System.Text.RegularExpressions;
using OpenQA.Selenium.Internal;

namespace OpenQA.Selenium
{
    /// <summary>
    /// Provides a mechanism by which to find elements within a document.
    /// </summary>
    /// <remarks>It is possible to create your own locating mechanisms for finding documents.
    /// In order to do this,subclass this class and override the protected methods. However,
    /// it is expected that that all subclasses rely on the basic finding mechanisms provided
    /// through static methods of this class. An example of this can be found in OpenQA.Support.ByIdOrName
    /// </remarks>
    [Serializable]
    public class By
    {
        private string description = "OpenQA.Selenium.By";
        private string mechanism = string.Empty;
        private string criteria = string.Empty;
        private Func<ISearchContext, IWebElement> findElementMethod;
        private Func<ISearchContext, ReadOnlyCollection<IWebElement>> findElementsMethod;

        /// <summary>
        /// Initializes a new instance of the <see cref="By"/> class.
        /// </summary>
        protected By()
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="By"/> class using the given functions to find elements.
        /// </summary>
        /// <param name="findElementMethod">A function that takes an object implementing <see cref="ISearchContext"/>
        /// and returns the found <see cref="IWebElement"/>.</param>
        /// <param name="findElementsMethod">A function that takes an object implementing <see cref="ISearchContext"/>
        /// and returns a <see cref="ReadOnlyCollection{T}"/> of the found<see cref="IWebElement">IWebElements</see>.
        /// <see cref="IWebElement">IWebElements</see>/>.</param>
        protected By(Func<ISearchContext, IWebElement> findElementMethod, Func<ISearchContext, ReadOnlyCollection<IWebElement>> findElementsMethod)
        {
            this.findElementMethod = findElementMethod;
            this.findElementsMethod = findElementsMethod;
        }

        /// <summary>
        /// Gets the value of the mechanism for this <see cref="By"/> class instance.
        /// </summary>
        public string Mechanism
        {
            get { return this.mechanism; }
        }

        /// <summary>
        /// Gets the value of the criteria for this <see cref="By"/> class instance.
        /// </summary>
        public string Criteria
        {
            get { return this.criteria; }
        }

        /// <summary>
        /// Gets or sets the value of the description for this <see cref="By"/> class instance.
        /// </summary>
        protected string Description
        {
            get { return this.description; }
            set { this.description = value; }
        }

        /// <summary>
        /// Gets or sets the method used to find a single element matching specified criteria.
        /// </summary>
        protected Func<ISearchContext, IWebElement> FindElementMethod
        {
            get { return this.findElementMethod; }
            set { this.findElementMethod = value; }
        }

        /// <summary>
        /// Gets or sets the method used to find all elements matching specified criteria.
        /// </summary>
        protected Func<ISearchContext, ReadOnlyCollection<IWebElement>> FindElementsMethod
        {
            get { return this.findElementsMethod; }
            set { this.findElementsMethod = value; }
        }

        /// <summary>
        /// Determines if two <see cref="By"/> instances are equal.
        /// </summary>
        /// <param name="one">One instance to compare.</param>
        /// <param name="two">The other instance to compare.</param>
        /// <returns><see langword="true"/> if the two instances are equal; otherwise, <see langword="false"/>.</returns>
        public static bool operator ==(By one, By two)
        {
            // If both are null, or both are same instance, return true.
            if (object.ReferenceEquals(one, two))
            {
                return true;
            }

            // If one is null, but not both, return false.
            if (((object)one == null) || ((object)two == null))
            {
                return false;
            }

            return one.Equals(two);
        }

        /// <summary>
        /// Determines if two <see cref="By"/> instances are unequal.
        /// </summary>s
        /// <param name="one">One instance to compare.</param>
        /// <param name="two">The other instance to compare.</param>
        /// <returns><see langword="true"/> if the two instances are not equal; otherwise, <see langword="false"/>.</returns>
        public static bool operator !=(By one, By two)
        {
            return !(one == two);
        }

        /// <summary>
        /// Gets a mechanism to find elements by their ID.
        /// </summary>
        /// <param name="idToFind">The ID to find.</param>
        /// <returns>A <see cref="By"/> object the driver can use to find the elements.</returns>
        public static By Id(string idToFind)
        {
            if (idToFind == null)
            {
                throw new ArgumentNullException("idToFind", "Cannot find elements with a null id attribute.");
            }

            By by = new By();
            by.description = "By.Id: " + idToFind;
            by.mechanism = "css selector";
            string selector = By.EscapeCssSelector(idToFind);
            by.criteria = "#" + selector;

            by.findElementMethod = (ISearchContext context) => ((IFindsElement)context).FindElement(by.mechanism, by.criteria);
            if (string.IsNullOrEmpty(selector))
            {
                // Finding multiple elements with an empty ID will return
                // an empty list. However, finding by a CSS selector of '#'
                // throws an exception, even in the multiple elements case,
                // which means we need to short-circuit that behavior.
                by.findElementsMethod = (ISearchContext context) => new List<IWebElement>().AsReadOnly();
            }
            else
            {
                by.findElementsMethod = (ISearchContext context) => ((IFindsElement)context).FindElements(by.mechanism, by.criteria);
            }

            return by;
        }

        /// <summary>
        /// Gets a mechanism to find elements by their link text.
        /// </summary>
        /// <param name="linkTextToFind">The link text to find.</param>
        /// <returns>A <see cref="By"/> object the driver can use to find the elements.</returns>
        public static By LinkText(string linkTextToFind)
        {
            if (linkTextToFind == null)
            {
                throw new ArgumentNullException("linkTextToFind", "Cannot find elements when link text is null.");
            }

            By by = new By();
            by.description = "By.LinkText: " + linkTextToFind;
            by.mechanism = "link text";
            by.criteria = linkTextToFind;

            by.findElementMethod = (ISearchContext context) => ((IFindsElement)context).FindElement(by.mechanism, by.criteria);
            by.findElementsMethod = (ISearchContext context) => ((IFindsElement)context).FindElements(by.mechanism, by.criteria);
            return by;
        }

        /// <summary>
        /// Gets a mechanism to find elements by their name.
        /// </summary>
        /// <param name="nameToFind">The name to find.</param>
        /// <returns>A <see cref="By"/> object the driver can use to find the elements.</returns>
        public static By Name(string nameToFind)
        {
            if (nameToFind == null)
            {
                throw new ArgumentNullException("nameToFind", "Cannot find elements when name text is null.");
            }

            By by = new By();
            by.description = "By.Name: " + nameToFind;
            by.mechanism = "css selector";
            by.criteria = "*[name =\"" + By.EscapeCssSelector(nameToFind) + "\"]";

            by.findElementMethod = (ISearchContext context) => ((IFindsElement)context).FindElement(by.mechanism, by.criteria);
            by.findElementsMethod = (ISearchContext context) => ((IFindsElement)context).FindElements(by.mechanism, by.criteria);
            return by;
        }

        /// <summary>
        /// Gets a mechanism to find elements by an XPath query.
        /// When searching within a WebElement using xpath be aware that WebDriver follows standard conventions:
        /// a search prefixed with "//" will search the entire document, not just the children of this current node.
        /// Use ".//" to limit your search to the children of this WebElement.
        /// </summary>
        /// <param name="xpathToFind">The XPath query to use.</param>
        /// <returns>A <see cref="By"/> object the driver can use to find the elements.</returns>
        public static By XPath(string xpathToFind)
        {
            if (xpathToFind == null)
            {
                throw new ArgumentNullException("xpathToFind", "Cannot find elements when the XPath expression is null.");
            }

            By by = new By();
            by.description = "By.XPath: " + xpathToFind;
            by.mechanism = "xpath";
            by.criteria = xpathToFind;

            by.findElementMethod = (ISearchContext context) => ((IFindsElement)context).FindElement(by.mechanism, by.criteria);
            by.findElementsMethod = (ISearchContext context) => ((IFindsElement)context).FindElements(by.mechanism, by.criteria);
            return by;
        }

        /// <summary>
        /// Gets a mechanism to find elements by their CSS class.
        /// </summary>
        /// <param name="classNameToFind">The CSS class to find.</param>
        /// <returns>A <see cref="By"/> object the driver can use to find the elements.</returns>
        /// <remarks>If an element has many classes then this will match against each of them.
        /// For example if the value is "one two onone", then the following values for the
        /// className parameter will match: "one" and "two".</remarks>
        public static By ClassName(string classNameToFind)
        {
            if (classNameToFind == null)
            {
                throw new ArgumentNullException("classNameToFind", "Cannot find elements when the class name expression is null.");
            }

            By by = new By();
            by.description = "By.ClassName[Contains]: " + classNameToFind;
            by.mechanism = "css selector";
            by.criteria = "." + By.EscapeCssSelector(classNameToFind);
            if (by.criteria.Contains(" "))
            {
                // Finding elements by class name with whitespace is not allowed.
                // However, converting the single class name to a valid CSS selector
                // by prepending a '.' may result in a still-valid, but incorrect
                // selector. Thus, we short-ciruit that behavior here.
                throw new InvalidSelectorException("Compound class names not allowed. Cannot have whitespace in class name. Use CSS selectors instead.");
            }

            by.findElementMethod = (ISearchContext context) => ((IFindsElement)context).FindElement(by.mechanism, by.criteria);
            by.findElementsMethod = (ISearchContext context) => ((IFindsElement)context).FindElements(by.mechanism, by.criteria);
            return by;
        }

        /// <summary>
        /// Gets a mechanism to find elements by a partial match on their link text.
        /// </summary>
        /// <param name="partialLinkTextToFind">The partial link text to find.</param>
        /// <returns>A <see cref="By"/> object the driver can use to find the elements.</returns>
        public static By PartialLinkText(string partialLinkTextToFind)
        {
            if (partialLinkTextToFind == null)
            {
                throw new ArgumentNullException("partialLinkTextToFind", "Cannot find elements when partial link text is null.");
            }

            By by = new By();
            by.description = "By.PartialLinkText: " + partialLinkTextToFind;
            by.mechanism = "partial link text";
            by.criteria = partialLinkTextToFind;

            by.findElementMethod = (ISearchContext context) => ((IFindsElement)context).FindElement(by.mechanism, by.criteria);
            by.findElementsMethod = (ISearchContext context) => ((IFindsElement)context).FindElements(by.mechanism, by.criteria);
            return by;
        }

        /// <summary>
        /// Gets a mechanism to find elements by their tag name.
        /// </summary>
        /// <param name="tagNameToFind">The tag name to find.</param>
        /// <returns>A <see cref="By"/> object the driver can use to find the elements.</returns>
        public static By TagName(string tagNameToFind)
        {
            if (tagNameToFind == null)
            {
                throw new ArgumentNullException("tagNameToFind", "Cannot find elements when name tag name is null.");
            }

            By by = new By();
            by.description = "By.TagName: " + tagNameToFind;
            by.mechanism = "css selector";
            by.criteria = tagNameToFind;

            by.findElementMethod = (ISearchContext context) => ((IFindsElement)context).FindElement(by.mechanism, by.criteria);
            by.findElementsMethod = (ISearchContext context) => ((IFindsElement)context).FindElements(by.mechanism, by.criteria);
            return by;
        }

        /// <summary>
        /// Gets a mechanism to find elements by their cascading style sheet (CSS) selector.
        /// </summary>
        /// <param name="cssSelectorToFind">The CSS selector to find.</param>
        /// <returns>A <see cref="By"/> object the driver can use to find the elements.</returns>
        public static By CssSelector(string cssSelectorToFind)
        {
            if (cssSelectorToFind == null)
            {
                throw new ArgumentNullException("cssSelectorToFind", "Cannot find elements when name CSS selector is null.");
            }

            By by = new By();
            by.description = "By.CssSelector: " + cssSelectorToFind;
            by.mechanism = "css selector";
            by.criteria = cssSelectorToFind;

            by.findElementMethod = (ISearchContext context) => ((IFindsElement)context).FindElement(by.mechanism, by.criteria);
            by.findElementsMethod = (ISearchContext context) => ((IFindsElement)context).FindElements(by.mechanism, by.criteria);
            return by;
        }

        /// <summary>
        /// Finds the first element matching the criteria.
        /// </summary>
        /// <param name="context">An <see cref="ISearchContext"/> object to use to search for the elements.</param>
        /// <returns>The first matching <see cref="IWebElement"/> on the current context.</returns>
        public virtual IWebElement FindElement(ISearchContext context)
        {
            return this.findElementMethod(context);
        }

        /// <summary>
        /// Finds all elements matching the criteria.
        /// </summary>
        /// <param name="context">An <see cref="ISearchContext"/> object to use to search for the elements.</param>
        /// <returns>A <see cref="ReadOnlyCollection{T}"/> of all <see cref="IWebElement">WebElements</see>
        /// matching the current criteria, or an empty list if nothing matches.</returns>
        public virtual ReadOnlyCollection<IWebElement> FindElements(ISearchContext context)
        {
            return this.findElementsMethod(context);
        }

        /// <summary>
        /// Gets a string representation of the finder.
        /// </summary>
        /// <returns>The string displaying the finder content.</returns>
        public override string ToString()
        {
            return this.description;
        }

        /// <summary>
        /// Determines whether the specified <see cref="object">Object</see> is equal
        /// to the current <see cref="object">Object</see>.
        /// </summary>
        /// <param name="obj">The <see cref="object">Object</see> to compare with the
        /// current <see cref="object">Object</see>.</param>
        /// <returns><see langword="true"/> if the specified <see cref="object">Object</see>
        /// is equal to the current <see cref="object">Object</see>; otherwise,
        /// <see langword="false"/>.</returns>
        public override bool Equals(object obj)
        {
            var other = obj as By;

            // TODO(dawagner): This isn't ideal
            return other != null && this.description.Equals(other.description);
        }

        /// <summary>
        /// Serves as a hash function for a particular type.
        /// </summary>
        /// <returns>A hash code for the current <see cref="object">Object</see>.</returns>
        public override int GetHashCode()
        {
            return this.description.GetHashCode();
        }

        /// <summary>
        /// Escapes invalid characters in a CSS selector.
        /// </summary>
        /// <param name="selector">The selector to escape.</param>
        /// <returns>The selector with invalid characters escaped.</returns>
        internal static string EscapeCssSelector(string selector)
        {
            string escaped = Regex.Replace(selector, @"([ '""\\#.:;,!?+<>=~*^$|%&@`{}\-/\[\]\(\)])", @"\$1");
            if (selector.Length > 0 && char.IsDigit(selector[0]))
            {
                escaped = @"\" + (30 + int.Parse(selector.Substring(0, 1), CultureInfo.InvariantCulture)).ToString(CultureInfo.InvariantCulture) + " " + selector.Substring(1);
            }

            return escaped;
        }
    }
}
