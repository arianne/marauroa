/* $Id: PlayerDatabaseFactory.java,v 1.6 2004/05/07 17:16:58 arianne_rpg Exp $ */
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
package marauroa.game;

import marauroa.*;

/** MessageFactory is the class that is in charge of creating the correct object
 *  for the database choosed in the configuration file. */
public class PlayerDatabaseFactory
  {
  /** This method returns an instance of PlayerDatabase choosen using the Configuration file.
   *  @return A shared instance of PlayerDatabase */
  public static PlayerDatabase getDatabase() throws PlayerDatabase.NoDatabaseConfException
    {
    marauroad.trace("PlayerDatabaseFactory::getDatabase",">");
    try
      {
      Configuration conf=Configuration.getConfiguration();
      String database_type=conf.get("marauroa_DATABASE");
    
      if(database_type.equals("MemoryPlayerDatabase"))
        {
        marauroad.trace("PlayerDatabaseFactory::getDatabase","D","Choosen MemoryPlayerDatabase");
        return MemoryPlayerDatabase.getDatabase();
        }
      if(database_type.equals("JDBCPlayerDatabase"))
        {
        marauroad.trace("PlayerDatabaseFactory::getDatabase","D","Choosen JDBCPlayerDatabase");
        return JDBCPlayerDatabase.getDatabase();
        }
      marauroad.trace("PlayerDatabaseFactory::getDatabase","X","No PlayerDatabase choosen");
      throw new PlayerDatabase.NoDatabaseConfException();
      }
    catch(Exception e)
      {
      marauroad.thrown("PlayerDatabaseFactory::getDatabase","X",e);
      throw new PlayerDatabase.NoDatabaseConfException();      
      }
    finally
      {     
      marauroad.trace("PlayerDatabaseFactory::getDatabase","<");
      }
    }

  /** This method returns an instance of PlayerDatabase choosen using the param.
   *  @param type A String containing the type of database
   *  @return A shared instance of PlayerDatabase */
  public static PlayerDatabase getDatabase(String database_type) throws PlayerDatabase.NoDatabaseConfException
    {
    marauroad.trace("PlayerDatabaseFactory::getDatabase",">");
    try
      {
      if(database_type.equals("MemoryPlayerDatabase"))
        {
        marauroad.trace("PlayerDatabaseFactory::getDatabase","D","Choosen MemoryPlayerDatabase");
        return MemoryPlayerDatabase.getDatabase();
        }
      if(database_type.equals("JDBCPlayerDatabase"))
        {
        marauroad.trace("PlayerDatabaseFactory::getDatabase","D","Choosen JDBCPlayerDatabase");
        return JDBCPlayerDatabase.getDatabase();
        }
      marauroad.trace("PlayerDatabaseFactory::getDatabase","X","No PlayerDatabase choosen");
      throw new PlayerDatabase.NoDatabaseConfException();
      }
    catch(Exception e)
      {
      marauroad.thrown("PlayerDatabaseFactory::getDatabase","X",e);
      throw new PlayerDatabase.NoDatabaseConfException();      
      }
    finally
      {     
      marauroad.trace("PlayerDatabaseFactory::getDatabase","<");
      }
    }
  }  
