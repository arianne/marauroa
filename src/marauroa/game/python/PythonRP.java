/* $Id: PythonRP.java,v 1.5 2004/05/31 07:31:46 root777 Exp $ */
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
