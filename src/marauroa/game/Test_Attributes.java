package marauroa.game;

import junit.framework.*;
import marauroa.net.*;
import marauroa.*;
import java.io.*;

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