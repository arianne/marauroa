/* $Id: Test_SerializerInt.java,v 1.2 2003/12/08 01:08:31 arianne_rpg Exp $ */
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

public class Test_SerializerInt extends Test_Serializer
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_SerializerInt.class);
	}
	
  public void testInt()
    {
    Integer[] data=
      {
      new Integer((int)Integer.MIN_VALUE),
      new Integer((int)-50),
      new Integer((int)0),
      new Integer((int)-0),
      new Integer((int)50),
      new Integer((int)Integer.MAX_VALUE)
      };
    
    test(data);   
    }
 
  protected void write(OutputSerializer out, Object obj) throws IOException
    {
    out.write(((Integer)obj).intValue());
    }
    
  protected Object read(InputSerializer in) throws IOException, ClassNotFoundException
    {
    return new Integer(in.readInt());
    }
  
  protected boolean equals(Object a, Object b)
    {
    return ((Integer)a).equals((Integer)b);
    }
  }