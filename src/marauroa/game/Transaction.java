/* $Id: Transaction.java,v 1.6 2004/05/31 07:26:22 root777 Exp $ */
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

public class Transaction
  {
  static public class TransactionException extends Exception
    {
    public TransactionException(String msg)
      {
      super(msg);
      }
    };
    
  public void commit() throws TransactionException
    {
    }
  
  public void rollback()
    {
    }
  }
