/* $Id: Item.java,v 1.5 2004/03/04 13:42:37 arianne_rpg Exp $ */
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
  private static Map defend_val;
  private static Map attack_val;
  private static Map price_val;
  
  static
    {
    defend_val=new HashMap();
    defend_val.put("sword","1");
    defend_val.put("shield","10");
    
    attack_val=new HashMap();
    attack_val.put("sword","10");
    attack_val.put("shield","0");

    price_val=new HashMap();
    price_val.put("sword","50");
    price_val.put("shield","30");
    }
  
  public Item(RPObject.ID id,String type)
    {
    put(RPCode.var_object_id,id.getObjectID());
    put(RPCode.var_type,type);
	put(RPCode.var_defend,(String)defend_val.get(type));
    put(RPCode.var_attack,(String)attack_val.get(type));
    put(RPCode.var_price,(String)price_val.get(type));
    }
  }