/* $Id: Item.java,v 1.3 2004/01/27 00:07:39 arianne_rpg Exp $ */
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

public class Item extends RPObject
  {
  public Item(RPObject.ID id,String type)
    {
    put(RPCode.var_object_id,id.getObjectID());
    put("type",type);
	put("defend",5);
	put("attack",5);
	put("price","30");
    }
  }