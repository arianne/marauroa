/* $Id: RPActionList.java,v 1.7 2004/03/24 15:25:34 arianne_rpg Exp $ */
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

import java.util.Iterator;
import java.util.LinkedList;

/** This class that represents a list of RPActions  uses a LinkedList as backstore.
 **/
public class RPActionList
  {
  /** A LinkedList<RPAction> that contains actions */
  private LinkedList actionsList;
  public RPActionList()
    {
    actionsList = new LinkedList();
    }
  
  /** This method adds a new rp action to list
   * @param rp_action - RPAction to add into list
   * @return actions that was just added
   **/
  public RPAction add(RPAction rp_action)
    {
    actionsList.add(rp_action);
    return(rp_action);
    }
  
  /** This method gets the RPAction
   * @param index index of RPAction to retrieve
   * @return actions that was just added
   **/
  public RPAction get(int index)
    {
    return((RPAction)actionsList.get(index));
    }
    
  /** This method removes the RPAction at position index
   * @param index index of RPAction to remove
   * @return actions that was just removed
   **/
  public RPAction remove(int index)
    {
    return (RPAction)actionsList.remove(index);
    }
  
  /** This method gets the size
   * @return count of RPActions in this list
   **/
  public int size()
    {
    return(actionsList.size());
    }
  
  /**
   * gets the RP Actions Iterator
   * @return RPActionIterator
   **/
  public RPActionIterator iterator()
    {
    return(new RPActionIterator(actionsList.iterator()));
    }
  /** An iterator for a RPActionList */
  public class RPActionIterator implements Iterator
    {
    private Iterator actionsIter;
    /** Constructor */
    private RPActionIterator(Iterator iter)
      {
      actionsIter = iter;
      }
     
    /** This method returns true if there are still most elements.
     *  @return true if there are more elements. */    
    public boolean hasNext()
      {
      return(actionsIter.hasNext());
      }
     
    /** This method returs the RPAction and move the pointer to the next element
     *  @return an RPAction */
    public Object next()
      {
      return actionsIter.next();
      }
    
    public void remove()
      {
      }
    }
  }
