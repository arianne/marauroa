/* $Id: Test_RPObject.java,v 1.5 2003/12/08 01:12:19 arianne_rpg Exp $ */
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

public class Test_RPObject extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_RPObject.class);
	}
	
  public void testRPObjectAttributes()
    {
    marauroad.trace("Test_RPObject::testRPObjectAttributes",">");

    try
      {
      RPObject SonGoku=new RPObject();
      SonGoku.put("object_id","1");
      SonGoku.put("name","Son Goku");
    
      assertTrue(SonGoku.has("object_id"));
      String id_string=SonGoku.get("object_id");
      assertEquals(id_string,"1");

      assertTrue(SonGoku.has("name"));
      String name_string=SonGoku.get("name");
      assertEquals(name_string,"Son Goku");
      
      SonGoku.remove("name");
      assertFalse(SonGoku.has("name"));
      
      assertFalse(SonGoku.hasSlot("left_hand"));
      RPSlot slot=new RPSlot("left_hand");
      SonGoku.addSlot(slot);
      assertTrue(SonGoku.hasSlot("left_hand"));
      assertEquals(SonGoku.getSlot("left_hand"),slot);
      
      RPObject.ID id=new RPObject.ID(SonGoku);
      RPObject.ID id_1=new RPObject.ID(1);
      assertEquals(id,id_1);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    finally
      {
      marauroad.trace("Test_RPObject::testRPObjectAttributes",">");
      }
    }

  public void testRPObjectException()
    {    
    marauroad.trace("Test_RPObject::testRPObjectException",">");

    try
      {
      RPObject SonGoku=new RPObject();      
      SonGoku.get("object_id");
      fail("Object did not throw an exception");
      }
    catch(Exception e)
      {      
      }

    try
      {
      RPObject SonGoku=new RPObject();      
      SonGoku.addSlot(new RPSlot("left_hand"));
      assertTrue(SonGoku.hasSlot("left_hand"));
      SonGoku.addSlot(new RPSlot("left_hand"));
      fail("Object did not throw an exception");
      }
    catch(Exception e)
      {      
      }

    try
      {
      RPObject SonGoku=new RPObject();      
      SonGoku.getSlot("left_hand");
      fail("Object did not throw an exception");
      }
    catch(Exception e)
      {      
      }

    marauroad.trace("Test_RPObject::testRPObjectException","<");
    }

  public void testRPObjectSerialization()
    {    
    marauroad.trace("Test_RPObject::testRPObjectSerialization",">");

    try
      {
      RPObject SonGoku=new RPObject();      
      SonGoku.put("object_id", Integer.toString(1031));
      SonGoku.put("name", "Son Goku");

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputSerializer os = new OutputSerializer(baos);
      SonGoku.writeObject(os);
      
      ByteArrayInputStream bais= new ByteArrayInputStream(baos.toByteArray());
      InputSerializer in=new InputSerializer(bais);
      
      RPObject result=(RPObject)in.readObject(new RPObject());
      
      assertEquals(result,SonGoku);
      }
    catch(Exception e)
      {      
      fail("Failed to serialize object");
      }
    finally
      {
      marauroad.trace("Test_RPObject::testRPObjectSerialization","<");
      }
    }
  }