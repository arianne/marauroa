/* $Id: Test_RPClass.java,v 1.3 2004/09/05 09:09:24 arianne_rpg Exp $ */
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
import marauroa.net.*;
import marauroa.*;
import java.io.*;

public class Test_RPClass extends TestCase
  {
  private ByteArrayOutputStream out;
  private ByteArrayInputStream in;
  private InputSerializer sin;
  private OutputSerializer sout;

  public static Test suite ( ) 
    {
    return new TestSuite(Test_RPClass.class);
    }
    
  public void testRPClass() throws Exception
    {
    marauroad.trace("Test_RPClass::testRPClass",">");
    RPClass rpclass=new RPClass("Test");
    rpclass.add("normal_s", RPClass.STRING);
    rpclass.add("normal_i", RPClass.INT);
    rpclass.add("hidden_s", RPClass.STRING,RPClass.HIDDEN);
    rpclass.add("hidden_i", RPClass.INT,RPClass.HIDDEN);
    
    RPObject object=rpclass.getInstance(new RPObject.ID(1,""));
    
    out=new ByteArrayOutputStream();
    sout=new OutputSerializer(out);
    try
      {
      sout.write(object);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data out");
      }

    RPObject result=new RPObject(RPClass.getRPClass("Test"));
  
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }

    System.out.println(object);
    System.out.println(result);
    
    System.out.println(object.instanceOf(RPClass.getRPClass("Test")));

    marauroad.trace("Test_RPClass::testRPClass","<");
    }
  
  public static void main(String[] args)
    {
    try
      {
      new Test_RPClass().testRPClass();
      }
    catch(Exception e)
      {
      e.printStackTrace();
      }
    }
  }
