/* $Id: Test_PlayerDatabase.java,v 1.19 2004/07/13 20:31:53 arianne_rpg Exp $ */
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
      IPlayerDatabase playerDatabase=PlayerDatabaseFactory.getDatabase("JDBCPlayerDatabase");
      Transaction trans = playerDatabase.getTransaction();

      assertNotNull(playerDatabase);
      playerDatabase.addPlayer(trans,"Test Player","Test Password","test@marauroa.ath.cx");
      assertTrue(playerDatabase.hasPlayer(trans,"Test Player"));
      assertFalse(playerDatabase.hasPlayer(trans,"b\" or 1=1 or username like 'b"));
      assertFalse(playerDatabase.hasPlayer(trans,"b\' or 1=1 or username like 'b"));
      assertFalse(playerDatabase.verifyAccount(trans,"b\' or 1=1 or username like 'b","b\' or 1=1 or password like 'b"));
      assertFalse(playerDatabase.verifyAccount(trans,"b\" or 1=1 or username like 'b","b\' or 1=1 or password like 'b"));
      playerDatabase.removePlayer(trans,"Test Player");
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
      IPlayerDatabase playerDatabase=PlayerDatabaseFactory.getDatabase(type);
      Transaction trans = playerDatabase.getTransaction();

      assertNotNull(playerDatabase);
      if(playerDatabase.hasPlayer(trans,"Test Player"))
        {
        playerDatabase.removePlayer(trans,"Test Player");
        }
      
      int size=playerDatabase.getPlayerCount(trans);
      
      assertFalse(playerDatabase.hasPlayer(trans,"Test Player"));
      playerDatabase.addPlayer(trans,"Test Player","Test Password","test@marauroa.ath.cx");
      assertTrue(playerDatabase.hasPlayer(trans,"Test Player"));
      assertTrue(playerDatabase.verifyAccount(trans,"Test Player","Test Password"));
      playerDatabase.addCharacter(trans,"Test Player", "Test Character",new RPObject());
      assertTrue(playerDatabase.hasCharacter(trans,"Test Player", "Test Character"));
      
      String[] characters=playerDatabase.getCharactersList(trans,"Test Player");

      assertEquals(characters[0],"Test Character");
      playerDatabase.removeCharacter(trans,"Test Player","Test Character");
      assertFalse(playerDatabase.hasCharacter(trans,"Test Player", "Test Character"));
      playerDatabase.removePlayer(trans,"Test Player");
      assertFalse(playerDatabase.hasPlayer(trans,"Test Player"));
      assertEquals(size,playerDatabase.getPlayerCount(trans));
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

    IPlayerDatabase playerDatabase=null;
    Transaction trans = null;

    try
      {
      playerDatabase=PlayerDatabaseFactory.getDatabase(type);
      trans = playerDatabase.getTransaction();
      assertNotNull(playerDatabase);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    try
      {
      playerDatabase.addPlayer(trans,"Test Player","Test Password","test@marauroa.ath.cx");
      assertTrue(playerDatabase.hasPlayer(trans,"Test Player"));
      playerDatabase.addPlayer(trans,"Test Player","Test Password","test@marauroa.ath.cx");
      fail("Player added twice");
      }
    catch(PlayerAlreadyAddedException e)
      {
      try
        {
        playerDatabase.removePlayer(trans,"Test Player");
        assertFalse(playerDatabase.hasPlayer(trans,"Test Player"));
        }
      catch(Exception epnf)
        {
        fail("Player has not been added");
        }
      }
    catch(GenericDatabaseException e)
      {
      fail(e.getMessage());      
      }
      
    try
      {
      playerDatabase.addCharacter(trans,"A new Test Player", "Test Character", new RPObject());
      fail("Player does not exist");
      }
    catch(PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    try
      {
      playerDatabase.addPlayer(trans,"A new Test Player","Test Password","test@marauroa.ath.cx");
      playerDatabase.addCharacter(trans,"A new Test Player", "Test Character", new RPObject());
      playerDatabase.addCharacter(trans,"A new Test Player", "Test Character", new RPObject());
      fail("Player already exists");
      }
    catch(CharacterAlreadyAddedException e)
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
        playerDatabase.removePlayer(trans,"A new Test Player");
        }
      catch(Exception epnf)
        {
        fail("Player has not been added");
        }
      }
    try
      {
      playerDatabase.removeCharacter(trans,"A new Test Player", "Test Character");
      fail("Player does not exist");
      }
    catch(PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    try
      {
      playerDatabase.getLoginEvent(trans,"A new Test Player");
      fail("Player does not exist");
      }
    catch(PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    try
      {
      String[] characters=playerDatabase.getCharactersList(trans,"A new Test Player");

      fail("Player does not exist");
      }
    catch(PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(GenericDatabaseException e)
      {
      fail(e.getMessage());      
      }     

    try
      {
      playerDatabase.hasCharacter(trans,"A new Test Player", "Test Character");
      fail("Player does not exist");
      }
    catch(PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    try
      {
      playerDatabase.addLoginEvent(trans,"A new Test Player", null, true);
      fail("Player does not exist");
      }
    catch(PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    try
      {
      playerDatabase.setRPObject(trans,"A new Test Player","Test Character",new RPObject());
      fail("Player does not exists");
      }
    catch(PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    try
      {
      playerDatabase.addPlayer(trans,"A new Test Player","Test Password","test@marauroa.ath.cx");
      playerDatabase.setRPObject(trans,"A new Test Player","Test Character",new RPObject());
      fail("Character does not exists");
      }
    catch(CharacterNotFoundException e)
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
        playerDatabase.removePlayer(trans,"A new Test Player");
        }
      catch(PlayerNotFoundException epnf)
        {
        fail("Player has not been added");
        }
      catch(GenericDatabaseException e)
        {
        fail(e.getMessage());      
        }
      
      }
    try
      {
      playerDatabase.getRPObject(trans,"A new Test Player","Test Character");
      fail("Player does not exists");
      }
    catch(PlayerNotFoundException e)
      {
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    try
      {
      playerDatabase.addPlayer(trans,"A new Test Player","Test Password","test@marauroa.ath.cx");
      playerDatabase.getRPObject(trans,"A new Test Player","Test Character");
      fail("Character does not exists");
      }
    catch(CharacterNotFoundException e)
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
        playerDatabase.removePlayer(trans,"A new Test Player");
        }
      catch(PlayerNotFoundException epnf)
        {
        fail("Player has not been added");
        }
      catch(GenericDatabaseException e)
        {
        fail(e.getMessage());      
        }      
      }
    marauroad.trace("Test_PlayerDatabase::testPlayerDatabaseExceptions","<");
    }
  }
