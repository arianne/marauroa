/* $Id: mapacmanRPZone.java,v 1.4 2004/05/22 09:18:58 root777 Exp $ */
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
package mapacman;

import marauroa.marauroad;
import marauroa.game.*;
import java.util.*;
//import the1001.objects.*;

public class mapacmanRPZone extends MarauroaRPZone
  {
  public mapacmanRPZone()
    {
    super();
    marauroad.trace("mapacmanRPZone::mapacmanRPZone",">");
    marauroad.trace("mapacmanRPZone::mapacmanRPZone","<");
    }
  
  protected void loadWorld() throws Exception
    {
    JDBCPlayerDatabase.RPObjectIterator it=rpobjectDatabase.zoneIterator(transaction);
    while(it.hasNext())
      {
      RPObject.ID id=it.next();
      
      try
        {
        rpobjectDatabase.removeFromRPZone(transaction,id);
        transaction.commit();
        }
      catch(Exception e)
        {
        transaction.rollback();
        throw new RPObjectNotFoundException(id);
        }
      }
    }
  }
