/* $Id: TransferContent.java,v 1.1 2004/08/29 11:12:42 arianne_rpg Exp $ */
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
package marauroa.net;

import java.io.*;

public class TransferContent
  {
  public String name;
  public int timestamp;
  public byte[] data;
  public boolean cacheable;
  public boolean ack;
  
  public TransferContent()
    {
    }
    
  public TransferContent(String name, int timestamp, byte[] data)
    {
    this.name=name;
    this.timestamp=timestamp;
    this.data=data;
    cacheable=true;
    ack=false;      
    }

  public void writeREQ(marauroa.net.OutputSerializer out) throws IOException
    {
    out.write(name);
    out.write(timestamp);
    out.write((byte)(cacheable?1:0));
    }
  
  public void readREQ(marauroa.net.InputSerializer in) throws IOException, ClassNotFoundException
    {
    name=in.readString();
    timestamp=in.readInt();      
    cacheable=(in.readByte()==1);      
    }
  
  public void writeACK(marauroa.net.OutputSerializer out) throws IOException
    {
    out.write(name);
    out.write((byte)(ack?1:0));
    }
  
  public void readACK(marauroa.net.InputSerializer in) throws IOException, ClassNotFoundException
    {
    name=in.readString();
    ack=(in.readByte()==1);      
    }
  
  public void writeFULL(marauroa.net.OutputSerializer out) throws IOException
    {
    out.write(name);
    out.write(data);
    out.write((byte)(cacheable?1:0));
    }
  
  public void readFULL(marauroa.net.InputSerializer in) throws IOException, ClassNotFoundException
    {
    name=in.readString();
    data=in.readByteArray();      
    cacheable=(in.readByte()==1);      
    }
  }
  