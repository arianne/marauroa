package marauroa.game;

import junit.framework.*;
import marauroa.net.*;
import java.io.*;

public class Test_RPZone extends TestCase
{
  public static Test suite ( )
  {
    return new TestSuite(Test_RPZone.class);
  }
  
  public void testRPZone()
  {
    RPObject SonGoku=new RPObject();
    SonGoku.put("object_id","1");
    SonGoku.put("name","Son Goku");
    
    RPZone zone=new MarauroaRPZone();
    assertNotNull(zone);
    
    try
    {
      zone.add(SonGoku);
      RPObject.ID id=new RPObject.ID(SonGoku);
      assertTrue(zone.has(id));
      
      RPObject object=zone.get(id);
      assertEquals(object,SonGoku);
      
      zone.remove(id);
      assertFalse(zone.has(id));
    }
    catch(RPZone.RPObjectInvalidException e)
    {
      fail("RPObject is not valid");
    }
    catch(RPZone.RPObjectNotFoundException e)
    {
      fail("RPObject doesn't exist");
    }
    catch(Attributes.AttributeNotFoundException e)
    {
      fail("Can't find the attribute we are looking for");
    }
  }
}
