/* $Id: RSAPublicKey.java,v 1.3 2005/04/16 10:29:38 arianne_rpg Exp $ */
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

import java.math.BigInteger;
import java.io.PrintWriter;
import java.io.PrintStream;

public class RSAPublicKey {
  public static final BigInteger big0 = new BigInteger("0");
  public static final BigInteger big1 = new BigInteger("1");
  public static final BigInteger big2 = new BigInteger("2");
  public static final BigInteger big6 = new BigInteger("6");

  protected BigInteger n;
  protected BigInteger e;

  public RSAPublicKey(BigInteger n, BigInteger e) {
    this.n = n;
    this.e = e;
  }

  public void print(PrintWriter out) {
    out.println("n = " + n);
    out.println("e = " + e);
  }

  public void print(PrintStream out) {
    out.println("n = " + n);
    out.println("e = " + e);
  }

  public BigInteger getN() {
    return n;
  }

  public BigInteger getE() {
    return e;
  }

  public BigInteger encode(BigInteger message) {
    return message.modPow(e,n);
  }

  public byte[] encodeByteArray(byte[] message) {
    return encode(Hash.BytesToBigInt(message)).toByteArray();
  }


  public boolean verifySignature(BigInteger message, BigInteger signature) {
    return message.equals(encode(signature));
  }

  public static BigInteger getValue(String str) {
    byte[] v = str.getBytes();
    boolean zero = true;
    for(byte b : v) {
      if(b != 0) {
        return new BigInteger(1,v);
      }
    }
    return big0;
  }

  public static String getString(BigInteger value) {
    return new String(value.toByteArray());
  }

}
