package marauroa.game;

import junit.framework.*;
import marauroa.*;

public class Test_MarauroaRPZone extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_MarauroaRPZone.class);
	}
	
  public void testRPZone()
    {
    marauroad.trace("Test_MarauroaRPZone::testRPZone",">");
    try
      {
      RPZone zone=new MarauroaRPZone();

      RPObject SonGoku=new RPObject();
      SonGoku.put("object_id","1");
      SonGoku.put("name","Son Goku");
    
      assertFalse(zone.has(new RPObject.ID(SonGoku)));
      zone.add(SonGoku);
      assertTrue(zone.has(new RPObject.ID(SonGoku)));
      
      RPObject result=zone.get(new RPObject.ID(SonGoku));
      assertEquals(result,SonGoku);
      }
    catch(Exception e)
      {
      }
    finally
      {
      marauroad.trace("Test_MarauroaRPZone::testRPZone","<");
      }
    }
  };

