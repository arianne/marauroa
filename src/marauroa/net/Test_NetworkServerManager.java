/* $Id: Test_NetworkServerManager.java,v 1.18 2004/11/12 15:39:16 arianne_rpg Exp $ */
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
package marauroa.net;

import marauroa.net.*;
import marauroa.*;
import java.io.*;
import java.net.*;
import junit.framework.*;
import marauroa.game.*;
import java.util.*;

public class Test_NetworkServerManager extends TestCase
  {
  public static Test suite()
    {
    return new TestSuite(Test_NetworkServerManager.class);
    }

  public void testNetworkServerManager()
    {
    marauroad.trace("Test_NetworkServerManager::testNetworkServerManager",">");

    NetworkServerManager netManager=null;
    NetworkClientManager netClient=null;
    
    try
      {
      netManager=new NetworkServerManager();
      netClient=new NetworkClientManager("127.0.0.1");
      }
    catch(SocketException e)
      {
      fail(e.getMessage());
      return;
      }
    try
      {
      assertNotNull(netManager);
      assertNotNull(netClient);
      
      Message msg=new MessageC2SLogin(null,"Test username","Test password");

      msg.setClientID(1423);
      netClient.addMessage(msg);

      Message result=netManager.getMessage();
      MessageC2SLogin realResult=(MessageC2SLogin)result;
    
      assertNotNull(result);
      assertEquals(realResult.getUsername(),"Test username");
      assertEquals(realResult.getPassword(),"Test password");
      
      InetSocketAddress clientAddress=result.getAddress();
      
      msg.setAddress(clientAddress);
      netManager.addMessage(msg);
      result=null;

      int i=0;

      while(result==null && i<10)
        {
        result=netClient.getMessage();
        ++i;
        }
      assertNotNull(result);
      assertEquals(realResult.getUsername(),"Test username");
      assertEquals(realResult.getPassword(),"Test password");
      
      Perception perception=new Perception (Perception.SYNC,new IRPZone.ID(""));
      perception.addedList=createBigPerception();
      MessageS2CPerception.clearPrecomputedPerception();
      msg=new MessageS2CPerception(clientAddress, perception);
      netManager.addMessage(msg);
      result=null;
      i=0;
      while(result==null && i<10)
        {
        result=netClient.getMessage();
        ++i;
        }
      assertNotNull(result);

      MessageS2CPerception perceptionMsg=(MessageS2CPerception)result;

      assertEquals(perceptionMsg.getAddedRPObjects(), perception.addedList);
      }
    catch(Exception e)
      {
      System.out.println(e);
      fail();
      }
    finally
      {
      netManager.finish();
      marauroad.trace("Test_NetworkServerManager::testNetworkServerManager","<");
      }
    }
    
  private List<RPObject> createBigPerception()
    {
    List<RPObject> list=new LinkedList<RPObject>();
    
    for(int i=0;i<400;++i)
      {
      RPObject object=new RPObject();

      object.put("id",i);
      object.put("name","A good name");
      object.put("extra","more extra attributes so that it takes several bytes");
      object.put("more","You know... this is big");
      list.add(object);
      }
    return list;
    }
  }
