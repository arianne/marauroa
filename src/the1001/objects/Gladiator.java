/* $Id: Gladiator.java,v 1.4 2003/12/30 17:26:35 arianne_rpg Exp $ */
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

public class Gladiator extends RPObject
  {
  private static String[] randomNames=
    {
    "Rirg",
    "Rool",
    "Bark",
    "Prort",
    "Bogdush",
    "Vugor",
    "Giurk",
    "Erunak",
    "Gaol",
    "Vrodush",
    "Prashnak",
    "Vugor",
    "Priurk",
    "Pradash",
    "Gudish",
    "Biol",
    "Vidash"
    };
  
  private static Random random=new Random();
  
  public Gladiator(RPObject.ID id) throws SlotAlreadyAddedException
    {
    put("object_id",id.getObjectID());
    put("type","gladiator");
    put("name",randomNames[random.nextInt()%randomNames.length]);
	put("size",5);
	put("breath",5);
	put("health",5);
	put("fame",0);
	put("strengh",5);
	put("speed",5);
	put("attack",5);
	put("defend",5);
	put("evasion",5);
	put("num_victory",0);
	put("num_defeat",0);    
	
	this.addSlot(new RPSlot("l_hand"));
	this.addSlot(new RPSlot("r_hand"));
    }
  }