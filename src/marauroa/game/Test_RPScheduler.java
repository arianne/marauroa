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
	
  private static class FakeRuleProcessor implements RPRuleProcessor
    {
    int i;
    
    public FakeRuleProcessor()
      {
      i=0;
      }
    
    public void setContext(RPZone zone) 
      {
      }
      
    public void approvedActions(RPActionList actionList)
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
    }
	
  public void testRPScheduler()
    {
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
    marauroad.trace("Test_RPScheduler::testRPScheduler",">");
    
    try
      {
      RPScheduler sched=new RPScheduler();
    
      RPAction action=new RPAction();
      action.put("type","testing");
    
      sched.addRPAction(action);
      fail("Should drop exception");
      }
    catch(RPScheduler.ActionInvalidException e)
      {      
      assertTrue(true);
      }
    catch(Exception e)
      {
      fail(e.getMessage());
      }
  finally
      {
      marauroad.trace("Test_RPScheduler::testRPScheduler","<");
      }
    }  
  }
