package marauroa.net;

import marauroa.net.*;
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
    NetworkServerManager netManager=null;
    
    try
      {
      netManager=new NetworkServerManager();
      }
    catch(java.net.SocketException e)
      {
      fail(e.getMessage());
      return;
      }
      
    assertNotNull(netManager);
      
    InetSocketAddress address=new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT);
    Message msg=new MessageC2SLogin(address,"Test username","Test password");
    msg.setClientID((short)1423);
    
    netManager.addMessage(msg);
    Message result=netManager.getMessage();
    
    MessageC2SLogin realResult=(MessageC2SLogin)result;
    
    assertNotNull(result);
    assertEquals(realResult.getUsername(),"Test username");
    assertEquals(realResult.getPassword(),"Test password");
    
    netManager.finish();
    }
  }