/* $Id: PythonRPZone.java,v 1.4 2004/08/29 11:07:42 arianne_rpg Exp $ */
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

public class PythonRPZone extends MarauroaRPZone
  {
  private GameScript gameScript;
  private PythonZone pythonZone;

  public PythonRPZone() throws Exception
    {
    super();

    marauroad.trace("PythonRPZone::PythonRPZone",">");

    try
      {
      gameScript=GameScript.getGameScript();
      gameScript.setRPZone(this);
      pythonZone=gameScript.getZone();
      }
    catch(Exception e)
      {
      marauroad.thrown("PythonRPZone::PythonRPZone","!",e);
      System.exit(-1);
      }

    marauroad.trace("PythonRPZone::PythonRPZone","<");
    }
  
  public void onInit() throws Exception
    {
    pythonZone.onInit();
    }
  
  public void onFinish() throws Exception
    {
    pythonZone.onFinish();
    }
  }
