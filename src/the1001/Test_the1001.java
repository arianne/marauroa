/* $Id: Test_the1001.java,v 1.6 2004/01/07 23:59:21 arianne_rpg Exp $ */
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

      RPObject arena=zone.getArena();    
      RPObject[] players=createPlayers(NUM_PLAYERS);

      assertEquals(arena.get("status"),"waiting");
      
      for(int i=0;i<players.length;++i)
        {
        RPAction.Status status=RPCode.RequestFight(new RPObject.ID(players[i]),new RPObject.ID(players[i].getSlot("gladiators").get()));
        assertEquals(status,RPAction.STATUS_SUCCESS);
        
        if(rand.nextBoolean())
          {
          rpu.nextTurn();
          }
          
        if(i==0)
          {
          assertEquals(arena.get("status"),"waiting");
          assertTrue(arena.getSlot("gladiators").has(new RPObject.ID(players[i].getSlot("gladiators").get())));
          assertTrue(players[i].has("fighting"));      
          assertFalse(players[i].has("requested"));      
          }
        else if(i==1)
          {
          assertEquals(arena.get("status"),"fighting");
          assertTrue(arena.getSlot("gladiators").has(new RPObject.ID(players[i].getSlot("gladiators").get())));
          assertTrue(players[i].has("fighting"));      
          assertFalse(players[i].has("requested"));      
          }
        else
          {          
          assertEquals(arena.get("status"),"fighting");
          assertFalse(arena.getSlot("gladiators").has(new RPObject.ID(players[i].getSlot("gladiators").get())));
          assertFalse(players[i].has("fighting"));    
          assertTrue(players[i].has("requested"));      
          }
        }
      
      int combatRound=0;
      while(arena.get("status").equals("fighting"))
        {
        ++combatRound;
        
//        if(combatRound<10 && rand.nextBoolean())
//          {
//          int j=rand.nextInt(NUM_PLAYERS);
////          if(!(players[j].has("requested") || players[j].has("fighting")))
//            {
//            marauroad.trace("Test_the1001::testFullGame","D","A gladiator request to fight again");          
//            RPCode.RequestFight(new RPObject.ID(players[j]),new RPObject.ID(players[j].getSlot("gladiators").get()));
//            }
//          }
          
        marauroad.trace("Test_the1001::testFullGame","D","Combat begin: "+combatRound);

        while(arena.get("status").equals("fighting"))
          {
          for(int i=0;i<players.length;++i)
            {
            String[] options={"rock","paper","scissor"};
                     
            RPAction.Status status=RPCode.FightMode(new RPObject.ID(players[i]),new RPObject.ID(players[i].getSlot("gladiators").get()),options[rand.nextInt(3)]);
            if(players[i].has("fighting"))
              {
              assertEquals(status,RPAction.STATUS_SUCCESS);
              }
            else
              {
              assertEquals(status,RPAction.STATUS_FAIL);
              }
            }
    
          rpu.nextTurn();
          }
    
        rpu.nextTurn();
          
        zone.print(System.out);
    
        for(int i=0;i<players.length;++i)
          {
          assertFalse(players[i].has("?damage"));
          }
    
        assertEquals(arena.get("status"),"request_fame");
        assertTrue(arena.has("fame"));      
        
        assertEquals(arena.getInt("thumbs_up"),0);
        assertEquals(arena.getInt("thumbs_down"),0);
          
        for(int i=0;i<players.length;++i)
          {
          String[] options={"up","down"};
          RPAction.Status status=RPCode.Vote(new RPObject.ID(players[i]),"up");
          assertEquals(status,RPAction.STATUS_SUCCESS);
          
          if(rand.nextBoolean())
            {
            rpu.nextTurn();
            }
          }
            
        while(arena.getInt("timeout")>0)
          {
          rpu.nextTurn();
          }            
    
        assertTrue(arena.has("timeout"));
        assertTrue(arena.has("thumbs_up"));
        assertTrue(arena.has("thumbs_down"));
        assertTrue(arena.has("fame"));
    
        rpu.nextTurn();
    
        assertFalse(arena.has("timeout"));
        assertFalse(arena.has("thumbs_up"));
        assertFalse(arena.has("thumbs_down"));
        assertFalse(arena.has("fame"));
    
        for(int i=0;i<players.length;++i)
          {
          assertFalse(players[i].has("!vote"));
          }

        marauroad.trace("Test_the1001::testFullGame","D","Combat end: "+combatRound);
        System.out.println(arena.get("status"));
        }
      
      System.out.println("Turns done: "+rpu.getTurn());
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
    
    
