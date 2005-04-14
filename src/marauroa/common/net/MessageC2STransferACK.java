/* $Id: MessageC2STransferACK.java,v 1.2 2005/04/14 09:59:06 quisar Exp $ */
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

public class MessageC2STransferACK extends Message
  {
  private List<TransferContent> contents;
  
  /** Constructor for allowing creation of an empty message */
  public MessageC2STransferACK()
    {
    super(MessageType.C2S_TRANSFER_ACK,null);
    }
  
  public MessageC2STransferACK(InetSocketAddress source,List<TransferContent> content)
    {
    super(MessageType.C2S_TRANSFER_ACK,source);
    
    this.contents=content;
    }
  
  public List<TransferContent> getContents()
    {
    return contents;
    }
  
  public String toString()
    {
    return "Message (C2S Transfer ACK) from ("+source.getAddress().getHostAddress()+") CONTENTS: ("+contents.size()+")";
    }

  public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    
    int size=contents.size();
    out.write(size);
    
    for(TransferContent content: contents)
      {
      content.writeACK(out);
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
      content.readACK(in);
      contents.add(content);
      }

    if(type!=MessageType.C2S_TRANSFER_ACK)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }
  }
