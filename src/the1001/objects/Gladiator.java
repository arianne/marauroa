/* $Id: Gladiator.java,v 1.12 2004/01/28 16:35:47 arianne_rpg Exp $ */
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

import the1001.RPCode;
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

  private static String[] randomLook=
    {
    "pknight",
    "yohko",
    "orc"    
    };
  
  private static Random random=new Random();
  
  public Gladiator(RPObject.ID id) throws SlotAlreadyAddedException
    {
    put(RPCode.var_object_id,id.getObjectID());
    put(RPCode.var_type,"gladiator");
    put(RPCode.var_name,randomNames[Math.abs(random.nextInt()%randomNames.length)]);
    put(RPCode.var_look,randomLook[Math.abs(random.nextInt()%randomLook.length)]);
    put(RPCode.var_initial_hp,100);
    put(RPCode.var_hp,100);
    put(RPCode.var_attack,5);
	put(RPCode.var_fame,100);
	put(RPCode.var_num_victory,0);
	put(RPCode.var_num_defeat,0);    
	
	this.addSlot(new RPSlot(RPCode.var_l_hand));
	this.addSlot(new RPSlot(RPCode.var_r_hand));
    }
  }