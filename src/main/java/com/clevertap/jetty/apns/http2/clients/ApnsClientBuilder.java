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

package com.clevertap.jetty.apns.http2.clients;

import com.clevertap.jetty.apns.http2.ApnsClient;
import okhttp3.ConnectionPool;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Semaphore;

/**
 * A builder to build an APNS client.
 */
public class ApnsClientBuilder {
    private InputStream certificate;
    private boolean production;
    private String password;

    private boolean asynchronous = false;
    private int maxQueuedNotifications = 2000;
    private Semaphore semaphore;
    private String defaultTopic = null;

    private boolean useOkHttp3 = false;
    private ConnectionPool connectionPool;

    public ApnsClientBuilder withOkHttp3() {
        useOkHttp3 = true;
        return this;
    }

    public ApnsClientBuilder withOkHttp3ConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        return this;
    }

    public ApnsClientBuilder withCertificate(InputStream inputStream) {
        certificate = inputStream;
        return this;
    }

    public ApnsClientBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public ApnsClientBuilder withProductionGateway() {
        this.production = true;
        return this;
    }

    public ApnsClientBuilder withProductionGateway(boolean production) {
        if (production) return withProductionGateway();

        return withDevelopmentGateway();
    }

    public ApnsClientBuilder withDevelopmentGateway() {
        this.production = false;
        return this;
    }

    public ApnsClientBuilder inSynchronousMode() {
        asynchronous = false;
        return this;
    }

    public ApnsClientBuilder inAsynchronousMode() {
        asynchronous = true;
        return this;
    }

    public ApnsClientBuilder withSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
        return this;
    }

    public ApnsClientBuilder withDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
        return this;
    }

    public ApnsClientBuilder withMaxQueuedNotifications(int maxQueuedNotifications) {
        this.maxQueuedNotifications = maxQueuedNotifications;
        return this;
    }

    public ApnsClient build() throws CertificateException,
            NoSuchAlgorithmException, KeyStoreException, IOException,
            UnrecoverableKeyException, KeyManagementException {

        if (certificate == null) throw new CertificateException("Certificate cannot be null");

        if (useOkHttp3) {
            return new SyncOkHttpApnsClient(certificate, password, production, defaultTopic, connectionPool);
        }

        if (asynchronous) {
            if (semaphore == null) {
                semaphore = new Semaphore(maxQueuedNotifications);
            }

            return new AsyncApnsClient(certificate, password,
                    production, maxQueuedNotifications, semaphore, defaultTopic);
        } else {
            return new SyncApnsClient(certificate, password, production, defaultTopic);
        }
    }
}
