/* $Id: Test_SerializerByteArray.java,v 1.3 2003/12/08 01:08:31 arianne_rpg Exp $ */
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

import marauroa.net.*;
import marauroa.*;
import java.io.*;
import java.lang.Byte;
import junit.framework.*;

public class Test_SerializerByteArray extends TestCase
  {
  private ByteArrayOutputStream out;
  private ByteArrayInputStream in;
  
  private InputSerializer sin;
  private OutputSerializer sout;

  public static Test suite ( ) 
    {
    return new TestSuite(Test_SerializerByteArray.class);
	}
	
  public void testByteArray()
    {
    marauroad.trace("Test_SerializerByteArray::testByteArray",">");

    byte[] data=new byte[256];
    for(int i=0;i<data.length;i++) data[i]=(byte)(java.lang.Math.random()*256-127);
    
    out=new ByteArrayOutputStream();
    sout=new OutputSerializer(out);
    
    try
      {
      sout.write(data);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data out");
      }
    
    byte[] result=null;
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    try
      {
      result=sin.readByteArray();
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(data.length,result.length);
    
    for(int i=0;i<data.length;i++)
      {
      assertTrue(data[i]==result[i]);    
      }

    marauroad.trace("Test_SerializerByteArray::testByteArray","<");
	}  
  }