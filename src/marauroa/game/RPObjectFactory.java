/* $Id: RPObjectFactory.java,v 1.9 2003/12/10 22:49:46 root777 Exp $ */
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import marauroa.game.RPObject;
import marauroa.marauroad;
import marauroa.net.InputSerializer;
import marauroa.net.OutputSerializer;

/** This class is used to creating a RPObject object from a stream or to serialize
 *  a RPObject object to a stream. */
public class RPObjectFactory
{
  private static Map factoryArray;
  private static RPObjectFactory rpObjectFactory;
  
  private RPObjectFactory()
  {
    factoryArray= new HashMap();
  }
  
  /** This method returns an instance of RPObjectFactory
   *  @return A shared instance of RPObjectFactory */
  public static RPObjectFactory getFactory()
  {
    if(rpObjectFactory==null)
    {
      rpObjectFactory=new RPObjectFactory();
      rpObjectFactory.register(0,RPObject.class);
    }
    
    return rpObjectFactory;
  }
  
  /** registers a rpObject class
   @param index the RPObject type
   @param rpObjectClass the RPObject class
   **/
  public void register(int index,Class rpObjectClass)
  {
    factoryArray.put(new Integer(index),rpObjectClass);
  }
  
  /** Returns a object of the right class from a stream of serialized data.
   @param data the serialized data
   @throws IOException in case of problems with the RPObject */
  public RPObject getRPObject(InputSerializer is) throws IOException
  {
    marauroad.trace("RPObjectFactory::getRPObject",">");
    try
    {
      int index = is.readInt();
      Class rpObjectType=(Class) factoryArray.get(new Integer(index));
      if(rpObjectType!=null)
      {
        RPObject tmp=(RPObject) rpObjectType.newInstance();
        tmp.readObject(is);
        return tmp;
      }
      else
      {
        marauroad.trace("RPObjectFactory::getRPObject","X","RPObject type ["+index+"] is not registered.");
        throw new IOException("RPObject type ["+index+"] is not registered.");
      }
    }
    catch(Exception e)
    {
      e.printStackTrace(System.out);
      marauroad.trace("RPObjectFactory::getRPObject","X",e.getMessage());
      throw new IOException(e.getMessage());
    }
    finally
    {
      marauroad.trace("RPObjectFactory::getRPObject","<");
    }
  }
  
  /**
   * adds the given RPObject into serializer
   **/
  public void addRPObject(OutputSerializer os,RPObject rp_object) throws IOException
  {
    marauroad.trace("RPObjectFactory::addRPObject",">");
    try
    {
      int index = rp_object.objectType;
      os.write(index);
      rp_object.writeObject(os);
    }
    catch(Exception e)
    {
      marauroad.trace("RPObjectFactory::addRPObject","X",e.getMessage());
      throw new IOException(e.getMessage());
    }
    finally
    {
      marauroad.trace("RPObjectFactory::addRPObject","<");
    }
  }
}


