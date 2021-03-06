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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.supertokens.Main;
import io.supertokens.ProcessState;
import io.supertokens.exceptions.TokenTheftDetectedException;
import io.supertokens.exceptions.TryRefreshTokenException;
import io.supertokens.exceptions.UnauthorisedException;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.exceptions.StorageTransactionLogicException;
import io.supertokens.session.Session;
import io.supertokens.session.info.SessionInformationHolder;
import io.supertokens.storageLayer.StorageLayer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class SessionTest2 {

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
    public void tokenTheft_S1_R1_S2_R1()
            throws InterruptedException, StorageQueryException, NoSuchAlgorithmException, InvalidKeyException,
            IOException, InvalidKeySpecException,
            StorageTransactionLogicException, TryRefreshTokenException, UnauthorisedException,
            TokenTheftDetectedException, SignatureException {

        String[] args = {"../"};
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        Main main = process.getProcess();

        String userId = "userId";
        JsonObject userDataInJWT = new JsonObject();
        userDataInJWT.addProperty("key", "value");
        JsonObject userDataInDatabase = new JsonObject();
        userDataInDatabase.addProperty("key", "value");

        SessionInformationHolder sessionInfo = Session.createNewSession(main, userId, userDataInJWT,
                userDataInDatabase);
        assert sessionInfo.refreshToken != null;
        assert sessionInfo.accessToken != null;

        SessionInformationHolder newRefreshedSession = Session
                .refreshSession(main, sessionInfo.refreshToken.token);
        assert newRefreshedSession.refreshToken != null;
        assert newRefreshedSession.accessToken != null;

        SessionInformationHolder sessionObj = Session
                .getSession(main, newRefreshedSession.accessToken.token, newRefreshedSession.antiCsrfToken, true);
        assert sessionObj.accessToken != null;
        assertNotEquals(sessionObj.accessToken.token, newRefreshedSession.accessToken.token);

        try {
            Session.refreshSession(main, sessionInfo.refreshToken.token);
        } catch (TokenTheftDetectedException e) {
            assertEquals(e.sessionHandle, sessionInfo.session.handle);
            assertEquals(e.userId, sessionInfo.session.userId);
        }

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));

    }

    @Test
    public void tokenTheft_S1_R1_R2_R1()
            throws InterruptedException, StorageQueryException, NoSuchAlgorithmException, InvalidKeyException,
            IOException, InvalidKeySpecException,
            StorageTransactionLogicException, UnauthorisedException,
            TokenTheftDetectedException, SignatureException {

        String[] args = {"../"};
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        Main main = process.getProcess();

        String userId = "userId";
        JsonObject userDataInJWT = new JsonObject();
        userDataInJWT.addProperty("key", "value");
        JsonObject userDataInDatabase = new JsonObject();
        userDataInDatabase.addProperty("key", "value");

        SessionInformationHolder sessionInfo = Session.createNewSession(main, userId, userDataInJWT,
                userDataInDatabase);
        assert sessionInfo.refreshToken != null;
        assert sessionInfo.accessToken != null;

        SessionInformationHolder newRefreshedSession1 = Session
                .refreshSession(main, sessionInfo.refreshToken.token);
        assert newRefreshedSession1.refreshToken != null;
        assert newRefreshedSession1.accessToken != null;

        SessionInformationHolder newRefreshedSession2 = Session
                .refreshSession(main, newRefreshedSession1.refreshToken.token);
        assert newRefreshedSession2.refreshToken != null;
        assert newRefreshedSession2.accessToken != null;

        try {
            Session.refreshSession(main, sessionInfo.refreshToken.token);
        } catch (TokenTheftDetectedException e) {
            assertEquals(e.sessionHandle, sessionInfo.session.handle);
            assertEquals(e.userId, sessionInfo.session.userId);
        }

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));

    }

    @Test
    public void updateSessionInfo()
            throws InterruptedException, StorageQueryException, NoSuchAlgorithmException, InvalidKeyException,
            IOException, InvalidKeySpecException,
            StorageTransactionLogicException, UnauthorisedException, SignatureException {

        String[] args = {"../"};
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        String userId = "userId";
        JsonObject userDataInJWT = new JsonObject();
        userDataInJWT.addProperty("key", "value");
        JsonObject userDataInDatabase = new JsonObject();
        userDataInDatabase.addProperty("key", "value");

        SessionInformationHolder sessionInfo = Session.createNewSession(process.getProcess(), userId, userDataInJWT,
                userDataInDatabase);

        JsonObject sessionDataBeforeUpdate = Session.getSessionData(process.getProcess(), sessionInfo.session.handle);
        assertEquals(userDataInDatabase.toString(), sessionDataBeforeUpdate.toString());

        JsonObject userDataInDatabase2 = new JsonObject();
        userDataInDatabase2.addProperty("key1", "value1");
        userDataInDatabase2.addProperty("key2", 1);
        JsonArray arr = new JsonArray();
        userDataInDatabase2.add("key3", arr);

        Session.updateSession(process.getProcess(), sessionInfo.session.handle, userDataInDatabase2, null, null);

        JsonObject sessionDataAfterUpdate = Session.getSessionData(process.getProcess(), sessionInfo.session.handle);
        assertEquals(userDataInDatabase2.toString(), sessionDataAfterUpdate.toString());
        assertNotEquals(sessionDataBeforeUpdate.toString(), sessionDataAfterUpdate.toString());

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));

    }

    @Test
    public void revokeSessionWithoutBlacklisting()
            throws InterruptedException, StorageQueryException, NoSuchAlgorithmException, InvalidKeyException,
            IOException, InvalidKeySpecException,
            StorageTransactionLogicException, TokenTheftDetectedException, TryRefreshTokenException,
            UnauthorisedException, SignatureException {

        String[] args = {"../"};
        TestingProcessManager.TestingProcess process = TestingProcessManager.start(args);
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STARTED));

        String userId = "userId";
        JsonObject userDataInJWT = new JsonObject();
        userDataInJWT.addProperty("key", "value");
        JsonObject userDataInDatabase = new JsonObject();
        userDataInDatabase.addProperty("key", "value");

        SessionInformationHolder sessionInfo = Session.createNewSession(process.getProcess(), userId, userDataInJWT,
                userDataInDatabase);
        assert sessionInfo.refreshToken != null;
        assert sessionInfo.accessToken != null;

        Session.createNewSession(process.getProcess(), userId, userDataInJWT,
                userDataInDatabase);

        assertEquals(StorageLayer.getStorageLayer(process.getProcess()).getNumberOfPastTokens(), 2);
        assertEquals(StorageLayer.getStorageLayer(process.getProcess()).getNumberOfSessions(), 2);

        Session.revokeSessionUsingSessionHandles(process.getProcess(), new String[]{sessionInfo.session.handle});
        assertEquals(StorageLayer.getStorageLayer(process.getProcess()).getNumberOfPastTokens(), 2);
        assertEquals(StorageLayer.getStorageLayer(process.getProcess()).getNumberOfSessions(), 1);

        try {
            Session.refreshSession(process.getProcess(), sessionInfo.refreshToken.token);
            fail();
        } catch (UnauthorisedException e) {

        }

        SessionInformationHolder verifiedSession = Session
                .getSession(process.getProcess(), sessionInfo.accessToken.token, sessionInfo.antiCsrfToken, true);
        assertEquals(verifiedSession.session.userId, sessionInfo.session.userId);
        assertEquals(verifiedSession.session.userDataInJWT.toString(), sessionInfo.session.userDataInJWT.toString());

        process.kill();
        assertNotNull(process.checkOrWaitForEvent(ProcessState.PROCESS_STATE.STOPPED));

    }

}
