/* $Id: InetAddressMask.java,v 1.1 2004/05/27 22:44:11 root777 Exp $ */
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import marauroa.game.Util;

/**
 * works only for ip4 addresses
 */
public class InetAddressMask
{
  private byte address[];
  private byte mask[];
  
  /**
   * address in form aaa.bbb.ccc.ddd
   * mask    in form aaa.bbb.ccc.ddd
   */
  public InetAddressMask(String address, String mask)
  {
    this(string2bytes(address),string2bytes(mask));
  }
  
  public InetAddressMask(byte address[], byte mask[])
  {
    this.address = address;
    this.mask = mask;
    address[0]=(byte)(address[0]&mask[0]);
    address[1]=(byte)(address[1]&mask[1]);
    address[2]=(byte)(address[2]&mask[2]);
    address[3]=(byte)(address[3]&mask[3]);
  }
  
  public boolean matches(InetAddress another_address)
  {
    byte ob[] = another_address.getAddress();
    ob[0]=(byte)(ob[0]&mask[0]);
    ob[1]=(byte)(ob[1]&mask[1]);
    ob[2]=(byte)(ob[2]&mask[2]);
    ob[3]=(byte)(ob[3]&mask[3]);
    
    boolean ret = true;
    ret = ret && (ob[3]^address[3]) == 0;
    ret = ret && (ob[2]^address[2]) == 0;
    ret = ret && (ob[1]^address[1]) == 0;
    ret = ret && (ob[0]^address[0]) == 0;
    //this kind of evaluation allows better debugging.
    //this can be rewritten later to
    //ret = (ob[3]^address[3]) == 0 && (ob[2]^address[2]) == 0 && (ob[1]^address[1]) == 0 && (ob[0]^address[0]) == 0;
    //which must be performanter - but no one knows one what part exactly was false and what part was true...
    
    return(ret);
  }
  
  public String toString()
  {
    try
    {
      return(InetAddress.getByAddress(address).getHostAddress()+"/"+InetAddress.getByAddress(mask).getHostAddress());
    }
    catch (UnknownHostException e) {return("");}
  }
  
  /**
   * converts string in form aaa.bbb.ccc.ddd into byte array[]{aaa,bbb,ccc,ddd}
   */
  public static byte[] string2bytes(String ipv4Address)
  {
    String [] str_bytes = ipv4Address.split("\\.");
    byte addr[] = new byte[4];
    addr[0]=addr[1]=addr[2]=addr[3]=0;
    for (int i = 0; i < str_bytes.length && i<4; i++)
    {
      addr[i]=(byte)Integer.parseInt(str_bytes[i]);
    }
    return(addr);
  }
  
  public static void main(String argv[])
  {
    try
    {
      InetAddress id = InetAddress.getLocalHost();
      InetAddressMask iam = new InetAddressMask("192.168.100.15","255.255.255.0");
      byte addr [] = id.getAddress();
      addr[3]++;
      InetAddress id2 = InetAddress.getByAddress(addr);
      System.out.println(id.getHostAddress());
      System.out.println(id2.getHostAddress());
      System.out.println(id2.getHostAddress() +" is "+(iam.matches(id2)?" matched ":" not matched ")+"by " + iam);
      int count = 100;
      boolean x = true;
      long start = System.currentTimeMillis();
      for (int i = 0; i < count; i++)
      {
        x=x&&iam.matches(id2);
      }
      long duration = System.currentTimeMillis()-start;
      System.out.println("Duration: "+duration + " for "+count+" calls.");
      
      System.out.println((duration>0?""+count/duration:"more then "+count)+" calls per ms.");
      System.out.println(x?"OK":"NOK");
    }
    catch (UnknownHostException e)
    {
      e.printStackTrace();
    }
  }
}

