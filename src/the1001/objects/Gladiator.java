/* $Id: Gladiator.java,v 1.2 2003/12/12 17:50:22 arianne_rpg Exp $ */
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
package the1001;

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
  
  public Gladiator(RPObject.ID id)
    {
    put("object_id",id.getObjectID());
    put("type","gladiator");
    put("name",randomNames[random.nextInt()%randomNames.length]);
	put("rp_size",5);
	put("rp_breath",5);
	put("rp_health",5);
	put("rp_fame",0);
	put("_strengh",5);
	put("_speed",5);
	put("_attack",5);
	put("_defend",5);
	put("_evasion",5);
	put("num_victory",0);
	put("num_defeat",0);    
    }
  }