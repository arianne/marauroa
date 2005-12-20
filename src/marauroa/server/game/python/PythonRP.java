/* $Id: PythonRP.java,v 1.4 2005/12/20 16:09:48 arianne_rpg Exp $ */
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

import marauroa.server.game.*;
import marauroa.common.game.*;
import marauroa.common.net.*;
import java.util.*;

public class PythonRP
  {
  public PythonRP()
    {
    }

  private RPServerManager rpMan;
  
  void setRPManager(RPServerManager rpMan)
    {    
    this.rpMan=rpMan;
    }
  
  public final void transferContent(RPObject.ID id, List<TransferContent> content)
    {
    rpMan.transferContent(id, content);
    }
      
  public boolean checkGameVersion(String game, String version)
    {
    return true;
    }
  
  public boolean createAccount(String username, String password, String email)
    {
    return false;
    }  

  public boolean onActionAdd(RPAction action, List<RPAction> actionList)
    {
    return true;
    }

  public boolean onIncompleteActionAdd(RPAction action, List<RPAction> actionList)
    {
    return true;
    }

  public int execute(RPObject.ID id, RPAction action)
    {
    return 0;
    }
  
  public void beginTurn()
    {
    }

  public void endTurn()
    {
    }

  public boolean onInit(RPObject object) throws RPObjectInvalidException
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
  }
