/* $Id: Test_RPScheduler.java,v 1.17 2004/07/13 20:31:53 arianne_rpg Exp $ */
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
import marauroa.game.*;
import marauroa.*;
import java.io.*;

public class Test_RPScheduler extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_RPScheduler.class);
    }
    
  private static class FakeRuleProcessor implements IRPRuleProcessor
    {
    int i;
    public FakeRuleProcessor()
      {
      i=0;
      }
    
    public void setContext(IRPZone zone) 
      {
      }
      
    public void approvedActions(RPObject.ID id, RPActionList actionList)
      {
      }
    
    public int getActionsExecuted()
      {
      return i;
      }   
      
    public RPAction.Status execute(RPObject.ID id, RPAction action)
      {
      i++;
      return RPAction.STATUS_SUCCESS;
      }
    
    public void nextTurn()
      {
      }

    public boolean onInit(RPObject object)
      {      
      return true;
      }
    
    public boolean onExit(RPObject.ID id)
      {
      return true;
      }
    
    public boolean onTimeout(RPObject.ID id)
      {
      return true;
      }

    public java.util.List buildMapObjectsList(RPObject.ID id)
      {
      return null;
      }      
    }
  public void testRPScheduler()
    {
    marauroad.trace("Test_RPScheduler::testRPScheduler","?","This test case show how the scheduler operates");
    marauroad.trace("Test_RPScheduler::testRPScheduler",">");
    try
      {
      RPScheduler sched=new RPScheduler();
      RPAction action=new RPAction();

      action.put("source_id","10");
      action.put("type","testing");
      sched.addRPAction(action);
      
      FakeRuleProcessor fake=new FakeRuleProcessor();

      sched.visit(fake);      
      assertEquals(0,fake.getActionsExecuted());      
      sched.nextTurn();
      fake=new FakeRuleProcessor();
      sched.visit(fake);      
      assertEquals(1,fake.getActionsExecuted());      
      sched.nextTurn();
      fake=new FakeRuleProcessor();
      sched.visit(fake);      
      assertEquals(0,fake.getActionsExecuted());
      }
    catch(Exception e)
      {
      System.out.println(e.getMessage());
      fail(e.getMessage());
      }
    finally
      {
      marauroad.trace("Test_RPScheduler::testRPScheduler","<");
      }
    }

  public void testRPSchedulerExceptions()
    {
    marauroad.trace("Test_RPScheduler::testRPSchedulerExceptions","?","This test case tests that when operated badly RPScheduler throws exceptions");
    marauroad.trace("Test_RPScheduler::testRPSchedulerExceptions",">");
    try
      {
      RPScheduler sched=new RPScheduler();
      RPAction action=new RPAction();

      action.put("type","testing");
      sched.addRPAction(action);
      fail("Should drop exception");
      }
    catch(ActionInvalidException e)
      {      
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
    finally
      {
      marauroad.trace("Test_RPScheduler::testRPSchedulerExceptions","<");
      }
    }  
  }
