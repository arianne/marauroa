/* $Id: RunTests.java,v 1.20 2003/12/08 23:46:24 arianne_rpg Exp $ */
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
import java.io.*;

/**
 * TestSuite that runs all the sample tests
 */
public class RunTests
  {  
  public static void main (String[] args)
    {
    try
      {
//      System.setOut(new PrintStream(new FileOutputStream("output.txt")));
//      boolean untilFailure=true;
//      while(untilFailure)
//        {
        //junit.swingui.TestRunner.run(RunTests.class);
        TestResult result=junit.textui.TestRunner.run(suite());
//        if(!result.wasSuccessful())
//          {
//          untilFailure=false;
//          }
//        }
      }
    catch(Exception e) 
      {
      }
    }
  
  public static Test suite ( )
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
   
    suite.addTest(new TestSuite(marauroa.game.Test_PlayerDatabase.class));
    suite.addTest(new TestSuite(marauroa.game.Test_PlayerEntryContainer.class));

    suite.addTest(new TestSuite(marauroa.game.Test_Attributes.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPAction.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPZone.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPObject.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPSlot.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPScheduler.class));

    suite.addTest(new TestSuite(marauroa.game.Test_MarauroaRPZone.class));

    suite.addTest(new TestSuite(marauroa.net.Test_NetworkServerManager.class));
    suite.addTest(new TestSuite(marauroa.game.Test_GameServerManager.class));
    suite.addTest(new TestSuite(marauroa.game.Test_RPServerManager.class));


    return suite;
    }
  }
  