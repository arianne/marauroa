package marauroa.game;

import marauroa.net.NetConst;
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
    
    try
      {
      playerDatabase=PlayerDatabaseFactory.getDatabase();
      assertNotNull(playerDatabase);
      }
    catch(PlayerDatabase.NoDatabaseConfException e)
      {
      fail(e.getMessage());
      }
    
    try
      {    
      playerDatabase.addPlayer("Test Player","Test Password");
      assertTrue(playerDatabase.hasPlayer("Test Player"));
      
      playerDatabase.verifyAccount("Test Player","Test Password");

      playerDatabase.addCharacter("Test Player", "Test Character",new RPObject());
      assertTrue(playerDatabase.hasCharacter("Test Player", "Test Character"));
      }
    catch(Throwable e)
      {
      fail(e.getMessage());
      }
    
    return playerDatabase;
    }
    
  public void testPlayerEntryContainer()
    {
    createDatabase();
    PlayerEntryContainer container=PlayerEntryContainer.getContainer();
    
    assertNotNull(container);
    
    try
      {
      assertEquals(container.size(),0);
      
      short clientid=container.addRuntimePlayer("Test Player", new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT));
      
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
      
      assertEquals(new RPObject(),container.getRPObject(clientid,"Test Character"));
      
      container.removeRuntimePlayer(clientid);
      
      assertEquals(container.size(),0);
      }
    catch(PlayerEntryContainer.NoSuchClientIDException e)
      {
      fail(e.getMessage());
      }
    catch(PlayerEntryContainer.NoSuchPlayerException e)
      {
      fail(e.getMessage());
      }
    catch(PlayerEntryContainer.NoSuchCharacterException e)
      {
      fail(e.getMessage());
      }
    }

  public void testPlayerEntryContainerExceptions()
    {
    PlayerEntryContainer container=PlayerEntryContainer.getContainer();
    
    assertNotNull(container);
    short randomClientID=0;
      
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
    }
  }