/* $Id: Test_RPObject.java,v 1.6 2004/03/22 18:31:48 arianne_rpg Exp $ */
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

  public void testRPObjectClonable()  
    {    
    try
      {
      RPObject example=new RPObject();
      example.put("object_id",10);
      example.put("type","gladiator");
      example.put("name","Stupid random name");
      
      RPObject example_mod=new RPObject();
      example_mod.copy(example);
      assertTrue(example.equals(example_mod));
      
      example.remove("name");
      assertFalse(example.equals(example_mod));
      }
    catch(Exception e)
      {
      fail("Failed to serialize object");
      }
    finally
      {
      marauroad.trace("Test_RPObject::testRPObjectClonable","<");
      }
    }

  public void testRPObjectDifferences()  
    {    
    try
      {
      RPObject example=new RPObject();
      example.put("object_id",10);
      example.put("type","gladiator");
      example.put("name","Stupid random name");
      example.put("look","database_look");
      example.put("!hp",100);
      example.put("hp",100);
      example.put("attack",5);
      example.put("karma",100);
  
      example.addSlot(new RPSlot("l_hand"));
      
      RPObject item=new RPObject();
      item.put("object_id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);
      
      example.getSlot("l_hand").add(item);
      
      example.addSlot(new RPSlot("r_hand"));

      item=new RPObject();
//      item.put("object_id",12);
//      item.put("type","sword");
//      item.put("def",0);
//      item.put("price",100);
//      
//      example.getSlot("r_hand").add(item);

      RPObject example_mod=new RPObject();
      example_mod.put("object_id",10);
      example_mod.put("type","gladiator");
      example_mod.put("name","A better stupid random name");
      example_mod.put("look","database_look");
      example_mod.put("!hp",100);
      example_mod.put("hp",90);
  
      example_mod.addSlot(new RPSlot("l_hand"));
      
      RPObject item_mod=new RPObject();
      item_mod.put("object_id",11);
      item_mod.put("type","shield");
      item_mod.put("def",10);
      item_mod.put("price",50);
      
      example_mod.getSlot("l_hand").add(item_mod);
      
      example_mod.addSlot(new RPSlot("r_hand"));

      item_mod=new RPObject();
      item_mod.put("object_id",12);
      item_mod.put("type","sword");
      item_mod.put("def",0);
      item_mod.put("price",100);
      
      example_mod.getSlot("r_hand").add(item_mod);
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
      
      example_mod.getDifferencesFrom(example,added,deleted);
      assertTrue(added.has("name"));
      assertTrue(added.has("hp"));
      assertTrue(deleted.has("karma"));
      assertTrue(deleted.has("attack"));
      
      System.out.println("Original --> "+example.toString());
      System.out.println("Added --> "+added.toString());
      System.out.println("Deleted --> "+deleted.toString());
      System.out.println("New --> "+example_mod.toString());
      
      RPObject build=example.applyDifferences(added,deleted);
      System.out.println("Build --> "+build.toString());
      
      assertTrue(example_mod.equals(build));      
      build.getSlot("r_hand").get().put("test_shit","");
      assertFalse(example_mod.equals(build));      
      }
    catch(Exception e)
      {      
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    finally
      {
      marauroad.trace("Test_RPObject::testRPObjectSerialization","<");
      }
    }
  }