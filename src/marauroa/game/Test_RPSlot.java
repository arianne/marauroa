/* $Id: Test_RPSlot.java,v 1.2 2003/12/08 01:12:19 arianne_rpg Exp $ */
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
import marauroa.game.*;
import marauroa.net.*;
import marauroa.*;
import java.io.*;


public class Test_RPSlot extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_RPSlot.class);
	}
	
  public void testRPSlot()
    {
    marauroad.trace("Test_RPSlot::testRPSlot",">");
    
    try
      {
      RPObject SonGoku=new RPObject();
      SonGoku.put("object_id","1");
      SonGoku.put("name","Son Goku");

      RPSlot slot=new RPSlot();
    
      slot.setName("left_hand");
      assertEquals(slot.getName(),"left_hand");
    
      assertFalse(slot.has(new RPObject.ID(SonGoku)));
      slot.add(SonGoku);
      assertTrue(slot.has(new RPObject.ID(SonGoku)));
      assertEquals(1,slot.size());
      
      assertEquals(slot.get(),SonGoku);
      assertEquals(slot.get(new RPObject.ID(SonGoku)),SonGoku);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }  
    finally
      {
      marauroad.trace("Test_RPSlot::testRPSlot","<");
      }     
    }

  public void testRPSlotException()
    {
    marauroad.trace("Test_RPSlot::testRPSlotException",">");
    
    try
      {
      RPSlot slot=new RPSlot();
      slot.get();
      fail("Should have thrown exception");
      }
    catch(Exception e)
      {
      assertTrue(true);
      }

    try
      {
      RPSlot slot=new RPSlot();
      slot.remove(new RPObject.ID(10123));
      fail("Should have thrown exception");
      }
    catch(Exception e)
      {
      assertTrue(true);
      }
    
    marauroad.trace("Test_RPSlot::testRPSlotException","<");
    }

  public void testRPSlotSerialization()
    {
    marauroad.trace("Test_RPSlot::testRPSlotSerialization",">");
    
    try
      {
      RPSlot slot=new RPSlot();
    
      slot.setName("left_hand");
      assertEquals(slot.getName(),"left_hand");

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputSerializer os = new OutputSerializer(baos);
      slot.writeObject(os);
      
      ByteArrayInputStream bais= new ByteArrayInputStream(baos.toByteArray());
      InputSerializer in=new InputSerializer(bais);
      
      RPSlot result=(RPSlot)in.readObject(new RPSlot());
      
      assertEquals(result,slot);
      }
    catch(Exception e)
      {
      }

    try
      {
      RPObject SonGoku=new RPObject();
      SonGoku.put("object_id","1");
      SonGoku.put("name","Son Goku");

      RPSlot slot=new RPSlot();
    
      slot.setName("left_hand");
      assertEquals(slot.getName(),"left_hand");
      slot.add(SonGoku);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputSerializer os = new OutputSerializer(baos);
      slot.writeObject(os);
      
      ByteArrayInputStream bais= new ByteArrayInputStream(baos.toByteArray());
      InputSerializer in=new InputSerializer(bais);
      
      RPSlot result=(RPSlot)in.readObject(new RPSlot());
      
      assertEquals(result,slot);
      }
    catch(Exception e)
      {
      }

    marauroad.trace("Test_RPSlot::testRPSlotSerialization","<");    
    }
  }