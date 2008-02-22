/* $Id: PingModel.java,v 1.2 2008/02/22 10:28:34 arianne_rpg Exp $ */
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
package marauroa.test.ping;

import java.util.Observable;

public class PingModel extends Observable {
	String[] lines=new String[]{ 
			"Hello world?",
			"Hi there.",
			"Nice to meet you",
			"How are you?",
			"Fine",
			"See you later",
			"Bye"
		};
	
	private int current;

	public void initConversation() {
		current=0;
	}
	
	public void next() {
		current++;
	}
	
	public boolean hasNext() {
		return current<lines.length;
	}
	
	public String getLine() {		
		return lines[current];
	}
}
