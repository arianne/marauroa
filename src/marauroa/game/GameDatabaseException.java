/* $Id: GameDatabaseException.java,v 1.2 2004/03/24 15:25:34 arianne_rpg Exp $ */
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

public interface GameDatabaseException
  {
  static public class PlayerAlreadyAddedException extends Exception
    {
    PlayerAlreadyAddedException(String player)
      {
      super("Player ["+player+"] already added to the database.");
      }
    }
  

  static public class PlayerNotFoundException extends Exception
    {
    PlayerNotFoundException(String player)
      {
      super("Player ["+player+"] not found on the database");
      }
    }
  

  static public class CharacterNotFoundException extends Exception
    {
    CharacterNotFoundException(String character)
      {
      super("Character ["+character+"] not found on the database");
      }
    }


  static public class CharacterAlreadyAddedException extends Exception
    {
    CharacterAlreadyAddedException(String character)
      {
      super("Character ["+character+"] already added to the database");
      }
    }
    

  static public class NoDatabaseConfException extends Exception
    {
    NoDatabaseConfException()
      {
      super("Database configuration file not found.");
      }
    }


  static public class GenericDatabaseException extends Exception
    {
    GenericDatabaseException(String msg)
      {
      super(msg);
      }
    }
  }
