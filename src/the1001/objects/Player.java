/* $Id: Player.java,v 1.1 2004/01/01 19:52:23 arianne_rpg Exp $ */
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
package the1001.objects;

import marauroa.game.*;
import java.util.*;

public class Player extends RPObject
  {
  public Player(RPObject.ID id, String name) throws SlotAlreadyAddedException
    {
    put("object_id",id.getObjectID());
    put("type","player");
    put("name",name);
    put("status","idle");
    put("fame",0);
    
    addSlot(new RPSlot("gladiators"));
    addSlot(new RPSlot("items"));
    }
  }