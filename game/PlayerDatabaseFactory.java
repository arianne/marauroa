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
    catch(Throwable e)
      {
      marauroad.trace("PlayerDatabaseFactory::getDatabase","X",e.getMessage());
      throw new PlayerDatabase.NoDatabaseConfException();      
      }
    finally
      {     
      marauroad.trace("PlayerDatabaseFactory::getDatabase","<");
      }
    }
  }  
