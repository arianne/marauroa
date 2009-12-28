/* $Id: RSAKey.java,v 1.9 2009/12/28 01:15:19 nhnb Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.crypto;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Implementation of a private RSA Key
 *
 * @author quisar
 *
 */
public class RSAKey extends RSAPublicKey {

	private BigInteger d;

	public RSAKey(BigInteger n, BigInteger d, BigInteger e) {
		super(n, e);
		this.d = d;
	}

	@Override
	public void print(PrintWriter out) {
		super.print(out);
		out.println("d = " + d);
	}

	@Override
	public void print(PrintStream out) {
		super.print(out);
		out.println("d = " + d);
	}

	public static RSAKey generateKey(int nbBits) {
		BigInteger p;
		BigInteger q;
		BigInteger n;
		BigInteger d;
		BigInteger e = new BigInteger("15");
		BigInteger phi;

		byte[] nonce = new byte[(nbBits / 16) + 1];

		SecureRandom rand;
		try {
			rand = SecureRandom.getInstance("SHA1PRNG");
			rand.nextBytes(nonce);
		} catch (Exception ex) {
			System.err.println("Can't happen...");
			ex.printStackTrace();
		}

		if (nonce[0] >= 0) {
			nonce[0] -= 128;
		}

		p = new BigInteger(1, nonce);

		p = p.subtract(p.remainder(big6)).subtract(big1);
		q = p.subtract(big6);

		// Select two prime numbers, p and q, such
		// that n = p*q > max and the number of
		// characters in n is <= blockSize, where
		// max is the maximum possible data value in
		// a data block.
		// Use the incoming first guess for p and then
		// iterate by increasing the value of p until
		// a highly-probable prime number is
		// found.

		while (!(p.isProbablePrime(1000) && p.multiply(big2).add(big1).isProbablePrime(1000))) {
			p = p.add(big6);
		}// end while loop

		// Make the first guess for q one less than p
		// and then iterate until a highly-probable
		// prime number is found for q that satisfies
		// the two conditions given above.

		// The current value of q satisfies one of
		// the required conditions, but may not be
		// a prime number. Get the next prime number
		// smaller than the current value of q.
		while (!(q.isProbablePrime(1000) && q.multiply(big2).add(big1).isProbablePrime(1000))) {
			q = q.subtract(big6);
		}// end while loop

		p = p.multiply(big2).add(big1);
		q = q.multiply(big2).add(big1);

		// Now we have a prime value for q that
		// satisfies one of the two required
		// conditions. Compute the current value
		// for n and start working to confirm or
		// satisfy the other condition.
		n = p.multiply(q);

		// Compute phi, as (p - 1)*(q - 1).
		BigInteger pPrime = p.subtract(big1);
		BigInteger qPrime = q.subtract(big1);
		phi = pPrime.multiply(qPrime);

		// Compute e. First guess is incoming
		// value.
		while (!e.gcd(phi).equals(big1)) {
			e = e.add(big1);
		}// end while loop

		// Compute the value for d
		d = e.modInverse(phi);
		return new RSAKey(n, d, e);
	}

	public BigInteger decode(BigInteger message) {
		return message.modPow(d, n);
	}

	public byte[] decodeByteArray(byte[] message) {
		return Hash.bigIntToBytes(decode(new BigInteger(message)));
	}

	public BigInteger sign(BigInteger message) {
		return decode(message);
	}

	public RSAPublicKey getPublicKey() {
		return new RSAPublicKey(n, e);
	}

	public BigInteger getD() {
		return d;
	}
}
