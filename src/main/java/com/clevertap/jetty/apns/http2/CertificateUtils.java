/*
 * Copyright (c) 2016, CleverTap
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of CleverTap nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.clevertap.jetty.apns.http2;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities around .p12 certificates.
 */
public class CertificateUtils {
    /**
     * Split the subject into it's components such as UID, C, OU, CN.
     * <p>
     * Example:
     * {UID=com.wizrocket.BeardedRobot, C=US, OU=PNEY234A6B, CN=Apple Production IOS Push Services: com.wizrocket.BeardedRobot}
     *
     * @param certificate The certificate
     * @param password    The password
     * @return A map containing the components of the subject
     */
    public static Map<String, String> splitCertificateSubject(InputStream certificate, String password)
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        password = password == null ? "" : password;
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(certificate, password.toCharArray());


        String subject = ((X509Certificate) ks.getCertificate(ks.aliases().nextElement())).getSubjectDN().getName();
        HashMap<String, String> map = new HashMap<>();
        if (subject != null) {
            String[] parts = subject.split(",");
            for (String part : parts) {
                String[] kv = part.split("=");
                if (kv.length != 2) continue;

                map.put(kv[0].trim(), kv[1].trim());
            }
        }

        return map;
    }
}