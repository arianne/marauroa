/* $Id: SimpleRPRuleProcessor.java,v 1.25 2003/12/12 07:48:50 root777 Exp $ */
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
package simplegame;

import marauroa.game.*;

import java.util.Iterator;
import marauroa.marauroad;
import simplegame.actions.ChallengeAction;
import simplegame.actions.ChallengeAnswer;
import simplegame.actions.GetCharacterListAction;
import simplegame.actions.MoveAction;
import simplegame.objects.CharacterList;
import simplegame.objects.GameBoard;

public class SimpleRPRuleProcessor implements RPRuleProcessor
{
  private SimpleRPZone zone;
  
  public SimpleRPRuleProcessor()
  {
		marauroad.trace("SimpleRPRuleProcessor::<init>",">");
		marauroad.trace("SimpleRPRuleProcessor::<init>","<");
  }
  
  
  
  public void setContext(RPZone zone)
  {
		marauroad.trace("SimpleRPRuleProcessor::setContext",">");
		try
		{
			this.zone = (SimpleRPZone)zone;
		}
		catch(ClassCastException cce)
		{
			marauroad.trace("SimpleRPRuleProcessor::setContext","!","Wrong class for RPZone, exiting.");
			System.exit(-1);
		}
		marauroad.trace("SimpleRPRuleProcessor::setContext","<");
  }
  
  public void approvedActions(RPObject.ID id, RPActionList actionList)
  {
		while(actionList.size()>1)
		{
			actionList.remove(0);
		}
  }
  
  
  public RPAction.Status execute(RPObject.ID id, RPAction action)
  {
		marauroad.trace("SimpleRPRuleProcessor::execute",">");
		RPAction.Status status = RPAction.STATUS_FAIL;
		try
		{
			RPObject rp_player = null;
			rp_player = zone.get(id);
			
			if(action instanceof MoveAction)
			{
				status = makeMove(id, action);
			}
			else if(action instanceof GetCharacterListAction)
			{
				status = getCharacterList(id, action);
			}
			else if(action instanceof ChallengeAnswer)
			{
				status = challengeAnswer(id, action);
			}
			else if(action instanceof ChallengeAction)
			{
				status = challenge(id, action);
			}
			else
			{
				//unknown type
			}
		}
		catch (Attributes.AttributeNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (RPZone.RPObjectNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			marauroad.trace("SimpleRPRuleProcessor::execute","<");
		}
		return status;
  }
  
  private RPAction.Status makeMove(RPObject.ID id, RPAction action)
		throws NumberFormatException,
		Attributes.AttributeNotFoundException,
		RPZone.RPObjectNotFoundException
  {
		marauroad.trace("SimpleRPRuleProcessor::makeMove",">");
		RPAction.Status status = RPAction.STATUS_FAIL;
		try
		{
			RPObject rp_player = zone.get(id);
			GameBoard gb=null;
			int last_id = -1;
			try
			{
				gb = (GameBoard)rp_player.getSlot("hand").get();
				last_id = gb.getLastPlayerID();
			}
			catch (RPObject.NoSlotFoundException e)
			{
				e.printStackTrace();
				//        gb = new GameBoard(3);
				//        RPSlot rp_slot = new RPSlot("hand");
				//        rp_slot.add(gb);
				//        try
				//        {
				//          rp_player.addSlot(rp_slot);
				//        }
				//        catch (RPObject.SlotAlreadyAddedException ex)
				//        {
				//        }
			}
			catch (RPSlot.RPObjectNotFoundException e)
			{
				e.printStackTrace();
				// TODO: There is no object on the slot. Decide what to do
			}
			
			if(gb!=null)
			{
				if(id.getObjectID()==last_id)
				{
					marauroad.trace("SimpleRPRuleProcessor::execute","D","Player "+id +" already did a move, ignore this action.");
				}
				else
				{
					int row = Integer.parseInt(action.get("row"));
					int column = Integer.parseInt(action.get("column"));
					if(gb.getRPCharacterAt(row,column)==-1)
					{
						gb.setRPCharacterAt(row,column,id.getObjectID());
						status = RPAction.STATUS_SUCCESS;
						marauroad.trace("SimpleRPRuleProcessor::makeMove","D",gb.toString());
						//          int winner_id  = gb.getWinner();
						//          if(winner_id!=-1)
						//          {
						//            //TODO
						//            RPObject rp_winner = zone.get(null);
						////            marauroad.trace("SimpleRPRuleProcessor::makeMove","D","And the winner is "+winner);
						//          }
					}
					else
					{
						//this field is already set
					}
					marauroad.trace("SimpleRPRuleProcessor::makeMove","D","Player "+id +" - no actions???.");
				}
			}
			else
			{
				marauroad.trace("SimpleRPRuleProcessor::makeMove","D","Make move - no gameboard assigned???");
			}
		}
		finally
		{
			marauroad.trace("SimpleRPRuleProcessor::makeMove","<");
		}
		return status;
  }
  
  private RPAction.Status getCharacterList(RPObject.ID id, RPAction action)
		throws NumberFormatException,
		Attributes.AttributeNotFoundException,
		RPZone.RPObjectNotFoundException
  {
		marauroad.trace("SimpleRPRuleProcessor::getCharacterList",">");
		RPAction.Status status = RPAction.STATUS_FAIL;
		try
		{
			RPObject rp_player = zone.get(id);
			Iterator iter = zone.iterator();
			if(iter!=null)
			{
				
				RPSlot playerlist = ensureSlot(rp_player,"ear");
				
				CharacterList clist = new CharacterList();
				while(iter.hasNext())
				{
					RPObject object = (RPObject)iter.next();
					int oid = Integer.parseInt(object.get("object_id"));
					if(oid!=id.getObjectID())
					{
						String name = object.get("name");
						String pl_status = "idle";
						try
						{
							pl_status = (object.getSlot("hand").get()==null?"idle":"busy");
						}
						catch(Exception ex)
						{
							pl_status = "idle";
						}
						clist.addCharacter(oid,object.get("name"),pl_status);
					}
				}
				playerlist.removeAll();
				playerlist.add(clist);
			}
		}
		finally
		{
			marauroad.trace("SimpleRPRuleProcessor::getCharacterList",">");
		}
		return(status);
  }
  
  private RPAction.Status challenge(RPObject.ID id, RPAction action)
		throws NumberFormatException,
		Attributes.AttributeNotFoundException,
		RPZone.RPObjectNotFoundException
  {
		marauroad.trace("SimpleRPRuleProcessor::challenge",">");
		RPAction.Status status = RPAction.STATUS_FAIL;
		try
		{
			marauroad.trace("SimpleRPRuleProcessor::challenge","D","Action: " +action);
			RPObject rp_player = zone.get(id);
			int challenged_id = ((ChallengeAction)action).getWhom();
			RPObject player_challenged = zone.get(new RPObject.ID(challenged_id));
			RPSlot challenge_slot = ensureSlot(player_challenged, "challenge");
			RPSlot slot = ensureSlot(rp_player,"ear");
			slot.removeAll();
			//it is enough just to send id/name...
			CharacterList clist = new CharacterList();
			clist.addCharacter(Integer.parseInt(rp_player.get("object_id")),rp_player.get("name"),"wurst");
			challenge_slot.add(clist);
			status = RPAction.STATUS_SUCCESS;
		}
		finally
		{
			marauroad.trace("SimpleRPRuleProcessor::challenge",">");
		}
		return(status);
  }
  
  private RPAction.Status challengeAnswer(RPObject.ID id, RPAction action)
		throws NumberFormatException,
		Attributes.AttributeNotFoundException,
		RPZone.RPObjectNotFoundException
  {
		marauroad.trace("SimpleRPRuleProcessor::challengeAnswer",">");
		RPAction.Status status = RPAction.STATUS_FAIL;
		try
		{
			RPObject rp_player = zone.get(id);
			int oppenent_id = ((ChallengeAnswer)action).getWhom();
			boolean accepted = ((ChallengeAnswer)action).isAccepted();
			if(accepted)
			{
				RPObject player_challenged = zone.get(new RPObject.ID(oppenent_id));
				RPSlot slot = ensureSlot(player_challenged,"hand");
				slot.removeAll();
				RPSlot slot2 = ensureSlot(rp_player,"hand");
				slot2.removeAll();
				
				//asign a one..
				GameBoard gb = new GameBoard(3);
				slot.add(gb);
				slot2.add(gb);
				
				//clear all other slots if any
				slot = ensureSlot(rp_player,"ear");
				slot.removeAll();
				slot = ensureSlot(player_challenged,"ear");
				slot.removeAll();
				slot = ensureSlot(rp_player,"challenge");
				slot.removeAll();
				slot = ensureSlot(player_challenged,"challenge");
				slot.removeAll();
				
				marauroad.trace("SimpleRPRuleProcessor::challengeAnswer","D","Player "+id +" got the board");
				marauroad.trace("SimpleRPRuleProcessor::challengeAnswer","D","Player "+oppenent_id +" got the board");
				status = RPAction.STATUS_SUCCESS;
			}
			else
			{
				marauroad.trace("SimpleRPRuleProcessor::challengeAnswer","D","Player "+id +" NOT ACCEPTED CHALLENGE!!!!!!!!");
			}
		}
		finally
		{
			marauroad.trace("SimpleRPRuleProcessor::challengeAnswer",">");
		}
		return(status);
  }
  
  
  private RPSlot ensureSlot(RPObject player, String slot_name)
  {
		RPSlot challenge_slot = null;
		if(player.hasSlot(slot_name))
		{
			try
			{
				challenge_slot = player.getSlot(slot_name);
			}
			catch (RPObject.NoSlotFoundException e)
			{
				//cant be because i've checked it.
				//something is really then
			}
		}
		else
		{
			challenge_slot = new RPSlot(slot_name);
			try
			{
				player.addSlot(challenge_slot);
			}
			catch (RPObject.SlotAlreadyAddedException e)
			{
				//cant be because i've checked it.
				//something is really wrong then
			}
		}
		return(challenge_slot);
  }
  
  public void nextTurn()
  {
  }
}


