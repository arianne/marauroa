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
    PlayerDatabase playerDatabase=PlayerDatabase.getDatabase();
    assertNotNull(playerDatabase);
    
    try
      {    
      playerDatabase.addPlayer("Test Player","Test Password");
      assertTrue(playerDatabase.exists("Test Player"));
      
      playerDatabase.isCorrect("Test Player","Test Password");

      playerDatabase.addCharacter("Test Player", "Test Character");
      assertTrue(playerDatabase.hasCharacter("Test Player", "Test Character"));

	  String[] characters=playerDatabase.getCharactersList("Test Player");      
	  assertEquals(characters[0],"Test Character");
	  
	  playerDatabase.removeCharacter("Test Player","Test Character");
	  assertFalse(playerDatabase.hasCharacter("Test Player", "Test Character"));
	  
	  playerDatabase.removePlayer("Test Player");
	  assertFalse(playerDatabase.exists("Test Player"));
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      fail(e.getMessage());
      }
    catch(PlayerDatabase.PlayerAlreadyAddedException e)
      {
      fail(e.getMessage());
      }
    }

  public void testPlayerDatabaseExceptions()
    {
    PlayerDatabase playerDatabase=PlayerDatabase.getDatabase();
    assertNotNull(playerDatabase);
    
    try
      {    
      playerDatabase.addPlayer("Test Player","Test Password");
      assertTrue(playerDatabase.exists("Test Player"));
      playerDatabase.addPlayer("Test Player","Test Password");
      fail("Player added twice");
      }
    catch(PlayerDatabase.PlayerAlreadyAddedException e)
      {
      assertTrue(true);
      }
      
    try
      {
      playerDatabase.addCharacter("A new Test Player", "Test Character");
      assertTrue(playerDatabase.hasCharacter("A new Test Player", "Test Character"));
      fail("Player does not exist");
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      assertTrue(true);
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