/* $Id: RPRuleProcessor.java,v 1.7 2003/12/08 12:39:53 arianne_rpg Exp $ */
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

/** Interface for the class that is in charge of executing actions.
 *  Implement it to personalize the game */
public interface RPRuleProcessor
  {
  /** Set the context where the actions are executed */
  public void setContext(RPZone zone);
  /** Pass the whole list of actions so that it can approve or deny the actions in it */
  public void approvedActions(RPActionList actionList);
  /** Execute an action in the name of a player. */
  public RPAction.Status execute(RPObject.ID id, RPAction action);
  /** Notify it when a new turn happens */
  void nextTurn();
  }
