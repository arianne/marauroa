package marauroa.net;

import marauroa.net.*;
import java.io.*;
import java.lang.Byte;
import junit.framework.*;

public class Test_SerializerInt extends Test_Serializer
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_SerializerInt.class);
	}
	
  public void testInt()
    {
    Integer[] data=
      {
      new Integer((int)Integer.MIN_VALUE),
      new Integer((int)-50),
      new Integer((int)0),
      new Integer((int)-0),
      new Integer((int)50),
      new Integer((int)Integer.MAX_VALUE)
      };
    
    test(data);   
    }
 
  protected void write(OutputSerializer out, Object obj) throws IOException
    {
    out.write(((Integer)obj).intValue());
    }
    
  protected Object read(InputSerializer in) throws IOException, ClassNotFoundException
    {
    return new Integer(in.readInt());
    }
  
  protected boolean equals(Object a, Object b)
    {
    return ((Integer)a).equals((Integer)b);
    }
  }