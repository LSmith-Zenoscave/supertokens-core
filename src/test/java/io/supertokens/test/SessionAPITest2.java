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

package io.supertokens.test;

import com.google.gson.JsonObject;
import io.supertokens.ProcessState;
import io.supertokens.config.Config;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertNotNull;


public class SessionAPITest2 {

    @Rule
    public TestRule watchman = Utils.getOnFailure();

    @AfterClass
    public static void afterTesting() {
        Utils.afterTesting();
    }

    @Before
    public void beforeEach() {
        Utils.reset();
    }

    @Test
    public void successOutputCheckWithAntiCsrf() throws Exception {
        String[] args = {"../"};

        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        String userId = "userId";
        JsonObject userDataInJWT = new JsonObject();
        userDataInJWT.addProperty("key", "value");
        JsonObject userDataInDatabase = new JsonObject();
        userDataInDatabase.addProperty("key", "value");

        JsonObject request = new JsonObject();
        request.addProperty("userId", userId);
        request.add("userDataInJWT", userDataInJWT);
        request.add("userDataInDatabase", userDataInDatabase);

        JsonObject response = io.supertokens.test.httpRequest.HttpRequest
                .sendJsonPOSTRequest(process.getProcess(), "", "http://localhost:3567/session", request, 1000, 1000,
                        null, Utils.getCdiVersion2ForTests());

        checkVersion2SessionResponse(response, process, userId, userDataInJWT);
        assertTrue(response.has("antiCsrfToken"));

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));


    }

    @Test
    public void successOutputCheckWithNoAntiCsrf() throws Exception {
        Utils.setValueInConfig("enable_anti_csrf", "false");

        String[] args = {"../"};
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        String userId = "userId";
        JsonObject userDataInJWT = new JsonObject();
        userDataInJWT.addProperty("key", "value");
        JsonObject userDataInDatabase = new JsonObject();
        userDataInDatabase.addProperty("key", "value");

        JsonObject request = new JsonObject();
        request.addProperty("userId", userId);
        request.add("userDataInJWT", userDataInJWT);
        request.add("userDataInDatabase", userDataInDatabase);

        JsonObject response = io.supertokens.test.httpRequest.HttpRequest
                .sendJsonPOSTRequest(process.getProcess(), "", "http://localhost:3567/session", request, 1000, 1000,
                        null, Utils.getCdiVersion2ForTests());
        checkVersion2SessionResponse(response, process, userId, userDataInJWT);
        assertFalse(response.has("antiCsrfToken"));

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));


    }

    // *  - check that config same site change is reflecting in the API
    @Test
    public void testThatConfigSameSiteChangeIsReflectedInAPI() throws Exception {
        String[] args = {"../"};
        Utils.setValueInConfig("cookie_same_site", "lax");
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));


        String userId = "userId";
        JsonObject userDataInJWT = new JsonObject();
        userDataInJWT.addProperty("key", "value");
        JsonObject userDataInDatabase = new JsonObject();
        userDataInDatabase.addProperty("key", "value");

        JsonObject request = new JsonObject();
        request.addProperty("userId", userId);
        request.add("userDataInJWT", userDataInJWT);
        request.add("userDataInDatabase", userDataInDatabase);

        JsonObject response = io.supertokens.test.httpRequest.HttpRequest
                .sendJsonPOSTRequest(process.getProcess(), "", "http://localhost:3567/session", request, 1000, 1000,
                        null, Utils.getCdiVersion2ForTests());
        assertEquals(response.get("status").getAsString(), "OK");
        assertEquals(response.get("accessToken").getAsJsonObject().get("sameSite").getAsString(), "lax");
        assertEquals(response.get("refreshToken").getAsJsonObject().get("sameSite").getAsString(), "lax");
        assertEquals(response.get("idRefreshToken").getAsJsonObject().get("sameSite").getAsString(), "lax");

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
    }

    // *  - check that version 2.0 is unsupported for /session DELETE
    @Test
    public void testThatVersion2IsNotSupportedBySessionDelete() throws Exception {
        String[] args = {"../"};
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        String userId = "userId";
        JsonObject userDataInJWT = new JsonObject();
        userDataInJWT.addProperty("key", "value");
        JsonObject userDataInDatabase = new JsonObject();
        userDataInDatabase.addProperty("key", "value");

        JsonObject request = new JsonObject();
        request.addProperty("userId", userId);
        request.add("userDataInJWT", userDataInJWT);
        request.add("userDataInDatabase", userDataInDatabase);

        JsonObject response = io.supertokens.test.httpRequest.HttpRequest.sendJsonPOSTRequest(process.getProcess(), "",
                "http://localhost:3567/session", request, 1000, 1000, null, Utils.getCdiVersion2ForTests());

        assertEquals(response.get("status").getAsString(), "OK");

        JsonObject sessionDeleteBody = new JsonObject();
        sessionDeleteBody.addProperty("userId", userId);

        try {
            io.supertokens.test.httpRequest.HttpRequest
                    .sendJsonDELETERequest(process.getProcess(), "", "http://localhost:3567/session", sessionDeleteBody,
                            1000, 1000, null, Utils.getCdiVersion2ForTests());
            fail();
        } catch (io.supertokens.test.httpRequest.HttpResponseException e) {
            assertEquals(e.getMessage(),
                    "Http error. Status Code: 400. Message: /session DELETE is only available in CDI 1.0. Please call" +
                            " /session/remove POST instead");
        }
        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));

    }

    @Test
    public void badInputTest() throws Exception {

        String[] args = {"../"};
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        String userId = "userId";
        JsonObject userDataInJWT = new JsonObject();
        userDataInJWT.addProperty("key", "value");
        JsonObject userDataInDatabase = new JsonObject();
        userDataInDatabase.addProperty("key", "value");

        try {
            JsonObject request = new JsonObject();
            request.add("userDataInJWT", userDataInJWT);
            request.add("userDataInDatabase", userDataInDatabase);
            io.supertokens.test.httpRequest.HttpRequest.sendJsonPOSTRequest(process.getProcess(), "",
                    "http://localhost:3567/session", request, 1000, 1000, null, Utils.getCdiVersion2ForTests());
            fail();
        } catch (io.supertokens.test.httpRequest.HttpResponseException e) {
            assertEquals(e.statusCode, 400);
            assertEquals(e.getMessage(),
                    "Http error. Status Code: 400. Message: Field name 'userId' is invalid in JSON input");
        }

        try {
            JsonObject request = new JsonObject();
            request.addProperty("userId", userId);
            request.add("userDataInDatabase", userDataInDatabase);
            io.supertokens.test.httpRequest.HttpRequest.sendJsonPOSTRequest(process.getProcess(), "",
                    "http://localhost:3567/session", request, 1000, 1000, null, Utils.getCdiVersion2ForTests());
            fail();
        } catch (io.supertokens.test.httpRequest.HttpResponseException e) {
            assertEquals(e.statusCode, 400);
            assertEquals(e.getMessage(),
                    "Http error. Status Code: 400. Message: Field name 'userDataInJWT' is invalid in JSON input");
        }

        try {
            JsonObject request = new JsonObject();
            request.addProperty("userId", userId);
            request.add("userDataInJWT", userDataInJWT);
            io.supertokens.test.httpRequest.HttpRequest.sendJsonPOSTRequest(process.getProcess(), "",
                    "http://localhost:3567/session", request, 1000, 1000, null, Utils.getCdiVersion2ForTests());
            fail();
        } catch (io.supertokens.test.httpRequest.HttpResponseException e) {
            assertEquals(e.statusCode, 400);
            assertEquals(e.getMessage(),
                    "Http error. Status Code: 400. Message: Field name 'userDataInDatabase' is invalid in JSON " +
                            "input");
        }

        try {
            JsonObject request = new JsonObject();
            request.addProperty("userId", userId);
            request.add("userDataInJWT", userDataInJWT);
            io.supertokens.test.httpRequest.HttpRequest.sendJsonPOSTRequest(process.getProcess(), "",
                    "http://localhost:3567/session", request, 1000, 1000, null, Utils.getCdiVersion2ForTests());
            fail();
        } catch (io.supertokens.test.httpRequest.HttpResponseException e) {
            assertEquals(e.statusCode, 400);
            assertEquals(e.getMessage(),
                    "Http error. Status Code: 400. Message: Field name 'userDataInDatabase' is invalid in JSON " +
                            "input");
        }

        JsonObject request = new JsonObject();
        request.addProperty("userId", userId);
        request.add("userDataInJWT", userDataInJWT);
        request.add("userDataInDatabase", userDataInDatabase);
        io.supertokens.test.httpRequest.HttpRequest.sendJsonPOSTRequest(process.getProcess(), "",
                "http://localhost:3567/session", request, 1000, 1000, null, Utils.getCdiVersion2ForTests());

        request = new JsonObject();
        request.addProperty("userId", userId);
        request.add("userDataInJWT", userDataInJWT);
        request.add("userDataInDatabase", userDataInDatabase);
        io.supertokens.test.httpRequest.HttpRequest.sendJsonPOSTRequest(process.getProcess(), "",
                "http://localhost:3567/session", request, 1000, 1000, null, Utils.getCdiVersion2ForTests());

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));
    }

    public static void checkVersion2SessionResponse(JsonObject response, TestingProcessManager.TestingProcess process,
                                                    String userId, JsonObject userDataInJWT) {
        assertNotNull(response.get("session").getAsJsonObject().get("handle").getAsString());
        assertEquals(response.get("session").getAsJsonObject().get("userId").getAsString(), userId);
        assertEquals(response.get("session").getAsJsonObject().get("userDataInJWT").getAsJsonObject().toString(),
                userDataInJWT.toString());
        assertEquals(response.get("session").getAsJsonObject().entrySet().size(), 3);

        assertTrue(response.get("accessToken").getAsJsonObject().has("token"));
        assertTrue(response.get("accessToken").getAsJsonObject().has("expiry"));
        assertTrue(response.get("accessToken").getAsJsonObject().has("createdTime"));
        assertTrue(response.get("accessToken").getAsJsonObject().has("cookiePath"));
        assertEquals(response.get("accessToken").getAsJsonObject().get("cookiePath").getAsString(),
                Config.getConfig(process.getProcess()).getAccessTokenPath());
        assertTrue(response.get("accessToken").getAsJsonObject().has("cookieSecure"));
        assertEquals(response.get("accessToken").getAsJsonObject().get("cookieSecure").getAsBoolean(),
                Config.getConfig(process.getProcess()).getCookieSecure(process.getProcess()));
        assertEquals(response.get("accessToken").getAsJsonObject().get("domain").getAsString(),
                Config.getConfig(process.getProcess()).getCookieDomain());
        assertEquals(response.get("accessToken").getAsJsonObject().get("sameSite").getAsString(),
                Config.getConfig(process.getProcess()).getCookieSameSite());
        assertEquals(response.get("accessToken").getAsJsonObject().entrySet().size(), 7);

        assertTrue(response.get("refreshToken").getAsJsonObject().has("token"));
        assertTrue(response.get("refreshToken").getAsJsonObject().has("expiry"));
        assertTrue(response.get("refreshToken").getAsJsonObject().has("createdTime"));
        assertTrue(response.get("refreshToken").getAsJsonObject().has("cookiePath"));
        assertEquals(response.get("refreshToken").getAsJsonObject().get("cookiePath").getAsString(),
                Config.getConfig(process.getProcess()).getRefreshAPIPath());
        assertTrue(response.get("refreshToken").getAsJsonObject().has("cookieSecure"));
        assertEquals(response.get("refreshToken").getAsJsonObject().get("cookieSecure").getAsBoolean(),
                Config.getConfig(process.getProcess()).getCookieSecure(process.getProcess()));
        assertEquals(response.get("refreshToken").getAsJsonObject().get("domain").getAsString(),
                Config.getConfig(process.getProcess()).getCookieDomain());
        assertEquals(response.get("refreshToken").getAsJsonObject().get("sameSite").getAsString(),
                Config.getConfig(process.getProcess()).getCookieSameSite());
        assertEquals(response.get("refreshToken").getAsJsonObject().entrySet().size(), 7);

        assertTrue(response.get("idRefreshToken").getAsJsonObject().has("token"));
        assertTrue(response.get("idRefreshToken").getAsJsonObject().has("expiry"));
        assertTrue(response.get("idRefreshToken").getAsJsonObject().has("createdTime"));
        assertEquals(response.get("idRefreshToken").getAsJsonObject().get("cookiePath").getAsString(),
                Config.getConfig(process.getProcess()).getAccessTokenPath());
        assertEquals(response.get("idRefreshToken").getAsJsonObject().get("cookieSecure").getAsBoolean(),
                Config.getConfig(process.getProcess()).getCookieSecure(process.getProcess()));
        assertEquals(response.get("idRefreshToken").getAsJsonObject().get("domain").getAsString(),
                Config.getConfig(process.getProcess()).getCookieDomain());
        assertEquals(response.get("idRefreshToken").getAsJsonObject().get("sameSite").getAsString(),
                Config.getConfig(process.getProcess()).getCookieSameSite());
        assertEquals(response.get("idRefreshToken").getAsJsonObject().entrySet().size(), 7);

        assertTrue(response.has("jwtSigningPublicKey"));
        assertTrue(response.has("jwtSigningPublicKeyExpiryTime"));
    }
}
