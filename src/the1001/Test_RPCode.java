/* $Id: Test_RPCode.java,v 1.3 2003/12/30 18:17:41 arianne_rpg Exp $ */
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
package the1001;

import the1001.objects.*;
import junit.framework.*;
import marauroa.*;
import marauroa.game.*;

public class Test_RPCode extends TestCase
  {
  private the1001RPRuleProcessor rpu;
  private the1001RPZone zone;
  
  public static Test suite ( ) 
    {
    return new TestSuite(Test_RPCode.class);
    }
    
  public void testRequestFight()
    {
    marauroad.trace("Test_RPCode::testRequestFight","?"/*TODO*/);      
    marauroad.trace("Test_RPCode::testRequestFight",">");
    
    try
      {
      zone=new the1001RPZone();
      rpu=new the1001RPRuleProcessor();
      rpu.setContext(zone);
    
      assertEquals(rpu.getTurn(),0);
    
      RPObject player=new Player(new RPObject.ID(zone.create()));
      zone.add(player);
      RPObject gladiator=new Gladiator(new RPObject.ID(zone.create()));
      player.getSlot("gladiators").add(gladiator);
      
      RPAction.Status status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      
      }
    catch(Exception e)
      {
      fail();
      }
    finally
      {     
      marauroad.trace("Test_RPCode::testRequestFight","<");
      }
    }
  }

