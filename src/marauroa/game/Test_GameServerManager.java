/* $Id: Test_GameServerManager.java,v 1.17 2004/04/03 17:40:31 arianne_rpg Exp $ */
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
      PlayerDatabase playerDatabase=PlayerDatabaseFactory.getDatabase();
      Transaction trans = playerDatabase.getTransaction();

      if(playerDatabase.hasPlayer(trans,"Test Player"))
        {
        playerDatabase.removePlayer(trans,"Test Player");
        }
      assertFalse(playerDatabase.hasPlayer(trans,"Test Player"));
      playerDatabase.addPlayer(trans,"Test Player","Test Password","test@marauroa.ath.cx");

      RPObject SonGoku=new RPObject();

      SonGoku.put("id","1");
      SonGoku.put("name","Son Goku");
      playerDatabase.addCharacter(trans,"Test Player", "Son Goku",SonGoku);
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
      Transaction trans = playerDatabase.getTransaction();

      playerDatabase.removeCharacter(trans,"Test Player", "Son Goku");
      playerDatabase.removePlayer(trans,"Test Player");
      }
    catch(Exception e)
      {
      }
    assertTrue("Shutdown correctly",true);
    }

  public void testMainProcedures()
    {
    marauroad.trace("Test_GameServerManager::testMainProcedures","?","This test case show how thing should go if everything is fine");
    marauroad.trace("Test_GameServerManager::testMainProcedures",">");
    createEnviroment();
    try
      {
      InetSocketAddress address=new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT);

      netMan.addMessage(new MessageC2SLogin(address,"Test Player","Test Password"));

      int clientid=-1;
      int recieved=0;

      while(recieved!=3)
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
        else if(msg instanceof MessageS2CServerInfo)
          {
          assertTrue("Recieved server info",true);
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
      while(recieved!=4)
        {
        Message msg=null;

        while(msg==null) msg=netMan.getMessage();
        if(msg instanceof MessageS2CChooseCharacterACK)
          {
          assertTrue("Correct character parameters",true);
          ++recieved;
          }
        else if(msg instanceof MessageS2CPerception)
          {
          }
        else
          {
          fail("ERROR: Can't choose character. Got "+msg.toString());
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
        else if(msg instanceof MessageS2CPerception)
          {
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
    marauroad.trace("Test_GameServerManager::testMainLoginFailures","?","This test case shows the two tipical login"+
      " failures: login with bad username/password or login twice");
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
      while(recieved!=4)
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
        else if(msg instanceof MessageS2CServerInfo)
          {
          assertTrue("Recieved server info",true);
          ++recieved;
          }
        else
          {
          fail("ERROR: Can't login. Got "+msg.toString());
          }
        }
      netMan.addMessage(new MessageC2SLogin(address,"Test Player","Test Password"));
      while(recieved!=5)
        {
        Message msg=null;

        while(msg==null) msg=netMan.getMessage();
        if(msg instanceof MessageS2CLoginACK)
          {
          assertTrue("Correct login",true);
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
      while(recieved!=6)
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
    catch(MessageFactory.InvalidVersionException e)
      {
      fail();
      }
    finally
      {
      finalizeEnviroment();
      marauroad.trace("Test_GameServerManager::testMainLoginFailures","<");
      }
    }
  
  public void testMainChooseCharacterFailures()
    {
    marauroad.trace("Test_GameServerManager::testMainChooseCharacterFailures","?","This test case show the failures on"+
      " chooseCharacter, for example that a not logged player can't choose character or that if we choose an incorrect"+
      " character it will also fail");
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
        else if(msg instanceof MessageS2CServerInfo)
          {
          assertTrue("Recieved server info",true);
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
      while(recieved!=4)
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
    catch(MessageFactory.InvalidVersionException e)
      {
      fail();
      }
    finally
      {
      finalizeEnviroment();
      marauroad.trace("Test_GameServerManager::testMainChooseCharacterFailures","<");
      }
    }

  public void testMainLogoutFailures()
    {
    marauroad.trace("Test_GameServerManager::testMainLogoutFailures","?","This test case shows that a not logged player"+
      " can't logout out");
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
    catch(MessageFactory.InvalidVersionException e)
      {
      fail();
      }
    finally
      {
      finalizeEnviroment();
      marauroad.trace("Test_GameServerManager::testMainLogoutFailures","<");
      }
    }

  public void testMainActionFailures()
    {
    marauroad.trace("Test_GameServerManager::testMainActionFailures","?","This test case try to show that"+
      " a not logged in player can't command any kind of actions");
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
    catch(MessageFactory.InvalidVersionException e)
      {
      fail();
      }
    finally
      {
      finalizeEnviroment();
      marauroad.trace("Test_GameServerManager::testMainActionFailures","<");
      }
    }
  }
