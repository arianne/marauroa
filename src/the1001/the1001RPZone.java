/* $Id: the1001RPZone.java,v 1.18 2004/03/22 18:31:48 arianne_rpg Exp $ */
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
			add(heroesHouse);
			
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
	try
	  {
	  RPObject result=(RPObject)object.copy();
	  result.put(RPCode.var_object_id,new RPObject.ID(super.create()).getObjectID());
	
//	  Iterator it=object.iterator();
//	  while(it.hasNext())
//	    {
//	    String attrib=(String)it.next();
//	    if(attrib.equals(RPCode.var_object_id)==false)
//	      {
//	      result.put(attrib,object.get(attrib));
//	      }
//	    }
//	  
	  return result;
	  }
	catch(Exception e)
	  {
      return null;	 
      }
	}
	
}

