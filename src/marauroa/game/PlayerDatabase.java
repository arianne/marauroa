package marauroa.game;

import java.net.InetSocketAddress;

/** The interface that all the databases marauroa use MUST implement. */
public interface PlayerDatabase
  {
  static public class PlayerAlreadyAddedException extends Throwable
    {
    PlayerAlreadyAddedException()
      {
      super("Player already added to the database.");
      }
    }
  
  static public class PlayerNotFoundException extends Throwable
    {
    PlayerNotFoundException()
      {
      super("Player not found on the database");
      }
    }
  
  static public class CharacterNotFoundException extends Throwable
    {
    CharacterNotFoundException()
      {
      super("Character not found on the database");
      }
    }

  static public class CharacterAlreadyAddedException extends Throwable
    {
    CharacterAlreadyAddedException()
      {
      super("Character already added to the database");
      }
    }
    
  static public class NoDatabaseConfException extends Throwable
    {
    NoDatabaseConfException()
      {
      super("Database configuration file not found.");
      }
    }

  static public class GenericDatabaseException extends Throwable
    {
    GenericDatabaseException(String msg)
      {
      super(msg);
      }
    }
  
  /** This method returns true if the username/password match with any of the accounts in 
   *  database or false if none of them match.
   *  @param username is the name of the player
   *  @param password is the string used to verify access.
   *  @return true if username/password is correct, false otherwise. */
  public boolean verifyAccount(String username, String password);
  /** This method returns the number of Players that exist on database 
   *  @return the number of players that exist on database */
  public int getPlayerCount() throws GenericDatabaseException;

  /** This method add a Login event to the player
   *  @param username is the name of the player 
   *  @param source the IP address of the player
   *  @param correctLogin true if the login has been correct.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database. */
  public void addLoginEvent(String username,InetSocketAddress source, boolean correctLogin) throws PlayerNotFoundException;
  /** This method returns the list of Login events as a array of Strings
   *  @param username is the name of the player 
   *  @return an array of String containing the login events.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database. */
  public String[] getLoginEvent(String username) throws PlayerNotFoundException;

  /** This method returns the lis of character that the player pointed by username has.
   *  @param username the name of the player from which we are requesting the list of characters.
   *  @return an array of String with the characters
   *  @throw PlayerNotFoundException if that player does not exists. */
  public String[] getCharactersList(String username) throws PlayerNotFoundException;

  /** This method is the opposite of getRPObject, and store in Database the object for
   *  an existing player and character.
   *  The difference between setRPObject and addCharacter are that setRPObject update it
   *  while addCharacter add it to database and fails if it already exists
   *.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player wants to add.
   *  @param object is the RPObject that represent this character in game.
   *
   *  @throws PlayerNotFoundException  if the player doesn't exist in database.
   *  @throws CharacterNotFoundException  if the player-character doesn't exist in database.
   *  @throws GenericDatabaseException if the character doesn't exist or it is not owned by the player. */
  public void setRPObject(String username,String character, RPObject object) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException;
  /** This method retrieves from Database the object for an existing player and character.
   *
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player wants to add.
   *  @return a RPObject that is the RPObject that represent this character in game.
   *
   *  @throws PlayerNotFoundException  if the player doesn't exist in database.
   *  @throws CharacterNotFoundException  if the player-character doesn't exist in database.
   *  @throws GenericDatabaseException if the character doesn't exist or it is not owned by the player. */
  public RPObject getRPObject(String username,String character) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException;

  /** This method returns true if the database has the player pointed by username
   *  @param username the name of the player we are asking if it exists.
   *  @return true if player exists or false otherwise. */
  public boolean hasPlayer(String username);
  /** This method add the player to database with username and password as identificator.
   *  @param username is the name of the player
   *  @param password is a string used to verify access.
   *  @throws PlayerAlreadyAddedExceptio if the player is already in database */
  public void addPlayer(String username, String password) throws PlayerAlreadyAddedException;
  /** This method remove the player with usernae from database.
   *  @param username is the name of the player
   *  @throws PlayerNotFoundException if the player doesn't exist in database. */
  public void removePlayer(String username) throws PlayerNotFoundException;

  /** This method returns true if the player has that character or false if it hasn't
   *  @param username is the name of the player 
   *  @param character is the name of the character
   *  @return true if player has the character or false if it hasn't
   *  @throws PlayerNotFoundException  if the player doesn't exist in database. */
  public boolean hasCharacter(String username, String character) throws PlayerNotFoundException;
  /** This method add a character asociated to a player.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player wants to add.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database.
   *  @throws CharacterAlreadyAddedException if that player-character exist in database.
   *  @throws GenericDatabaseException if the character doesn't exist or it is not owned by the player. */
  public void addCharacter(String username, String character, RPObject object) throws PlayerNotFoundException, CharacterAlreadyAddedException, GenericDatabaseException;
  /** This method removes a character asociated with a player.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player owns.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database.
   *  @throws CharacterNotFoundException if the character doesn't exist or it is not owned by the player. */
  public void removeCharacter(String username, String character) throws PlayerNotFoundException, CharacterNotFoundException;
  }

