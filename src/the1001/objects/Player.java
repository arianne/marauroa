/* $Id: Player.java,v 1.8 2004/03/05 16:27:48 arianne_rpg Exp $ */
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
import the1001.RPCode;
import java.util.*;

public class Player extends RPObject
  {
  private static String[] randomLook=
    {
    "billgates"
    };

  private static Random random=new Random();
  
  public Player(RPObject.ID id, String name) throws SlotAlreadyAddedException
    {
    put(RPCode.var_object_id,id.getObjectID());
    put(RPCode.var_type,"character");
    put(RPCode.var_name,name);
    put(RPCode.var_look,randomLook[Math.abs(random.nextInt()%randomLook.length)]);
    put(RPCode.var_fame,50);
    
    addSlot(new RPSlot(RPCode.var_myGladiators));
    addSlot(new RPSlot(RPCode.var_myItems));
    }
  }