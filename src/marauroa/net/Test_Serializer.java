package marauroa.net;

import junit.framework.*;
import marauroa.net.*;
import marauroa.*;
import java.io.*;

abstract class Test_Serializer extends TestCase
  {
  private ByteArrayOutputStream out;
  private ByteArrayInputStream in;
  
  private InputSerializer sin;
  private OutputSerializer sout;

  protected abstract void write(OutputSerializer out, Object obj) throws IOException;
  protected abstract Object read(InputSerializer in) throws IOException, ClassNotFoundException;
  protected abstract boolean equals(Object a, Object b);
	
  protected void test(Object[] data) 
    {
    marauroad.trace("Test_Serializer::test",">");

    out=new ByteArrayOutputStream();
    sout=new OutputSerializer(out);
    
    try
      {
      for(int i=0;i<data.length;i++)
        {
        write(sout,data[i]);
        }
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data out");
      }
    
    Object[] result=new Object[data.length];
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    try
      {
      for(int i=0;i<data.length;i++)
        {
        result[i]=read(sin);
        }
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    for(int i=0;i<data.length;i++)
      {
      assertTrue(equals(data[i],result[i]));    
      }

    marauroad.trace("Test_Serializer::test","<");
	}  
  }
  