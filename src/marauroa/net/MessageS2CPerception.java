package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
import java.util.*;

import marauroa.game.*;


/** This message indicate the client the objects that the server has determined that
 *  this client is able to see.
 *
 *  @see marauroa.net.Message
 *  @see marauroa.game.RPZone
 */
public class MessageS2CPerception extends Message
  {  
  private byte typePerception;
  private List modifiedRPObjects;
  private List deletedRPObjects;
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CPerception()
    {
    super(null);
    
    /** TODO: Make this choosable */
    typePerception=RPZone.Perception.TOTAL;
    
    type=TYPE_S2C_PERCEPTION;
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param modifiedRPObjects the list of object that has been modified.
   *  @param deletedRPObjects the list of object that has been deleted since the last perception.
   */
  public MessageS2CPerception(InetSocketAddress source,List modifiedRPObjects, List deletedRPObjects)
    {    
    super(source);
    
    type=TYPE_S2C_PERCEPTION;

    /** TODO: Make this choosable */
    typePerception=RPZone.Perception.TOTAL;
    this.modifiedRPObjects=modifiedRPObjects;
    this.deletedRPObjects=deletedRPObjects;
    }
  
  public List getModifiedRPObjects() 
    {
    return modifiedRPObjects;
    }  

  public List getDeletedRPObjects() 
    {
    return deletedRPObjects;
    }  
  
  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (S2C Perception) from ("+source.toString()+") CONTENTS: ("+modifiedRPObjects.size()+" modified objects and "+
           deletedRPObjects.size()+" deleted objects)";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    
    out.write(typePerception);    
    out.write(modifiedRPObjects.size());
    
    Iterator it_mod=modifiedRPObjects.iterator();
    while(it_mod.hasNext())
      {
      out.write((RPObject)it_mod.next());
      }

    out.write(deletedRPObjects.size());

    Iterator it_del=deletedRPObjects.iterator();
    while(it_del.hasNext())
      {
      out.write((RPObject)it_del.next());
      }
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    
    typePerception=in.readByte();
    modifiedRPObjects=new LinkedList();
    deletedRPObjects=new LinkedList();
    
    int mod=in.readInt();
    
    for(int i=0;i<mod;++i)
      {
      RPObject tmp=new RPObject(); 
      tmp.readObject(in);
      modifiedRPObjects.add(tmp);
      }

    int del=in.readInt();
    
    for(int i=0;i<del;++i)
      {
      RPObject tmp=new RPObject(); 
      tmp.readObject(in);
      deletedRPObjects.add(tmp);
      }
    }    
  };


  