/* $Id: Test_RPCode.java,v 1.2 2003/12/30 17:26:34 arianne_rpg Exp $ */
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
package the1001;

import junit.framework.*;
import marauroa.*;

public class Test_RPCode extends TestCase
  {
  private the1001RPRuleProcessor rpu;
  private the1001RPZone zone;
  
  public static Test suite ( ) 
    {
    return new TestSuite(Test_RPCode.class);
    }
    
  public void testRequestFight()
    {
    marauroad.trace("Test_RPCode::testRequestFight","?"/*TODO*/);      
    marauroad.trace("Test_RPCode::testRequestFight",">");
    
    zone=new the1001RPZone();
    rpu=new the1001RPRuleProcessor();
    rpu.setContext(zone);
    
      
    marauroad.trace("Test_RPCode::testRequestFight","<");
    }
  }

