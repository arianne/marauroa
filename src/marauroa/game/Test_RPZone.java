/* $Id: Test_RPZone.java,v 1.3 2003/12/08 01:12:19 arianne_rpg Exp $ */
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

import junit.framework.*;
import marauroa.net.*;
import marauroa.*;
import java.io.*;

public class Test_RPZone extends TestCase
  {
  public static Test suite ( )
    {
    return new TestSuite(Test_RPZone.class);
    }
  
  public void testRPZone()
    {
    marauroad.trace("Test_RPZone::testRPZone",">");

    RPObject SonGoku=new RPObject();
    SonGoku.put("object_id","1");
    SonGoku.put("name","Son Goku");
    
    RPZone zone=new MarauroaRPZone();
    assertNotNull(zone);
    
    try
      {
      zone.add(SonGoku);
      RPObject.ID id=new RPObject.ID(SonGoku);
      assertTrue(zone.has(id));
      
      RPObject object=zone.get(id);
      assertEquals(object,SonGoku);
      
      zone.remove(id);
      assertFalse(zone.has(id));
      }
    catch(RPZone.RPObjectInvalidException e)
      {
      fail("RPObject is not valid");
      }
    catch(RPZone.RPObjectNotFoundException e)
      {
      fail("RPObject doesn't exist");
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      fail("Can't find the attribute we are looking for");
      }
    finally
      {
      marauroad.trace("Test_RPZone::testRPZone","<");
      }
  }
}
