/* $Id: RPWorld.java,v 1.3 2004/11/20 20:06:46 arianne_rpg Exp $ */
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
  
  public void add(RPObject object) throws NoRPZoneException, RPObjectInvalidException  
    {
    IRPZone zone=assignRPObjectID(object);
    zone.add(object);
    }
    
  private IRPZone assignRPObjectID(RPObject object) throws NoRPZoneException
    {
    try
      {
      if(object.has("zoneid"))
        {
        IRPZone zone=(IRPZone)zones.get(new IRPZone.ID(object.get("zoneid")));
        zone.assignRPObjectID(object);
        return zone;
        }        
      }
    catch(Exception e)
      {
      }

    throw new NoRPZoneException();  
    }
  
  public void changeZone(IRPZone.ID oldzone, IRPZone.ID newzone, RPObject object)
    {
    /* TODO: Do this, lazy guy */
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
