/* $Id: GameDataModelListenerIF.java,v 1.1 2004/02/15 19:21:06 root777 Exp $ */
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

package the1001.client;

/**
 *@author Waldemar Tribus
 */
public interface GameDataModelListenerIF
{
	/** called if the gamedatamodel has been changed **/
	public void modelUpdated(GameDataModel gdm);
}

