package marauroa.game;

import junit.framework.*;
import marauroa.game.*;
import marauroa.net.*;
import marauroa.*;
import java.net.*;

public class Test_GameServerManager extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_GameServerManager.class);
	}

  private NetworkClientManager netMan;
  private NetworkClientManager netManSpoffer;
  private NetworkServerManager netServerMan;
  private GameServerManager gameMan;

  private void createEnviroment()
    {    
    try
      {
      /* We want to avoid the port in use error */
      NetConst.marauroa_PORT=NetConst.marauroa_PORT+1;
      
      netMan=new NetworkClientManager("127.0.0.1");
      netManSpoffer=new NetworkClientManager("127.0.0.1");
      netServerMan=new NetworkServerManager();
      gameMan= new GameServerManager(netServerMan);
      }
    catch(java.net.SocketException e)
      {
      fail(e.getMessage());
      }
    
    assertTrue("NetworkManager and GameManager inited",netMan!=null && gameMan!=null);
    
    try
      {
      PlayerDatabase playerDatabase=PlayerDatabaseFactory.getDatabase("MemoryPlayerDatabase");
      assertFalse(playerDatabase.hasPlayer("Test Player"));

      playerDatabase.addPlayer("Test Player","Test Password");

      RPObject SonGoku=new RPObject();
      SonGoku.put("object_id","1");
      SonGoku.put("name","Son Goku");
      playerDatabase.addCharacter("Test Player", "Son Goku",SonGoku);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    }
    
  private void finalizeEnviroment()
    {
    gameMan.finish();
    netServerMan.finish();

    try
      {
      PlayerDatabase playerDatabase=PlayerDatabaseFactory.getDatabase("MemoryPlayerDatabase");
      playerDatabase.removeCharacter("Test Player", "Son Goku");
      playerDatabase.removePlayer("Test Player");
      }
    catch(Exception e)
      {
      }
    
    assertTrue("Shutdown correctly",true);
    }    

  public void testMainProcedures()
    {
    marauroad.trace("Test_GameServerManager::testMainProcedures",">");
	createEnviroment();
	
    try
      {
      InetSocketAddress address=new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT);
      netMan.addMessage(new MessageC2SLogin(address,"Test Player","Test Password"));
      int clientid=-1;
      
      int recieved=0;
      while(recieved!=2)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();
        
        if(msg instanceof MessageS2CLoginACK)
          {
          assertTrue("Correct login parameters",true);
          clientid=msg.getClientID();
          ++recieved;
          }
        else if(msg instanceof MessageS2CCharacterList)
          {
          assertTrue("Recieved character list",true);
          ++recieved;
          }
        else
          {
          fail("ERROR: Can't login. Got "+msg.toString());
          }        
        }

      Message msgCC=new MessageC2SChooseCharacter(address,"Son Goku");
      msgCC.setClientID(clientid);      
      netMan.addMessage(msgCC);
      
      while(recieved!=3)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();

        if(msg instanceof MessageS2CChooseCharacterACK)
          {
          assertTrue("Correct character parameters",true);
          ++recieved;
          }
        else
          {
          fail("ERROR: Can't choose character. Got "+msg.toString());
          }        
        }
        
      Message msgL=new MessageC2SLogout(address);
      msgL.setClientID(clientid);      
      netMan.addMessage(msgL);
      
      while(recieved!=4)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();

        if(msg instanceof MessageS2CLogoutACK)
          {
          assertTrue("Correct logout parameters",true);
          ++recieved;
          }
        else
          {
          fail("ERROR: Can't logout. Got "+msg.toString());
          }        
        }
      }
    catch(Exception e)
      {
      }
    finally
      {
      finalizeEnviroment();
      marauroad.trace("Test_GameServerManager::testMainProcedures","<");
      }
    }  

  public void testMainLoginFailures()
    {
    marauroad.trace("Test_GameServerManager::testMainLoginFailures",">");
	createEnviroment();
	
	try
	  {
      InetSocketAddress address=new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT);
      netMan.addMessage(new MessageC2SLogin(address,"Wrong Test Player","Wrong Test Password"));
      int clientid=-1;

      int recieved=0;
      while(recieved!=1)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();
        
        if(msg instanceof MessageS2CLoginNACK)
          {
          assertTrue("Correct login failure",true);
          ++recieved;
          }
        else
          {
          fail("ERROR: Can login. Got "+msg.toString());
          }        
        }

	  netMan.addMessage(new MessageC2SLogin(address,"Test Player","Test Password"));
      
      while(recieved!=3)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();
        
        if(msg instanceof MessageS2CLoginACK)
          {
          assertTrue("Correct login parameter",true);
          clientid=msg.getClientID();
          ++recieved;
          }
        else if(msg instanceof MessageS2CCharacterList)
          {
          assertTrue("Recieved character list",true);
          ++recieved;
          }
        else
          {
          fail("ERROR: Can't login. Got "+msg.toString());
          }        
        }

      netMan.addMessage(new MessageC2SLogin(address,"Test Player","Test Password"));
      
      while(recieved!=4)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();
        
        if(msg instanceof MessageS2CLoginNACK)
          {
          assertTrue("Correct login failure",true);
          ++recieved;
          }
        else
          {
          fail("ERROR: Can login. Got "+msg.toString());
          }        
        }
        
      Message msgLSpoffer=new MessageC2SLogout(address);
      msgLSpoffer.setClientID(clientid);  
      netManSpoffer.addMessage(msgLSpoffer);
      
      if(msgLSpoffer!=null)
        {  
        int i=0;
        Message msg=null;
        while(msg==null && i<10) 
          {
          msg=netMan.getMessage();
          ++i;
          }

        if(i!=10)
          {
          fail("ERROR: Can spof. Got "+msg.toString());
          }

	    i=0;
        while(msg==null && i<10) 
          {
          msg=netManSpoffer.getMessage();
          ++i;
          }

        if(i!=10)
          {
          fail("ERROR: Can spof. Got "+msg.toString());
          }
        }


      Message msgL=new MessageC2SLogout(address);
      msgL.setClientID(clientid);      
      netMan.addMessage(msgL);
      
      while(recieved!=5)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();

        if(msg instanceof MessageS2CLogoutACK)
          {
          assertTrue("Correct logout parameters",true);
          ++recieved;
          }
        else
          {
          fail("ERROR: Can't logout. Got "+msg.toString());
          }        
        }
	  }
    finally
      {
      finalizeEnviroment();
      marauroad.trace("Test_GameServerManager::testMainLoginFailures","<");
      }
    } 
	
  public void testMainChooseCharacterFailures()
    {    
    marauroad.trace("Test_GameServerManager::testMainChooseCharacterFailures",">");
	createEnviroment();
	
	try
	  {
      InetSocketAddress address=new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT);
      int clientid=-1;

      Message msgCC=new MessageC2SChooseCharacter(address,"Son Goku");
      msgCC.setClientID(clientid);  
      netMan.addMessage(msgCC);
      
      if(msgCC!=null)
        {  
        int i=0;
        Message msg=null;
        while(msg==null && i<10) 
          {
          msg=netMan.getMessage();
          ++i;
          }

        if(i!=10)
          {
          fail("ERROR: Can choose character. Got "+msg.toString());
          }
        }
        
	  netMan.addMessage(new MessageC2SLogin(address,"Test Player","Test Password"));
	  int recieved=0;
      
      while(recieved!=2)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();
        
        if(msg instanceof MessageS2CLoginACK)
          {
          assertTrue("Correct login parameter",true);
          clientid=msg.getClientID();
          ++recieved;
          }
        else if(msg instanceof MessageS2CCharacterList)
          {
          assertTrue("Recieved character list",true);
          ++recieved;
          }
        else
          {
          fail("ERROR: Can't login. Got "+msg.toString());
          }        
        }

      Message msgCCFail=new MessageC2SChooseCharacter(address,"Wrong Son Goku");
      msgCCFail.setClientID(clientid);  
      netMan.addMessage(msgCCFail);
      
      while(recieved!=3)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();
        
        if(msg instanceof MessageS2CChooseCharacterNACK)
          {
          assertTrue("Correct choose character failure",true);
          ++recieved;
          }
        else
          {
          fail("ERROR: Can choose character. Got "+msg.toString());
          }        
        }

      Message msgCCSpoffer=new MessageC2SChooseCharacter(address,"Son Goku");
      msgCCSpoffer.setClientID(clientid);  
      netManSpoffer.addMessage(msgCCSpoffer);
      
      if(msgCCSpoffer!=null)
        {  
        int i=0;
        Message msg=null;
        while(msg==null && i<10) 
          {
          msg=netMan.getMessage();
          ++i;
          }

        if(i!=10)
          {
          fail("ERROR: Can sppof. Got "+msg.toString());
          }

	    i=0;
        while(msg==null && i<10) 
          {
          msg=netManSpoffer.getMessage();
          ++i;
          }

        if(i!=10)
          {
          fail("ERROR: Can sppof. Got "+msg.toString());
          }
        }
        
      Message msgL=new MessageC2SLogout(address);
      msgL.setClientID(clientid);      
      netMan.addMessage(msgL);
      
      while(recieved!=4)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();

        if(msg instanceof MessageS2CLogoutACK)
          {
          assertTrue("Correct logout parameters",true);
          ++recieved;
          }
        else
          {
          fail("ERROR: Can't logout. Got "+msg.toString());
          }        
        }
      }
    finally
      {
      finalizeEnviroment();
      marauroad.trace("Test_GameServerManager::testMainChooseCharacterFailures","<");
      }
    }  

  public void testMainLogoutFailures()
    {    
    marauroad.trace("Test_GameServerManager::testMainLogoutFailures",">");
	createEnviroment();
	
	try
	  {
      InetSocketAddress address=new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT);
      int clientid=-1;

      Message msgL=new MessageC2SLogout(address);
      msgL.setClientID(clientid);      
      netMan.addMessage(msgL);
      
      if(msgL!=null)
        {  
        int i=0;
        Message msg=null;
        while(msg==null && i<10) 
          {
          msg=netMan.getMessage();
          ++i;
          }

        if(i!=10)
          {
          fail("ERROR: Can logout. Got "+msg.toString());
          }
        }
	  }
    finally
      {
      finalizeEnviroment();
      marauroad.trace("Test_GameServerManager::testMainLogoutFailures","<");
      }
    }  


  public void testMainActionFailures()
    {    
    marauroad.trace("Test_GameServerManager::testMainActionFailures",">");
	createEnviroment();
	
	try
	  {
      InetSocketAddress address=new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT);
      int clientid=-1;

      Message msgA=new MessageC2SAction(address,new RPAction());
      msgA.setClientID(clientid);      
      netMan.addMessage(msgA);
      
      if(msgA!=null)
        {  
        int i=0;
        Message msg=null;
        while(msg==null && i<10) 
          {
          msg=netMan.getMessage();
          ++i;
          }

        if(i!=10)
          {
          fail("ERROR: Can add action. Got "+msg.toString());
          }
        }
	  }
    finally
      {
      finalizeEnviroment();
      marauroad.trace("Test_GameServerManager::testMainActionFailures","<");
      }
    }  
  }