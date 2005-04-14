package marauroa.common.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.LinkedList;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RSAPublicKey {
  public static final BigInteger big0 = new BigInteger("0");
  public static final BigInteger big1 = new BigInteger("1");
  public static final BigInteger big2 = new BigInteger("2");
  public static final BigInteger big6 = new BigInteger("6");
  public static final BigInteger big256 = new BigInteger("256");

  protected BigInteger n;
  protected BigInteger e;

  public RSAPublicKey(BigInteger n, BigInteger e) {
    this.n = n;
    this.e = e;
  }

  public void print(PrintWriter out) {
    int size = 0;
    BigInteger foo = n;
    while(foo.compareTo(big0) > 0) {
      foo = foo.divide(big2);
      size++;
    }
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
