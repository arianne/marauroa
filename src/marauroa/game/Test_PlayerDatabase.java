/* $Id: Test_PlayerDatabase.java,v 1.13 2004/01/29 18:36:41 arianne_rpg Exp $ */
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
import marauroa.*;

public class Test_PlayerDatabase extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_PlayerDatabase.class);
	}
  
  public void testMemoryPlayerDatabase()
    {
    testPlayerDatabase("MemoryPlayerDatabase");
    }

  public void testMemoryPlayerDatabaseExceptions()
    {
    testPlayerDatabaseExceptions("MemoryPlayerDatabase");
    }
    
  public void testJDBCPlayerDatabase()
    {
    testPlayerDatabase("JDBCPlayerDatabase");
    }

  public void testJDBCPlayerDatabaseExceptions()
    {
    testPlayerDatabaseExceptions("JDBCPlayerDatabase");
    }
    
  public void testJDBCPlayerDatabaseSecurity()
    {
    marauroad.trace("Test_PlayerDatabase::testJDBCPlayerDatabaseSecurity",">");

    try
      {
      PlayerDatabase playerDatabase=PlayerDatabaseFactory.getDatabase("JDBCPlayerDatabase");
      assertNotNull(playerDatabase);

      playerDatabase.addPlayer("Test Player","Test Password");
      assertTrue(playerDatabase.hasPlayer("Test Player"));
      
      assertFalse(playerDatabase.hasPlayer("b\" or 1=1 or username like 'b"));
      assertFalse(playerDatabase.hasPlayer("b\' or 1=1 or username like 'b"));
      assertFalse(playerDatabase.verifyAccount("b\' or 1=1 or username like 'b","b\' or 1=1 or password like 'b"));
      assertFalse(playerDatabase.verifyAccount("b\" or 1=1 or username like 'b","b\' or 1=1 or password like 'b"));

      playerDatabase.removePlayer("Test Player");
      }
    catch(Exception e)
      {
      marauroad.trace("Test_PlayerDatabase::testJDBCPlayerDatabaseSecurity","X",e.getMessage());
      }
    finally 
      {
      marauroad.trace("Test_PlayerDatabase::testJDBCPlayerDatabaseSecurity","<");
      }
    }

  private void testPlayerDatabase(String type)
    {
    marauroad.trace("Test_PlayerDatabase::testPlayerDatabase","?","This test case operates the Database in a similar"+
      " way of what PlayerEntryContainer does, by adding, getting and removing players and login events");
    marauroad.trace("Test_PlayerDatabase::testPlayerDatabase",">");
    
    try
      {    
      PlayerDatabase playerDatabase=PlayerDatabaseFactory.getDatabase(type);
      assertNotNull(playerDatabase);

 	  if(playerDatabase.hasPlayer("Test Player"))
 	    {
 	    playerDatabase.removePlayer("Test Player");
 	    }

      int size=playerDatabase.getPlayerCount();
 	    
 	  assertFalse(playerDatabase.hasPlayer("Test Player"));
 	    
      playerDatabase.addPlayer("Test Player","Test Password");
      assertTrue(playerDatabase.hasPlayer("Test Player"));
      
      assertTrue(playerDatabase.verifyAccount("Test Player","Test Password"));

      playerDatabase.addCharacter("Test Player", "Test Character",new RPObject());
      assertTrue(playerDatabase.hasCharacter("Test Player", "Test Character"));

	  String[] characters=playerDatabase.getCharactersList("Test Player");      
	  assertEquals(characters[0],"Test Character");
	  
	  playerDatabase.removeCharacter("Test Player","Test Character");
	  assertFalse(playerDatabase.hasCharacter("Test Player", "Test Character"));
	  
	  playerDatabase.removePlayer("Test Player");
	  assertFalse(playerDatabase.hasPlayer("Test Player"));

      assertEquals(size,playerDatabase.getPlayerCount());
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    finally
      {
      marauroad.trace("Test_PlayerDatabase::testPlayerDatabase","<");
      }
    }

  private void testPlayerDatabaseExceptions(String type)
    {
    marauroad.trace("Test_PlayerDatabase::testPlayerDatabaseExceptions","?","This test case try to show that"+
      " when operatted incorrectly PlayerDatabase will throw several types of exceptions");
    marauroad.trace("Test_PlayerDatabase::testPlayerDatabaseExceptions",">");
    PlayerDatabase playerDatabase=null;
    
    try
      {
      playerDatabase=PlayerDatabaseFactory.getDatabase(type);
      assertNotNull(playerDatabase);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    
    try
      {    
      playerDatabase.addPlayer("Test Player","Test Password");
      assertTrue(playerDatabase.hasPlayer("Test Player"));
      playerDatabase.addPlayer("Test Player","Test Password");
      fail("Player added twice");
      }
    catch(PlayerDatabase.PlayerAlreadyAddedException e)
      {
      try
        {
        playerDatabase.removePlayer("Test Player");
        }
      catch(PlayerDatabase.PlayerNotFoundException epnf)
        {
        fail("Player has not been added");
        }
        
      assertFalse(playerDatabase.hasPlayer("Test Player"));
      }
      
    try
      {
      playerDatabase.addCharacter("A new Test Player", "Test Character", new RPObject());
      fail("Player does not exist");
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }

    try
      {
      playerDatabase.addPlayer("A new Test Player","Test Password");
      playerDatabase.addCharacter("A new Test Player", "Test Character", new RPObject());
      playerDatabase.addCharacter("A new Test Player", "Test Character", new RPObject());
      fail("Player already exists");
      }
    catch(PlayerDatabase.CharacterAlreadyAddedException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    finally
      {
      try
        {
        playerDatabase.removePlayer("A new Test Player");
        }
      catch(PlayerDatabase.PlayerNotFoundException epnf)
        {
        fail("Player has not been added");
        }
      }

    try
      {
      playerDatabase.removeCharacter("A new Test Player", "Test Character");
      fail("Player does not exist");
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }

    try
      {
      playerDatabase.getLoginEvent("A new Test Player");
      fail("Player does not exist");
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }

	try
	  {
	  String[] characters=playerDatabase.getCharactersList("A new Test Player");      
	  fail("Player does not exist");
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      assertTrue(true);
      }

    try
      {
      playerDatabase.hasCharacter("A new Test Player", "Test Character");
      fail("Player does not exist");
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }

    try
      {
      playerDatabase.addLoginEvent("A new Test Player", null, true);
      fail("Player does not exist");
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }

    try
      {
      playerDatabase.setRPObject("A new Test Player","Test Character",new RPObject());
      fail("Player does not exists");
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }

    try
      {
      playerDatabase.addPlayer("A new Test Player","Test Password");
      playerDatabase.setRPObject("A new Test Player","Test Character",new RPObject());
      fail("Character does not exists");
      }
    catch(PlayerDatabase.CharacterNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    finally
      {
      try
        {
        playerDatabase.removePlayer("A new Test Player");
        }
      catch(PlayerDatabase.PlayerNotFoundException epnf)
        {
        fail("Player has not been added");
        }
      }

    try
      {
      playerDatabase.getRPObject("A new Test Player","Test Character");
      fail("Player does not exists");
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }

    try
      {
      playerDatabase.addPlayer("A new Test Player","Test Password");
      playerDatabase.getRPObject("A new Test Player","Test Character");
      fail("Character does not exists");
      }
    catch(PlayerDatabase.CharacterNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      { 
      fail(e.getMessage());
      }
    finally
      {
      try
        {
        playerDatabase.removePlayer("A new Test Player");
        }
      catch(PlayerDatabase.PlayerNotFoundException epnf)
        {
        fail("Player has not been added");
        }
      }

    marauroad.trace("Test_PlayerDatabase::testPlayerDatabaseExceptions","<");
    }
  }