/* $Id: Test_PlayerEntryContainer.java,v 1.11 2004/03/22 19:10:20 root777 Exp $ */
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

import marauroa.net.NetConst;
import marauroa.*;
import junit.framework.*;
import java.net.*;

public class Test_PlayerEntryContainer extends TestCase
{
  public static Test suite ( )
  {
    return new TestSuite(Test_PlayerEntryContainer.class);
  }
  
  private PlayerDatabase createDatabase()
  {
    PlayerDatabase playerDatabase=null;
    Transaction trans = null;
    
    try
    {
      playerDatabase=PlayerDatabaseFactory.getDatabase();
      trans = playerDatabase.getTransaction();
      assertNotNull(playerDatabase);
    }
    catch(Exception e)
    {
      fail(e.getMessage());
    }
    
    try
    {
      playerDatabase.addPlayer(trans,"Test Player","Test Password");
      assertTrue(playerDatabase.hasPlayer(trans,"Test Player"));
      
      playerDatabase.verifyAccount(trans,"Test Player","Test Password");
      
      RPObject test=new RPObject();
      test.put("object_id",1);
      playerDatabase.addCharacter(trans,"Test Player", "Test Character",test);
      assertTrue(playerDatabase.hasCharacter(trans,"Test Player", "Test Character"));
    }
    catch(Exception e)
    {
      fail(e.getMessage());
    }
    
    return playerDatabase;
  }
  
  private void cleanDatabase()
  {
    PlayerDatabase playerDatabase=null;
    
    try
    {
      playerDatabase=PlayerDatabaseFactory.getDatabase();
      Transaction trans = playerDatabase.getTransaction();
      assertNotNull(playerDatabase);
      
      playerDatabase.removeCharacter(trans,"Test Player", "Test Character");
      assertFalse(playerDatabase.hasCharacter(trans,"Test Player", "Test Character"));
      
      playerDatabase.removePlayer(trans,"Test Player");
      assertFalse(playerDatabase.hasPlayer(trans,"Test Player"));
    }
    catch(Exception e)
    {
      fail(e.getMessage());
    }
  }
  
  public void testPlayerEntryContainer()
  {
    marauroad.trace("Test_PlayerEntryContainer::testPlayerEntryContainer","?","This test case try to use the PlayerEntryContainer"+
                      " in a very similar way of what the server would do, by adding players, getting them, changing the state and checking that"+
                      " the value contained is the expected");
    marauroad.trace("Test_PlayerEntryContainer::testPlayerEntryContainer",">");
    
    createDatabase();
    PlayerEntryContainer container=PlayerEntryContainer.getContainer();
    
    assertNotNull(container);
    
    try
    {
      assertEquals(container.size(),0);
      
      int clientid=container.addRuntimePlayer("Test Player", new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT));
      
      assertEquals(container.size(),1);
      
      assertTrue(container.hasPlayer("Test Player"));
      assertEquals("Test Player",container.getUsername(clientid));
      assertEquals(new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT), container.getInetSocketAddress(clientid));
      assertEquals(container.getRuntimeState(clientid),PlayerEntryContainer.STATE_NULL);
      
      assertTrue(container.verifyRuntimePlayer(clientid,new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT)));
      container.changeRuntimeState(clientid,PlayerEntryContainer.STATE_LOGIN_COMPLETE);
      assertEquals(container.getRuntimeState(clientid),PlayerEntryContainer.STATE_LOGIN_COMPLETE);
      
      assertTrue(container.verifyAccount("Test Player","Test Password"));
      assertEquals(1,container.getCharacterList(clientid).length);
      assertEquals("Test Character",container.getCharacterList(clientid)[0]);
      assertTrue(container.hasCharacter(clientid,"Test Character"));
      
      RPObject test=new RPObject();
      test.put("object_id",1);
      
      assertEquals(test,container.getRPObject(clientid,"Test Character"));
      
      container.removeRuntimePlayer(clientid);
      
      assertEquals(container.size(),0);
    }
    catch(PlayerEntryContainer.NoSuchClientIDException e)
    {
      fail(e.getMessage());
    }
    catch(PlayerEntryContainer.NoSuchPlayerException e)
    {
      e.printStackTrace();
      fail(e.getMessage());
    }
    catch(PlayerEntryContainer.NoSuchCharacterException e)
    {
      fail(e.getMessage());
    }
    finally
    {
      cleanDatabase();
      marauroad.trace("Test_PlayerEntryContainer::testPlayerEntryContainer","<");
    }
  }
  
  public void testPlayerEntryContainerExceptions()
  {
    marauroad.trace("Test_PlayerEntryContainer::testPlayerEntryContainerExceptions","?","This test case try to show that"+
                      " when operatted incorrectly playerEntryContainer will throw several types of exceptions");
    marauroad.trace("Test_PlayerEntryContainer::testPlayerEntryContainerExceptions",">");
    PlayerEntryContainer container=PlayerEntryContainer.getContainer();
    
    assertNotNull(container);
    int randomClientID=0;
    
    assertEquals(container.size(),0);
    
    try
    {
      container.getUsername(randomClientID);
      fail("Exception did not happened");
    }
    catch(PlayerEntryContainer.NoSuchClientIDException e)
    {
      assertTrue(true);
    }
    
    assertEquals(container.size(),0);
    
    try
    {
      container.getRuntimeState(randomClientID);
      fail("Exception did not happened");
    }
    catch(PlayerEntryContainer.NoSuchClientIDException e)
    {
      assertTrue(true);
    }
    
    assertEquals(container.size(),0);
    
    try
    {
      container.removeRuntimePlayer(randomClientID);
      fail("Exception did not happened");
    }
    catch(PlayerEntryContainer.NoSuchClientIDException e)
    {
      assertTrue(true);
    }
    
    assertEquals(container.size(),0);
    
    try
    {
      container.changeRuntimeState(randomClientID,PlayerEntryContainer.STATE_NULL);
      fail("Exception did not happened");
    }
    catch(PlayerEntryContainer.NoSuchClientIDException e)
    {
      assertTrue(true);
    }
    
    marauroad.trace("Test_PlayerEntryContainer::testPlayerEntryContainerExceptions","<");
  }
}
