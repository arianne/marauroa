/* $Id: PythonRPWorld.java,v 1.2 2004/11/28 20:35:30 arianne_rpg Exp $ */
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
import java.util.*;

public class PythonRPWorld extends RPWorld
  {
  private GameScript gameScript;
  private PythonWorld pythonWorld;

  public PythonRPWorld() throws Exception
    {
    super();

    marauroad.trace("PythonRPWorld::PythonRPWorld",">");

    try
      {
      gameScript=GameScript.getGameScript();
      gameScript.setRPWorld(this);
      pythonWorld=gameScript.getWorld();
      }
    catch(Exception e)
      {
      marauroad.thrown("PythonRPWorld::PythonRPWorld","!",e);
      //@@@@@ System.exit(-1);
      }

    marauroad.trace("PythonRPWorld::PythonRPWorld","<");
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
