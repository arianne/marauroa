/* $Id: IRPRuleProcessor.java,v 1.7 2004/11/19 20:30:06 arianne_rpg Exp $ */
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

import java.util.*;
import marauroa.net.*;

/** Interface for the class that is in charge of executing actions.
 *  Implement it to personalize the game */
public interface IRPRuleProcessor
  {
  /** Set the context where the actions are executed.
   *  @param zone The zone where actions happens. */
  public void setContext(RPServerManager rpman,RPWorld world);
  
  /** Pass the whole list of actions so that it can approve or deny the actions in it.
   *  @param id the id of the object owner of the actions.
   *  @param actionList the list of actions that the player wants to execute. */
  public void approvedActions(RPObject.ID id, RPActionList actionList);
  
  /** Execute an action in the name of a player.
   *  @param id the id of the object owner of the actions.
   *  @param action the action to execute
   *  @return the action status, that can be Success, Fail or incomplete, please
   *      refer to Actions Explained for more info. */
  public RPAction.Status execute(RPObject.ID id, RPAction action);
  
  /** Notify it when a new turn happens */
  public void nextTurn();
  
  /** Callback method called when a new player enters in the game
   *  @param object the new player that enters in the game. */
  public boolean onInit(RPObject object) throws RPObjectInvalidException;
  
  /** Callback method called when a new player exits the game
   *  @param id the new player id that exits the game.
   *  @return true to update the player on database. */
  public boolean onExit(RPObject.ID id) throws RPObjectNotFoundException;
  
  /** Callback method called when a new player time out
   *  @param id the new player id that timeouts. */
  public boolean onTimeout(RPObject.ID id) throws RPObjectNotFoundException;
  }
