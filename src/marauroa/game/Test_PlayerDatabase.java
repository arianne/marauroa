package marauroa.game;

import junit.framework.*;

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
    
	
  private void testPlayerDatabase(String type)
    {
    try
      {    
      PlayerDatabase playerDatabase=PlayerDatabaseFactory.getDatabase(type);
      assertNotNull(playerDatabase);
      int size=playerDatabase.getPlayerCount();

 	  if(playerDatabase.hasPlayer("Test Player"))
 	    {
 	    playerDatabase.removePlayer("Test Player");
 	    }
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
    }

  private void testPlayerDatabaseExceptions(String type)
    {
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
    }
  }