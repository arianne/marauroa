/* $Id: GameDataModel.java,v 1.5 2004/02/26 06:22:09 root777 Exp $ */
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

package the1001.client;


import java.util.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import marauroa.game.Attributes;
import marauroa.game.RPAction;
import marauroa.game.RPObject;
import marauroa.marauroad;
import marauroa.net.MessageC2SAction;
import marauroa.net.NetworkClientManager;
import the1001.RPCode;

/**
 *@author Waldemar Tribus
 */
public final class GameDataModel
{
	public final static String CMD_SCISSOR = "SCISSOR";
	public final static String CMD_STONE   = "STONE";
	public final static String CMD_PAPER   = "PAPER";
	public final static String CMD_FIGHT   = "FIGHT";
	public final static String CMD_VOTE_UP = RPCode.var_voted_up;
	public final static String CMD_VOTE_DOWN = "VOTE_DOWN";
	
	
	public final static String ARENA_MODE_WAITING  = RPCode.var_waiting;
	public final static String ARENA_MODE_FIGHTING = RPCode.var_fighting;
	public final static String ARENA_MODE_REQ_FAME = RPCode.var_request_fame;
	
	private RPObject ownGladiator;
	private RPObject ownCharacter;
	private RPObject arena;
	private Map spectators;
	private Map fighters;
	private transient NetworkClientManager netMan;
	private List listeners;
	private ActionListener commandListener;
	private String status;
	
	public GameDataModel(NetworkClientManager net_man)
	{
		netMan     = net_man;
		spectators = new HashMap(8);
		fighters   = new HashMap(2);
		listeners  = new ArrayList(1);
		commandListener = new ActionHandler();
	}
	
	/**
	 * Sets Arena
	 *
	 * @param    Arena               a  RPObject
	 */
	public void setArena(RPObject arena)
	{
		this.arena = arena;
	}
	
	/**
	 * Returns Arena
	 *
	 * @return    a  RPObject
	 */
	public RPObject getArena()
	{
		return arena;
	}
	

	/**
	 * Sets OwnCharacter
	 *
	 * @param    OwnCharacter        a  RPObject
	 */
	public void setOwnCharacter(RPObject ownCharacter)
	{
		this.ownCharacter = ownCharacter;
	}
	
	/**
	 * Returns OwnCharacter
	 *
	 * @return    a  RPObject
	 */
	public RPObject getOwnCharacter()
	{
		return ownCharacter;
	}
	
	public ActionListener getActionHandler()
	{
		return(commandListener);
	}
	
	public void setStatus(String mode)
	{
		this.status = mode;
	}
	
	public String getStatus()
	{
		return(status);
	}
	
	
	/**
	 * Sets the own Gladiator
	 *
	 * @param    Gladiator           a  RPObject
	 */
	public void setOwnGladiator(RPObject gladiator)
	{
		this.ownGladiator = gladiator;
	}
	
	/**
	 * Returns the own Gladiator
	 *
	 * @return    a  RPObject
	 */
	public RPObject getOwnGladiator()
	{
		return ownGladiator;
	}
	
//		public String getName()
//		{
//			String str = "";
//			if(gladiator!=null)
//			{
//				try
//				{
//					str = gladiator.get(RPCode.var_name);
//				}
//				catch (Attributes.AttributeNotFoundException e)
//				{
//					str = "<unknown>";
//				}
//			}
//			return(str);
//		}
//
//		public String getInitHP()
//		{
//			String str = "";
//			if(gladiator!=null)
//			{
//				try
//				{
//					str = gladiator.get(RPCode.var_initial_hp);
//				}
//				catch (Attributes.AttributeNotFoundException e)
//				{
//					str = "<unknown>";
//				}
//			}
//			return(str);
//		}
	
//		public String getHP()
//		{
//			String str = "";
//			if(gladiator!=null)
//			{
//				try
//				{
//					str = gladiator.get(RPCode.var_hp);
//				}
//				catch (Attributes.AttributeNotFoundException e)
//				{
//					str = "<unknown>";
//				}
//			}
//			return(str);
//		}
	
//		public int getGladiatorId()
//		{
//			int id = -1;
//			try
//			{
//				id = getGladiator().getInt("object_id");
//			}
//			catch (Attributes.AttributeNotFoundException e) {}
//			return(id);
//		}
	
	/**
	 * adds a new spectator to world.
	 * if the spectator is already there then the old instance
	 * will be replaced by a new one
	 **/
	public void addSpectator(RPObject spectator)
	{
		synchronized(spectators)
		{
			try
			{
//				marauroad.trace("The1001Game::addSpectator","D","Adding spectator " + spectator);
				spectators.put(spectator.get(RPCode.var_object_id),spectator);
				fireListeners();
			}
			catch (Attributes.AttributeNotFoundException e)
			{
				marauroad.trace("The1001Game::addSpectator","X",e.getMessage());
			}
//			dumpList(spectators.values());
		}
		
	}
	
	/**
	 * Method dumpList
	 *
	 * @param    spectators          a  List
	 *
	 */
	private void dumpList(Collection rpobjects)
	{
		for (Iterator iter = rpobjects.iterator(); iter.hasNext();)
		{
			marauroad.trace("#","D",""+iter.next());
		}
	}
	
	/**
	 * deletes the spectator from world, if he was there.
	 **/
	public void deleteSpectator(RPObject spectator)
	{
		synchronized(spectators)
		{
			try
			{
				if(spectators.remove(spectator.get(RPCode.var_object_id))!=null)
				{
					fireListeners();
				}
			}
			catch (Attributes.AttributeNotFoundException e)
			{
				marauroad.trace("The1001Game::deleteSpectator","X",e.getMessage());
			}
		}
		fireListeners();
	}
	
	/**
	 * returns all the spectators
	 **/
	public RPObject[] getSpectators()
	{
		synchronized(spectators)
		{
			RPObject[] spectators_a = new RPObject[spectators.size()];
			return((RPObject[])spectators.values().toArray(spectators_a));
		}
	}
	
	/**
	 * adds a new fighter to arena.
	 * if the fighter is already there then the old instance
	 * will be replaced by a new one
	 **/
	public void addFighter(RPObject fighter)
	{
		synchronized(fighters)
		{
//			marauroad.trace("The1001Game::addFighter","D","Adding fighter " + fighter);
			try
			{
				fighters.put(fighter.get(RPCode.var_object_id),fighter);
				fireListeners();
			}
			catch (Attributes.AttributeNotFoundException e)
			{
				marauroad.trace("The1001Game::addFighter","X",e.getMessage());
			}
//			dumpList(fighters.values());
		}
	}
	
	/**
	 * deletes the fighter from world, if he was there.
	 **/
	public void deleteFighter(RPObject fighter)
	{
		try
		{
			if(fighters.remove(fighter.get(RPCode.var_object_id))!=null)
			{
				fireListeners();
			}
		}
		catch (Attributes.AttributeNotFoundException e)
		{
			marauroad.trace("The1001Game::deleteFighter","X",e.getMessage());
		}
	}
	
	public RPObject[] getFighters()
	{
		synchronized(fighters)
		{
			RPObject[] fighters_a = new RPObject[fighters.size()];
			return((RPObject[])fighters.values().toArray(fighters_a));
		}
	}
	
	public void requestFight()
	{
		RPObject gladiator = getOwnGladiator();
		if(gladiator!=null)
		{
			int gl_id = RPObject.INVALID_ID.getObjectID();
			try
			{
				gl_id = getOwnGladiator().getInt("object_id");
			}
			catch (Attributes.AttributeNotFoundException e)
			{
				marauroad.trace("The1001Game::requestFight","X","Gladiator has no or invalid object id.");
			}
			RPAction action = new RPAction();
			action.put(RPCode.var_type,"request_fight");
			action.put(RPCode.var_gladiator_id,gl_id);
			netMan.addMessage(new MessageC2SAction(null,action));
			marauroad.trace("The1001Game::requestFight","D","Fight requested.");
		}
	}
	
	public void sendMessage(String msg)
	{
		RPAction action = new RPAction();
		action.put(RPCode.var_type,RPCode.var_chat);
		action.put(RPCode.var_content,msg);
		netMan.addMessage(new MessageC2SAction(null,action));
		marauroad.trace("The1001Game::sendMessage","D","Chat message sent.");
	}
	
	public void setFightMode(String mode)
	{
		RPObject gladiator = getOwnGladiator();
		if(gladiator!=null)
		{
			int gl_id = RPObject.INVALID_ID.getObjectID();
			try
			{
				gl_id = getOwnGladiator().getInt("object_id");
			}
			catch (Attributes.AttributeNotFoundException e)
			{
				marauroad.trace("The1001Game::setFightMode","X","Gladiator has no or invalid object id.");
			}
			RPAction action = new RPAction();
			action.put(RPCode.var_gladiator_id,gl_id);
			action.put(RPCode.var_type,"fight_mode");
			action.put("fight_mode",mode);
			netMan.addMessage(new MessageC2SAction(null,action));
			marauroad.trace("The1001Game::setFightMode","D","Fight mode set.");
		}
	}
	
	public void vote(String vote)
	{
		RPObject gladiator = getOwnGladiator();
		if(gladiator!=null)
		{
			int gl_id = RPObject.INVALID_ID.getObjectID();
			try
			{
				gl_id = getOwnGladiator().getInt("object_id");
			}
			catch (Attributes.AttributeNotFoundException e)
			{
				marauroad.trace("The1001Game::vote","X","Gladiator has no or invalid object id.");
			}
			RPAction action = new RPAction();
			action.put(RPCode.var_gladiator_id,gl_id);
			action.put(RPCode.var_type,RPCode.var_vote);
			action.put(RPCode.var_vote,vote);
			netMan.addMessage(new MessageC2SAction(null,action));
			marauroad.trace("The1001Game::vote","D","Voted ["+vote+"].");
		}
	}
	
	/**
	 * adds a new listener
	 **/
	public GameDataModelListenerIF addListener(GameDataModelListenerIF lissi)
	{
		if(lissi!=null)
		{
			if(!listeners.contains(lissi))
			{
				listeners.add(lissi);
			}
		}
		return(lissi);
	}
	
	/**
	 * removes an already registered listener
	 */
	public boolean removeListener(GameDataModelListenerIF lissi)
	{
		boolean ret = false;
		if(lissi!=null)
		{
			ret = listeners.remove(lissi);
		}
		return(ret);
	}
	
	private void fireListeners()
	{
		for (int i = 0; i < listeners.size(); i++)
		{
			((GameDataModelListenerIF)listeners.get(i)).modelUpdated(this);
		}
	}
	
	private final class ActionHandler
		implements ActionListener
	{
		/**
		 * Invoked when an action occurs.
		 */
		public void actionPerformed(ActionEvent e)
		{
			if(e!=null)
			{
				String command = e.getActionCommand();
				if(CMD_FIGHT.equals(command))
				{
					requestFight();
				}
				else if(CMD_SCISSOR.equals(command))
				{
					setFightMode(RPCode.var_scissor);
				}
				else if(CMD_PAPER.equals(command))
				{
					setFightMode(RPCode.var_paper);
				}
				else if(CMD_STONE.equals(command))
				{
					setFightMode(RPCode.var_rock);
				}
				else if(CMD_VOTE_UP.equals(command)||CMD_VOTE_DOWN.equals(command))
				{
					vote(command);
				}
				else
				{
					marauroad.trace("GameDataModel","D","Unknown command "+command + ", e="+e);
				}
			}
		}
	}
}
