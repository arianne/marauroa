/* $Id: Test_the1001.java,v 1.2 2004/01/07 14:44:38 arianne_rpg Exp $ */
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
import java.util.*;
import marauroa.*;
import marauroa.game.*;

public class Test_the1001 extends TestCase
  {
  private the1001RPRuleProcessor rpu;
  private the1001RPZone zone;
  
  public static Test suite ( ) 
    {
    return new TestSuite(Test_the1001.class);
    }
   
  private RPObject[] createPlayers(int num) throws Exception
    {
    RPObject[] objects=new RPObject[num];
    for(int i=0;i<objects.length;++i)
      {
      objects[i]=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(objects[i]);
      RPObject gladiator=new Gladiator(new RPObject.ID(zone.create()));
      objects[i].getSlot("gladiators").add(gladiator);
      }
    
    return objects;
    }
  
  public void testFullGame()
    {
    marauroad.trace("Test_the1001::testFullGame","?","This test documents how the game should procced on normal conditions");
    marauroad.trace("Test_the1001::testFullGame",">");
    
    try
      {
      Random rand=new Random();
      zone=new the1001RPZone();
      rpu=new the1001RPRuleProcessor();
      rpu.setContext(zone);
    
      assertEquals(rpu.getTurn(),0);
      
      RPObject[] players=createPlayers(10);
      
      for(int i=0;i<players.length;++i)
        {
        RPAction.Status status=RPCode.RequestFight(new RPObject.ID(players[i]),new RPObject.ID(players[i].getSlot("gladiators").get()));
        assertEquals(status,RPAction.STATUS_SUCCESS);
        
        if(rand.nextBoolean())
          {
          rpu.nextTurn();
          }
        }

/*
      RPObject player=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(player);
      RPObject gladiator=new Gladiator(new RPObject.ID(zone.create()));
      player.getSlot("gladiators").add(gladiator);
      
      RPAction.Status status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);

      RPObject arena=zone.getArena();
      assertEquals(arena.get("status"),"waiting");
      assertTrue(arena.getSlot("gladiators").has(new RPObject.ID(gladiator)));
      assertEquals(player.get("status"),"onArena");
      
      RPObject newplayer=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(newplayer);
      RPObject newgladiator=new Gladiator(new RPObject.ID(zone.create()));
      newplayer.getSlot("gladiators").add(newgladiator);

      status=RPCode.RequestFight(new RPObject.ID(newplayer),new RPObject.ID(newgladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);

      assertEquals(arena.get("status"),"fighting");
      assertTrue(arena.getSlot("gladiators").has(new RPObject.ID(newgladiator)));
      assertEquals(newplayer.get("status"),"onArena");

      status=RPCode.FightMode(new RPObject.ID(player),new RPObject.ID(gladiator),"rock");
      assertEquals(status,RPAction.STATUS_SUCCESS);

      status=RPCode.FightMode(new RPObject.ID(newplayer),new RPObject.ID(newgladiator),"scissor");
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      while(newgladiator.getInt("hp")>0)
        {
        // Now it is turn to begin the fight
        rpu.nextTurn();
      
        assertFalse(gladiator.has("?damage"));
        assertTrue(newgladiator.has("?damage"));
        assertTrue(newgladiator.getInt("?damage")<newgladiator.getInt("attack"));
        }

      // We make sure that the combat has ends
      rpu.nextTurn();
      
      assertEquals(arena.get("winner"),gladiator.get("object_id"));
      
      assertFalse(gladiator.has("?damage"));
      assertFalse(newgladiator.has("?damage"));

      assertEquals(arena.get("status"),"request_fame");
      assertTrue(arena.has("fame"));      
      
      assertEquals(arena.getInt("thumbs_up"),0);

      status=RPCode.Vote(new RPObject.ID(player),"up");
      assertEquals(status,RPAction.STATUS_SUCCESS);
      assertTrue(player.has("!vote"));
      status=RPCode.Vote(new RPObject.ID(newplayer),"up");
      assertEquals(status,RPAction.STATUS_SUCCESS);
      assertTrue(newplayer.has("!vote"));
      
      status=RPCode.Vote(new RPObject.ID(player),"up");
      assertEquals(status,RPAction.STATUS_FAIL);
      
      assertEquals(arena.getInt("thumbs_up"),2);
      
      while(arena.getInt("timeout")>0)
        {
        RPCode.RequestFame();
        }

      assertTrue(arena.has("timeout"));
      assertTrue(arena.has("thumbs_up"));
      assertTrue(arena.has("thumbs_down"));
      assertTrue(arena.has("fame"));
      assertTrue(player.has("!vote"));
      assertTrue(newplayer.has("!vote"));

      RPCode.RequestFame();
      
      assertEquals(arena.get("status"),"waiting");
      
      assertFalse(arena.has("timeout"));
      assertFalse(arena.has("thumbs_up"));
      assertFalse(arena.has("thumbs_down"));
      assertFalse(arena.has("fame"));
      assertFalse(player.has("!vote"));
      assertFalse(newplayer.has("!vote"));

*/      
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    finally
      {     
      marauroad.trace("Test_the1001::testFullGame","<");
      }
    }
  }
    
    
