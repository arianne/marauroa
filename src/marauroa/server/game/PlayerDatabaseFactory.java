/* $Id: PlayerDatabaseFactory.java,v 1.1 2005/01/23 21:00:46 arianne_rpg Exp $ */
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
package marauroa.server.game;

import marauroa.common.*;
import marauroa.server.*;

/** MessageFactory is the class that is in charge of creating the correct object
 *  for the database choosed in the configuration file. */
public class PlayerDatabaseFactory
  {
  /** This method returns an instance of PlayerDatabase choosen using the Configuration file.
   *  @return A shared instance of PlayerDatabase */
  public static IPlayerDatabase getDatabase() throws NoDatabaseConfException
    {
    Logger.trace("PlayerDatabaseFactory::getDatabase",">");
    try
      {
      Configuration conf=Configuration.getConfiguration();
      String database_type=conf.get("marauroa_DATABASE");
      
      return getDatabase(database_type);    
      }
    catch(Exception e)
      {
      Logger.thrown("PlayerDatabaseFactory::getDatabase","X",e);
      throw new NoDatabaseConfException();      
      }
    finally
      {     
      Logger.trace("PlayerDatabaseFactory::getDatabase","<");
      }
    }

  /** This method returns an instance of PlayerDatabase choosen using the param.
   *  @param type A String containing the type of database
   *  @return A shared instance of PlayerDatabase */
  public static IPlayerDatabase getDatabase(String database_type) throws NoDatabaseConfException
    {
    Logger.trace("PlayerDatabaseFactory::getDatabase",">");
    try
      {
      if(database_type.equals("JDBCPlayerDatabase"))
        {
        Logger.trace("PlayerDatabaseFactory::getDatabase","D","Choosen JDBCPlayerDatabase");
        return JDBCPlayerDatabase.getDatabase();
        }

      Logger.trace("PlayerDatabaseFactory::getDatabase","X","No PlayerDatabase choosen");
      throw new NoDatabaseConfException();
      }
    catch(Exception e)
      {
      Logger.thrown("PlayerDatabaseFactory::getDatabase","X",e);
      throw new NoDatabaseConfException();      
      }
    finally
      {     
      Logger.trace("PlayerDatabaseFactory::getDatabase","<");
      }
    }
  }  
