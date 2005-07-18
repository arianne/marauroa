/* $Id: PythonRPWorld.java,v 1.2 2005/07/18 20:52:41 mtotz Exp $ */
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

import marauroa.common.Log4J;
import marauroa.server.game.RPWorld;
import org.apache.log4j.Logger;


public class PythonRPWorld extends RPWorld
  {
  /** the logger instance. */
  private static final Logger logger = Log4J.getLogger(PythonRPWorld.class);

  private GameScript gameScript;
  private PythonWorld pythonWorld;

  public PythonRPWorld() throws Exception
    {
    super();

    Log4J.startMethod(logger, "PythonRPWorld");

    try
      {
      gameScript=GameScript.getGameScript();
      gameScript.setRPWorld(this);
      pythonWorld=gameScript.getWorld();
      }
    catch(Exception e)
      {
      logger.error("cannot initialize PythonRPWorld()",e);
      }

    Log4J.finishMethod(logger, "PythonRPWorld");
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
