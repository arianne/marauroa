/* $Id: Test_RPObject.java,v 1.13 2004/04/04 22:20:13 arianne_rpg Exp $ */
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

      SonGoku.put("id","1");
      SonGoku.put("name","Son Goku");
      assertTrue(SonGoku.has("id"));

      String id_string=SonGoku.get("id");

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

      SonGoku.get("id");
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

      SonGoku.put("id", Integer.toString(1031));
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

      example.put("id",10);
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
      RPObject player=new RPObject();
      player.put("id",1);
      player.put("type","player");
      player.put("name","A nice name");
      player.put("fame",1231);
      player.addSlot(new RPSlot("!gladiators"));
      
      RPObject example1=new RPObject();

      example1.put("id",10);
      example1.put("type","gladiator");
      example1.put("name","Stupid random name");
      example1.put("look","database_look");
      example1.put("!hp",100);
      example1.put("hp",100);
      example1.put("attack",5);
      example1.put("karma",100);
      example1.addSlot(new RPSlot("l_hand"));      

      RPObject item=new RPObject();

      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example1.getSlot("l_hand").add(item);
      example1.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example1.getSlot("r_hand").add(item);
      player.getSlot("!gladiators").add(example1);
   
      RPObject example2=new RPObject();

      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);
      example2.applyDifferences(added,deleted);
      
      System.out.println(player.toString());
      System.out.println(added.toString());
      System.out.println(deleted.toString());
      System.out.println(example2.toString());
      
      assertTrue(player.equals(example2));  
      
      item.put("def",50);
      assertFalse(player.equals(example2));            

      added=new RPObject();
      deleted=new RPObject();
            
      player.getDifferences(added,deleted);
      example2.applyDifferences(added,deleted);

      System.out.println(player.toString());
      System.out.println(added.toString());
      System.out.println(deleted.toString());
      System.out.println(example2.toString());
      
      assertTrue(player.equals(example2));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  public void testRPObjectDifferences_EqualObjects()  
    {    
    try
      {
      RPObject example1=new RPObject();

      example1.put("id",10);
      example1.put("type","gladiator");
      example1.put("name","Stupid random name");
      example1.put("look","database_look");
      example1.put("!hp",100);
      example1.put("hp",100);
      example1.put("attack",5);
      example1.put("karma",100);
      example1.addSlot(new RPSlot("l_hand"));      

      RPObject item=new RPObject();

      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example1.getSlot("l_hand").add(item);
      example1.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example1.getSlot("r_hand").add(item);

      RPObject example2=new RPObject();

      example2.put("id",10);
      example2.put("type","gladiator");
      example2.put("name","Stupid random name");
      example2.put("look","database_look");
      example2.put("!hp",100);
      example2.put("hp",100);
      example2.put("attack",5);
      example2.put("karma",100);
      example2.addSlot(new RPSlot("l_hand"));      
      item=new RPObject();
      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example2.getSlot("l_hand").add(item);
      example2.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example2.getSlot("r_hand").add(item);
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      example1.getDifferences(added,deleted);

      RPObject build=example1.applyDifferences(added,deleted);

      assertTrue(example2.equals(build));      
      build.getSlot("r_hand").get().put("test_shit","");
      assertFalse(example2.equals(build));      
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

  public void testRPObjectDifferences_AhasNewAttrib()  
    {    
    try
      {
      RPObject example1=new RPObject();

      example1.put("id",10);
      example1.put("type","gladiator");
      example1.put("name","Stupid random name");
      example1.put("look","database_look");
      example1.put("!hp",100);
      example1.put("hp",100);
      example1.put("attack",5);
      example1.put("karma",100);
      example1.addSlot(new RPSlot("l_hand"));      

      RPObject item=new RPObject();

      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example1.getSlot("l_hand").add(item);
      example1.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example1.getSlot("r_hand").add(item);

      RPObject example2=new RPObject();

      example2.put("id",10);
      example2.put("type","gladiator");
      example2.put("name","Stupid random name");
      example2.put("look","database_look");
      example2.put("attack",5);
      example2.put("karma",100);
      example2.addSlot(new RPSlot("l_hand"));      
      item=new RPObject();
      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example2.getSlot("l_hand").add(item);
      example2.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example2.getSlot("r_hand").add(item);
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      example1.getDifferences(added,deleted);

      RPObject build=example1.applyDifferences(added,deleted);

      assertTrue(example2.equals(build));      
      build.getSlot("r_hand").get().put("test_shit","");
      assertFalse(example2.equals(build));      
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

  public void testRPObjectDifferences_BhasNewAttrib()  
    {    
    try
      {
      RPObject example1=new RPObject();

      example1.put("id",10);
      example1.put("type","gladiator");
      example1.put("name","Stupid random name");
      example1.put("look","database_look");
      example1.put("attack",5);
      example1.put("karma",100);
      example1.addSlot(new RPSlot("l_hand"));      

      RPObject item=new RPObject();

      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example1.getSlot("l_hand").add(item);
      example1.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example1.getSlot("r_hand").add(item);

      RPObject example2=new RPObject();

      example2.put("id",10);
      example2.put("type","gladiator");
      example2.put("name","Stupid random name");
      example2.put("look","database_look");
      example2.put("!hp",100);
      example2.put("hp",100);
      example2.put("attack",5);
      example2.put("karma",100);
      example2.addSlot(new RPSlot("l_hand"));      
      item=new RPObject();
      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example2.getSlot("l_hand").add(item);
      example2.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example2.getSlot("r_hand").add(item);
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      example1.getDifferences(added,deleted);

      RPObject build=example1.applyDifferences(added,deleted);

      assertTrue(example2.equals(build));      
      build.getSlot("r_hand").get().put("test_shit","");
      assertFalse(example2.equals(build));      
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
  
  public void testRPObjectDifferences_AhasDifferentAttributes()  
    {    
    try
      {
      RPObject example1=new RPObject();

      example1.put("id",10);
      example1.put("type","gladiator");
      example1.put("name","Stupid random name");
      example1.put("look","database_look");
      example1.put("!hp",100);
      example1.put("hp",100);
      example1.put("attack",5);
      example1.put("karma",100);
      example1.addSlot(new RPSlot("l_hand"));      

      RPObject item=new RPObject();

      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example1.getSlot("l_hand").add(item);
      example1.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example1.getSlot("r_hand").add(item);

      RPObject example2=new RPObject();

      example2.put("id",10);
      example2.put("type","gladiator");
      example2.put("name","Another stupid random name");
      example2.put("look","database_look");
      example2.put("!hp",100);
      example2.put("hp",80);
      example2.put("attack",5);
      example2.put("karma",100);
      example2.addSlot(new RPSlot("l_hand"));      
      item=new RPObject();
      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example2.getSlot("l_hand").add(item);
      example2.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example2.getSlot("r_hand").add(item);
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      example1.getDifferences(added,deleted);

      RPObject build=example1.applyDifferences(added,deleted);

      assertTrue(example2.equals(build));      
      build.getSlot("r_hand").get().put("test_shit","");
      assertFalse(example2.equals(build));      
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

  public void testRPObjectDifferences_AhasNewSlot()  
    {    
    try
      {
      RPObject example1=new RPObject();

      example1.put("id",10);
      example1.put("type","gladiator");
      example1.put("name","Stupid random name");
      example1.put("look","database_look");
      example1.put("!hp",100);
      example1.put("hp",100);
      example1.put("attack",5);
      example1.put("karma",100);
      example1.addSlot(new RPSlot("l_hand"));      

      RPObject item=new RPObject();

      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example1.getSlot("l_hand").add(item);
      example1.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example1.getSlot("r_hand").add(item);

      RPObject example2=new RPObject();

      example2.put("id",10);
      example2.put("type","gladiator");
      example2.put("name","Stupid random name");
      example2.put("look","database_look");
      example2.put("!hp",100);
      example2.put("hp",100);
      example2.put("attack",5);
      example2.put("karma",100);
      example2.addSlot(new RPSlot("l_hand"));      
      item=new RPObject();
      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example2.getSlot("l_hand").add(item);
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      example1.getDifferences(added,deleted);

      RPObject build=example1.applyDifferences(added,deleted);

      assertTrue(example2.equals(build));      
      build.getSlot("l_hand").get().put("test_shit","");
      assertFalse(example2.equals(build));      
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

  public void testRPObjectDifferences_BhasNewSlot()  
    {    
    try
      {
      RPObject example1=new RPObject();

      example1.put("id",10);
      example1.put("type","gladiator");
      example1.put("name","Stupid random name");
      example1.put("look","database_look");
      example1.put("!hp",100);
      example1.put("hp",100);
      example1.put("attack",5);
      example1.put("karma",100);
      example1.addSlot(new RPSlot("l_hand"));      

      RPObject item=new RPObject();

      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example1.getSlot("l_hand").add(item);
      
      RPObject example2=new RPObject();

      example2.put("id",10);
      example2.put("type","gladiator");
      example2.put("name","Stupid random name");
      example2.put("look","database_look");
      example2.put("!hp",100);
      example2.put("hp",100);
      example2.put("attack",5);
      example2.put("karma",100);
      example2.addSlot(new RPSlot("l_hand"));      
      item=new RPObject();
      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example2.getSlot("l_hand").add(item);
      example2.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example2.getSlot("r_hand").add(item);
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      example1.getDifferences(added,deleted);

      RPObject build=example1.applyDifferences(added,deleted);

      assertTrue(example2.equals(build));      
      build.getSlot("r_hand").get().put("test_shit","");
      assertFalse(example2.equals(build));      
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

  public void testRPObjectDifferences_AhasDifferentSlot()  
    {    
    try
      {
      RPObject example1=new RPObject();

      example1.put("id",10);
      example1.put("type","gladiator");
      example1.put("name","Stupid random name");
      example1.put("look","database_look");
      example1.put("!hp",100);
      example1.put("hp",100);
      example1.put("attack",5);
      example1.put("karma",100);
      example1.addSlot(new RPSlot("l_hand"));      

      RPObject item=new RPObject();

      item.put("id",11);
      item.put("type","shield");
      item.put("def",10);
      item.put("price",50);      
      example1.getSlot("l_hand").add(item);
      example1.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example1.getSlot("r_hand").add(item);

      RPObject example2=new RPObject();

      example2.put("id",10);
      example2.put("type","gladiator");
      example2.put("name","Stupid random name");
      example2.put("look","database_look");
      example2.put("!hp",100);
      example2.put("hp",100);
      example2.put("attack",5);
      example2.put("karma",100);
      example2.addSlot(new RPSlot("l_hand"));      
      item=new RPObject();
      item.put("id",13);
      item.put("type","sword");
      item.put("def",10);
      item.put("price",50);      
      example2.getSlot("l_hand").add(item);
      example2.addSlot(new RPSlot("r_hand"));
      item=new RPObject();
      item.put("id",12);
      item.put("type","sword");
      item.put("def",0);
      item.put("price",100);
      example2.getSlot("r_hand").add(item);
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      example1.getDifferences(added,deleted);

      RPObject build=example1.applyDifferences(added,deleted);      

      assertTrue(example2.equals(build));      
      build.getSlot("r_hand").get().put("test_shit","");
      assertFalse(example2.equals(build));   
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