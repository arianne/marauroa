/* $Id: InputSerializer.java,v 1.4 2004/01/27 17:03:12 arianne_rpg Exp $ */
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
import marauroa.*;

/** InputSerializer is used to serialize classes that implement the Serializable
 *  interface from a InputStream.
 */
public class InputSerializer
  {
  private InputStream in;
  
  /** Constructor that pass the InputStream to the serializer
      @param in the InputStream */
  public InputSerializer(InputStream in)
    {
    this.in=in;
    }
  
  /** This method serialize an object that implements the interface Serializable
      allowing to implement this behaviour in several classes
      @param obj the object were we will serialize the data
      @return the object serialized, just for interface coherence
      @throws java.io.IOException if there is an IO error
      @throws java.lang.ClassNotFoundException if the class to serialize doesn't exist. */
    public Object readObject(marauroa.net.Serializable obj) throws IOException, java.lang.ClassNotFoundException
    {
    obj.readObject(this);
    return obj;
    }
    
  /** This method read a byte from the Serializer
      @return the byte serialized
      @throws java.io.IOException if there is an IO error
      @throws java.lang.ClassNotFoundException if the class to serialize doesn't exist. */
  public byte readByte() throws IOException, java.lang.ClassNotFoundException
    {
    int result=in.read();
    if(result<0)
      {
      throw new IOException();
      }
    return (byte)result;
    }
    
  /** This method read a byte array from the Serializer
      @return the byte array serialized
      @throws java.io.IOException if there is an IO error
      @throws java.lang.ClassNotFoundException if the class to serialize doesn't exist. */
  public byte[] readByteArray() throws IOException, java.lang.ClassNotFoundException
    {
    int size=readInt();
    
    if(size>TimeoutConf.MAX_BYTE_ARRAY_ELEMENTS)
      {
      throw new IOException("Ilegal request of an array of "+String.valueOf(size)+" size");
      }
    
    byte[] buffer=new byte[size];
    
    in.read(buffer);
    
    return buffer;
    }

  /** This method read a short from the Serializer
      @return the short serialized
      @throws java.io.IOException if there is an IO error
      @throws java.lang.ClassNotFoundException
        if the class to serialize doesn't exist. */
  public short readShort() throws IOException, java.lang.ClassNotFoundException
    {
    byte[] data=new byte[2];
    
    int result=in.read(data);
    if(result<0)
      {
      throw new IOException();
      }
      
    result=data[0]&0xFF;
    result+=(data[1]&0xFF) << 8;
    return (short)result;
    }

  /** This method read a int from the Serializer
      @return the int serialized
      @throws java.io.IOException if there is an IO error
      @throws java.lang.ClassNotFoundException
        if the class to serialize doesn't exist. */
  public int readInt() throws IOException, java.lang.ClassNotFoundException
    {
    byte[] data=new byte[4];
    int result=in.read(data);
    if(result<0)
      {
      throw new IOException();
      }
      
    result=data[0]&0xFF;
    result+=(data[1]&0xFF) << 8;
    result+=(data[2]&0xFF) << 16;
    result+=(data[3]&0xFF) << 24;
    return result;
    }
  
  /** This method read a String from the Serializer
      @return the String serialized
      @throws java.io.IOException if there is an IO error
      @throws java.lang.ClassNotFoundException
        if the class to serialize doesn't exist. */
  public String readString() throws IOException, java.lang.ClassNotFoundException,UnsupportedEncodingException
    {
    return new String(readByteArray(),"UTF-8");
    }

  /** This method read a String array from the Serializer
      @return the String array serialized
      @throws java.io.IOException if there is an IO error
      @throws java.lang.ClassNotFoundException
        if the class to serialize doesn't exist. */
  public String[] readStringArray() throws IOException, java.lang.ClassNotFoundException
    {
    int size=readInt();

    if(size>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Ilegal request of an array of "+String.valueOf(size)+" size");
      }
    
    String[] buffer=new String[size];
    for(int i=0;i<size;i++)
      {
      buffer[i]=readString();
      }
    
    return buffer;
    }
  };
