/**
 * RPActionList.java
 *
 * @author Created by wt
 */

package marauroa.game;

import java.util.Iterator;
import java.util.LinkedList;


/**
 * a class that represents a list of RPActions
 * uses (currently )LinkedList as backstore.
 **/
public class RPActionList
{
  private LinkedList actionsList;
  
  public RPActionList()
  {
    actionsList = new LinkedList();
  }
  
  /**
   *  adds a new rp action to list
   * @param rp_action - RPAction to add into list
   * @return actions that was just added
   **/
  public RPAction add(RPAction rp_action)
  {
    actionsList.add(rp_action);
    return(rp_action);
  }
  
  /**
   *  gets the RPAction
   * @param index index of RPAction to retrieve
   * @return actions that was just added
   **/
  public RPAction get(int index)
  {
    return((RPAction)actionsList.get(index));
  }
  
  /**
   *  gets the size
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
  
  
  public class RPActionIterator
  {
    private Iterator actionsIter;
    public RPActionIterator(Iterator iter)
    {
      actionsIter = iter;
    }
    public boolean hasNext()
    {
      return(actionsIter.hasNext());
    }
    public RPAction next()
    {
      return((RPAction)actionsIter.next());
    }
  }
  
}

