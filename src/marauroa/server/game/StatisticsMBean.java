/* $Id: StatisticsMBean.java,v 1.1 2005/03/02 22:21:59 root777 Exp $ */
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
/** statistics interface for the java management bean **/
public interface StatisticsMBean
{
  public long getBytesRecv();
  public long getBytesSend();

  public long getMessagesRecv();
  public long getMessagesSend();
  public long getMessagesIncorrect();

  public long getPlayersLogin();
  public long getPlayersInvalidLogin();
  public long getPlayersLogout();
  public long getPlayersTimeout();
  public long getPlayersOnline();

  public long getObjectsNow();
  public long getActionsAdded();
  public long getActionsInvalid();
}
