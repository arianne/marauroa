/* $Id: PythonRPAIManager.java,v 1.3 2004/06/03 13:04:44 arianne_rpg Exp $ */
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
package marauroa.game.python;

import marauroa.marauroad;
import marauroa.game.*;
import marauroa.*;

/** Interface for the class that is in charge of executing AI.
 *  Implement it to personalize the AI */
public class PythonRPAIManager implements IRPAIManager
  {
  private GameScript gameScript;
  private PythonAI pythonAI;

  public PythonRPAIManager() throws Exception 
    {
    marauroad.trace("PythonRPZone::PythonRPZone",">");
    marauroad.trace("PythonRPZone::PythonRPZone","<");
    }
  
  public void setContext(IRPZone zone, RPScheduler sched)
    {
    marauroad.trace("PythonRPZone::setContext",">");

    try
      {
      gameScript=GameScript.getGameScript();
      gameScript.setRPZone(zone);
      gameScript.setRPScheduler(sched);
      pythonAI=gameScript.getAI();
      }
    catch(Exception e)
      {
      marauroad.thrown("PythonRPZone::PythonRPZone","!",e);
      System.exit(-1);
      }

    marauroad.trace("PythonRPZone::setContext","<");
    }
    
  public boolean compute(long timelimit)
    {
    return pythonAI.compute(timelimit);
    }    
  }
