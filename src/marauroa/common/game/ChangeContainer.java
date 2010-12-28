/* $Id: Attributes.java,v 1.85 2010/07/08 20:12:21 nhnb Exp $ */
/***************************************************************************
 *						(C) Copyright 2010 - Marauroa					   *
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
 * Collects changes that needs to be communicated to the client.
 *
 * @author hendrik
 */
public interface ChangeContainer {

	/**
	 * marks an attribute as new or a new value of an attribute
	 * 
	 * @param zone name of zone
	 * @param id   id of the object
	 * @param attribute name of attibute
	 * @param value new value
	 */
	public void addAttribute(String zone, String id, String attribute, String value);

	/**
	 * marks attributes as deleted
	 * 
	 * @param zone name of zone
	 * @param id   id of the object
	 * @param attribute name of attibute
	 */
	public void removeAttribute(String zone, String id, String attribute);

}
