/* $Id: Test_MarauroaRPZone.java,v 1.14 2004/11/28 20:35:29 arianne_rpg Exp $ */
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
      IRPZone zone=new MarauroaRPZone();
      RPObject SonGoku=new RPObject();

      SonGoku.put("id","1");
      SonGoku.put("zoneid","somewhere");
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
      IRPZone zone=new MarauroaRPZone();
      RPObject SonGoku=new RPObject();

      SonGoku.put("id","1");
      SonGoku.put("zoneid","somewhere");
      SonGoku.put("name","Son Goku");
      assertFalse(zone.has(new RPObject.ID(SonGoku)));
      zone.add(SonGoku);
      assertTrue(zone.has(new RPObject.ID(SonGoku)));

      Perception perception=zone.getPerception(new RPObject.ID(SonGoku),Perception.DELTA);

      assertEquals(perception.size(),1);
      assertEquals(SonGoku,perception.addedList.get(0));
      zone.nextTurn();
      
      perception=zone.getPerception(new RPObject.ID(SonGoku),Perception.DELTA);
      assertEquals(perception.size(),0);
      zone.nextTurn();
      
      perception=zone.getPerception(new RPObject.ID(SonGoku),Perception.DELTA);
      assertEquals(perception.size(),0);
      zone.nextTurn();
      
      zone.get(new RPObject.ID(SonGoku));
      SonGoku.put("name","A new SonGoku");
      zone.modify(SonGoku);
      perception=zone.getPerception(new RPObject.ID(SonGoku),Perception.DELTA);
      assertEquals(perception.size(),0);
      zone.nextTurn();
      
      perception=zone.getPerception(new RPObject.ID(SonGoku),Perception.SYNC);
      assertEquals(perception.size(),1);
      assertEquals(SonGoku,perception.addedList.get(0));
      zone.remove(new RPObject.ID(SonGoku));
      zone.nextTurn();
      
      perception=zone.getPerception(new RPObject.ID(SonGoku),Perception.DELTA);
      assertEquals(perception.size(),0);
      }
    catch(Exception e)
      {
      e.printStackTrace();
      fail(e.toString());
      }
    finally
      {
      marauroad.trace("Test_MarauroaRPZone::testRPZonePerception","<");
      }
    }
  }


;
