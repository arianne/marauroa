package marauroa.game;

import junit.framework.*;
import marauroa.game.*;
import marauroa.net.*;
import marauroa.*;
import java.io.*;
import java.net.*;

public class Test_RPServerManager extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_RPServerManager.class);
	}
	
  public void testRPServerManager()  
	{
	marauroad.trace("Test_RPServerManager::testRPServerManager",">");
	
	try
	  {
	  RPServerManager rpMan=new RPServerManager(new NetworkServerManager());
	  }
	catch(Exception e)
	  {
	  fail(e.getMessage());
	  }
	finally
	  {
      marauroad.trace("Test_RPServerManager::testRPServerManager","<");
	  }
    }
  }