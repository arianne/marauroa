/* $Id: the1001createaccount.java,v 1.5 2004/06/03 13:04:45 arianne_rpg Exp $ */
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

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import marauroa.game.*;
import marauroa.Configuration;
import the1001.objects.*;

public class the1001createaccount extends marauroa.createaccount
  {
  public static void main(String[] args)
    {
    the1001createaccount instance=new the1001createaccount();
    System.exit(instance.run(args));
    }
  
  public the1001createaccount()
    {
    super();
    
    information.add(new Information("-cm","character_model"));
    information.add(new Information("-g","gladiator",4,20));
    information.add(new Information("-gm","gladiator_model"));

    Configuration.setConfigurationFile("the1001.ini");
    }
  
  public RPObject populatePlayerRPObject(IPlayerDatabase playerDatabase) throws Exception
    {
    Transaction trans=playerDatabase.getTransaction();

    RPObject object=new Player(((JDBCPlayerDatabase)playerDatabase).getValidRPObjectID(trans),get("character"));
    object.put("look",get("character_model"));      
    
    Gladiator gladiator_obj=new Gladiator(((JDBCPlayerDatabase)playerDatabase).getValidRPObjectID(trans));
    gladiator_obj.put("name",get("gladiator"));
    gladiator_obj.put("look",get("gladiator_model"));
    
    object.getSlot("!gladiators").add(gladiator_obj);
    
    return object;
    }

  }
