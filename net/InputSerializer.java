package marauroa.net;

import java.io.*;

/** ImputSerializer is used to serialize classes that implement the Serializable 
 *  interface into a InputStream.
 *  
 * @author Miguel Angel Blanch Lardin
 */
public class InputSerializer
  {
  private InputStream in;
  
  /** Constructor that pass the InputStream to the serializer 
      @param in the InputStream */
  public InputSerializer(InputStream in)
    {
    this.in=in;
    }
  
  /** This method serialize an object that implements the interface Serializable
      allowing to implement this behaviour in several classes
      @param obj the object were we will serialize the data
      @return the object serialized, just for interface coherence */
  public Object readObject(marauroa.net.Serializable obj) throws IOException, java.lang.ClassNotFoundException
    {
    obj.readObject(this);
    return obj;
    }
    
  public byte readByte() throws IOException, java.lang.ClassNotFoundException
    {
    int result=in.read();
    if(result<0)
      {
      throw new IOException();
      }
    return (byte)result;
    }
    
  public byte[] readByteArray() throws IOException, java.lang.ClassNotFoundException
    {
    int size=readInt();
    byte[] buffer=new byte[size];
    
    in.read(buffer);
    
    return buffer;
    }

  public short readShort() throws IOException, java.lang.ClassNotFoundException
    {
    byte[] data=new byte[2];
    
    int result=in.read(data);
    if(result<0)
      {
      throw new IOException();
      }
      
    result=data[0]&0xFF;
    result+=(data[1]&0xFF) << 8;
    return (short)result;
    }

  public int readInt() throws IOException, java.lang.ClassNotFoundException
    {
    byte[] data=new byte[4];
    int result=in.read(data);
    if(result<0)
      {
      throw new IOException();
      }
      
    result=data[0]&0xFF;
    result+=(data[1]&0xFF) << 8;
    result+=(data[2]&0xFF) << 16;
    result+=(data[3]&0xFF) << 24;
    return result;
    }
  
  public String readString() throws IOException, java.lang.ClassNotFoundException
    {
    /** TODO: Fix to use UTF charset */
    return new String(readByteArray());
    }

  public String[] readStringArray() throws IOException, java.lang.ClassNotFoundException
    {
    int size=readInt();
    
    String[] buffer=new String[size];    
    for(int i=0;i<size;i++)
      {
      buffer[i]=readString();
      }    
    
    return buffer;
    }
  };
