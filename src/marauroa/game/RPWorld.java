/* $Id: RPWorld.java,v 1.7 2004/11/25 17:03:20 arianne_rpg Exp $ */
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
  PlayerEntryContainer playerContainer;
    
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
  
  public void setPlayerContainer(PlayerEntryContainer playerContainer)
    {
    this.playerContainer=playerContainer;
    }
  
  public void addRPZone(IRPZone zone)
    {
    zones.put(zone.getID(),zone);
    }
  
  public IRPZone getRPZone(IRPZone.ID zoneid)
    {
    return zones.get(zoneid);
    }

  public IRPZone getRPZone(RPObject.ID objectid)
    {
    return zones.get(new IRPZone.ID(objectid.getZoneID()));
    }
  
  public void add(RPObject object) throws NoRPZoneException, RPObjectInvalidException  
    {
    try
      {
      if(object.has("zoneid"))
        {
        IRPZone zone=zones.get(new IRPZone.ID(object.get("zoneid")));
        zone.assignRPObjectID(object);
        zone.add(object);
        
        playerContainer.setRPObjectID(object.getInt("clientid"),object.getID());
        }        
      }
    catch(Exception e)
      {
      throw new NoRPZoneException();  
      }
    }
    
  public void changeZone(IRPZone.ID oldzone, IRPZone.ID newzone, RPObject object)
    {
    /* TODO: Do this, lazy guy */
    }  
  
  public void nextTurn()
    {
    for(IRPZone zone: zones.values())
      {
      zone.nextTurn();
      }
    }
  
  public int size()
    {
    int size=0;
    
    for(IRPZone zone: zones.values()) 
      {
      size+=zone.size();
      }
     
    return size;
    }
  }
