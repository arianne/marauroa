/**
 * GameBoard.java
 *
 * @author Waldemar Tribus
 */

package simplegame.objects;

import marauroa.game.Attributes;
import marauroa.game.RPObject;
import simplegame.SimpleGameDataModelIF;

public class GameBoard
  extends RPObject
  implements SimpleGameDataModelIF
{
  
  public GameBoard(int size)
  {
    put("size",size);
    put("l_id",-1);
  }
  
  public int getLastPlayerID()
  {
    int ret = -1;
    try
    {
      ret = Integer.parseInt(get("l_id"));
    }
    catch (NumberFormatException e)
    {
    }
    catch (Attributes.AttributeNotFoundException e)
    {
    }
    return(ret);
  }
  
  public int getSize()
  {
    return(getSize(this));
  }
  
  public static int getSize(RPObject obj)
  {
    int ret = -1;
    try
    {
      ret = Integer.parseInt(obj.get("size"));
    }
    catch (NumberFormatException e)
    {
    }
    catch (Attributes.AttributeNotFoundException e)
    {
    }
    return(ret);
  }
  
  public void setRPCharacterAt(int row, int column, int characterID)
  {
    try
    {
      get(row+"X"+column);
    }
    catch (Attributes.AttributeNotFoundException e)
    {
      put(row+"X"+column,characterID);
      put("l_id",characterID);
    }
  }
  
  public int getRPCharacterAt(int row, int column)
  {
    return getRPCharacterAt(this,row,column);
  }
  
  public static int getRPCharacterAt(RPObject obj,int row, int column)
  {
    int id = -1;
    try
    {
      id = Integer.parseInt(obj.get(row+"X"+column));
    }
    catch (NumberFormatException e)
    {
      id = -1;
    }
    catch (Attributes.AttributeNotFoundException e)
    {
      id = -1;
    }
    return id;
  }
  
  public int getWinner()
  {
    return -1;
  }
  
  public void addGameUpdateListener(simplegame.SimpleGameDataModelIF.GameUpdateListener ul)
  {
    //no need to implement it
  }
  
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer("\n");
    int size = getSize();
    for (int i = 0; i < size; i++)
    {
      for (int j = 0; j < size; j++)
      {
        if(getRPCharacterAt(i,j)==-1)
        {
          sb.append('-');
        }
        else
        {
          sb.append(getRPCharacterAt(i,j));
        }
      }
      sb.append('\n');
    }
    sb.append('\n');
    return(sb.toString());
  }
}

