/* $Id: Statistics.java,v 1.2 2004/01/29 19:57:35 arianne_rpg Exp $ */
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
package marauroa;

import java.util.*;
import java.io.*;

public class Statistics
  {
  private static Date startTime=new Date();
  
  private static long bytesRecv;
  private static long bytesSend;
  
  public static void addBytesRecv(long bytes)
    {
    bytesRecv+=bytes;
    }

  public static void addBytesSend(long bytes)
    {
    bytesSend+=bytes;
    }

  public static void print(PrintStream out)
    {
    Date actualTime=new Date();
    double diff=(actualTime.getTime()-startTime.getTime())/1000;
    
    out.println("Bytes RECV: "+String.valueOf(bytesRecv));
    out.println("Bytes RECV (avg secs): "+String.valueOf(bytesRecv/diff));
    out.println("Bytes SEND: "+String.valueOf(bytesSend));
    out.println("Bytes SEND (avg secs): "+String.valueOf(bytesSend/diff));
    } 
  }
