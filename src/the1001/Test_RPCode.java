/* $Id: Test_RPCode.java,v 1.25 2004/03/05 13:39:21 arianne_rpg Exp $ */
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
      player.getSlot(RPCode.var_gladiators).add(gladiator);
      
      RPAction.Status status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      RPObject arena=zone.getArena();
      assertEquals(arena.get(RPCode.var_status),RPCode.var_waiting);
      assertTrue(arena.getSlot(RPCode.var_gladiators).has(new RPObject.ID(gladiator)));
      assertTrue(player.has(RPCode.var_fighting));
      
      /** If we try to add again it should fail */
      status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_FAIL);      
      
      RPObject newplayer=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(newplayer);
      RPObject newgladiator=new Gladiator(new RPObject.ID(zone.create()));
      newplayer.getSlot(RPCode.var_gladiators).add(newgladiator);

      status=RPCode.RequestFight(new RPObject.ID(newplayer),new RPObject.ID(newgladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);

      assertEquals(arena.get(RPCode.var_status),RPCode.var_fighting);
      assertTrue(arena.getSlot(RPCode.var_gladiators).has(new RPObject.ID(newgladiator)));
      assertTrue(newplayer.has(RPCode.var_fighting));

      /** We now add another player, but the fight has already begin... */
      RPObject waitingPlayer=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(waitingPlayer);
      RPObject waitingGladiator=new Gladiator(new RPObject.ID(zone.create()));
      waitingPlayer.getSlot(RPCode.var_gladiators).add(waitingGladiator);

      status=RPCode.RequestFight(new RPObject.ID(waitingPlayer),new RPObject.ID(waitingGladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);

      assertEquals(arena.get(RPCode.var_status),RPCode.var_fighting);
      assertFalse(arena.getSlot(RPCode.var_gladiators).has(new RPObject.ID(waitingGladiator)));
      assertTrue(arena.has(RPCode.var_waiting));
      assertFalse(waitingPlayer.has(RPCode.var_fighting));
      assertEquals(waitingPlayer.getInt(RPCode.var_requested),rpu.getTurn());
      }
    catch(Exception e)
      {
      e.printStackTrace(System.out);
      fail(e.getMessage());
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
      player.getSlot(RPCode.var_gladiators).add(gladiator);
      
      RPAction.Status status=RPCode.FightMode(new RPObject.ID(player),new RPObject.ID(gladiator),RPCode.var_rock);
      assertEquals(status,RPAction.STATUS_FAIL);
      
      status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      RPObject arena=zone.getArena();
      assertEquals(arena.get(RPCode.var_status),RPCode.var_waiting);
      assertTrue(arena.getSlot(RPCode.var_gladiators).has(new RPObject.ID(gladiator)));
      assertTrue(player.has(RPCode.var_fighting));
      
      status=RPCode.FightMode(new RPObject.ID(player),new RPObject.ID(gladiator),RPCode.var_rock);
      assertEquals(status,RPAction.STATUS_SUCCESS);
      assertEquals(gladiator.get(RPCode.var_hidden_combat_mode),RPCode.var_rock);
      }
    catch(Exception e)
      {
      e.printStackTrace(System.out);
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
      player.getSlot(RPCode.var_gladiators).add(gladiator);
      
      RPAction.Status status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      RPObject arena=zone.getArena();
      assertEquals(arena.get(RPCode.var_status),RPCode.var_waiting);
      assertTrue(arena.getSlot(RPCode.var_gladiators).has(new RPObject.ID(gladiator)));
      assertTrue(player.has(RPCode.var_fighting));
      
      RPObject newplayer=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(newplayer);
      RPObject newgladiator=new Gladiator(new RPObject.ID(zone.create()));
      newplayer.getSlot(RPCode.var_gladiators).add(newgladiator);

      status=RPCode.RequestFight(new RPObject.ID(newplayer),new RPObject.ID(newgladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);

      assertEquals(arena.get(RPCode.var_status),RPCode.var_fighting);
      assertTrue(arena.getSlot(RPCode.var_gladiators).has(new RPObject.ID(newgladiator)));
      assertTrue(newplayer.has(RPCode.var_fighting));

      status=RPCode.FightMode(new RPObject.ID(player),new RPObject.ID(gladiator),RPCode.var_rock);
      assertEquals(status,RPAction.STATUS_SUCCESS);

      status=RPCode.FightMode(new RPObject.ID(newplayer),new RPObject.ID(newgladiator),RPCode.var_scissor);
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      while(newgladiator.getInt(RPCode.var_hp)>0)
        {
        /** Now it is turn to begin the fight */
        rpu.nextTurn();
        //RPCode.ResolveFight();
      
        assertFalse(gladiator.has(RPCode.var_damage));
        assertTrue(newgladiator.has(RPCode.var_damage));
        assertTrue(newgladiator.getInt(RPCode.var_damage)<=newgladiator.getInt(RPCode.var_attack));
        }

      /** We make sure that the combat has ends */
      rpu.nextTurn();
      
      assertFalse(gladiator.has(RPCode.var_damage));
      assertFalse(newgladiator.has(RPCode.var_damage));

      assertEquals(arena.get(RPCode.var_status),RPCode.var_request_fame);
      assertTrue(arena.has(RPCode.var_karma));      
      }
    catch(Exception e)
      {
      e.printStackTrace(System.out);
      fail(e.getMessage());
      }
    finally
      {     
      marauroad.trace("Test_RPCode::testResolveFight","<");
      }
    }
    
  public void testRequestFame()
    {
    marauroad.trace("Test_RPCode::testRequestFame",">");
    try
      {
      zone=new the1001RPZone();
      rpu=new the1001RPRuleProcessor();
      rpu.setContext(zone);
    
      assertEquals(rpu.getTurn(),0);
    
      RPObject player=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(player);
      RPObject gladiator=new Gladiator(new RPObject.ID(zone.create()));
      player.getSlot(RPCode.var_gladiators).add(gladiator);
      
      RPAction.Status status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      RPObject arena=zone.getArena();
      assertEquals(arena.get(RPCode.var_status),RPCode.var_waiting);
      assertTrue(arena.getSlot(RPCode.var_gladiators).has(new RPObject.ID(gladiator)));
      assertTrue(player.has(RPCode.var_fighting));
      
      RPObject newplayer=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(newplayer);
      RPObject newgladiator=new Gladiator(new RPObject.ID(zone.create()));
      newplayer.getSlot(RPCode.var_gladiators).add(newgladiator);

      status=RPCode.RequestFight(new RPObject.ID(newplayer),new RPObject.ID(newgladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);

      assertEquals(arena.get(RPCode.var_status),RPCode.var_fighting);
      assertTrue(arena.getSlot(RPCode.var_gladiators).has(new RPObject.ID(newgladiator)));
      assertTrue(newplayer.has(RPCode.var_fighting));

      status=RPCode.FightMode(new RPObject.ID(player),new RPObject.ID(gladiator),RPCode.var_rock);
      assertEquals(status,RPAction.STATUS_SUCCESS);

      status=RPCode.FightMode(new RPObject.ID(newplayer),new RPObject.ID(newgladiator),RPCode.var_scissor);
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      while(newgladiator.getInt(RPCode.var_hp)>0)
        {
        /** Now it is turn to begin the fight */
        rpu.nextTurn();
        //RPCode.ResolveFight();
      
        assertFalse(gladiator.has(RPCode.var_damage));
        assertTrue(newgladiator.has(RPCode.var_damage));
        assertTrue(newgladiator.getInt(RPCode.var_damage)<=newgladiator.getInt(RPCode.var_attack));
        }

      /** We make sure that the combat has ends */
      rpu.nextTurn();
      
      assertEquals(arena.get(RPCode.var_winner),gladiator.get(RPCode.var_object_id));
      
      assertFalse(gladiator.has(RPCode.var_damage));
      assertFalse(newgladiator.has(RPCode.var_damage));

      assertEquals(arena.get(RPCode.var_status),RPCode.var_request_fame);
      assertTrue(arena.has(RPCode.var_karma));      
      
      assertEquals(arena.getInt(RPCode.var_thumbs_up),0);

      status=RPCode.Vote(new RPObject.ID(player),RPCode.var_voted_up);
      assertEquals(status,RPAction.STATUS_SUCCESS);
      assertTrue(player.has(RPCode.var_hidden_vote));
      status=RPCode.Vote(new RPObject.ID(newplayer),RPCode.var_voted_up);
      assertEquals(status,RPAction.STATUS_SUCCESS);
      assertTrue(newplayer.has(RPCode.var_hidden_vote));
      
      status=RPCode.Vote(new RPObject.ID(player),RPCode.var_voted_up);
      assertEquals(status,RPAction.STATUS_FAIL);
      
      assertEquals(arena.getInt(RPCode.var_thumbs_up),2);
      
      while(arena.getInt(RPCode.var_timeout)>0)
        {
        RPCode.RequestFame();
        }

      assertTrue(arena.has(RPCode.var_timeout));
      assertTrue(arena.has(RPCode.var_thumbs_up));
      assertTrue(arena.has(RPCode.var_thumbs_down));
      assertTrue(arena.has(RPCode.var_karma));
      assertTrue(player.has(RPCode.var_hidden_vote));
      assertTrue(newplayer.has(RPCode.var_hidden_vote));

      RPCode.RequestFame();
      
      assertEquals(arena.get(RPCode.var_status),RPCode.var_waiting);
      
      assertFalse(arena.has(RPCode.var_timeout));
      assertFalse(arena.has(RPCode.var_thumbs_up));
      assertFalse(arena.has(RPCode.var_thumbs_down));
      assertFalse(arena.has(RPCode.var_fame));
      assertFalse(player.has(RPCode.var_hidden_vote));
      assertFalse(newplayer.has(RPCode.var_hidden_vote));
      }
    catch(Exception e)
      {
      e.printStackTrace(System.out);
      fail(e.getMessage());
      }
    finally
      {     
      marauroad.trace("Test_RPCode::testRequestFame","<");
      }
    }
  }

