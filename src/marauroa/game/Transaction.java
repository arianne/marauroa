/* $Id: Transaction.java,v 1.8 2004/07/07 10:07:20 arianne_rpg Exp $ */
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

/**
 * This class represents a transaction which can be used to retrieve/store/change
 * in PlayerDatabase. Different PlayerDatabase implementaions may requiere different
 * implementations of this class. This dummy implementation can only be used with 
 * MemoryPlayerDatabase(which does not support transactions anyway).  
 * JDBCPlayerDatabase needs an instance of JDBCTransaction in order to work properly
 */
public class Transaction
  {  
  static public class TransactionException extends Exception
    {
    public TransactionException(String msg)
      {
      super(msg);
      }
    }
   
  /** commits the changes made to backstore.
   * @exception TransactionException if the underlaying backstore throws an Exception   */
  public void commit() throws TransactionException
    {
    }

  /** Makes previous changes to backstore invalid */
  public void rollback()
    {
    }
  }
