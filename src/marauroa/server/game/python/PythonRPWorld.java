/* $Id: PythonRPWorld.java,v 1.1 2005/01/23 21:00:47 arianne_rpg Exp $ */
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

import marauroa.*;
import java.util.*;

import marauroa.common.*;
import marauroa.server.game.*;

public class PythonRPWorld extends RPWorld
  {
  private GameScript gameScript;
  private PythonWorld pythonWorld;

  public PythonRPWorld() throws Exception
    {
    super();

    Logger.trace("PythonRPWorld::PythonRPWorld",">");

    try
      {
      gameScript=GameScript.getGameScript();
      gameScript.setRPWorld(this);
      pythonWorld=gameScript.getWorld();
      }
    catch(Exception e)
      {
      Logger.thrown("PythonRPWorld::PythonRPWorld","!",e);
      //@@@@@ System.exit(-1);
      }

    Logger.trace("PythonRPWorld::PythonRPWorld","<");
    }
  
  public void onInit() throws Exception
    {
    pythonWorld.onInit();
    }
  
  public void onFinish() throws Exception
    {
    pythonWorld.onFinish();
    }
   
  }
