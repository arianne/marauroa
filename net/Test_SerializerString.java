package marauroa.net;

import marauroa.net.*;
import java.io.*;
import java.lang.Byte;
import junit.framework.*;

public class Test_SerializerString extends Test_Serializer
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_SerializerString.class);
	}
	
  public void testString()
    {
    String[] data=
      {
      new String(),
      new String("marauroa"),
      new String("espa—a"),
      new String("GNU"),
      new String("java"),
      new String("Miguel Angel Blanch Lardin")
      };
    
    test(data);   
    }
 
  protected void write(OutputSerializer out, Object obj) throws IOException
    {
    out.write((String)obj);
    }
    
  protected Object read(InputSerializer in) throws IOException, ClassNotFoundException
    {
    return new String(in.readString());
    }
  
  protected boolean equals(Object a, Object b)
    {
    return ((String)a).equals((String)b);
    }
  }