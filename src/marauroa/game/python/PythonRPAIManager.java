/* $Id: PythonRPAIManager.java,v 1.1 2004/05/30 14:35:22 arianne_rpg Exp $ */
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

import org.python.util.PythonInterpreter;
import org.python.core.*;

import marauroa.marauroad;
import marauroa.game.*;
import marauroa.*;

/** Interface for the class that is in charge of executing AI.
 *  Implement it to personalize the AI */
public class PythonRPAIManager implements RPAIManager
  {
  private PythonInterpreter interpreter;
  private PythonAI pythonAI;

  public PythonRPAIManager() throws Exception 
    {
    marauroad.trace("PythonRPZone::PythonRPZone",">");
    
    Configuration conf=Configuration.getConfiguration();
    interpreter=new PythonInterpreter();
    interpreter.execfile(conf.get("python_script"));

    try
      {
      interpreter.set("zone",this);

      String pythonRPZoneClass=conf.get("python_script_ai_class");
      PyInstance object=(PyInstance)interpreter.eval(pythonRPZoneClass+"(zone)");
      pythonAI=(PythonAI)object.__tojava__(PythonAI.class);
      }
    catch(Exception e)
      {
      marauroad.thrown("PythonRPZone::PythonRPZone","!",e);
      System.exit(-1);
      }

    marauroad.trace("PythonRPZone::PythonRPZone","<");
    }
    
  public void setScheduler(RPScheduler sched)
    {
    }
    
  public void setZone(RPZone zone)
    {
    }
    
  public boolean compute(long timelimit)
    {
    return true;
    }    
  }
