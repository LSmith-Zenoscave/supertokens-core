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

// Do not modify after and including this line
package io.supertokens.cli.licenseKey;

import com.google.gson.Gson;
import io.supertokens.cli.Utils;
import io.supertokens.cli.exception.QuitProgramException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class LicenseKey {

    public static final String LICENSE_KEY_SIGNATURE_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6OxC3L2q7N/gjQy4xGetHmKg0GnuEaN4iV82ED5IMeqL/h" +
                    "+XbPDfAML97hnHfvb+/gmY15jmdfgX2cfdJC5ZUEyasijZ7WrTkHMqLe+GBhKO" +
                    "/EGeSGHgsICA91NyMk8TrcHGOrk0FwyGvHURJ15ryQFAYmf2hEMv2VZy7XrUn4GqrNaxUmT+ycRKtZnAeHV" +
                    "+pyY2Dm3Xh3759B5byBSKEN8eXeIKwd/MtD17HnHy61nu0K4lOlA6a7YV53QL02bGxewl4qpxSnrmNM8qp5F+bAP" +
                    "/rpDxWN3o9n5c1xyCfSHB1gIf/DPV59LB/WJDY9x1rDT6chSVs2br+RvcAjhvvwIDAQAB";

    public static void verifyLicenseKeySignature(LicenseKeyContent content) {
        try {

            String toVerify = new Gson().toJson(content.getInfo());
            String hashFromInput = Utils.hashSHA256(toVerify);

            Signature sig = Signature.getInstance("SHA1WithRSA");
            PublicKey puK = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(LICENSE_KEY_SIGNATURE_PUBLIC_KEY)));
            sig.initVerify(puK);
            sig.update(hashFromInput.getBytes());

            if (!sig.verify(Base64.getDecoder().decode(content.getSignature()))) {
                throw new QuitProgramException(
                        "Failed to verify licenseKey signature. Please visit https://supertokens.io/dashboard to " +
                                "redownload your license key", null);
            }

        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new QuitProgramException(e.getMessage(), e);
        }
    }

    // if licenseKeyContent != null, then it will be used
    public static LicenseKeyContent loadAndCheckContent(String filePath) throws FileNotFoundException {

        Reader licenseKeyReader = new FileReader(new File(filePath));
        Gson gson = new Gson();
        LicenseKeyContent content = gson.fromJson(
                licenseKeyReader,
                LicenseKeyContent.class);

        content.setOriginalPlayType();

        verifyLicenseKeySignature(content);

        return content;
    }

    public enum MODE {
        PRODUCTION, DEV
    }

    public enum PLAN_TYPE {
        FREE("FREE");

        private String playType;

        PLAN_TYPE(String playType) {
            this.playType = playType;
        }

        @Override
        public String toString() {
            return playType;
        }
    }
}
// Do not modify before and including this line