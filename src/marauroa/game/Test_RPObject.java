/* $Id: Test_RPObject.java,v 1.19 2004/09/05 09:09:24 arianne_rpg Exp $ */
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
      RPObject.ID id_1=new RPObject.ID(1,"");

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

  private RPObject createRPObject() throws Exception
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
      
      return player;
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      throw e;
      }
    }
  
  private void changeRPObjectAttributes(RPObject object)
    {
    object.put("name","This is a test");
    }

  private void addRPObjectAttributes(RPObject object)
    {
    object.put("Fooo","This is a test");
    object.put("bar","12312");
    }

  private void deleteRPObjectAttributes(RPObject object) throws Exception
    {
    object.remove("fame");
    }

  private void changeInSlotRPObjectAttributes(RPObject object) throws Exception
    {
    RPObject gladiator=object.getSlot("!gladiators").get();
    gladiator.put("name","This is a test");
    }

  private void addInSlotRPObjectAttributes(RPObject object) throws Exception
    {
    RPObject gladiator=object.getSlot("!gladiators").get();
    gladiator.put("Fooo","This is a test");
    gladiator.put("bar","12312");
    }

  private void deleteInSlotRPObjectAttributes(RPObject object) throws Exception
    {
    RPObject gladiator=object.getSlot("!gladiators").get();
    gladiator.remove("karma");
    }

  private void addRPObjectSlot(RPObject object) throws Exception
    {
    object.addSlot(new RPSlot("bag"));
    
    RPObject item=new RPObject();
    item.put("id",21);
    item.put("type","coin");
    item.put("val",10);
    
    object.getSlot("bag").add(item);
    }

  private void deleteRPObjectSlot(RPObject object) throws Exception
    {
    object.removeSlot("!gladiators");
    }

  public void testRPObjectDifferences_NoChanges()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      assertEquals(0,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  public void testRPObjectDifferences_ChangedAttributes()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      
      changeRPObjectAttributes(player);

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      
      assertEquals(2,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      assertEquals(0,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  public void testRPObjectDifferences_AddedAttributes()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      
      addRPObjectAttributes(player);

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      
      assertEquals(3,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      assertEquals(0,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  public void testRPObjectDifferences_DeletedAttributes()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      
      deleteRPObjectAttributes(player);

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      
      assertEquals(0,added.size());
      assertEquals(2,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      assertEquals(0,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  public void testRPObjectDifferences_EverythingAttributes()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      
      addRPObjectAttributes(player);
      changeRPObjectAttributes(player);
      deleteRPObjectAttributes(player);

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      
      assertEquals(4,added.size());
      assertEquals(2,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      assertEquals(0,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  public void testRPObjectDifferences_addSlot()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      
      addRPObjectSlot(player);

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      
      assertEquals(4,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      assertEquals(0,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  public void testRPObjectDifferences_deleteSlot()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      
      deleteRPObjectSlot(player);

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      
      assertEquals(0,added.size());
      assertEquals(1,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      assertEquals(0,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  public void testRPObjectDifferences_everythingSlot()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      
      addRPObjectSlot(player);
      deleteRPObjectSlot(player);

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      
      assertEquals(4,added.size());
      assertEquals(1,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      assertEquals(0,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }


























  public void testRPObjectDifferences_InSlotChangedAttributes()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      
      changeInSlotRPObjectAttributes(player);

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      
      assertEquals(2+1,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  public void testRPObjectDifferences_InSlotAddedAttributes()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      
      addInSlotRPObjectAttributes(player);

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      
      assertEquals(3+1,added.size());
      assertEquals(0,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  public void testRPObjectDifferences_InSlotDeletedAttributes()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      
      deleteInSlotRPObjectAttributes(player);

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      
      assertEquals(0,added.size());
      assertEquals(2+1,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  public void testRPObjectDifferences_InSlotEverythingAttributes()  
    {
    try
      {
      RPObject player=createRPObject();
      
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
            
      player.getDifferences(added,deleted);    
      assertTrue(added.size()>0);
      assertTrue(deleted.size()==0);
      
      RPObject recovered_player=new RPObject();      
      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      
      addInSlotRPObjectAttributes(player);
      changeInSlotRPObjectAttributes(player);
      deleteInSlotRPObjectAttributes(player);

      added=new RPObject();
      deleted=new RPObject();

      player.getDifferences(added,deleted);   
      
      assertEquals(4+1,added.size());
      assertEquals(2+1,deleted.size());

      recovered_player.applyDifferences(added,deleted);
      assertTrue(player.equals(recovered_player));  
      }      
    catch(Exception e)
      {
      e.printStackTrace();
      fail("Failed to serialize object");
      }
    }

  }