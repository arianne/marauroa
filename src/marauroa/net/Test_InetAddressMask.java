/* $Id: Test_InetAddressMask.java,v 1.2 2004/11/27 11:03:50 root777 Exp $ */
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
import java.net.*;

public class Test_InetAddressMask extends TestCase 
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_InetAddressMask.class);
    }
	
  public void testMatches()
    {
    try
      {
      InetAddressMask mask = new InetAddressMask("127.0.0.1","255.255.255.0");

      InetAddress address = InetAddress.getByAddress(new byte[]{(byte)127,(byte)0,(byte)0,(byte)113});
      assertTrue(address.toString() +" should be matched by "+mask.toString(),mask.matches(address));

      address = InetAddress.getByAddress(new byte[]{(byte)128,(byte)0,(byte)0,(byte)113});
      assertFalse(address.toString() +" should NOT be matched by "+mask.toString(),mask.matches(address));
   
      mask = new InetAddressMask("127.0.0.1","255.255.0.255");
      assertFalse(address.toString() +" should NOT be matched by "+mask.toString() ,mask.matches(address));   
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    }

  }
