/* $Id: Test_Configuration.java,v 1.7 2004/07/07 10:07:04 arianne_rpg Exp $ */
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

public class Test_Configuration extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_Configuration.class);
    }
	
  public void testConfiguration()
    {
    marauroad.trace("Test_Configuration::testConfiguration","?","This test case "+
      "try to see if the Configuration class is able to load the file and get the "+
      "test attribute and set it, and check that the value is the correct");
    marauroad.trace("Test_Configuration::testConfiguration",">");
    try
      {    
      Configuration conf=Configuration.getConfiguration();
      String result=conf.get("test_ATestString");

      assertEquals(result,"ATestString");
      conf.set("test_ATestString", "AnotherTestString");
      result=conf.get("test_ATestString");
      assertEquals(result,"AnotherTestString");
      conf.set("test_ATestString", "ATestString");
      }
    catch(Configuration.PropertyFileNotFoundException e)
      {
      fail(e.getMessage());
      }
    catch(Configuration.PropertyNotFoundException e)
      {
      fail(e.getMessage());
      }
    finally
      {
      marauroad.trace("Test_Configuration::testConfiguration","<");
      }
    }
  }