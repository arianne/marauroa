/* $Id: IRPZone.java,v 1.1 2004/06/03 13:04:44 arianne_rpg Exp $ */
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

import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import marauroa.*;

public interface IRPZone
  {
  public static class RPObjectNotFoundException extends Exception
    {
    public RPObjectNotFoundException(RPObject.ID id)
      {
      super("RP Object ["+id+"] not found");
      }
    }
  

  public static class RPObjectInvalidException extends Exception
    {
    public RPObjectInvalidException(String attribute)
      {
      super("Object is invalid: It lacks of mandatory attribute ["+attribute+"]");
      }
    }

  public void onInit() throws Exception;
  public void onFinish() throws Exception;
  public void add(RPObject object) throws RPObjectInvalidException;
  public void modify(RPObject object) throws RPObjectInvalidException;
  public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException;
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException;
  public boolean has(RPObject.ID id);
  public RPObject create();
  public Iterator iterator();
  public long size();
  
  public Perception getPerception(RPObject.ID id, byte type);
  
  public void nextTurn();
  }
