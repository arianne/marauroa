/* $Id: Test_the1001.java,v 1.14 2004/08/29 11:07:43 arianne_rpg Exp $ */
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
  private final static int NUM_PLAYERS=10;
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

      /** NOTE: The rest of the test expect one gladiator per player */
      RPObject gladiator=new Gladiator(new RPObject.ID(zone.create()));

      objects[i].getSlot(RPCode.var_myGladiators).add(gladiator);
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
      rpu.setContext(null,zone);
      assertEquals(rpu.getTurn(),0);

      RPObject arena=zone.getArena();    
      RPObject[] players=createPlayers(NUM_PLAYERS);

      assertEquals(arena.get(RPCode.var_status),RPCode.var_waiting);
      for(int i=0;i<players.length;++i)
        {
        RPAction.Status status=RPCode.RequestFight(new RPObject.ID(players[i]),new RPObject.ID(players[i].getSlot(RPCode.var_myGladiators).get()));

        assertEquals(status,RPAction.STATUS_SUCCESS);
        if(rand.nextBoolean())
          {
          rpu.nextTurn();
          }
        if(i==0)
          {
          assertEquals(arena.get(RPCode.var_status),RPCode.var_waiting);
          assertTrue(arena.getSlot(RPCode.var_gladiators).has(new RPObject.ID(players[i].getSlot(RPCode.var_myGladiators).get())));
          assertTrue(players[i].has(RPCode.var_fighting));      
          assertFalse(players[i].has(RPCode.var_requested));      
          }
        else if(i==1)
          {
          assertEquals(arena.get(RPCode.var_status),RPCode.var_fighting);
          assertTrue(arena.getSlot(RPCode.var_gladiators).has(new RPObject.ID(players[i].getSlot(RPCode.var_myGladiators).get())));
          assertTrue(players[i].has(RPCode.var_fighting));      
          assertFalse(players[i].has(RPCode.var_requested));      
          }
        else
          {          
          assertEquals(arena.get(RPCode.var_status),RPCode.var_fighting);
          assertFalse(arena.getSlot(RPCode.var_gladiators).has(new RPObject.ID(players[i].getSlot(RPCode.var_myGladiators).get())));
          assertFalse(players[i].has(RPCode.var_fighting));    
          assertTrue(players[i].has(RPCode.var_requested));      
          }
        }
      
      int combatRound=0;

      while(arena.get(RPCode.var_status).equals(RPCode.var_fighting))
        {
        ++combatRound;
        if(combatRound>3 && combatRound<100)
          {
          RPAction.Status status=RPAction.STATUS_FAIL;

          while(status.equals(RPAction.STATUS_FAIL))
            {
            int j=rand.nextInt(NUM_PLAYERS);

            marauroad.trace("Test_the1001::testFullGame","D","A gladiator("+j+") request to fight again");          
            status=RPCode.RequestFight(new RPObject.ID(players[j]),new RPObject.ID(players[j].getSlot(RPCode.var_myGladiators).get()));
            marauroad.trace("Test_the1001::testFullGame","D","request: "+status.toString());          
            }
          status=RPAction.STATUS_FAIL;
          while(status.equals(RPAction.STATUS_FAIL))
            {
            int j=rand.nextInt(NUM_PLAYERS);

            marauroad.trace("Test_the1001::testFullGame","D","A gladiator("+j+") request to fight again");          
            status=RPCode.RequestFight(new RPObject.ID(players[j]),new RPObject.ID(players[j].getSlot(RPCode.var_myGladiators).get()));
            marauroad.trace("Test_the1001::testFullGame","D","request: "+status.toString());          
            }
          }
        marauroad.trace("Test_the1001::testFullGame","D","Combat begin: "+combatRound);
        while(arena.get(RPCode.var_status).equals(RPCode.var_fighting))
          {
          for(int i=0;i<players.length;++i)
            {
            if(players[i].has(RPCode.var_fighting))
              {
              String[] options={RPCode.var_rock,RPCode.var_paper,RPCode.var_scissor};                      
              RPAction.Status status=RPCode.FightMode(new RPObject.ID(players[i]),new RPObject.ID(players[i].getSlot(RPCode.var_myGladiators).get()),options[rand.nextInt(3)]);

              assertEquals(status,RPAction.STATUS_SUCCESS);
              }
            }
          rpu.nextTurn();
          }
        rpu.nextTurn();
        zone.print(System.out);
        for(int i=0;i<players.length;++i)
          {
          assertFalse(players[i].has(RPCode.var_damage));
          }
        assertEquals(arena.get(RPCode.var_status),RPCode.var_request_fame);
        assertTrue(arena.has(RPCode.var_karma));      
        assertEquals(arena.getInt(RPCode.var_thumbs_up),0);
        assertEquals(arena.getInt(RPCode.var_thumbs_down),0);
        for(int i=0;i<players.length;++i)
          {
          String[] options={RPCode.var_voted_up,"down"};
          RPAction.Status status=RPCode.Vote(new RPObject.ID(players[i]),RPCode.var_voted_up);

          assertEquals(status,RPAction.STATUS_SUCCESS);
          if(rand.nextBoolean())
            {
            rpu.nextTurn();
            }
          }
        while(arena.getInt(RPCode.var_timeout)>0)
          {
          rpu.nextTurn();
          }            
        assertTrue(arena.has(RPCode.var_timeout));
        assertTrue(arena.has(RPCode.var_thumbs_up));
        assertTrue(arena.has(RPCode.var_thumbs_down));
        assertTrue(arena.has(RPCode.var_karma));
        rpu.nextTurn();
        assertFalse(arena.has(RPCode.var_timeout));
        assertFalse(arena.has(RPCode.var_thumbs_up));
        assertFalse(arena.has(RPCode.var_thumbs_down));
        assertFalse(arena.has(RPCode.var_fame));
        for(int i=0;i<players.length;++i)
          {
          assertFalse(players[i].has(RPCode.var_hidden_vote));
          }
        marauroad.trace("Test_the1001::testFullGame","D","Combat end: "+combatRound);
        }
      System.out.println("Turns done: "+rpu.getTurn());
      }
    catch(Exception e)
      {
      e.printStackTrace(System.out);
      fail(e.getMessage());
      }
    finally
      {     
      marauroad.trace("Test_the1001::testFullGame","<");
      }
    }
  }
