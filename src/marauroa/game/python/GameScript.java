/* $Id: GameScript.java,v 1.5 2004/07/13 18:16:48 arianne_rpg Exp $ */
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

import marauroa.game.*;
import marauroa.*;

/** This class is a wrapper for calling python in a better way. */
class GameScript 
  {
  private PythonInterpreter interpreter;
  private Configuration conf;
  
  private GameScript() throws Exception
    {
    conf=Configuration.getConfiguration();
    interpreter=new PythonInterpreter();
    interpreter.execfile(conf.get("python_script"));
    }
  
  private static GameScript gameScript=null;
  
  /** Set the RPZone on the script */
  public void setRPZone(IRPZone zone)
    {    
    interpreter.set("gamescript__zone",zone);   
    }

  /** Set the RPSheduler on the script */
  public void setRPScheduler(RPScheduler scheduler)
    {    
    interpreter.set("gamescript__scheduler",scheduler);   
    }
    
  /** Gets an instance of the GameScript */
  public static GameScript getGameScript() throws Exception
    {
    if(gameScript==null)
      {
      gameScript=new GameScript();
      }
    
    return gameScript;
    }

  public PythonZone getZone() throws Exception
    {    
    String pythonZoneClass=conf.get("python_script_zone_class");
    PyInstance object=(PyInstance)interpreter.eval(pythonZoneClass+"(gamescript__zone)");
    return (PythonZone)object.__tojava__(PythonZone.class);
    }
    
  public PythonRP getGameRules() throws Exception
    {
    String pythonRPClass=conf.get("python_script_rules_class");
    PyInstance object=(PyInstance)interpreter.eval(pythonRPClass+"(gamescript__zone)");
    return (PythonRP)object.__tojava__(PythonRP.class);
    }
  }
