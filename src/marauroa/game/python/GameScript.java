/* $Id: GameScript.java,v 1.2 2004/05/31 07:20:46 root777 Exp $ */
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
  
  public void setRPZone(RPZone zone)
    {    
    interpreter.set("gamescript__zone",zone);   
    }

  public void setRPScheduler(RPScheduler scheduler)
    {    
    interpreter.set("gamescript__scheduler",scheduler);   
    }
    
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
    
  public PythonAI getAI() throws Exception
    {
    String pythonAIClass=conf.get("python_script_ai_class");
    PyInstance object=(PyInstance)interpreter.eval(pythonAIClass+"(gamescript__zone,gamescript__scheduler)");
    return (PythonAI)object.__tojava__(PythonAI.class);
    }   
  }
