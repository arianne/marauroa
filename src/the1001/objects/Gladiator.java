/* $Id: Gladiator.java,v 1.1 2003/12/12 17:00:48 arianne_rpg Exp $ */
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

public class Gladiator extends RPObject
  {
  Gladiator(RPObject.ID id)
    {
    put("object_id",id.getObjectID());
    put("type","gladiator");
	put("rp_strengh",0);
	put("rp_size",0);
	put("rp_speed",0);
	put("rp_breath",0);
	put("rp_health",0);
	put("rp_fame",0);
	put("_attack",0);
	put("_defend",0);
	put("_evasion",0);
	put("num_victory",0);
	put("num_defeat",0);    
    }
  }