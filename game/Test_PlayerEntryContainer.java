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
	
  public void testPlayerEntryContainer()
    {
    PlayerEntryContainer container=PlayerEntryContainer.getContainer();
    
    assertNotNull(container);
    
    try
      {
      assertEquals(container.size(),0);
      
      short clientid=container.addPlayer("Test player", new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT));
      
      assertEquals(container.size(),1);

      assertEquals("Test player",container.getUsername(clientid));
      assertEquals(new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT), container.getInetSocketAddress(clientid));
      assertEquals(container.getState(clientid),PlayerEntryContainer.STATE_NULL);
      
      assertTrue(container.isPlayer(clientid,new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT)));
      container.setState(clientid,PlayerEntryContainer.STATE_LOGIN_COMPLETE);
      assertEquals(container.getState(clientid),PlayerEntryContainer.STATE_LOGIN_COMPLETE);
      
      container.removePlayer(clientid);
      
      assertEquals(container.size(),0);
      }
    catch(PlayerEntryContainer.NoSuchClientIDException e)
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
      }
    catch(PlayerEntryContainer.NoSuchClientIDException e)
      {
      assertTrue(true);
      }

    assertEquals(container.size(),0);
     
    try
      {     
      container.getState(randomClientID);
      }
    catch(PlayerEntryContainer.NoSuchClientIDException e)
      {
      assertTrue(true);
      }
      
    assertEquals(container.size(),0);
     
    try
      {     
      container.removePlayer(randomClientID);
      }
    catch(PlayerEntryContainer.NoSuchClientIDException e)
      {
      assertTrue(true);
      }

    assertEquals(container.size(),0);
     
    try
      {     
      container.setState(randomClientID,PlayerEntryContainer.STATE_NULL);
      }
    catch(PlayerEntryContainer.NoSuchClientIDException e)
      {
      assertTrue(true);
      }
    }
  }