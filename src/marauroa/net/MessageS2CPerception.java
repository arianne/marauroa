/* $Id: MessageS2CPerception.java,v 1.33 2004/04/21 14:38:02 arianne_rpg Exp $ */
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
package marauroa.net;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import marauroa.game.*;
import marauroa.*;

/** This message indicate the client the objects that the server has determined that
 *  this client is able to see.
 *
 *  @see marauroa.net.Message
 *  @see marauroa.game.RPZone
 */
public class MessageS2CPerception extends Message
  {
  private int timestamp;
  private byte typePerception;

  private List addedRPObjects;
  private List modifiedAddedAttribsRPObjects;
  private List modifiedDeletedAttribsRPObjects;
  private List deletedRPObjects;
    
  private RPObject myRPObject;
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CPerception()
    {
    super(null);
    type=TYPE_S2C_PERCEPTION;
    
    myRPObject=new RPObject();
    }
  
  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param modifiedRPObjects the list of object that has been modified.
   *  @param deletedRPObjects the list of object that has been deleted since the last perception.
   */
  public MessageS2CPerception(InetSocketAddress source,RPZone.Perception perception)
    {
    super(source);
    type=TYPE_S2C_PERCEPTION;
    
    typePerception=perception.type;
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
  
  public void setTimestamp(int timestamp)
    {
    this.timestamp=timestamp;
    }
  
  public int getTimestamp()
    {
    return timestamp;
    }
  
  
  public byte getTypePerception()
    {
    return typePerception;
    }
  
  /** This method returns the list of modified objects
   *  @return List<RPObject> of added objects */
  public List getAddedRPObjects()
    {
    return addedRPObjects;
    }

  /** This method returns the list of modified objects
   *  @return List<RPObject> of modified objects that has attributes added*/
  public List getModifiedAddedRPObjects()
    {
    return modifiedAddedAttribsRPObjects;
    }

  /** This method returns the list of modified objects
   *  @return List<RPObject> of modified objects that has attributes removed*/
  public List getModifiedDeletedRPObjects()
    {
    return modifiedDeletedAttribsRPObjects;
    }
  
  /** This method returns the list of deleted objects
   *  @return List<RPObject> of deleted objects */
  public List getDeletedRPObjects()
    {
    return deletedRPObjects;
    }
  
  /** This method returns a String that represent the object
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (S2C Perception) from ("+source.toString()+") CONTENTS: ("+addedRPObjects.size()+
      " added objects, "+modifiedAddedAttribsRPObjects.size()+" modified Added attributes objects, "+
      modifiedDeletedAttribsRPObjects.size()+" modified Deleted attributes objects and "+
      deletedRPObjects.size()+" deleted objects)";
    }
  
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    
    if(marauroad.loggable("MessageS2CPerception::writeObject","D"))     
      {
      Iterator it;
      
      StringBuffer perception_string=new StringBuffer();
      perception_string.append("Type: "+typePerception+" Timestamp: "+timestamp+") contents: ");

      perception_string.append("\n  added: \n");
      it=addedRPObjects.iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        perception_string.append("    "+object.toString()+"\n");
        }

      perception_string.append("\n  modified added: \n");
      it=modifiedAddedAttribsRPObjects.iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        perception_string.append("    "+object.toString()+"\n");
        }

      perception_string.append("\n  modified deleted: \n");
      it=modifiedDeletedAttribsRPObjects.iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        perception_string.append("    "+object.toString()+"\n");
        }
    
      perception_string.append("\n  deleted: \n");
      it=deletedRPObjects.iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        perception_string.append("    "+object.toString()+"\n");
        }
    
      marauroad.trace("MessageS2CPerception::writeObject","D",perception_string.toString());
      }

    ByteArrayOutputStream compressed_array=new ByteArrayOutputStream();
    ByteCounterOutputStream out_stream = new ByteCounterOutputStream(new DeflaterOutputStream(compressed_array));
    OutputSerializer ser=new OutputSerializer(out_stream);
    
    ser.write((int)timestamp);    
    ser.write((byte)typePerception);
    
    Iterator it=null;
    
    it=addedRPObjects.iterator();
    ser.write((int)addedRPObjects.size());
    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();
      ser.write(object);
      }

    ser.write((int)modifiedAddedAttribsRPObjects.size());
    it=modifiedAddedAttribsRPObjects.iterator();
    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();
      ser.write(object);
      }

    ser.write((int)modifiedDeletedAttribsRPObjects.size());
    it=modifiedDeletedAttribsRPObjects.iterator();
    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();
      ser.write(object);
      }
    
    ser.write((int)deletedRPObjects.size());
    it=deletedRPObjects.iterator();
    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();
      ser.write(object);
      }
    
    if(myRPObject==null)
      {
      ser.write((byte)0);
      }
    else
      {
      ser.write((byte)1);
      myRPObject.writeObject(ser,true);
      }
      
    out_stream.close();

    byte [] array = compressed_array.toByteArray();
    long saved = out_stream.getBytesWritten()-array.length;

    Statistics.getStatistics().addBytesSaved(saved);
    out.write(array);
    }
  
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    
    ByteArrayInputStream array=new ByteArrayInputStream(in.readByteArray());
    java.util.zip.InflaterInputStream szlib=new java.util.zip.InflaterInputStream(array,new java.util.zip.Inflater());
    InputSerializer ser=new InputSerializer(szlib);
    
    timestamp=ser.readInt();
    typePerception=ser.readByte();
    addedRPObjects=new LinkedList();
    deletedRPObjects=new LinkedList();
    modifiedAddedAttribsRPObjects=new LinkedList();
    modifiedDeletedAttribsRPObjects=new LinkedList();
    
    int added=ser.readInt();
    
    if(added>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(added)+" size");
      }
    marauroad.trace("MessageS2CPerception::readObject()","D",added + " added objects..");
    for(int i=0;i<added;++i)
      {
      addedRPObjects.add(ser.readObject(new RPObject()));
      }    

    int modAdded=ser.readInt();
    
    if(modAdded>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(modAdded)+" size");
      }
    marauroad.trace("MessageS2CPerception::readObject()","D",modAdded + " modified Added objects..");
    for(int i=0;i<modAdded;++i)
      {
      modifiedAddedAttribsRPObjects.add(ser.readObject(new RPObject()));
      }

    int modDeleted=ser.readInt();
    
    if(modDeleted>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(modDeleted)+" size");
      }
    marauroad.trace("MessageS2CPerception::readObject()","D",modDeleted + " modified Deleted objects..");
    for(int i=0;i<modDeleted;++i)
      {
      modifiedDeletedAttribsRPObjects.add(ser.readObject(new RPObject()));
      }

    int del=ser.readInt();
    
    if(del>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(del)+" size");
      }
    marauroad.trace("MessageS2CPerception::readObject()","D",del + " deleted objects..");
    for(int i=0;i<del;++i)
      {
      deletedRPObjects.add(ser.readObject(new RPObject()));
      }
      
    marauroad.trace("MessageS2CPerception::readObject()","D","My RPObject");
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
    
  /** This class just counts the bytes written into underlaying outputstream */
  private final static class ByteCounterOutputStream
    extends OutputStream
    {
    OutputStream os;
    long bytesWritten;
    private ByteCounterOutputStream(OutputStream os)
      {
      if(os==null) throw new NullPointerException("OutputStream is null!!!");
      this.os = os;
      bytesWritten=0;
      }

    public void write(int b) throws IOException
      {
      os.write(b);
      bytesWritten++;
      }

    public long getBytesWritten()
      {
      return(bytesWritten);
      }
    
    public void flush() throws IOException
      {
      os.flush();
      }
    
    public void close() throws IOException
      {
      os.close();
      }
    }
  
  static public class OutOfSyncException extends Exception
    {
    public OutOfSyncException()
      {
      super("Out of sync Exception");
      }
    }
  
  /** This method applys perceptions to the Map<RPObject::ID,RPObject> passed as argument. 
   *  It may clear the map if it is a sync perception and it will add the perception to 
   *  unattended perceptions if it is out of sync, in the hope that a soon to come perception
   *  help us to fix the situation. 
   *  This method returns this perception timestamp. */
  public int applyPerception(Map world, int previous, List unattended_perceptions) throws OutOfSyncException
    {
    try
      {
      if(previous+1==timestamp)
        { 
        applyPerceptionDeletedRPObjects(world);
        applyPerceptionModifiedRPObjects(world);
        applyPerceptionAddedRPObjects(world);
        
        if(myRPObject!=null)
          {
          world.put(myRPObject.get("id"),myRPObject);
          }
        }        
      else
        {
        /* Out of Sync */
        /* TODO: Handle out of order perceptions 
        unattended_perceptions.add(this);
        */
        throw new OutOfSyncException();
        }
      }
    catch(Exception e)
      {
      e.printStackTrace();
      marauroad.trace("MessageS2CPerception::applyPerception","X",e.getMessage());
      throw new OutOfSyncException();
      }

    return timestamp;
    }


  /** This method applys perceptions addedto the Map<RPObject::ID,RPObject> passed as argument.
   *  It clears the map if this is a sync perception */
  public void applyPerceptionAddedRPObjects(Map world) throws RPZone.RPObjectNotFoundException
    {
    try
      {
      Iterator it;
      
      if(typePerception==RPZone.Perception.SYNC)
        {
        world.clear();
        }
    
      it=getAddedRPObjects().iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        world.put(object.get("id"),object);    
        }
      }
    catch(Exception e)
      {
      marauroad.trace("MessageS2CPerception::applyPerceptionAddedRPObjects","X",e.getMessage());
      throw new RPZone.RPObjectNotFoundException(new RPObject.ID(-1));
      }
    }

  /** This method applys perceptions deleted to the Map<RPObject::ID,RPObject> passed as argument. */
  public void applyPerceptionDeletedRPObjects(Map world) throws RPZone.RPObjectNotFoundException
    {
    try
      {
      Iterator it;
    
      it=getDeletedRPObjects().iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        world.remove(object.get("id"));    
        /** TODO: Check that object exists */
        }
      }
    catch(Exception e)
      {
      marauroad.trace("MessageS2CPerception::applyPerceptionDeletedRPObjects","X",e.getMessage());
      throw new RPZone.RPObjectNotFoundException(new RPObject.ID(-1));
      }
    }

  /** This method applys perceptions modified added and modified deleted to the
   *  Map<RPObject::ID,RPObject> passed as argument. */
  public void applyPerceptionModifiedRPObjects(Map world) throws RPZone.RPObjectNotFoundException
    {
    try
      {
      Iterator it;
    
      it=getModifiedDeletedRPObjects().iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        RPObject w_object=(RPObject)world.get(object.get("id"));    
        w_object.applyDifferences(null,object);        
        }

      it=getModifiedAddedRPObjects().iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        RPObject w_object=(RPObject)world.get(object.get("id"));    
        w_object.applyDifferences(object,null);        
        }
      }
    catch(RPZone.RPObjectNotFoundException e)
      {
      marauroad.trace("MessageS2CPerception::applyModifiedRPObjects","X",e.getMessage());
      throw e;
      }
    catch(Exception e)
      {
      e.printStackTrace();
      marauroad.trace("MessageS2CPerception::applyModifiedRPObjects","X",e.getMessage());
      throw new RPZone.RPObjectNotFoundException(new RPObject.ID(-1));
      }
    }
  }
