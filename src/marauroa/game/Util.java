/* $Id: Util.java,v 1.3 2004/05/27 18:49:00 arianne_rpg Exp $ */
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
package marauroa.game;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * this class contains some methods which are taken from GNU crypto project
 **/
public class Util
{
  // Hex charset
  private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
  
  //use WeakReference, so gc can remove MessageDigest instance
  private static WeakReference md5DigestRef;
  
  /**
   * returns a md5 hash (as string) of input string
   */
  public synchronized static String getMd5Hash(String string)
    throws NoSuchAlgorithmException,UnsupportedEncodingException
  {
    String ret = null;
    if(string!=null)
    {
      MessageDigest md = getMD5Instance();
      md.update(string.getBytes("UTF-8"));
      ret = toHexString(md.digest());
    }
    return(ret);
  }
  
  /**
   * Stolen from GNU crypto package
   * <p>Returns a string of hexadecimal digits from a byte array. Each byte is
   * converted to 2 hex symbols; zero(es) included.</p>
   *
   * <p>This method calls the method with same name and three arguments as:</p>
   *
   * <pre>
   *    toString(ba, 0, ba.length);
   * </pre>
   *
   * @param ba the byte array to convert.
   * @return a string of hexadecimal characters (two for each byte)
   * representing the designated input byte array.
   */
  public static String toHexString(byte[] ba)
  {
    return toHexString(ba, 0, ba.length);
  }
  
  /**
   * Stolen from GNU crypto package
   * <p>Returns a string of hexadecimal digits from a byte array, starting at
   * <code>offset</code> and consisting of <code>length</code> bytes. Each byte
   * is converted to 2 hex symbols; zero(es) included.</p>
   *
   * @param ba the byte array to convert.
   * @param offset the index from which to start considering the bytes to
   * convert.
   * @param length the count of bytes, starting from the designated offset to
   * convert.
   * @return a string of hexadecimal characters (two for each byte)
   * representing the designated input byte sub-array.
   */
  public static final String toHexString(byte[] ba, int offset, int length)
  {
    char[] buf = new char[length * 2];
    for (int i = 0, j = 0, k; i < length; )
    {
      k = ba[offset + i++];
      buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
      buf[j++] = HEX_DIGITS[ k        & 0x0F];
    }
    return new String(buf);
  }
  
  /**
   * creates (if needed) a new instance of MD5 MessageDigest and returns it
   */
  private synchronized static MessageDigest getMD5Instance()
    throws NoSuchAlgorithmException
  {
    MessageDigest md = null;
    if(md5DigestRef==null || (md=(MessageDigest)md5DigestRef.get())==null)
    {
      md = MessageDigest.getInstance("MD5");
      md5DigestRef = new WeakReference(md);
    }
    md = (MessageDigest)md5DigestRef.get();
    md.reset();
    return(md);
  }
  
  /**
   * just tests
   */
  public static void main(String argv[])
  {
    try
    {
      System.out.println(getMd5Hash(argv[0])+" --");
	}
	catch(Exception e)
	{
	e.printStackTrace();
	}
  }
}
