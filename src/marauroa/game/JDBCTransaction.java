package marauroa.game;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import marauroa.marauroad;

public class JDBCTransaction extends Transaction
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
  
  public void commit() throws TransactionException
    {
    try
      {
      connection.commit();
      }
    catch(SQLException e)
      {
      throw new TransactionException(e.getMessage());
      }
    }
  
  public void rollback()
    {
    try
      {
      connection.rollback();
      }
    catch(SQLException e)
      {
      //throw new TransactionException(e.getMessage());
      marauroad.trace("JDBCTransaction::rollback","!",e.getMessage());
      }
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
          String query = "show tables";

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
        marauroad.trace("JDBCTransaction::isValid","X","Invalid!: "+sqle.getMessage());
        }
      }
    return(valid);
    }
  }
