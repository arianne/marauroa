/* $Id: Test_RPAction.java,v 1.6 2004/07/13 20:31:53 arianne_rpg Exp $ */
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

public class Test_RPAction extends TestCase
  {
  private ByteArrayOutputStream out;
  private ByteArrayInputStream in;
  private InputSerializer sin;
  private OutputSerializer sout;
  public static Test suite ( ) 
    {
    return new TestSuite(Test_RPAction.class);
    }
	
  public void testAttributes()
    {
    marauroad.trace("Test_RPAction::testAttributes",">");
    try
      {
      RPAction attr=new RPAction();

      assertNotNull(attr);
      attr.put("Attribute","value");
    
      String value=null;

      value=attr.get("Attribute");
      assertNotNull(value);
      assertEquals("value",value);
      assertTrue(attr.has("Attribute"));
      }
    catch(AttributeNotFoundException e)
      {
      fail(e.getMessage());
      }
    finally
      {
      marauroad.trace("Test_RPAction::testAttributes","<");
      }
    }

  public void testAttributesException()
    {
    marauroad.trace("Test_RPAction::testAttributesException",">");
    try
      {
      RPAction attr=new RPAction();

      assertNotNull(attr);
    
      String value=null;

      value=attr.get("Attribute");
      fail("Exception not throwed");
      }
    catch(AttributeNotFoundException e)
      {
      assertTrue(true);
      }
    finally
      {
      marauroad.trace("Test_RPAction::testAttributesException","<");
      }
    }

  public void testAttributesSerialization()
    {
    marauroad.trace("Test_RPAction::testAttributesSerialization",">");

    RPAction attr=new RPAction();

    assertNotNull(attr);
    attr.put("Attribute","value");
    attr.put("Name","A random name");
    attr.put("Location","nowhere");
    attr.put("Test number",Integer.toString(5));
    out=new ByteArrayOutputStream();
    sout=new OutputSerializer(out);
    try
      {
      sout.write(attr);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data out");
      }
    
    RPAction result=new RPAction();
  
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
    assertEquals(attr,result);
    marauroad.trace("Test_RPAction::testAttributesSerialization","<");
    }

  public void testAttributesStatus()
    {
    marauroad.trace("Test_RPAction::testAttributesStatus",">");

    RPAction.Status status=new RPAction.Status(RPAction.STATUS_FAIL.getStatus());

    assertTrue(status.equals(RPAction.STATUS_FAIL));
    assertFalse(status.equals(RPAction.STATUS_INCOMPLETE));
    assertFalse(status.equals(RPAction.STATUS_SUCCESS));
    marauroad.trace("Test_RPAction::testAttributesStatus","<");
    }
  }
