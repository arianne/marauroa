package marauroa.net;

import java.io.*;

public class OutputSerializer
  {
  OutputStream out;
  
  public OutputSerializer(OutputStream out)
    {
    this.out=out;
    }
    
  public void write(marauroa.net.Serializable obj) throws IOException
    {
    obj.writeObject(this);
    }

  public void write(byte a) throws IOException
    {
    out.write(a);
    }
  
  public void write(byte[] a) throws IOException
    {
    write((int)a.length);
    out.write(a);
    }

  public void write(short a) throws IOException
    {
    int tmp;
    
    tmp=a&0xFF;
    out.write(tmp);
    tmp=(a>>8)&0xFF;
    out.write(tmp);
    }

  public void write(int a) throws IOException
    {
    int tmp;
    
    tmp=a&0xFF;
    out.write(tmp);
    tmp=(a>>8)&0xFF;
    out.write(tmp);
    tmp=(a>>16)&0xFF;
    out.write(tmp);
    tmp=(a>>24)&0xFF;
    out.write(tmp);
    }
  
  public void write(String a) throws IOException
    {
    /** TODO: Fix to use UTF charset */
    write(a.getBytes());
    }

  public void write(String[] a) throws IOException
    {
    write(a.length);
    for(int i=0;i<a.length;i++) write(a[i]);
    }
  };

