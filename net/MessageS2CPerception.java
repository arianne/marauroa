package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
import java.util.*;

import marauroa.game.*;

public class MessageS2CPerception extends Message
  {  
  private List modifiedRPObjects;
  private List deletedRPObjects;
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CPerception()
    {
    super(null);
    
    type=TYPE_S2C_PERCEPTION;
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param 
   *  @see marauroa.net.MessageS2CCharacters
   */
  public MessageS2CPerception(InetSocketAddress source,List modifiedRPObjects, List deletedRPObjects)
    {    
    super(source);
    
    type=TYPE_S2C_PERCEPTION;
    this.modifiedRPObjects=modifiedRPObjects;
    this.deletedRPObjects=deletedRPObjects;
    }  
  

  public String toString()
    {
    return "Message (S2C Perception) from ("+source.toString()+") CONTENTS: (TODO)";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    
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


  