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
package io.entgra.device.mgt.core.certificate.mgt.core.service;

import io.entgra.device.mgt.core.device.mgt.common.CertificatePaginationRequest;
import io.entgra.device.mgt.core.certificate.mgt.core.dto.CertificateResponse;
import io.entgra.device.mgt.core.certificate.mgt.core.dto.SCEPResponse;
import io.entgra.device.mgt.core.certificate.mgt.core.exception.CertificateManagementException;
import io.entgra.device.mgt.core.certificate.mgt.core.exception.KeystoreException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

public interface CertificateManagementService {

    Certificate getCACertificate() throws KeystoreException;

    Certificate getRACertificate() throws KeystoreException;

    List<X509Certificate> getRootCertificates(byte[] ca, byte[] ra) throws KeystoreException;

    X509Certificate generateX509Certificate() throws KeystoreException;

    SCEPResponse getCACertSCEP() throws KeystoreException;

    byte[] getCACapsSCEP();

    byte[] getPKIMessageSCEP(InputStream inputStream) throws KeystoreException;

    X509Certificate generateCertificateFromCSR(
            PrivateKey privateKey, PKCS10CertificationRequest request, String issueSubject) throws KeystoreException;

    Certificate getCertificateByAlias(String alias) throws KeystoreException;

    boolean verifySignature(String headerSignature) throws KeystoreException;

    CertificateResponse verifyPEMSignature(X509Certificate requestCertificate) throws KeystoreException;

    CertificateResponse verifySubjectDN(String requestDN) throws KeystoreException;

    X509Certificate extractCertificateFromSignature(String headerSignature) throws KeystoreException;

    String extractChallengeToken(X509Certificate certificate);

    X509Certificate getSignedCertificateFromCSR(String binarySecurityToken) throws KeystoreException;

    CertificateResponse getCertificateBySerial(String serial) throws KeystoreException;

    void saveCertificate(List<io.entgra.device.mgt.core.certificate.mgt.core.bean.Certificate> certificate)
            throws KeystoreException;

    X509Certificate pemToX509Certificate(String pem) throws KeystoreException;

    CertificateResponse retrieveCertificate(String serialNumber) throws CertificateManagementException;

    PaginationResult getAllCertificates(CertificatePaginationRequest request) throws CertificateManagementException;

    boolean removeCertificate(String serialNumber) throws CertificateManagementException;

    boolean getValidateMetaValue() throws CertificateManagementException;

    List<CertificateResponse> getCertificates() throws CertificateManagementException;

    List<CertificateResponse> searchCertificates(String serialNumber) throws CertificateManagementException;

    X509Certificate generateAlteredCertificateFromCSR(String csr) throws KeystoreException;

}
