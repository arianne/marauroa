/* $Id: MessageS2CTransfer.java,v 1.1 2005/01/23 21:00:44 arianne_rpg Exp $ */
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

import java.util.*;
import java.net.*;
import java.io.*;

public class MessageS2CTransfer extends Message
  {
  private List<TransferContent> contents;
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CTransfer()
    {
    super(null);
    type=TYPE_S2C_TRANSFER;
    }
  
  public MessageS2CTransfer(InetSocketAddress source,TransferContent content)
    {
    super(source);
    type=TYPE_S2C_TRANSFER;
    
    this.contents=new LinkedList<TransferContent>();
    contents.add(content);
    }
  
  public List<TransferContent> getContents()
    {
    return contents;
    }
  
  public String toString()
    {
    return "Message (S2C Transfer) from ("+source.getAddress().getHostAddress()+") CONTENTS: ("+contents.size()+")";
    }

  public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    
    int size=contents.size();
    out.write(size);
    
    for(TransferContent content: contents)
      {
      content.writeFULL(out);
      }    
    }
  
  public void readObject(marauroa.common.net.InputSerializer in) throws IOException, ClassNotFoundException
    {
    super.readObject(in);
    
    int size=in.readInt();
    contents=new LinkedList<TransferContent>();
      
    for(int i=0;i<size;i++)
      {
      TransferContent content=new TransferContent();
      content.readFULL(in);
      contents.add(content);
      }

    if(type!=TYPE_S2C_TRANSFER)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }
  }
