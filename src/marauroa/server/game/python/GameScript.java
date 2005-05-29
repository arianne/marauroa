/* $Id: GameScript.java,v 1.3 2005/05/29 22:24:24 arianne_rpg Exp $ */
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

package marauroa.server.game.python;

import org.python.util.PythonInterpreter;
import org.python.core.*;

import marauroa.server.*;
import marauroa.server.game.*;
import marauroa.common.*;
import marauroa.common.game.*;

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
  
  /** Gets an instance of the GameScript */
  public static GameScript getGameScript() throws Exception
    {
    if(gameScript==null)
      {
      gameScript=new GameScript();
      }
    
    return gameScript;
    }

  /** Set the RPZone on the script */
  public void setRPWorld(RPWorld world)
    {    
    interpreter.set("gamescript__world",world);   
    }

  /** Set the RPSheduler on the script */
  public void setRPScheduler(RPScheduler scheduler)
    {    
    interpreter.set("gamescript__scheduler",scheduler);   
    }
    
  public PythonWorld getWorld() throws Exception
    {    
    String pythonZoneClass=conf.get("python_script_world_class");
    PyInstance object=(PyInstance)interpreter.eval(pythonZoneClass+"()");
    return (PythonWorld)object.__tojava__(PythonWorld.class);
    }
    
  public PythonRP getGameRules() throws Exception
    {
    String pythonRPClass=conf.get("python_script_rules_class");
    PyInstance object=(PyInstance)interpreter.eval(pythonRPClass+"(gamescript__world)");
    return (PythonRP)object.__tojava__(PythonRP.class);
    }
  }
