/* $Id: PythonRPZone.java,v 1.1 2004/05/30 14:35:22 arianne_rpg Exp $ */
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
import java.util.*;

public class PythonRPZone extends MarauroaRPZone
  {
  private PythonInterpreter interpreter;
  private PythonZoneRP pythonZoneRP;

  public PythonRPZone() throws Exception
    {
    super();

    marauroad.trace("PythonRPZone::PythonRPZone",">");
    
    Configuration conf=Configuration.getConfiguration();
    interpreter=new PythonInterpreter();
    interpreter.execfile(conf.get("python_script"));

    try
      {
      interpreter.set("zone",this);

      String pythonRPZoneClass=conf.get("python_script_zone_class");
      PyInstance object=(PyInstance)interpreter.eval(pythonRPZoneClass+"(zone)");
      pythonZoneRP=(PythonZoneRP)object.__tojava__(PythonZoneRP.class);
      }
    catch(Exception e)
      {
      marauroad.thrown("PythonRPZone::PythonRPZone","!",e);
      System.exit(-1);
      }

    marauroad.trace("PythonRPZone::PythonRPZone","<");
    }
  
  protected void loadWorld() throws Exception
    {
    pythonZoneRP.onInit();
    }
  
  protected void storeWorld() throws Exception
    {
    pythonZoneRP.onFinish();
    }
  }
