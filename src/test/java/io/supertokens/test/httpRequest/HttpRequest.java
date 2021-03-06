/*
 *    Copyright (c) 2020, VRAI Labs and/or its affiliates. All rights reserved.
 *
 *    This program is licensed under the SuperTokens Community License (the
 *    "License") as published by VRAI Labs. You may not use this file except in
 *    compliance with the License. You are not permitted to transfer or
 *    redistribute this file without express written permission from VRAI Labs.
 *
 *    A copy of the License is available in the file titled
 *    "SuperTokensLicense.pdf" inside this repository or included with your copy of
 *    the software or its source code. If you have not received a copy of the
 *    License, please write to VRAI Labs at team@supertokens.io.
 *
 *    Please read the License carefully before accessing, downloading, copying,
 *    using, modifying, merging, transferring or sharing this software. By
 *    undertaking any of these activities, you indicate your agreement to the terms
 *    of the License.
 *
 *    This program is distributed with certain software that is licensed under
 *    separate terms, as designated in a particular file or component or in
 *    included license documentation. VRAI Labs hereby grants you an additional
 *    permission to link the program and your derivative works with the separately
 *    licensed software that they have included with this program, however if you
 *    modify this program, you shall be solely liable to ensure compliance of the
 *    modified program with the terms of licensing of the separately licensed
 *    software.
 *
 *    Unless required by applicable law or agreed to in writing, this program is
 *    distributed under the License on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *    CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *    specific language governing permissions and limitations under the License.
 *
 */

package io.supertokens.test.httpRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.supertokens.Main;
import io.supertokens.licenseKey.LicenseKey;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpRequest {
    private static final int STATUS_CODE_ERROR_THRESHOLD = 400;

    private static URL getURL(Main main, String requestID, String url) throws MalformedURLException {
        URL obj = new URL(url);
        if (Main.isTesting && LicenseKey.get(main).getMode() == LicenseKey.MODE.DEV) {
            URL mock = HttpRequestMocking.getInstance(main).getMockURL(requestID, url);
            if (mock != null) {
                obj = mock;
            }
        }
        return obj;
    }

    private static boolean isJsonValid(String jsonInString) {
        JsonElement el = null;
        try {
            el = new JsonParser().parse(jsonInString);
            el.getAsJsonObject();
            return true;
        } catch (Exception ex) {
            try {
                assert el != null;
                el.getAsJsonArray();
                return true;
            } catch (Throwable e) {
                return false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T sendGETRequest(Main main, String requestID, String url, Map<String, String> params,
                                       int connectionTimeoutMS, int readTimeoutMS, Integer version, String cdiVersion)
            throws IOException, io.supertokens.test.httpRequest.HttpResponseException {
        StringBuilder paramBuilder = new StringBuilder();

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                paramBuilder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(),
                        StandardCharsets.UTF_8))
                        .append("&");
            }
        }
        String paramsStr = paramBuilder.toString();
        if (!paramsStr.equals("")) {
            paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
            url = url + "?" + paramsStr;
        }
        URL obj = getURL(main, requestID, url);
        InputStream inputStream = null;
        HttpURLConnection con = null;

        try {
            con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(connectionTimeoutMS);
            con.setReadTimeout(readTimeoutMS);
            if (version != null) {
                con.setRequestProperty("api-version", version + "");
            }
            if (cdiVersion != null) {
                con.setRequestProperty("cdi-version", cdiVersion);
            }

            int responseCode = con.getResponseCode();

            if (responseCode < STATUS_CODE_ERROR_THRESHOLD) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            if (responseCode < STATUS_CODE_ERROR_THRESHOLD) {
                if (!isJsonValid(response.toString())) {
                    return (T) response.toString();
                }
                return (T) (new JsonParser().parse(response.toString()));
            }
            throw new io.supertokens.test.httpRequest.HttpResponseException(responseCode, response.toString());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (con != null) {
                con.disconnect();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T sendJsonRequest(Main main, String requestID, String url, JsonElement requestBody,
                                         int connectionTimeoutMS, int readTimeoutMS, Integer version, String cdiVersion,
                                         String method)
            throws IOException, io.supertokens.test.httpRequest.HttpResponseException {
        URL obj = getURL(main, requestID, url);
        InputStream inputStream = null;
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(method);
            con.setConnectTimeout(connectionTimeoutMS);
            con.setReadTimeout(readTimeoutMS);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (version != null) {
                con.setRequestProperty("api-version", version + "");
            }
            if (cdiVersion != null) {
                con.setRequestProperty("cdi-version", cdiVersion);
            }

            if (requestBody != null) {
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            int responseCode = con.getResponseCode();

            if (responseCode < STATUS_CODE_ERROR_THRESHOLD) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            if (responseCode < STATUS_CODE_ERROR_THRESHOLD) {
                if (!isJsonValid(response.toString())) {
                    return (T) response.toString();
                }
                return (T) (new JsonParser().parse(response.toString()));
            }
            throw new io.supertokens.test.httpRequest.HttpResponseException(responseCode, response.toString());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (con != null) {
                con.disconnect();
            }
        }
    }

    public static <T> T sendJsonPOSTRequest(Main main, String requestID, String url, JsonElement requestBody,
                                            int connectionTimeoutMS, int readTimeoutMS, Integer version,
                                            String cdiVersion)
            throws IOException, io.supertokens.test.httpRequest.HttpResponseException {
        return sendJsonRequest(main, requestID, url, requestBody, connectionTimeoutMS, readTimeoutMS, version,
                cdiVersion,
                "POST");
    }

    public static <T> T sendJsonPUTRequest(Main main, String requestID, String url, JsonElement requestBody,
                                           int connectionTimeoutMS, int readTimeoutMS, Integer version,
                                           String cdiVersion)
            throws IOException, io.supertokens.test.httpRequest.HttpResponseException {
        return sendJsonRequest(main, requestID, url, requestBody, connectionTimeoutMS, readTimeoutMS, version,
                cdiVersion,
                "PUT");
    }

    public static <T> T sendJsonDELETERequest(Main main, String requestID, String url, JsonElement requestBody,
                                              int connectionTimeoutMS, int readTimeoutMS, Integer version,
                                              String cdiVersion)
            throws IOException, HttpResponseException {
        return sendJsonRequest(main, requestID, url, requestBody, connectionTimeoutMS, readTimeoutMS, version,
                cdiVersion, "DELETE");
    }
}
