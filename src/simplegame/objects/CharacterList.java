/* $Id: CharacterList.java,v 1.6 2003/12/17 16:05:29 arianne_rpg Exp $ */
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

import marauroa.game.RPObject;
import marauroa.game.Attributes;
import marauroa.net.OutputSerializer;
import marauroa.net.InputSerializer;
import marauroa.marauroad;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;


public class CharacterList
  extends RPObject
{
  public final static int TYPE_CHARACTER_LIST=4;
  public final static int TYPE_CHARACTER_LIST_ENTRY=5;
  
  public CharacterList()
  {
    put("type",TYPE_CHARACTER_LIST);
  }
  
  public void addCharacter(int char_id, String char_name, String char_status) throws Attributes.AttributeNotFoundException
  {
    CharEntry entry = new CharEntry();
    entry.setName(char_name);
    entry.setId(char_id);
    entry.setStatus(char_status);
    
    List charList=Attributes.StringToList(get("charList"));
    charList.add(entry);
    put("charList",charList);
  }
  
  public CharEntryIterator iterator() throws Attributes.AttributeNotFoundException
  {
    List charList=Attributes.StringToList(get("charList"));
    return(new CharEntryIterator(charList));
  }
  
  public class CharEntryIterator
  {
    private Iterator iterator;
    public CharEntryIterator(List list)
    {
      if(list!=null)
      {
        iterator = list.iterator();
      }
      else
      {
        iterator = null;
      }
    }
    
    public boolean hasNext()
    {
      return(iterator!=null && iterator.hasNext());
    }
    
    public CharEntry next()
    {
      return((CharEntry)iterator.next());
    }
  }//CharEntryIterator
  
  public static class CharEntry
    extends RPObject
  {
    public CharEntry()
    {
      put("type",TYPE_CHARACTER_LIST_ENTRY);
    }
    
    public void setName(String char_name)
    {
      put("name",char_name);
    }
    
    public String getName()
      throws Attributes.AttributeNotFoundException
    {
      return(get("name"));
    }
    
    public void setId(int char_id)
    {
      put("id",char_id);
    }
    
    public String getId()
      throws Attributes.AttributeNotFoundException
    {
      return(get("id"));
    }
    
    public void setStatus(String char_st)
    {
      put("status",char_st);
    }
    
    public String getStatus()
      throws Attributes.AttributeNotFoundException
    {
      return(get("status"));
    }
    
    public String toString()
    {
      String name = "invalid";
      try
      {
        name = getName();
      }
      catch (marauroa.game.Attributes.AttributeNotFoundException e)
      {
      }
      return(name);
    }
    
  }
}
