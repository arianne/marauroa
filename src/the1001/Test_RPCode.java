/* $Id: Test_RPCode.java,v 1.21 2004/01/08 14:14:47 arianne_rpg Exp $ */
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
      assertTrue(player.has("fighting"));
      
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
      assertTrue(newplayer.has("fighting"));

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
      assertFalse(waitingPlayer.has("fighting"));
      assertEquals(waitingPlayer.getInt("requested"),rpu.getTurn());
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
      player.getSlot("gladiators").add(gladiator);
      
      RPAction.Status status=RPCode.FightMode(new RPObject.ID(player),new RPObject.ID(gladiator),"rock");
      assertEquals(status,RPAction.STATUS_FAIL);
      
      status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      RPObject arena=zone.getArena();
      assertEquals(arena.get("status"),"waiting");
      assertTrue(arena.getSlot("gladiators").has(new RPObject.ID(gladiator)));
      assertTrue(player.has("fighting"));
      
      status=RPCode.FightMode(new RPObject.ID(player),new RPObject.ID(gladiator),"rock");
      assertEquals(status,RPAction.STATUS_SUCCESS);
      assertEquals(gladiator.get("!mode"),"rock");
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
      player.getSlot("gladiators").add(gladiator);
      
      RPAction.Status status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      RPObject arena=zone.getArena();
      assertEquals(arena.get("status"),"waiting");
      assertTrue(arena.getSlot("gladiators").has(new RPObject.ID(gladiator)));
      assertTrue(player.has("fighting"));
      
      RPObject newplayer=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(newplayer);
      RPObject newgladiator=new Gladiator(new RPObject.ID(zone.create()));
      newplayer.getSlot("gladiators").add(newgladiator);

      status=RPCode.RequestFight(new RPObject.ID(newplayer),new RPObject.ID(newgladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);

      assertEquals(arena.get("status"),"fighting");
      assertTrue(arena.getSlot("gladiators").has(new RPObject.ID(newgladiator)));
      assertTrue(newplayer.has("fighting"));

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
        assertTrue(newgladiator.getInt("?damage")<=newgladiator.getInt("attack"));
        }

      /** We make sure that the combat has ends */
      rpu.nextTurn();
      
      assertFalse(gladiator.has("?damage"));
      assertFalse(newgladiator.has("?damage"));

      assertEquals(arena.get("status"),"request_fame");
      assertTrue(arena.has("fame"));      
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
      player.getSlot("gladiators").add(gladiator);
      
      RPAction.Status status=RPCode.RequestFight(new RPObject.ID(player),new RPObject.ID(gladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);
      
      RPObject arena=zone.getArena();
      assertEquals(arena.get("status"),"waiting");
      assertTrue(arena.getSlot("gladiators").has(new RPObject.ID(gladiator)));
      assertTrue(player.has("fighting"));
      
      RPObject newplayer=new Player(new RPObject.ID(zone.create()),"a name");
      zone.add(newplayer);
      RPObject newgladiator=new Gladiator(new RPObject.ID(zone.create()));
      newplayer.getSlot("gladiators").add(newgladiator);

      status=RPCode.RequestFight(new RPObject.ID(newplayer),new RPObject.ID(newgladiator));
      assertEquals(status,RPAction.STATUS_SUCCESS);

      assertEquals(arena.get("status"),"fighting");
      assertTrue(arena.getSlot("gladiators").has(new RPObject.ID(newgladiator)));
      assertTrue(newplayer.has("fighting"));

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
        assertTrue(newgladiator.getInt("?damage")<=newgladiator.getInt("attack"));
        }

      /** We make sure that the combat has ends */
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

