/* $Id: Test_RPServerManager.java,v 1.5 2004/03/24 15:25:34 arianne_rpg Exp $ */
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

import junit.framework.*;
import marauroa.game.*;
import marauroa.net.*;
import marauroa.*;
import java.io.*;
import java.net.*;

public class Test_RPServerManager extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_RPServerManager.class);
    }
	
  public void testRPServerManager()  
    {
    /** It is really, really, really hard to verify RPServerManager, as all the 
     *  behaviour is hidden by GameManager and Scheduler */
    }
  }