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
package marauroa.server.game.container;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.game.ChangeContainer;

/**
 * container for changes
 *
 * @author hendrik
 */
public class ChangeContainerImpl extends ChangeContainer {
	private Logger logger = Log4J.getLogger(ChangeContainerImpl.class);

	@Override
	public void addAttribute(String zone, String id, String attribute, String value) {
		logger.debug("added: zone: " + zone + " id: " + id + ": " + attribute + "=" + value);
	}

	@Override
	public void removeAttribute(String zone, String id, String attribute) {
		logger.debug("removed: zone: " + zone + " id: " + id + ": " + attribute);
	}

}
