/* $Id: Gladiator.java,v 1.6 2004/01/01 11:55:04 arianne_rpg Exp $ */
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
    put("name",randomNames[Math.abs(random.nextInt()%randomNames.length)]);
    put("hp",100);
    put("hit",10);
	put("fame",0);
	put("num_victory",0);
	put("num_defeat",0);    
	
	this.addSlot(new RPSlot("l_hand"));
	this.addSlot(new RPSlot("r_hand"));
    }
  }