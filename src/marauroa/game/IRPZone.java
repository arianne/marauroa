/* $Id: IRPZone.java,v 1.4 2004/07/13 20:31:52 arianne_rpg Exp $ */
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

/** Interface for managing the objects in a RPZone. */
public interface IRPZone
  {
  /** This method is called when the zone is created to popullate it */
  public void onInit() throws Exception;
  /** This method is called when the server finish to save the content of the zone */
  public void onFinish() throws Exception;
  
  /** This method adds an object to the Zone */
  public void add(RPObject object) throws RPObjectInvalidException;
  /** This method tag an object of the Zone as modified */
  public void modify(RPObject object) throws RPObjectInvalidException;
  /** This method removed an object of the Zone and return it.*/
  public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException;
  /** This method returns an object of the Zone */
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException;
  /** This method returns true if the object exists in the Zone */
  public boolean has(RPObject.ID id);

  /** This method create a new RPObject with a valid id */
  public RPObject create();
  
  /** Iterates over the elements of the zone */
  public Iterator iterator();
  /** Returns the number of elements of the zone */
  public long size();
  
  /** This method return the perception of a zone for a player */
  public Perception getPerception(RPObject.ID id, byte type);
  /** This method is called to take zone to the next turn */
  public void nextTurn();
  /** Method to create the map to send to player's client */
  public java.util.List buildMapObjectsList(RPObject.ID id);
  }
