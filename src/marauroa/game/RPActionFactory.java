/**
 * RPActionFactory.java
 *
 * @author Waldemar Tribus
 */

package marauroa.game;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import marauroa.marauroad;
import marauroa.net.InputSerializer;
import marauroa.net.OutputSerializer;

public class RPActionFactory
{
  private static Map factoryArray;
  private static RPActionFactory rpActionFactory;
  
  private RPActionFactory()
  {
    factoryArray= new HashMap();
  }
  
  /** This method returns an instance of RPObjectFactory
   *  @return A shared instance of RPObjectFactory */
  public static RPActionFactory getFactory()
  {
    if(rpActionFactory==null)
    {
      rpActionFactory=new RPActionFactory();
      rpActionFactory.register(0,RPAction.class);
    }
    return rpActionFactory;
  }
  
  /** registers a RPAction class
   @param index the RPAction type
   @param rpActionClass the RPAction class
   **/
  private void register(int index,Class rpActionClass)
  {
    factoryArray.put(new Integer(index),rpActionClass);
  }
  
  /** Returns a object of the right class from a stream of serialized data.
   @param is InputSerializer
   @throws IOException in case of problems with the RPAction */
  public RPAction getRPAction(InputSerializer is)
    throws IOException
  {
    marauroad.trace("RPActionFactory::getRPAction",">");
    try
    {
      int index = is.readInt();
      Class rpActionType=(Class) factoryArray.get(new Integer(index));
      if(rpActionType!=null)
      {
        RPAction tmp=(RPAction) rpActionType.newInstance();
        tmp.readObject(is);
        return tmp;
      }
      else
      {
        marauroad.trace("RPActionFactory::getRPAction","X","RPAction type ["+index+"] is not registered.");
        throw new IOException("RPAction type ["+index+"] is not registered.");
      }
    }
    catch(Exception e)
    {
      marauroad.trace("RPActionFactory::getRPAction","X",e.getMessage());
      throw new IOException(e.getMessage());
    }
    finally
    {
      marauroad.trace("RPActionFactory::getRPAction","<");
    }
  }
  
  /**
   * adds the given RPAction into serializer
   */
  public void addRPAction(OutputSerializer os,RPAction rp_action)
    throws IOException
  {
    marauroad.trace("RPActionFactory::addRPAction",">");
    try
    {
      int index = rp_action.actionType;
      os.write(index);
      rp_action.writeObject(os);
      if(factoryArray.get(new Integer(index))==null)
      {
        //it is not registered yet, so register it
        register(index,rp_action.getClass());
      }
    }
    catch(Exception e)
    {
      marauroad.trace("RPActionFactory::addRPAction","X",e.getMessage());
      throw new IOException(e.getMessage());
    }
    finally
    {
      marauroad.trace("RPActionFactory::addRPAction","<");
    }
  }
  
}

