/* $Id: the1001RPZone.java,v 1.10 2004/01/08 14:14:47 arianne_rpg Exp $ */
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
import marauroa.*;
import the1001.objects.*;

public class the1001RPZone extends MarauroaRPZone
  {
  private RPObject heroesHouse;
  private RPObject arena;
  
  public the1001RPZone()
    {
    marauroad.trace("the1001RPZone::the1001RPZone",">");
    
    try
      {
      heroesHouse=super.create();
      heroesHouse.put("type","shop");
      heroesHouse.put("name","Heroes' House");
    
      RPSlot gladiators=new RPSlot("gladiators");    
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      heroesHouse.addSlot(gladiators);
      
      RPSlot items=new RPSlot("items");
      items.add(new Item(new RPObject.ID(super.create()),"sword"));
      items.add(new Item(new RPObject.ID(super.create()),"shield"));
      heroesHouse.addSlot(items);      
      /* Add to zone */
      add(heroesHouse);      
      
      arena=super.create();
      arena.put("type","arena");
      arena.put("name","Arena");
      arena.put("status","waiting");
      arena.put("waiting",0);
      
      arena.addSlot(new RPSlot("gladiators"));      
      /* Add to zone */
      add(arena);
      }
    catch(Exception e)
      {
      marauroad.trace("the1001RPZone::the1001RPZone","!","Can't initialize world: "+e.getMessage());
      e.printStackTrace(System.out);
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
    return arena;
    }    
    
  public RPObject create(RPObject object)
    {
    /** TODO: Must copy the object and assign a new Object id 
     *  It is used in the Buy action. */
    return null;
    }    
  }
  