package marauroa.net;

import marauroa.net.*;
import java.io.*;
import java.lang.Byte;
import junit.framework.*;

public class Test_SerializerByte extends Test_Serializer
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_SerializerByte.class);
	}
	
  public void testByte()
    {
    Byte[] data=
      {
      new Byte((byte)Byte.MIN_VALUE),
      new Byte((byte)-50),
      new Byte((byte)0),
      new Byte((byte)-0),
      new Byte((byte)50),
      new Byte((byte)Byte.MAX_VALUE)
      };
    
    test(data);   
    }
 
  protected void write(OutputSerializer out, Object obj) throws IOException
    {
    out.write(((Byte)obj).byteValue());
    }
    
  protected Object read(InputSerializer in) throws IOException, ClassNotFoundException
    {
    return new Byte(in.readByte());
    }
  
  protected boolean equals(Object a, Object b)
    {
    return ((Byte)a).equals((Byte)b);
    }
  }