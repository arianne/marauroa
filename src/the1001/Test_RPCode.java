/* $Id: Test_RPCode.java,v 1.13 2004/01/01 23:45:01 arianne_rpg Exp $ */
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
    marauroad.trace("Test_RPCode::testRequestFight","?","This test case add Gladiators to Arena to see if the code "
      +"behaves as expected and it does the expected checks.");      
    marauroad.trace("Test_RPCode::testRequestFight",">");
    
    try
      {
      zone=new the1001RPZone();
      rpu=new the1001RPRuleProcessor();
      rpu.setContext(zone);
    
      assertEquals(rpu.getTurn(),0);
    
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
      
      /** If we try to add again it should fail */
      status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_FAIL);      
      
      RPObject newplayer=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(newplayer);
      RPObject newgladiator=new Gladiator(new RPObject.ID(zone.create()));
      newplayer.getSlot("gladiators").add(newgladiator);

      status=RPCode.RequestFight(new RPObject.ID(newplayer),new RPObject.ID(newgladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);

      assertEquals(arena.get("status"),"fighting");
      assertTrue(arena.getSlot("gladiators").has(new RPObject.ID(newgladiator)));
      assertEquals(newplayer.get("status"),"onArena");

      /** We now add another player, but the fight has already begin... */
      RPObject waitingPlayer=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(waitingPlayer);
      RPObject waitingGladiator=new Gladiator(new RPObject.ID(zone.create()));
      waitingPlayer.getSlot("gladiators").add(waitingGladiator);

      status=RPCode.RequestFight(new RPObject.ID(waitingPlayer),new RPObject.ID(waitingGladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);

      assertEquals(arena.get("status"),"fighting");
      assertFalse(arena.getSlot("gladiators").has(new RPObject.ID(waitingGladiator)));
      assertTrue(arena.has("waiting"));
      assertFalse(waitingPlayer.get("status").equals("onArena"));
      assertEquals(waitingPlayer.getInt("requested"),rpu.getTurn());
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
  
  public void testFightMode()
    {
    marauroad.trace("Test_RPCode::testFightMode",">");
    try
      {
      zone=new the1001RPZone();
      rpu=new the1001RPRuleProcessor();
      rpu.setContext(zone);
    
      assertEquals(rpu.getTurn(),0);
    
      RPObject player=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(player);
      RPObject gladiator=new Gladiator(new RPObject.ID(zone.create()));
      player.getSlot("gladiators").add(gladiator);
      
      RPAction.Status status=RPCode.FightMode(new RPObject.ID(player),new RPObject.ID(gladiator),"rock");
      assertEquals(status,RPAction.STATUS_FAIL);
      
      status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      RPObject arena=zone.getArena();
      assertEquals(arena.get("status"),"waiting");
      assertTrue(arena.getSlot("gladiators").has(new RPObject.ID(gladiator)));
      assertEquals(player.get("status"),"onArena");
      
      status=RPCode.FightMode(new RPObject.ID(player),new RPObject.ID(gladiator),"rock");
      assertEquals(status,RPAction.STATUS_SUCCESS);
      assertEquals(gladiator.get("!mode"),"rock");
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    finally
      {     
      marauroad.trace("Test_RPCode::testFightMode","<");
      }
    }
    
  public void testResolveFight()
    {
    marauroad.trace("Test_RPCode::testResolveFight",">");
    try
      {
      zone=new the1001RPZone();
      rpu=new the1001RPRuleProcessor();
      rpu.setContext(zone);
    
      assertEquals(rpu.getTurn(),0);
    
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
        /** Now it is turn to begin the fight */
        rpu.nextTurn();
        //RPCode.ResolveFight();
      
        assertFalse(gladiator.has("?damage"));
        assertTrue(newgladiator.has("?damage"));
        assertTrue(newgladiator.getInt("?damage")<newgladiator.getInt("attack"));
        }

      /** Now it is turn to begin the fight */
      rpu.nextTurn();
      
      assertFalse(gladiator.has("?damage"));
      assertFalse(newgladiator.has("?damage"));

//      status=RPCode.FightMode(new RPObject.ID(newplayer),new RPObject.ID(newgladiator),"paper");
//      assertEquals(status,RPAction.STATUS_SUCCESS);
//
//      RPCode.ResolveFight();
//      
//      assertFalse(gladiator.has("?damage"));
//      assertTrue(newgladiator.has("?damage"));
//      assertTrue(newgladiator.getInt("?damage")<newgladiator.getInt("attack"));
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    finally
      {     
      marauroad.trace("Test_RPCode::testResolveFight","<");
      }
    }
  }

