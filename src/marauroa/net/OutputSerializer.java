/* $Id: OutputSerializer.java,v 1.2 2003/12/08 01:08:30 arianne_rpg Exp $ */
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
package marauroa.net;

import java.io.*;

/** OutputSerializer is used to serialize classes that implement the Serializable
 *  interface into a OutputStream.
 */
public class OutputSerializer
  {
  OutputStream out;
  
  /** Constructor that pass the OutputStream to the serializer
      @param in the InputStream */
  public OutputSerializer(OutputStream out)
    {
    this.out=out;
    }
  
  /** Add the Object to the serializer, if it implements the marauroa.net.Serializable interface 
   *  @param obj the object to serialize */
  public void write(marauroa.net.Serializable obj) throws IOException
    {
    obj.writeObject(this);
    }

  /** Add the byte to the serializer 
   *  @param a the byte to serialize */
  public void write(byte a) throws IOException
    {
    out.write(a);
    }
  
  /** Add the byte array to the serializer 
   *  @param a the byte array to serialize */
  public void write(byte[] a) throws IOException
    {
    write((int)a.length);
    out.write(a);
    }

  /** Add the short to the serializer 
   *  @param a the short to serialize */
  public void write(short a) throws IOException
    {
    int tmp;
    
    tmp=a&0xFF;
    out.write(tmp);
    tmp=(a>>8)&0xFF;
    out.write(tmp);
    }

  /** Add the int to the serializer 
   *  @param a the int to serialize */
  public void write(int a) throws IOException
    {
    int tmp;
    
    tmp=a&0xFF;
    out.write(tmp);
    tmp=(a>>8)&0xFF;
    out.write(tmp);
    tmp=(a>>16)&0xFF;
    out.write(tmp);
    tmp=(a>>24)&0xFF;
    out.write(tmp);
    }
  
  /** Add the String to the serializer, using UTF-8 encoding 
   *  @param a the String to serialize */
  public void write(String a) throws IOException,UnsupportedEncodingException
    {
    write(a.getBytes("UTF-8"));
    }

  /** Add the String array to the serializer, using UTF-8 encoding
   *  @param a the String array to serialize */
  public void write(String[] a) throws IOException
    {
    write(a.length);
    for(int i=0;i<a.length;i++) write(a[i]);
    }
  };

