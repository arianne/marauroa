package marauroa.net;

import marauroa.net.*;
import java.io.*;
import java.lang.Byte;
import junit.framework.*;

public class Test_SerializerByteArray extends TestCase
  {
  private ByteArrayOutputStream out;
  private ByteArrayInputStream in;
  
  private InputSerializer sin;
  private OutputSerializer sout;

  public static Test suite ( ) 
    {
    return new TestSuite(Test_SerializerByteArray.class);
	}
	
  public void testByteArray()
    {
    byte[] data=new byte[256];
    for(int i=0;i<data.length;i++) data[i]=(byte)(java.lang.Math.random()*256-127);
    
    out=new ByteArrayOutputStream();
    sout=new OutputSerializer(out);
    
    try
      {
      sout.write(data);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data out");
      }
    
    byte[] result=null;
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    try
      {
      result=sin.readByteArray();
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(data.length,result.length);
    
    for(int i=0;i<data.length;i++)
      {
      assertTrue(data[i]==result[i]);    
      }
	}  
  }