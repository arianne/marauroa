package marauroa.game;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import marauroa.marauroad;

public class JDBCTransaction
  extends Transaction
{
  private Connection connection;
  
  public JDBCTransaction(Connection connection)
  {
    this.connection = connection;
  }
  
  /**
   * Sets Connection
   *
   * @param    Connection          a  Connection
   */
  public void setConnection(Connection connection)
  {
    this.connection = connection;
  }
  
  /**
   * Returns Connection
   *
   * @return    a  Connection
   */
  public Connection getConnection()
  {
    return connection;
  }
  
  
  public boolean isValid()
  {
    boolean valid = false;
    if(connection!=null)
    {
      try
      {
        if(!connection.isClosed())
        {
          Statement stmt = connection.createStatement();
          String query = "select id from player where id=-1";
          marauroad.trace("JDBCTransaction::isValid","D",query);
          stmt.executeQuery(query);
          valid = true;
        }
        else
        {
          marauroad.trace("JDBCTransaction::isValid","D","Invalid, already closed.");
        }
      }
      catch(SQLException sqle)
      {
        marauroad.trace("JDBCTransaction::isValid","D","Invalid!: "+sqle.getMessage());
      }
    }
    return(valid);
    
  }
}

