/* $Id: RunTests.java,v 1.48 2004/11/27 11:05:07 root777 Exp $ */
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
package marauroa;

import junit.framework.*;
import java.util.*;
import java.io.*;

/* TODO: Improve code quality here. It stinks */

/** TestSuite that runs all the sample tests */
public class RunTests
  {
  private static class Killer extends Thread
    {
    private long timeout;
    private boolean finishRequest;
    public Killer(long timeout)
      {
      this.timeout=timeout;
      finishRequest=false;
      start();
      }
    
    public void run()
      {
      try
        {
        while(!finishRequest && timeout>0)
          {
          sleep(1000);
          timeout-=1000;
          }
        }
      catch(Exception e)
        {
        }
      if(timeout<=0)
        {
        System.err.println("ERROR: RunTests killed, please send the log files to:");
        System.err.println("http://sourceforge.net/tracker/?func=add&group_id=66537&atid=514826");
        System.exit(-1);
        }
      }
    
    public void finish()
      {
      finishRequest=true;
      }
    }
    
  public static void main (String[] args)
    {
    /** NOTE: On my machine it took 93000 milliseconds. */
    final long TIMEOUT_KILL_APPLICATION=180000;
    
    try
      {
      Killer killer=new Killer(TIMEOUT_KILL_APPLICATION);
      Date start=new Date();
      boolean wasSuccessful=true;
      String timestamp=Long.toString(new Date().getTime());
      
      System.err.println("Test will last around 93.0 seconds. Please wait!");
      wasSuccessful&=runTest(suiteBase(),timestamp).wasSuccessful();
      wasSuccessful&=runTest(suiteNet(),timestamp).wasSuccessful();
      wasSuccessful&=runTest(suiteGame(),timestamp).wasSuccessful();
      wasSuccessful&=runTest(suiteActive(),timestamp).wasSuccessful();
//      wasSuccessful&=runTest(suiteOneTest(),timestamp).wasSuccessful();

      Date end=new Date();
      
      System.err.println("Total time: "+(end.getTime()-start.getTime()));
      System.err.println("The test ("+timestamp+") has been "+(wasSuccessful?"SUCCESSFULL":"FAILED"));
      if(!wasSuccessful)
        {
        System.err.println("ERROR: RunTests failed, please send the log files ("+timestamp+") to:");
        System.err.println("http://sourceforge.net/tracker/?func=add&group_id=66537&atid=514826");
        }
      killer.finish();
      }
    catch(Exception e)
      {
      }
    }
    
  private static TestResult runTest(Test e,String timestamp) throws FileNotFoundException
    {
    TestSuite testSuite=(TestSuite)e;
    String filename="output_"+testSuite.getName()+"_"+timestamp+".txt";

    System.err.println("TestResult::runTest\t>\t"+testSuite.getName());
    System.setOut(new PrintStream(new FileOutputStream(filename)));

    TestResult result=junit.textui.TestRunner.run(e);
    String testResult=(result.wasSuccessful()?"Correct":"Failed");

    System.err.println("TestResult::runTest\t<\t"+testSuite.getName()+"("+testResult+")");
    return result;
    }
  
  public static Test suite()
    {
    TestSuite suite= new TestSuite("All marauroa Tests");

    suite.addTest(new TestSuite(marauroa.Test_RWLock.class));
    suite.addTest(new TestSuite(marauroa.Test_Configuration.class));
    suite.addTest(new TestSuite(marauroa.net.Test_SerializerByte.class));
    suite.addTest(new TestSuite(marauroa.net.Test_SerializerShort.class));
    suite.addTest(new TestSuite(marauroa.net.Test_SerializerInt.class));
    suite.addTest(new TestSuite(marauroa.net.Test_SerializerByteArray.class));
    suite.addTest(new TestSuite(marauroa.net.Test_SerializerString.class));
    suite.addTest(new TestSuite(marauroa.net.Test_Messages.class));
    suite.addTest(new TestSuite(marauroa.net.Test_MessageFactory.class));
//    suite.addTest(new TestSuite(marauroa.game.Test_PlayerDatabase.class));
    suite.addTest(new TestSuite(marauroa.game.Test_PlayerEntryContainer.class));
    suite.addTest(new TestSuite(marauroa.game.Test_Attributes.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPAction.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPZone.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPObject.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPClass.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPSlot.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPScheduler.class));
    suite.addTest(new TestSuite(marauroa.game.Test_MarauroaRPZone.class));
    suite.addTest(new TestSuite(marauroa.net.Test_NetworkServerManager.class));
    suite.addTest(new TestSuite(marauroa.game.Test_GameServerManager.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPServerManager.class));
    return suite;
    }

  public static Test suiteBase ( )
    {
    TestSuite suite= new TestSuite("Base marauroa Tests");

    suite.addTest(new TestSuite(marauroa.Test_RWLock.class));
    suite.addTest(new TestSuite(marauroa.Test_Configuration.class));
    return suite;
    }

  public static Test suiteNet ( )
    {
    TestSuite suite= new TestSuite("Network marauroa Tests");

    suite.addTest(new TestSuite(marauroa.net.Test_SerializerByte.class));
    suite.addTest(new TestSuite(marauroa.net.Test_SerializerShort.class));
    suite.addTest(new TestSuite(marauroa.net.Test_SerializerInt.class));
    suite.addTest(new TestSuite(marauroa.net.Test_SerializerByteArray.class));
    suite.addTest(new TestSuite(marauroa.net.Test_SerializerString.class));
    suite.addTest(new TestSuite(marauroa.net.Test_Messages.class));
    suite.addTest(new TestSuite(marauroa.net.Test_MessageFactory.class));
    suite.addTest(new TestSuite(marauroa.net.Test_InetAddressMask.class));
    suite.addTest(new TestSuite(marauroa.net.Test_PacketValidator.class));
    return suite;
    }

  public static Test suiteOneTest ( )
    {
    TestSuite suite= new TestSuite("One Test case marauroa Tests");

    suite.addTest(new TestSuite(marauroa.game.Test_RPClass.class));
    return suite;
    }

  public static Test suiteGame ( )
    {
    TestSuite suite= new TestSuite("Game marauroa Tests");
   
//    suite.addTest(new TestSuite(marauroa.game.Test_PlayerDatabase.class));
    suite.addTest(new TestSuite(marauroa.game.Test_PlayerEntryContainer.class));
    suite.addTest(new TestSuite(marauroa.game.Test_Attributes.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPAction.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPZone.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPObject.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPSlot.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPScheduler.class));
    suite.addTest(new TestSuite(marauroa.game.Test_MarauroaRPZone.class));
    return suite;
    }

  public static Test suiteActive ( )
    {
    TestSuite suite= new TestSuite("Active marauroa Tests");

    suite.addTest(new TestSuite(marauroa.net.Test_NetworkServerManager.class));
    suite.addTest(new TestSuite(marauroa.game.Test_GameServerManager.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPServerManager.class));
    return suite;
    }
  }

