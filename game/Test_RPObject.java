package marauroa.game;

import junit.framework.*;
import marauroa.net.*;
import java.io.*;

public class Test_RPObject extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_RPObject.class);
	}
	
  public void testRPObject()
    {
    try
      {
      RPObject SonGoku=new RPObject();
      SonGoku.put("object_id","1");
      SonGoku.put("name","Son Goku");
    
      assertTrue(SonGoku.has("object_id"));
      String id_string=SonGoku.get("object_id");
      assertEquals(id_string,"1");

      assertTrue(SonGoku.has("name"));
      String name_string=SonGoku.get("name");
      assertEquals(name_string,"Son Goku");
      
      SonGoku.remove("name");
      assertFalse(SonGoku.has("name"));
      
      RPObject.ID id=new RPObject.ID(SonGoku);
      RPObject.ID id_1=new RPObject.ID(1);
      assertEquals(id,id_1);
      
      /* TODO: Test slots and serialization */
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      fail(e.getMessage());
      }
    }
  }