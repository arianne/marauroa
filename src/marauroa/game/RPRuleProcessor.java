/* $Id: RPRuleProcessor.java,v 1.6 2003/12/08 01:12:19 arianne_rpg Exp $ */
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

public interface RPRuleProcessor
  {
  public void setContext(RPZone zone);
  public void approvedActions(RPActionList actionList);
  public RPAction.Status execute(RPObject.ID id, RPAction action);
  }
