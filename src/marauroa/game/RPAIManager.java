/* $Id: RPAIManager.java,v 1.1 2004/05/30 14:35:22 arianne_rpg Exp $ */
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
public interface RPAIManager 
  {
  public void setScheduler(RPScheduler sched);
  public void setZone(RPZone zone);
  public boolean compute(long timelimit);
  }
