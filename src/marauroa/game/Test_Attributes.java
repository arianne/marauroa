/* $Id: Test_Attributes.java,v 1.6 2003/12/30 10:24:35 arianne_rpg Exp $ */
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
import java.util.*;

public class Test_Attributes extends TestCase
  {
  private ByteArrayOutputStream out;
  private ByteArrayInputStream in;
  
  private InputSerializer sin;
  private OutputSerializer sout;

 
  public static Test suite ( ) 
    {
    return new TestSuite(Test_Attributes.class);
	}
	
  public void testAttributes()
    {
    marauroad.trace("Test_Attributes::testAttributes","?","This test case tests the normal operations done on a attribute");
    marauroad.trace("Test_Attributes::testAttributes",">");

    try
      {
      Attributes attr=new Attributes();
      assertNotNull(attr);
    
      attr.put("Attribute","value");
    
      String value=null;
      value=attr.get("Attribute");
      assertNotNull(value);
      assertEquals("value",value);
    
      assertTrue(attr.has("Attribute"));
      
      attr.put("Attribute",1);
      value=attr.get("Attribute");
      assertNotNull(value);
      assertEquals(1,Integer.parseInt(value));      
      assertEquals(1,attr.getInt("Attribute"));      

      List list=new LinkedList();
      list.add("Hi");
      list.add("World");
      list.add("This is a test");
      
      attr.put("Attribute",list);
      value=attr.get("Attribute");
      assertNotNull(value);
      
      Iterator it=list.iterator();
      Iterator it_result=Attributes.StringToList(value).iterator();
      while(it.hasNext())
        {
        assertEquals(it.next(),it_result.next());
        }
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      fail(e.getMessage());
      }
    finally
      { 
      marauroad.trace("Test_Attributes::testAttributes","<");
      }
    }

  public void testAttributesException()
    {
    marauroad.trace("Test_Attributes::testAttributesException","?","This test case tests that when operated badly Attributes throws exceptions");
    marauroad.trace("Test_Attributes::testAttributesException",">");

    try
      {
      Attributes attr=new Attributes();
      assertNotNull(attr);
    
      String value=null;
      value=attr.get("Attribute");
      fail("Exception not throwed");
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      assertTrue(true);
      }
    finally
      {
      marauroad.trace("Test_Attributes::testAttributesException","<");
      }
    }

  public void testAttributesSerialization()
    {
    marauroad.trace("Test_Attributes::testAttributesSerialization","?","Thjis test case tests that the serialization of the attribute is fine");
    marauroad.trace("Test_Attributes::testAttributesSerialization",">");
    
    Attributes attr=new Attributes();
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
    
    Attributes result=new Attributes();
  
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
    
    marauroad.trace("Test_Attributes::testAttributesSerialization","<");
    }
  }