/* $Id: the1001RPZone.java,v 1.3 2003/12/12 17:50:21 arianne_rpg Exp $ */
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

public class the1001RPZone extends MarauroaRPZone
  {
  public the1001RPZone()
    {
    marauroad.trace("the1001RPZone::the1001RPZone",">");
    
    try
      {
      RPObject HeroesHouse=super.create();
      HeroesHouse.put("type","shop");
      HeroesHouse.put("name","Heroes' House");
    
      RPSlot gladiators=new RPSlot("gladiators");    
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      gladiators.add(new Gladiator(new RPObject.ID(super.create())));
      HeroesHouse.addSlot(gladiators);

      HeroesHouse.addSlot(new RPSlot("gladiators"));
      }
    catch(Exception e)
      {
      marauroad.trace("the1001RPZone::the1001RPZone","!","Can't initialize world");
      System.exit(-1);      
      }
    finally
      {
      marauroad.trace("the1001RPZone::the1001RPZone","<");
      }
    }
  }
  