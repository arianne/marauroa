/* $Id: the1001RPZone.java,v 1.24 2004/05/10 15:40:31 arianne_rpg Exp $ */
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
    
    if(size()==0)
      {
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
        rpobjectDatabase.storeRPObject(transaction, heroesHouse);
        add(heroesHouse);
      
        arena=super.create();
        arena.put(RPCode.var_type,"arena");
        arena.put(RPCode.var_name,"Arena");
        arena.put(RPCode.var_status,RPCode.var_waiting);
        arena.put(RPCode.var_waiting,0);
        arena.addSlot(new RPSlot(RPCode.var_gladiators));

        /* Add to zone */
        rpobjectDatabase.storeRPObject(transaction, arena);
        add(arena);
        }
      catch(Exception e)
        {
        marauroad.thrown("the1001RPZone::the1001RPZone","X",e);
        marauroad.trace("the1001RPZone::the1001RPZone","!","Can't initialize world: "+e.getMessage());
        System.exit(-1);
        }
      }
    else
      {
      try
        {
        Iterator it=iterator();
        while(it.hasNext())
          {
          RPObject object=(RPObject)it.next();
        
          if(object.get("type").equals("shop"))
            {
            heroesHouse=object;
            }
          else if(object.get("type").equals("arena"))
            {
            arena=object;
            }
          else
            {
            remove(object.getID());
            it=iterator();
            }
          }
        
        if(arena==null || heroesHouse==null)
          {
          throw new Exception("Can't find the Arena, nor the Heroes House at the world");
          }
        }
      catch(Exception e)
        {
        marauroad.thrown("the1001RPZone::the1001RPZone","X",e);
        marauroad.trace("the1001RPZone::the1001RPZone","!","Can't initialize world: "+e.getMessage());
        System.exit(-1);
        }      
      }
    }
    
  public RPObject getHeroesHouse()
    {
    return heroesHouse;
    }
    
  public RPObject getArena()
    {
    return arena;
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
