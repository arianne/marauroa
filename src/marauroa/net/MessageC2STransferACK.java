/* $Id: MessageC2STransferACK.java,v 1.4 2004/11/25 19:34:11 arianne_rpg Exp $ */
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

import java.util.*;
import java.net.*;
import java.io.*;

public class MessageC2STransferACK extends Message
  {
  private List<TransferContent> contents;
  
  /** Constructor for allowing creation of an empty message */
  public MessageC2STransferACK()
    {
    super(null);
    type=TYPE_C2S_TRANSFER_ACK;
    }
  
  public MessageC2STransferACK(InetSocketAddress source,List<TransferContent> content)
    {
    super(source);
    type=TYPE_C2S_TRANSFER_ACK;
    
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

  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    
    int size=contents.size();
    out.write(size);
    
    for(TransferContent content: contents)
      {
      content.writeACK(out);
      }    
    }
  
  public void readObject(marauroa.net.InputSerializer in) throws IOException, ClassNotFoundException
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

    if(type!=TYPE_C2S_TRANSFER_ACK)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }
  }
