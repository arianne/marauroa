/* $Id: JDBCTransaction.java,v 1.1 2005/01/23 21:00:46 arianne_rpg Exp $ */
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

import java.sql.*;
import marauroa.common.*;

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
      Logger.thrown("JDBCTransaction::rollback","!",e);
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

          Logger.trace("JDBCTransaction::isValid","D",query);
          stmt.executeQuery(query);
          valid = true;
          }
        else
          {
          Logger.trace("JDBCTransaction::isValid","D","Invalid, already closed.");
          }
        }
      catch(SQLException sqle)
        {
        Logger.thrown("JDBCTransaction::isValid","X",sqle);
        }
      }
    return(valid);
    }
  }
