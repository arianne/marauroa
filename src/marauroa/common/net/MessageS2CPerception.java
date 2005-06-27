/* $Id: MessageS2CPerception.java,v 1.8 2005/06/27 16:59:50 arianne_rpg Exp $ */
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
package marauroa.common.net;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import marauroa.common.*;
import marauroa.common.game.*;

/** This message indicate the client the objects that the server has determined that
 *  this client is able to see.
 *
 *  @see marauroa.common.net.Message
 *  @see marauroa.game.RPZone
 */
public class MessageS2CPerception extends Message
  {
  private byte typePerception;
    
  private int timestampPerception;
  private IRPZone.ID zoneid;

  private List<RPObject> addedRPObjects;
  private List<RPObject> modifiedAddedAttribsRPObjects;
  private List<RPObject> modifiedDeletedAttribsRPObjects;
  private List<RPObject> deletedRPObjects;
  private RPObject myRPObject;
  
  private static CachedCompressedPerception cache=CachedCompressedPerception.get();
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CPerception()
    {
    super(MessageType.S2C_PERCEPTION,null);
    
    myRPObject=new RPObject();
    }
  
  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param modifiedRPObjects the list of object that has been modified.
   *  @param deletedRPObjects the list of object that has been deleted since the last perception.
   */
  public MessageS2CPerception(InetSocketAddress source,Perception perception)
    {
    super(MessageType.S2C_PERCEPTION,source);
    
    typePerception=perception.type;
    zoneid=perception.zoneid;
    addedRPObjects=perception.addedList;
    modifiedAddedAttribsRPObjects=perception.modifiedAddedAttribsList;
    modifiedDeletedAttribsRPObjects=perception.modifiedDeletedAttribsList;
    deletedRPObjects=perception.deletedList;
    
    myRPObject=new RPObject();
    }
  
  public void setMyRPObject(RPObject object)
    {
    myRPObject=object;
    }
  
  public RPObject getMyRPObject()
    {
    return myRPObject;
    }
  
  public void setPerceptionTimestamp(int ts)
    {
    timestampPerception=ts;
    }
  
  public int getPerceptionTimestamp()
    {
    return timestampPerception;
    }
  
  
  public byte getTypePerception()
    {
    return typePerception;
    }
   
  public IRPZone.ID getRPZoneID()
    {
    return zoneid;
    }
    
  /** This method returns the list of modified objects
   *  @return List<RPObject> of added objects */
  public List<RPObject> getAddedRPObjects()
    {
    return addedRPObjects;
    }

  /** This method returns the list of modified objects
   *  @return List<RPObject> of modified objects that has attributes added*/
  public List<RPObject> getModifiedAddedRPObjects()
    {
    return modifiedAddedAttribsRPObjects;
    }

  /** This method returns the list of modified objects
   *  @return List<RPObject> of modified objects that has attributes removed*/
  public List<RPObject> getModifiedDeletedRPObjects()
    {
    return modifiedDeletedAttribsRPObjects;
    }
  
  /** This method returns the list of deleted objects
   *  @return List<RPObject> of deleted objects */
  public List<RPObject> getDeletedRPObjects()
    {
    return deletedRPObjects;
    }
  
  /** This method returns a String that represent the object
   *  @return a string representing the object.*/
  public String toString()
    {
    StringBuffer perception_string=new StringBuffer();
    perception_string.append("Type: "+typePerception+" Timestamp: "+timestampPerception+") contents: ");

    perception_string.append("\n  zoneid: "+zoneid+"\n");
    perception_string.append("\n  added: \n");
    for(RPObject object: addedRPObjects)
      {
      perception_string.append("    "+object.toString()+"\n");
      }

    perception_string.append("\n  modified added: \n");
    for(RPObject object: modifiedAddedAttribsRPObjects)
      {
      perception_string.append("    "+object.toString()+"\n");
      }

    perception_string.append("\n  modified deleted: \n");
    for(RPObject object: modifiedDeletedAttribsRPObjects)
      {
      perception_string.append("    "+object.toString()+"\n");
      }
    
    perception_string.append("\n  deleted: \n");
    for(RPObject object: deletedRPObjects)
      {
      perception_string.append("    "+object.toString()+"\n");
      }

    return perception_string.toString();
    }
  
  public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException
    {
    if(Logger.loggable("MessageS2CPerception::writeObject","D"))
      {
      Logger.trace("MessageS2CPerception::writeObject","D",this.toString());
      }
      
    super.writeObject(out);
    out.write(getPrecomputedStaticPartPerception());
    out.write(getDynamicPartPerception());      
    }
    
  private void setZoneid(RPObject object,String zoneid)
    {
    object.put("zoneid",zoneid);
    
    Iterator<RPSlot> it=object.slotsIterator();
    while(it.hasNext())
      {
      RPSlot slot=it.next();
      for(RPObject son: slot)
        {
        setZoneid(son,zoneid);
        }
      }    
    }
    
  public void readObject(marauroa.common.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);

    ByteArrayInputStream array=new ByteArrayInputStream(in.readByteArray());
    java.util.zip.InflaterInputStream szlib=new java.util.zip.InflaterInputStream(array,new java.util.zip.Inflater());
    InputSerializer ser=new InputSerializer(szlib);
    
    typePerception=ser.readByte();
    zoneid=(IRPZone.ID)ser.readObject(new IRPZone.ID(""));
    addedRPObjects=new LinkedList<RPObject>();
    deletedRPObjects=new LinkedList<RPObject>();
    modifiedAddedAttribsRPObjects=new LinkedList<RPObject>();
    modifiedDeletedAttribsRPObjects=new LinkedList<RPObject>();
    
    int added=ser.readInt();
    
    if(added>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(added)+" size");
      }
    Logger.trace("MessageS2CPerception::readObject()","D",added + " added objects..");
    for(int i=0;i<added;++i)
      {
      RPObject object=(RPObject)ser.readObject(new RPObject());
      setZoneid(object,zoneid.getID());
      addedRPObjects.add(object);
      }

    int modAdded=ser.readInt();
    
    if(modAdded>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(modAdded)+" size");
      }
    Logger.trace("MessageS2CPerception::readObject()","D",modAdded + " modified Added objects..");
    for(int i=0;i<modAdded;++i)
      {
      RPObject object=(RPObject)ser.readObject(new RPObject());
      setZoneid(object,zoneid.getID());
      modifiedAddedAttribsRPObjects.add(object);
      }

    int modDeleted=ser.readInt();
    
    if(modDeleted>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(modDeleted)+" size");
      }
    Logger.trace("MessageS2CPerception::readObject()","D",modDeleted + " modified Deleted objects..");
    for(int i=0;i<modDeleted;++i)
      {
      RPObject object=(RPObject)ser.readObject(new RPObject());
      setZoneid(object,zoneid.getID());
      modifiedDeletedAttribsRPObjects.add(object);
      }

    int del=ser.readInt();
    
    if(del>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(del)+" size");
      }
    Logger.trace("MessageS2CPerception::readObject()","D",del + " deleted objects..");
    for(int i=0;i<del;++i)
      {
      RPObject object=(RPObject)ser.readObject(new RPObject());
      setZoneid(object,zoneid.getID());
      deletedRPObjects.add(object);
      }
    
    
    /** Dynamic part */  
    array=new ByteArrayInputStream(in.readByteArray());
    ser=new InputSerializer(array);

    timestampPerception=ser.readInt();

    Logger.trace("MessageS2CPerception::readObject()","D","My RPObject");
    byte modifiedMyRPObject=ser.readByte();
    if(modifiedMyRPObject==1)
      {
      myRPObject=(RPObject)ser.readObject(myRPObject);
      }
    else
      {
      myRPObject=null;
      }
    }

  static class CachedCompressedPerception 
    {
    static class CacheKey
     {
      byte type;
      IRPZone.ID zoneid;
     
      public CacheKey(byte type, IRPZone.ID zoneid)
        {
        this.type=type;
        this.zoneid=zoneid;
        }
      
      public boolean equals(Object obj)
        {
        if(obj instanceof CacheKey)
          {
          CacheKey a=(CacheKey)obj;
          if(a.type==type && a.zoneid.equals(zoneid))
            {
            return true;
            }
          }
          
        return false;
        }
        
      public int hashCode()
        {
        return (type+1)*zoneid.hashCode();
        }
      }

    private Map<CacheKey,byte[]> cachedContent;
    
    private CachedCompressedPerception()
      {
      cachedContent=new HashMap<CacheKey,byte[]>();
      }
      
    static CachedCompressedPerception instance;
      
    synchronized static public CachedCompressedPerception get()
      {
      if(instance==null)
        {
        instance=new CachedCompressedPerception();
        }
       
      return instance;
      }
     
    synchronized public void clear()
      {
      cachedContent.clear();
      }
    
    synchronized public byte[] get(MessageS2CPerception perception) throws IOException
      {
      CacheKey key=new CacheKey(perception.typePerception, perception.zoneid);
      
      if(!cachedContent.containsKey(key))
        {
        Logger.trace("MessageS2CPerception::CachedCompressedPerception::get()","D","Perception not found in cache");
        ByteArrayOutputStream array=new ByteArrayOutputStream();
        DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
        OutputSerializer serializer=new OutputSerializer(out_stream);
        
        perception.computeStaticPartPerception(serializer);
        
        out_stream.close();
        byte[] content=array.toByteArray();
        
        cachedContent.put(key,content);
        }
      else
        {
        Logger.trace("MessageS2CPerception::CachedCompressedPerception::get()","D","Perception FOUND in cache");
        }
    
      return (byte[])cachedContent.get(key);
      } 
    }

  public static void clearPrecomputedPerception()
    {
    cache.clear();
    }
  
  private byte[] getPrecomputedStaticPartPerception() throws IOException
    {
    return cache.get(this);
    }

  private byte[] getDynamicPartPerception() throws IOException
    {
    ByteArrayOutputStream array=new ByteArrayOutputStream();
    OutputSerializer serializer=new OutputSerializer(array);

    serializer.write((int)timestampPerception);
    if(myRPObject==null)
      {
      serializer.write((byte)0);
      }
    else
      {
      serializer.write((byte)1);
      myRPObject.writeObject(serializer,true);
      }
      
    return array.toByteArray();
    }
  
  private void computeStaticPartPerception(OutputSerializer ser) throws IOException
    {
    ser.write((byte)typePerception);
    ser.write(zoneid);
    
    ser.write((int)addedRPObjects.size());
    for(RPObject object: addedRPObjects)
      {
      ser.write(object);
      }

    ser.write((int)modifiedAddedAttribsRPObjects.size());
    for(RPObject object: modifiedAddedAttribsRPObjects)
      {
      ser.write(object);
      }

    ser.write((int)modifiedDeletedAttribsRPObjects.size());
    for(RPObject object: modifiedDeletedAttribsRPObjects)
      {
      ser.write(object);
      }
    
    ser.write((int)deletedRPObjects.size());
    for(RPObject object: deletedRPObjects)
      {
      ser.write(object);
      }
    }
  }
