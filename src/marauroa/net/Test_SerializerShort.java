/* $Id: Test_SerializerShort.java,v 1.3 2004/03/24 15:25:34 arianne_rpg Exp $ */
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
import junit.framework.*;

public class Test_SerializerShort extends Test_Serializer
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_SerializerShort.class);
    }
	
  public void testShort()
    {
    Short[] data=
        {
      new Short((short)Short.MIN_VALUE),
      new Short((short)-50),
      new Short((short)0),
      new Short((short)-0),
      new Short((short)50),
      new Short((short)Short.MAX_VALUE)
      };
    
    test(data);   
    }
 
  protected void write(OutputSerializer out, Object obj) throws IOException
    {
    out.write(((Short)obj).shortValue());
    }
    
  protected Object read(InputSerializer in) throws IOException, ClassNotFoundException
    {
    return new Short(in.readShort());
    }
  
  protected boolean equals(Object a, Object b)
    {
    return ((Short)a).equals((Short)b);
    }
  }