package marauroa.game;

import junit.framework.*;
import marauroa.*;
import java.util.*;

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
      
      Iterator it=zone.iterator();
      assertTrue(it.hasNext());
      result=(RPObject)it.next();
      assertEquals(result,SonGoku);      
      assertFalse(it.hasNext());
      }
    catch(Exception e)
      {
      fail(e.toString());
      }
    finally
      {
      marauroad.trace("Test_MarauroaRPZone::testRPZone","<");
      }
    }

  public void testRPZonePerception()
    {
    marauroad.trace("Test_MarauroaRPZone::testRPZonePerception",">");
    try
      {
      RPZone zone=new MarauroaRPZone();

      RPObject SonGoku=new RPObject();
      SonGoku.put("object_id","1");
      SonGoku.put("name","Son Goku");
    
      assertFalse(zone.has(new RPObject.ID(SonGoku)));
      zone.add(SonGoku);
      assertTrue(zone.has(new RPObject.ID(SonGoku)));

	  RPZone.Perception perception=zone.getPerception(new RPObject.ID(SonGoku),RPZone.Perception.DELTA);
	  assertEquals(perception.size(),1);
	  assertEquals(SonGoku,perception.modifiedList.get(0));
	  
	  zone.nextTurn();
	  
	  perception=zone.getPerception(new RPObject.ID(SonGoku),RPZone.Perception.DELTA);
	  assertEquals(perception.size(),0);
	  
	  zone.nextTurn();

	  perception=zone.getPerception(new RPObject.ID(SonGoku),RPZone.Perception.TOTAL);
	  assertEquals(perception.size(),1);
	  assertEquals(SonGoku,perception.modifiedList.get(0));

	  zone.remove(new RPObject.ID(SonGoku));
	  
	  zone.nextTurn();

	  perception=zone.getPerception(new RPObject.ID(SonGoku),RPZone.Perception.DELTA);
	  assertEquals(perception.size(),0);
      }
    catch(Exception e)
      {
      fail(e.toString());
      }
    finally
      {
      marauroad.trace("Test_MarauroaRPZone::testRPZonePerception","<");
      }
    }
  };

