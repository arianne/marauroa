/* $Id: RPWorld.java,v 1.2 2005/02/17 22:55:07 arianne_rpg Exp $ */
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
package marauroa.server.game;

import java.util.*;
import marauroa.common.*;
import marauroa.common.game.*;

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

        /** NOTE: Document this hack */        
        if(object.has("clientid"))
          {
          playerContainer.setRPObjectID(object.getInt("clientid"),object.getID());
          PlayerEntryContainer.RuntimePlayerEntry entry=playerContainer.get(object.getInt("clientid"));
          entry.perception_OutOfSync=true;
          }
        }        
      }
    catch(Exception e)
      {
      Logger.thrown("RPWorld::add","X",e);
      throw new NoRPZoneException();  
      }
    }
  
  public RPObject get(RPObject.ID id) throws NoRPZoneException, RPObjectInvalidException  
    {
    try
      {
      IRPZone zone=zones.get(new IRPZone.ID(id.getZoneID()));
      return zone.get(id);
      }
    catch(Exception e)
      {
      throw new NoRPZoneException();  
      }
    }

  public RPObject remove(RPObject.ID id) throws NoRPZoneException, RPObjectNotFoundException  
    {
    try
      {
      IRPZone zone=zones.get(new IRPZone.ID(id.getZoneID()));
      return zone.remove(id);
      }
    catch(Exception e)
      {
      throw new NoRPZoneException();  
      }
    }
  
  public void modify(RPObject object) throws NoRPZoneException  
    {
    try
      {
      IRPZone zone=zones.get(new IRPZone.ID(object.get("zoneid")));
      zone.modify(object);
      }
    catch(Exception e)
      {
      throw new NoRPZoneException();  
      }
    }
  
    
  public void changeZone(IRPZone.ID oldzoneid, IRPZone.ID newzoneid, RPObject object) throws NoRPZoneException
    {
    Logger.trace("RPWorld::changeZone",">");
    try
      {
      if(newzoneid.equals(oldzoneid))
        {
        return;
        }
      
      IRPZone newzone=getRPZone(newzoneid);
      IRPZone oldzone=getRPZone(oldzoneid);
    
      oldzone.remove(object.getID());
      object.put("zoneid",newzoneid.getID());
      add(object);    
      }
    catch(Exception e)
      {
      Logger.thrown("RPWorld::changeZone","X",e);
      throw new NoRPZoneException();
      }
    finally
      {
      Logger.trace("RPWorld::changeZone","<");
      }
    }  

  public void changeZone(String oldzone, String newzone, RPObject object) throws NoRPZoneException
    {
    changeZone(new IRPZone.ID(oldzone),new IRPZone.ID(newzone),object);
    }  
  
  public void nextTurn()
    {
    Logger.trace("RPWorld::nextTurn",">");
    for(IRPZone zone: zones.values())
      {
      zone.nextTurn();
      }
    Logger.trace("RPWorld::nextTurn","<");
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
