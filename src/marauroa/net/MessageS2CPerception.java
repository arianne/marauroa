/* $Id: MessageS2CPerception.java,v 1.47 2004/05/28 07:54:30 arianne_rpg Exp $ */
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
  private byte typePerception;

  private List addedRPObjects;
  private List modifiedAddedAttribsRPObjects;
  private List modifiedDeletedAttribsRPObjects;
  private List deletedRPObjects;
    
  private int timestampPerception;
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
    Iterator it;
      
    StringBuffer perception_string=new StringBuffer();
    perception_string.append("Type: "+typePerception+" Timestamp: "+timestampPerception+") contents: ");

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

    return perception_string.toString();
    }
  
  private static byte[] precomputed_StaticPartPerception=null;
  private static long precomputed_StaticPartSavedBytes=0;
  
  public static void clearPrecomputedPerception()
    {
    precomputed_StaticPartPerception=null;
    precomputed_StaticPartSavedBytes=0;
    }
  
  private byte[] getPrecomputedStaticPartPerception() throws IOException
    {
    if(precomputed_StaticPartPerception==null)
      {      
      ByteArrayOutputStream array=new ByteArrayOutputStream();
      ByteCounterOutputStream out_stream = new ByteCounterOutputStream(new DeflaterOutputStream(array));
      OutputSerializer serializer=new OutputSerializer(out_stream);
      
      computeStaticPartPerception(serializer);
      
      out_stream.close();
      precomputed_StaticPartPerception=array.toByteArray();
      
      precomputed_StaticPartSavedBytes=out_stream.getBytesWritten()-precomputed_StaticPartPerception.length;
      }
    
    Statistics.getStatistics().addBytesSaved(precomputed_StaticPartSavedBytes);
    return precomputed_StaticPartPerception;
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
    }
  
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write(getPrecomputedStaticPartPerception());
    out.write(getDynamicPartPerception());      
    }
  
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    
    ByteArrayInputStream array=new ByteArrayInputStream(in.readByteArray());
    java.util.zip.InflaterInputStream szlib=new java.util.zip.InflaterInputStream(array,new java.util.zip.Inflater());
    InputSerializer ser=new InputSerializer(szlib);
    
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
    
    
    /** Dynamic part */  
    array=new ByteArrayInputStream(in.readByteArray());
    ser=new InputSerializer(array);

    timestampPerception=ser.readInt();

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
  final static class ByteCounterOutputStream
    extends OutputStream
    {
    OutputStream os;
    long bytesWritten;
    public ByteCounterOutputStream(OutputStream os)
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

    public void write(byte[] b) throws IOException
      {
      os.write(b);
      bytesWritten+=b.length;
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
  }
