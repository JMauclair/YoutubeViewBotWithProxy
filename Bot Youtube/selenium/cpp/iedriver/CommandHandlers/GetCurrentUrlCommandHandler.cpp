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

#include "GetCurrentUrlCommandHandler.h"
#include "errorcodes.h"
#include "logging.h"
#include "../Browser.h"
#include "../IECommandExecutor.h"
#include "../StringUtilities.h"

namespace webdriver {

GetCurrentUrlCommandHandler::GetCurrentUrlCommandHandler(void) {
}

GetCurrentUrlCommandHandler::~GetCurrentUrlCommandHandler(void) {
}

void GetCurrentUrlCommandHandler::ExecuteInternal(
    const IECommandExecutor& executor,
    const ParametersMap& command_parameters,
    Response* response) {
  BrowserHandle browser_wrapper;
  int status_code = executor.GetCurrentBrowser(&browser_wrapper);
  if (status_code != WD_SUCCESS) {
    response->SetErrorResponse(ERROR_NO_SUCH_WINDOW, "Unable to get browser");
    return;
  }

  // Start with the browser URL.
  std::string current_url = browser_wrapper->GetBrowserUrl();
  CComPtr<IHTMLDocument2> top_level_document;
  browser_wrapper->GetDocument(true, &top_level_document);
  if (!top_level_document) {
    LOG(WARN) << "Unable to get document from browser. Are you viewing a "
              << "non-HTML document? Falling back to potentially "
              << "inconsistent method for obtaining URL.";
  } else {
    CComBSTR url;
    HRESULT hr = top_level_document->get_URL(&url);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "IHTMLDocument2::get_URL failed.";
    }
    
    std::wstring converted_url(url, ::SysStringLen(url));

    // HACK: If the URL starts with "res://", an internal
    // resource was loaded, so don't get the document URL.
    if (converted_url.find_first_of(L"res://") != 0) {
      current_url = StringUtilities::ToString(converted_url);
    }
  }

  response->SetSuccessResponse(current_url);
}

} // namespace webdriver
