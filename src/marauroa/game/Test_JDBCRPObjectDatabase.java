/* $Id: Test_JDBCRPObjectDatabase.java,v 1.4 2004/03/22 19:09:09 root777 Exp $ */
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
import java.util.*;
import marauroa.*;

public class Test_JDBCRPObjectDatabase extends TestCase
{
  public static Test suite ( )
  {
    return new TestSuite(Test_JDBCRPObjectDatabase.class);
  }
  
  private RPObject createObject(int i) throws Exception
  {
    RPObject example=new RPObject();
    example.put("object_id",i);
    example.put("type","gladiator");
    example.put("name","Stupid random name");
    example.put("look","database_look");
    example.put("!hp",100);
    example.put("hp",100);
    example.put("attack",5);
    example.put("karma",100);
    
    example.addSlot(new RPSlot("l_hand"));
    
    RPObject item=new RPObject();
    item.put("object_id",i+1000);
    item.put("type","shield");
    item.put("def",10);
    item.put("price",50);
    
    example.getSlot("l_hand").add(item);
    
    example.addSlot(new RPSlot("r_hand"));
    
    item=new RPObject();
    item.put("object_id",i+100000);
    item.put("type","sword");
    item.put("def",0);
    item.put("price",100);
    
    example.getSlot("r_hand").add(item);
    
    return example;
  }
  
  public void testStoreRPObject()
  {
    String initial="Hi World 'example', See \"This is \\ an example \" Rocks, isn't it?!";
    String escaped=JDBCRPObjectDatabase.EscapeString(initial);
    assertEquals(JDBCRPObjectDatabase.UnescapeString(escaped),initial);
    
    try
    {
      JDBCRPObjectDatabase database=JDBCRPObjectDatabase.getDatabase();
      Transaction trans = database.getTransaction();
      int total=500;
      
      long t1,t2,t3,t4,t5;
      
      t1=new Date().getTime();
      
      for(int i=0;i<total;++i)
      {
        assertFalse(database.hasRPObject(trans,new RPObject.ID(i)));
        database.storeRPObject(trans,createObject(i));
        assertTrue(database.hasRPObject(trans,new RPObject.ID(i)));
      }
      
      t2=new Date().getTime();
      
      JDBCRPObjectDatabase.RPObjectIterator it=database.iterator(trans);
      
      while(it.hasNext())
      {
        RPObject object=database.loadRPObject(trans,it.next());
        //System.out.println(object);
      }
      
      t3=new Date().getTime();
      
      for(int i=0;i<total;++i)
      {
        RPObject result=database.loadRPObject(trans,new RPObject.ID(i));
        assertEquals(createObject(i), result);
      }
      
      t4=new Date().getTime();
      
      for(int i=0;i<total;++i)
      {
        database.deleteRPObject(trans,new RPObject.ID(i));
        assertFalse(database.hasRPObject(trans,new RPObject.ID(i)));
      }
      
      t5=new Date().getTime();
      
      marauroad.trace("Test_JDBCRPObjectDatabase::testStoreRPObject","D","Store TIME: "+(t2-t1));
      marauroad.trace("Test_JDBCRPObjectDatabase::testStoreRPObject","D","Load TIME: "+(t4-t3));
      marauroad.trace("Test_JDBCRPObjectDatabase::testStoreRPObject","D","Delete TIME: "+(t5-t4));
    }
    catch(Exception e)
    {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
  
  public void testStoreRPObjectException()
  {
    try
    {
      JDBCRPObjectDatabase database=JDBCRPObjectDatabase.getDatabase();
      Transaction trans = database.getTransaction();
      RPObject example=new RPObject();
      example.put("object_id",1);
      example.put("type","TEST");
      
      example.addSlot(new RPSlot("l_hand"));
      
      RPObject item=new RPObject();
      item.put("example","shield");
      
      example.getSlot("l_hand").add(item);
      
      database.storeRPObject(trans,example);
      assertFalse(database.hasRPObject(trans,new RPObject.ID(1)));
    }
    catch(Exception e)
    {
    }
  }
}
