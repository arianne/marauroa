/***************************************************************************
 *                   (C) Copyright 2013 - Faiumoni e. V.                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.net.nio;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import marauroa.common.Configuration;

/**
 * ssl handling
 *
 * @author hendrik
 */
public class SslHelper {
	private final KeyManagerFactory keyManagerFactory;
	private final TrustManagerFactory trustManagerFactory;

	/**
	 * initializes key- and truststore
	 *
	 * @throws GeneralSecurityException on an error with the keystore
	 * @throws IOException on an input/output error
	 */
	public SslHelper()  throws GeneralSecurityException, IOException {
		char[] passphrase = Configuration.getConfiguration().get("keystore.password", "").toCharArray();

		// First initialize the key and trust material.
		KeyStore keyStore = KeyStore.getInstance("JKS");
		String filename = Configuration.getConfiguration().get("keystore.filename", "keystore.ks");
		keyStore.load(new FileInputStream(filename), passphrase);

		// KeyManager's decide which key material to use.
		keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
		keyManagerFactory.init(keyStore, passphrase);

		// TrustManager's decide whether to allow connections.
		trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
		trustManagerFactory.init(keyStore);
	}

	/**
	 * creates an ssl engine
	 *
	 * @return SSLEngine
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public SSLEngine createSSLEngine() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
		SSLEngine sslEngine = sslContext.createSSLEngine();
		sslEngine.setUseClientMode(false);
		return sslEngine;
	}
}
