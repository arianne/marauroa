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
	/** It is really, really, really hard to verify RPServerManager, as all the 
	 *  behaviour is hidden by GameManager and Scheduler */
    }
  }