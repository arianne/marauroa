/* $Id: MarauroaDBRPZone.java,v 1.1 2004/07/07 10:12:01 arianne_rpg Exp $ */
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
import java.io.*;
import marauroa.marauroad;

/** The MarauroaDBRPZone extends the MarauroaRPZone class to add Database support
 *  for the adding and removal of objects from the zone. It is slow*/
public class MarauroaDBRPZone extends MarauroaRPZone
  {
  protected JDBCPlayerDatabase rpobjectDatabase;
  protected Transaction transaction;

  private static Random rand=new Random();

  public MarauroaDBRPZone()
    {
    super();
    rand.setSeed(new Date().getTime());

    try
      {
      rpobjectDatabase=(JDBCPlayerDatabase)JDBCPlayerDatabase.getDatabase();
      transaction=rpobjectDatabase.getTransaction();
      }
    catch(Exception e)
      {
      marauroad.thrown("MarauroaDBRPZone::MarauroaDBRPZone","X",e);
      marauroad.trace("MarauroaDBRPZone::MarauroaDBRPZone","!",e.getMessage());
      System.exit(1);
      }
    }
  
  public void onInit() throws Exception
    {
    super.onInit();
    
    JDBCPlayerDatabase.RPObjectIterator it=rpobjectDatabase.zoneIterator(transaction);
    while(it.hasNext())
      {
      RPObject.ID id=it.next();
      RPObject object=rpobjectDatabase.loadRPObject(transaction,id);
        
      add(object);
      }
    }

  public void onFinish() throws Exception
    {
    super.onFinish();

    JDBCPlayerDatabase.RPObjectIterator it=rpobjectDatabase.zoneIterator(transaction);
    while(it.hasNext())
      {
      RPObject.ID id=it.next();
      rpobjectDatabase.storeRPObject(transaction,get(id));
      }
    }
  
  public void add(RPObject object) throws RPObjectInvalidException
    {
    try
      {
      super.add(object);
      
      if(!rpobjectDatabase.hasInRPZone(transaction,object))
        {
        rpobjectDatabase.addToRPZone(transaction,object);
        transaction.commit();
        }
      }
    catch(Exception e)
      {
      transaction.rollback();
      marauroad.thrown("MarauroaRPZone::add","X",e);
      throw new RPObjectInvalidException("id");
      }
    }
  
  public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException
    {
    try
      {
      RPObject object=super.remove(id);

      rpobjectDatabase.removeFromRPZone(transaction,id);
      transaction.commit();
      
      return object;
      }
    catch(Exception e)
      {
      transaction.rollback();
      throw new RPObjectNotFoundException(id);
      }
    }
 
  public RPObject create()
    {
    RPObject.ID id=rpobjectDatabase.getValidRPObjectID(transaction);
    while(has(id))
      {
      id=rpobjectDatabase.getValidRPObjectID(transaction);
      }
      
    return new RPObject(id);
    }
  }
