package marauroa.game;

import junit.framework.*;
import marauroa.net.*;
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
    catch(Attributes.AttributeNotFoundException e)
      {
      fail(e.getMessage());
      }
    }

  public void testAttributesException()
    {
    try
      {
      RPAction attr=new RPAction();
      assertNotNull(attr);
    
      String value=null;
      value=attr.get("Attribute");
      fail("Exception not throwed");
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      assertTrue(true);
      }
    }

  public void testAttributesSerialization()
    {
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
    }


  public void testAttributesStatus()
    {
    RPAction.Status status=new RPAction.Status(RPAction.STATUS_FAIL.getStatus());
    assertTrue(status.equals(RPAction.STATUS_FAIL));
    assertFalse(status.equals(RPAction.STATUS_INCOMPLETE));
    assertFalse(status.equals(RPAction.STATUS_SUCCESS));
    
    assertEquals(RPAction.STATUS_FAIL,new RPAction.Status("fail"));
    assertEquals(RPAction.STATUS_INCOMPLETE,new RPAction.Status("incomplete"));
    assertEquals(RPAction.STATUS_SUCCESS,new RPAction.Status("success"));   
    }
  }
