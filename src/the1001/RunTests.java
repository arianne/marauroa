/* $Id: RunTests.java,v 1.6 2004/01/07 17:32:10 arianne_rpg Exp $ */
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
package the1001;

import junit.framework.*;
import java.util.*;
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
      Date start=new Date();
      boolean wasSuccessful=true;
      String timestamp=Long.toString(new Date().getTime());
      
      wasSuccessful&=runTest(suite(),timestamp).wasSuccessful();

      Date end=new Date();
      
      System.err.println("Total time: "+(end.getTime()-start.getTime()));
      System.err.println("The test ("+timestamp+") has been "+(wasSuccessful?"SUCCESSFULL":"FAILED"));
      
      if(!wasSuccessful)
        {
        System.err.println("ERROR: the1001 RunTests failed, please send the log files ("+timestamp+") to:");
        System.err.println("http://sourceforge.net/tracker/?func=add&group_id=66537&atid=514826");
        }
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
//    System.setOut(new PrintStream(new FileOutputStream(filename)));
    TestResult result=junit.textui.TestRunner.run(e);
    String testResult=(result.wasSuccessful()?"Correct":"Failed");
    System.err.println("TestResult::runTest\t<\t"+testSuite.getName()+"("+testResult+")");
    return result;
    }
  
  public static Test suite()
    {
    TestSuite suite= new TestSuite("All marauroa Tests");

    suite.addTest(new TestSuite(the1001.Test_RPCode.class));
    suite.addTest(new TestSuite(the1001.Test_the1001.class));

    return suite;
    }
  }
  