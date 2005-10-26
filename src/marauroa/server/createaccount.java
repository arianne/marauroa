/* $Id: createaccount.java,v 1.7 2005/10/26 14:11:01 arianne_rpg Exp $ */
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
package marauroa.server;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import marauroa.server.game.*;
import marauroa.common.Log4J;
import marauroa.common.Configuration;
import marauroa.common.game.*;
import marauroa.common.crypto.Hash;
import org.apache.log4j.Logger;

/** This is the base class to extend in order to create an account.
 *  The class provide a few methods of which you inherit and implement. */
public abstract class createaccount
  {
  private static final Logger logger=Log4J.getLogger(createaccount.class);

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
    for(Information item: information)
      {
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
    int i=0;
    String iniFile = "marauroa.ini";

    while(i!=args.length)
      {
      for(Information item: information)
        {
        if(args[i].equals(item.param))
          {
          item.value=args[i+1];
          logger.info(item.name+"="+item.value);
          break;
          }
        }

      if(args[i].equals("-i"))
        {
	      iniFile=args[i+1];
        }

      if(args[i].equals("-h"))
        {
        logger.info("createaccount application for Marauroa");
        for(Information item: information)
          {
          logger.info(item.param+" to use/add "+item.name);
          }

        logger.info("-i"+" to to define .ini file");

        System.exit(0);
        }

      ++i;
      }

    Transaction trans=null;
    PrintWriter out=null;

    try
      {
      Configuration.setConfigurationFile(iniFile);
      Configuration conf=Configuration.getConfiguration();

      logger.info("Trying to create username("+get("username")+"), password("+get("password")+"), character("+get("character")+")");

      JDBCPlayerDatabase playerDatabase=(JDBCPlayerDatabase)PlayerDatabaseFactory.getDatabase("JDBCPlayerDatabase");
      trans=playerDatabase.getTransaction();
      
      trans.begin();

      logger.info("Checking for null/empty string");
      for(Information item: information)
        {
        if(item.value.equals(""))
          {
          logger.info("String is empty or null: "+item.name);
          return 1;
          }
        }

      logger.info("Checking for valid string");
      for(Information item: information)
        {
        if(!playerDatabase.validString(item.value))
          {
          logger.info("String not valid: "+item.name);
          return 2;
          }
        }

      logger.info("Checking string size");
      for(Information item: information)
        {
        if(item.value.length()>item.max || item.value.length()<item.min)
          {
          logger.info("String size not valid: "+item.name);
          return 3;
          }
        }

      logger.info("Checking if player exists");
      if(playerDatabase.hasPlayer(trans, get("username")))
        {
        logger.info("ERROR: Player exists");
        return 4;
        }

      logger.info("Adding player");
      playerDatabase.addPlayer(trans,get("username"),Hash.hash(get("password")),get("email"));

      RPObject object=populatePlayerRPObject(playerDatabase);

      playerDatabase.addCharacter(trans, get("username"),get("character"),object);
      logger.info("Correctly created");
      
      trans.commit();
      }
    catch(Exception e)
      {
      if(out!=null)
        {
        logger.warn("Failed: "+e.getMessage());
        e.printStackTrace(out);

        try
          {
          trans.rollback();
          }
        catch(Exception ae)
          {
          logger.fatal("Failed Rollback: "+ae.getMessage());
          }
        }
      return 5;
      }

    return 0;
    }
  }
