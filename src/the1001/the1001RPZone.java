/* $Id: the1001RPZone.java,v 1.22 2004/05/07 17:16:59 arianne_rpg Exp $ */
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

import marauroa.marauroad;
import marauroa.game.*;
import org.w3c.dom.*;
import java.util.*;
import the1001.objects.*;

public class the1001RPZone extends MarauroaRPZone
  {
  private RPObject heroesHouse;
  private RPObject arena;
  public the1001RPZone()
    {
    super();
    marauroad.trace("the1001RPZone::the1001RPZone",">");
    try
      {
      heroesHouse=super.create();
      heroesHouse.put(RPCode.var_type,"shop");
      heroesHouse.put(RPCode.var_name,"Heroes' House");

      RPSlot gladiators=new RPSlot(RPCode.var_gladiators);

      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      heroesHouse.addSlot(gladiators);

      RPSlot items=new RPSlot(RPCode.var_items);

      items.add(new Item(new RPObject.ID(super.create()),"sword"));
      items.add(new Item(new RPObject.ID(super.create()),"shield"));
      heroesHouse.addSlot(items);
      /* Add to zone */
      //add(heroesHouse);
      arena=super.create();
      arena.put(RPCode.var_type,"arena");
      arena.put(RPCode.var_name,"Arena");
      arena.put(RPCode.var_status,RPCode.var_waiting);
      arena.put(RPCode.var_waiting,0);
      arena.addSlot(new RPSlot(RPCode.var_gladiators));
      /* Add to zone */
      add(arena);
      }
    catch(Exception e)
      {
      marauroad.thrown("the1001RPZone::the1001RPZone","X",e);
      marauroad.trace("the1001RPZone::the1001RPZone","!","Can't initialize world: "+e.getMessage());
      System.exit(-1);
      }
    finally
      {
      marauroad.trace("the1001RPZone::the1001RPZone","<");
      }
    }
    
  public RPObject getHeroesHouse()
    {
    return heroesHouse;
    }
    
  public RPObject getArena()
    {
    try
      {
      return get(new RPObject.ID(arena));
      }
    catch(Exception e)
      {
      return null;
      }
    }
    
  public RPObject create(RPObject object)
    {
    try
      {
      RPObject result=(RPObject)object.copy();
      result.put(RPCode.var_object_id,new RPObject.ID(super.create()).getObjectID());

      return result;
      }
    catch(Exception e)
      {
      return null;
      }
    }
  }
