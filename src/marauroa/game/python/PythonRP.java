/* $Id: PythonRP.java,v 1.1 2004/05/20 12:34:52 arianne_rpg Exp $ */
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

import marauroa.game.*;
import java.io.*;
import java.util.*;


public class PythonRP
  {
  public PythonRP()
    {
    }
      
  public int execute(RPObject.ID id, RPAction action)
    {
    return 0;
    }
    
  public void nextTurn()
    {
    }

  public boolean onInit(RPObject object) throws RPZone.RPObjectInvalidException
    {
    return false;
    }
    
  public boolean onExit(RPObject.ID id)
    {
    return false;
    }
    
  public boolean onTimeout(RPObject.ID id)
    {
    return false;
    }
  
  public List serializeMap()
    {
    return null;
    }
  }
