/* $Id: RPWorld.java,v 1.2 2004/11/12 15:39:15 arianne_rpg Exp $ */
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
package marauroa.game;

import java.util.*;

public class RPWorld 
  {
  HashMap<IRPZone.ID,IRPZone> zones;
    
  public RPWorld()
    {
    zones=new HashMap<IRPZone.ID,IRPZone>();
    }
  
  public void onInit() throws Exception
    {
    }
  
  public void onFinish() throws Exception
    {
    }
  
  public void addRPZone(IRPZone zone)
    {
    zones.put(zone.getID(),zone);
    }
  
  public IRPZone getRPZone(IRPZone.ID zoneid)
    {
    return (IRPZone)zones.get(zoneid);
    }

  public IRPZone getRPZone(RPObject.ID objectid)
    {
    return (IRPZone)zones.get(new IRPZone.ID(objectid.getZoneID()));
    }
    
  public void assignRPObjectID(RPObject object) throws NoRPZoneException
    {
    try
      {
      if(object.has("zoneid"))
        {
        IRPZone zone=(IRPZone)zones.get(new IRPZone.ID(object.get("zoneid")));
        zone.assignRPObjectID(object);
        return;
        }        
      }
    catch(Exception e)
      {
      }

    throw new NoRPZoneException();  
    }
  
  public void changeZone(IRPZone.ID oldzone, IRPZone.ID newzone, RPObject object)
    {
    }  
  
  public void nextTurn()
    {
    Iterator it=zones.values().iterator();
    
    while(it.hasNext())
      {
      IRPZone zone=(IRPZone)it.next();
      zone.nextTurn();
      }
    }
  
  public int size()
    {
    int size=0;
    
    Iterator it=zones.values().iterator();
    
    while(it.hasNext())
      {
      IRPZone zone=(IRPZone)it.next();
      size+=zone.size();
      }
     
    return size;
    }
  }
