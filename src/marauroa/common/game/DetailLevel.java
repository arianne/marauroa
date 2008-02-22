/* $Id: DetailLevel.java,v 1.5 2008/02/22 10:28:31 arianne_rpg Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.common.game;

/**
 * This enum provides detatil level of the serialization
 * 
 * @author miguel
 * 
 */
public enum DetailLevel {
	/** Remove hidden and private attributes */
	NORMAL,
	/** Removes hidden attributes */
	PRIVATE,
	/** Full objects */
	FULL
};
