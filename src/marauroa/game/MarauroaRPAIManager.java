/* $Id: MarauroaRPAIManager.java,v 1.4 2004/07/07 10:07:20 arianne_rpg Exp $ */
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

public class MarauroaRPAIManager implements IRPAIManager
  {
  public MarauroaRPAIManager()
    {
    }
    
  public void setContext(IRPZone zone,RPScheduler sched)
    {
    }
    
  public boolean compute(long timelimit)
    {
    return true;
    }
  }
