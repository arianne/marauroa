package marauroa.net;

import marauroa.net.*;
import marauroa.*;
import java.io.*;
import java.net.*;
import junit.framework.*;

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
    
    netManager.finish();
    
    marauroad.trace("Test_NetworkServerManager::testNetworkServerManager","<");
    }
  }