/* $Id: Item.java,v 1.2 2003/12/30 17:26:35 arianne_rpg Exp $ */
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

public class Item extends RPObject
  {
  public Item(RPObject.ID id,String type)
    {
    put("object_id",id.getObjectID());
    put("type",type);
	put("defend",5);
	put("attack",5);
	put("price","30");
    }
  }