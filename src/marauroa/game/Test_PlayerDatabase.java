package marauroa.game;

import junit.framework.*;

public class Test_PlayerDatabase extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_PlayerDatabase.class);
	}
	
  public void testPlayerDatabase()
    {
    try
      {    
      PlayerDatabase playerDatabase=PlayerDatabaseFactory.getDatabase();
      assertNotNull(playerDatabase);
      assertEquals(0,playerDatabase.getPlayerCount());

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

      assertEquals(0,playerDatabase.getPlayerCount());
      }
    catch(Throwable e)
      {
      fail(e.getMessage());
      }
    }

  public void testPlayerDatabaseExceptions()
    {
    PlayerDatabase playerDatabase=null;
    
    try
      {
      playerDatabase=PlayerDatabaseFactory.getDatabase();
      assertNotNull(playerDatabase);
      assertEquals(0,playerDatabase.getPlayerCount());
      }
    catch(Throwable e)
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
        
      assertTrue(true);
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
    catch(PlayerDatabase.GenericDatabaseException e)
      {
      fail(e.getMessage());
      }
    catch(PlayerDatabase.CharacterAlreadyAddedException e)
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
    }
  }