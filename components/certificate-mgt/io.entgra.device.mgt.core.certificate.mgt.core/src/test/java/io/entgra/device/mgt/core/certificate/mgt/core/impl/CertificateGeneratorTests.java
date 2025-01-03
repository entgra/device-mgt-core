/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.certificate.mgt.core.impl;

import io.entgra.device.mgt.core.certificate.mgt.core.exception.KeystoreException;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

public class CertificateGeneratorTests {

    private static final Log log = LogFactory.getLog(CertificateGeneratorTests.class);

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testVerifyNullPEMSignature() {
        CertificateGenerator certGenerator = new CertificateGenerator();
        try {
            certGenerator.verifyPEMSignature(null);
        } catch (KeystoreException e) {
            log.error("Error occurred while verifying PEM signature", e);
            Assert.fail();
        }
    }

}
