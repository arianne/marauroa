/* $Id: createaccount.java,v 1.31 2004/11/12 15:39:15 arianne_rpg Exp $ */
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

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import marauroa.game.*;

/** This is the base class to extend in order to create an account.
 *  The class provide a few methods of which you inherit and implement. */
public abstract class createaccount
  {
  /** This class store some basic information about the type of param, its name
   *  it default value and the allowed size ( > min and < max ) */
  public static class Information
    {
    public String param;
    public String name;
    public String value;
    public int min;
    public int max;
    
    
    public Information(String param, String name)
      {
      this.param=param;
      this.name=name;
      this.value="";
      this.max=256;
      }

    public Information(String param, String name,int min, int max)
      {
      this.param=param;
      this.name=name;
      this.value="";
      this.min=min;
      this.max=max;
      }
    }
    
  protected List<Information> information;
  
  public createaccount()
    {
    information= new LinkedList<Information>();
    
    information.add(new Information("-u","username",4,20));
    information.add(new Information("-p","password",4,256));
    information.add(new Information("-e","email"));
    information.add(new Information("-c","character",4,20));
    }
  
  protected String get(String name) throws AttributeNotFoundException
    {
    for(Iterator it=information.iterator();it.hasNext();)
      {
      Information item=(Information)it.next();
      
      if(item.name.equals(name))
        {
        return item.value;
        }
      }
    
    throw new AttributeNotFoundException(name);
    }

  /** Implement this method on the subclass in order to create an object that will be 
   *  inserted into the database by the createaccount class. You are given a playerDatabase
   *  instance so that you can get valid rpobjects' ids */    
  public abstract RPObject populatePlayerRPObject(IPlayerDatabase playerDatabase) throws Exception;

  protected int run(String[] args)
    {
    /** TODO: Factorize this method */
    int i=0;

    while(i!=args.length)
      {
      for(Iterator it=information.iterator();it.hasNext();)
        {
        Information item=(Information)it.next();
        
        if(args[i].equals(item.param))
          {
          item.value=args[i+1];
          System.out.println(item.name+"="+item.value);
          break;
          }
        }
      
      if(args[i].equals("-h"))
        {
        System.out.println("createaccount application for Marauroa");
        for(Iterator it=information.iterator();it.hasNext();)
          {
          Information item=(Information)it.next();          
          System.out.println(item.param+" to use/add "+item.name);
          }
          
        System.exit(0);
        }
        
      ++i;
      }
    
    Transaction trans=null;
    PrintWriter out=null;
      
    try
      {
      Configuration conf=Configuration.getConfiguration();
      String webfolder=conf.get("server_logs_directory");

      out=new PrintWriter(new FileOutputStream(webfolder+"/createaccount_log.txt",true));
      out.println(new Date().toString()+": Trying to create username("+get("username")+"), password("+get("password")+"), character("+get("character")+")");
      
      JDBCPlayerDatabase playerDatabase=(JDBCPlayerDatabase)PlayerDatabaseFactory.getDatabase("JDBCPlayerDatabase");
      trans=playerDatabase.getTransaction();
      
      out.println("Checking for null/empty string");
      for(Iterator it=information.iterator();it.hasNext();)
        {
        Information item=(Information)it.next();
       
        if(item.value.equals(""))
          {        
          out.println("String is empty or null: "+item.name);
          return 1;
          }
        }      

      out.println("Checking for valid string");
      for(Iterator it=information.iterator();it.hasNext();)
        {
        Information item=(Information)it.next();
        
        if(!playerDatabase.validString(item.value))
          {
          out.println("String not valid: "+item.name);
          return 2;
          }
        }

      out.println("Checking string size");
      for(Iterator it=information.iterator();it.hasNext();)
        {
        Information item=(Information)it.next();
        if(item.value.length()>item.max || item.value.length()<item.min)
          {
          out.println("String size not valid: "+item.name);
          return 3;
          }
        }

      out.println("Checking if player exists");
      if(playerDatabase.hasPlayer(trans, get("username")))
        {
        out.println("ERROR: Player exists");
        return 4;
        }
        
      out.println("Adding player");
      playerDatabase.addPlayer(trans,get("username"),get("password"),get("email"));

      RPObject object=populatePlayerRPObject(playerDatabase);
      
      playerDatabase.addCharacter(trans, get("username"),get("character"),object);
      out.println("Correctly created");
      trans.commit();
      }
    catch(Exception e)
      {
      if(out!=null)
        {
        out.println("Failed: "+e.getMessage());
        e.printStackTrace(out);
      
        try
          {
          trans.rollback();
          }
        catch(Exception ae)
          {
          out.println("Failed Rollback: "+ae.getMessage());
          }
        }
      return 5;
      }
    finally
      {
      if(out!=null)
        {
        out.flush();
        out.close();
        }
      }
      
    return 0;
    }
  }
