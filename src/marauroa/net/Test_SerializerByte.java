/* $Id: Test_SerializerByte.java,v 1.3 2004/03/24 15:25:34 arianne_rpg Exp $ */
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
import java.io.*;
import java.lang.Byte;
import junit.framework.*;

public class Test_SerializerByte extends Test_Serializer
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_SerializerByte.class);
    }
	
  public void testByte()
    {
    Byte[] data=
        {
      new Byte((byte)Byte.MIN_VALUE),
      new Byte((byte)-50),
      new Byte((byte)0),
      new Byte((byte)-0),
      new Byte((byte)50),
      new Byte((byte)Byte.MAX_VALUE)
      };
    
    test(data);   
    }
 
  protected void write(OutputSerializer out, Object obj) throws IOException
    {
    out.write(((Byte)obj).byteValue());
    }
    
  protected Object read(InputSerializer in) throws IOException, ClassNotFoundException
    {
    return new Byte(in.readByte());
    }
  
  protected boolean equals(Object a, Object b)
    {
    return ((Byte)a).equals((Byte)b);
    }
  }