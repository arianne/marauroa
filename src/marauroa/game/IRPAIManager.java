/* $Id: IRPAIManager.java,v 1.1 2004/06/03 13:04:44 arianne_rpg Exp $ */
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

/** Interface for the class that is in charge of executing AI.
 *  Implement it to personalize the AI */
public interface IRPAIManager
  {
  public void setContext(IRPZone zone, RPScheduler sched);
  public boolean compute(long timelimit);
  }
