/* $Id: GameDataModel.java,v 1.2 2004/02/15 23:23:51 root777 Exp $ */
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
	private RPObject gladiator;
	private Map spectators;
	private Map fighters;
	private transient NetworkClientManager netMan;
	private List listeners;
	
	public GameDataModel(NetworkClientManager net_man)
	{
		netMan     = net_man;
		spectators = new HashMap(8);
		fighters   = new HashMap(2);
		listeners  = new ArrayList(1);
	}
	
	/**
	 * Sets the own Gladiator
	 *
	 * @param    Gladiator           a  RPObject
	 */
	public void setGladiator(RPObject gladiator)
	{
		this.gladiator = gladiator;
	}
	
	/**
	 * Returns the own Gladiator
	 *
	 * @return    a  RPObject
	 */
	public RPObject getGladiator()
	{
		return gladiator;
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
				marauroad.trace("The1001Game::addSpectator","D","Adding spectator " + spectator);
				if(spectators.put(spectator.get("object_id"),spectator)==null)
				{
					fireListeners();
				}
			}
			catch (Attributes.AttributeNotFoundException e)
			{
				marauroad.trace("The1001Game::addSpectator","X",e.getMessage());
			}
			dumpList(spectators.values());
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
				if(spectators.remove(spectator.get("object_id"))!=null)
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
			marauroad.trace("The1001Game::addFighter","D","Adding fighter " + fighter);
			try
			{
				if(fighters.put(fighter.get("object_id"),fighter)==null)
				{
					fireListeners();
				}
			}
			catch (Attributes.AttributeNotFoundException e)
			{
				marauroad.trace("The1001Game::addFighter","X",e.getMessage());
			}
			dumpList(fighters.values());
		}
	}
	
	/**
	 * deletes the fighter from world, if he was there.
	 **/
	public void deleteFighter(RPObject fighter)
	{
		try
		{
			if(fighters.remove(fighter.get("object_id"))!=null)
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
		RPObject gladiator = getGladiator();
		if(gladiator!=null)
		{
			int gl_id = RPObject.INVALID_ID.getObjectID();
			try
			{
				gl_id = getGladiator().getInt("object_id");
			}
			catch (Attributes.AttributeNotFoundException e)
			{
				marauroad.trace("The1001Game::requestFight","X","Gladiator has no or invalid object id.");
			}
			RPAction action = new RPAction();
			action.put(RPCode.var_type,"request_fight");
			action.put(RPCode.var_gladiator_id,gl_id);
			netMan.addMessage(new MessageC2SAction(null,action));
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
}
