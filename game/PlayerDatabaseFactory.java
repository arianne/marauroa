package marauroa.game;

import marauroa.*;

public class PlayerDatabaseFactory
  {
  public static PlayerDatabase getDatabase() throws PlayerDatabase.NoDatabaseConfException
    {
    marauroad.trace("PlayerDatabaseFactory::getDatabase",">");
    try
      {
      Configuration conf=Configuration.getConfiguration();
    
      String database_type=conf.get("marauroa_DATABASE");
    
      if(database_type.equals("MemoryPlayerDatabase"))
        {
        marauroad.trace("PlayerDatabaseFactory::getDatabase","<");
        return MemoryPlayerDatabase.getDatabase();
        }
    
      if(database_type.equals("JDBCPlayerDatabase"))
        {
        marauroad.trace("PlayerDatabaseFactory::getDatabase","<");
        return JDBCPlayerDatabase.getDatabase();
        }
      }
    catch(Configuration.PropertyNotFoundException e)
      {
      marauroad.trace("PlayerDatabaseFactory::getDatabase","<");
      throw new PlayerDatabase.NoDatabaseConfException();      
      }
    catch(Configuration.PropertyFileNotFoundException e)
      {
      marauroad.trace("PlayerDatabaseFactory::getDatabase","<");
      throw new PlayerDatabase.NoDatabaseConfException();
      }
     
    marauroad.trace("PlayerDatabaseFactory::getDatabase","<");
    throw new PlayerDatabase.NoDatabaseConfException();
    }
  }  
