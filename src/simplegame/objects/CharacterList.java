/* $Id: CharacterList.java,v 1.10 2003/12/18 19:18:48 arianne_rpg Exp $ */
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
package simplegame.objects;

import marauroa.game.*;
import marauroa.net.*;
import marauroa.marauroad;
import java.io.IOException;
import java.util.*;


public class CharacterList extends RPObject
  {
  public final static int TYPE_CHARACTER_LIST=4;
  public final static int TYPE_CHARACTER_LIST_ENTRY=5;
  
  public CharacterList(RPObject object) throws AttributeNotFoundException
    {
    Iterator it=object.iterator();
    while(it.hasNext())
      {
      String attr=(String)it.next();
      put(attr,object.get(attr));
      }
      
    put("type",TYPE_CHARACTER_LIST);
    }

  public CharacterList()
    {    
    put("type",TYPE_CHARACTER_LIST);
    put("charList","");
    }
  
  public void addCharacter(int char_id, String char_name, String char_status) throws Attributes.AttributeNotFoundException
    {
    List charList=Attributes.StringToList(get("charList"));
    charList.add(new String(char_id+","+char_name+","+char_status));
    put("charList",charList);
    }
    
  public Iterator CharacterIterator() throws AttributeNotFoundException
    {
    List charList=Attributes.StringToList(get("charList"));
    return charList.iterator();
    }
    
  public static int getId(String player)
    {
    String[] list=player.split(",");
    return Integer.parseInt(list[0]);
    } 

  public static int getName(String player)
    {
    String[] list=player.split(",");
    return Integer.parseInt(list[1]);
    } 

  public static int getStatus(String player)
    {
    String[] list=player.split(",");
    return Integer.parseInt(list[2]);
    } 
  }
