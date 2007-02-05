/* $Id: AttributeNotFoundException.java,v 1.5 2007/02/05 19:11:14 arianne_rpg Exp $ */
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
package marauroa.common.game;

public class AttributeNotFoundException extends RuntimeException {
	private static final long serialVersionUID = -6055703660022859631L;

	private String attribute;

	public AttributeNotFoundException(String attrib) {
		super("Attribute [" + attrib + "] not found");
		attribute = attrib;
	}

	public String getAttribute() {
		return attribute;
	}
}
