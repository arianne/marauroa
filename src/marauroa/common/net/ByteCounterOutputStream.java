/* $Id: ByteCounterOutputStream.java,v 1.2 2005/11/01 10:09:29 mtotz Exp $ */
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
package marauroa.common.net;
  
import java.io.*;
  
/** This class just counts the bytes written into underlaying outputstream */
public class ByteCounterOutputStream extends OutputStream
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
  