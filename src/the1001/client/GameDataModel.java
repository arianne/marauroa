/* $Id: GameDataModel.java,v 1.23 2004/04/25 14:26:46 root777 Exp $ */
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

package the1001.client;

import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import marauroa.game.Attributes;
import marauroa.game.RPAction;
import marauroa.game.RPObject;
import marauroa.game.RPSlot;
import marauroa.marauroad;
import marauroa.net.MessageC2SAction;
import marauroa.net.MessageC2SLogout;
import marauroa.net.NetworkClientManager;
import the1001.RPCode;

/**
 *@author Waldemar Tribus
 */
public final class GameDataModel
{
  public final static String CMD_SCISSOR = "SCISSOR";
  public final static String CMD_STONE   = "STONE";
  public final static String CMD_PAPER   = "PAPER";
  public final static String CMD_FIGHT   = "FIGHT";
  public final static String CMD_VOTE_UP = RPCode.var_voted_up;
  public final static String CMD_VOTE_DOWN = "VOTE_DOWN";
  public final static int REQ_FIGHT_WAIT_TIME = 5*60*1000; // 5 minutes
  public final static String ARENA_MODE_WAITING  = RPCode.var_waiting;
  public final static String ARENA_MODE_FIGHTING = RPCode.var_fighting;
  public final static String ARENA_MODE_REQ_FAME = RPCode.var_request_fame;
  
  private long switchStrategyTimeOut; //all 30 mins choose strategy
  private long switchStrategyTS;
  private int strategy;               //0 - smart, 1 - more random, other - completely random
  
  private transient NetworkClientManager netMan;
  private List listeners;
  private ActionListener commandListener;
  private String currentFightMode;
  private String lastFightMode;
  private long lastReqFightTS;
  private long arenaWaitingTS;
  private boolean voted;
  private Random random=new Random(System.currentTimeMillis());
  private String myName;
  private String ownCharacterId;
  private String arenaId;
  private String shopId;
  private Map mAllObjects;
  //  private static Map mGlobalObjects=new HashMap();
  
  public GameDataModel(NetworkClientManager net_man)
  {
    netMan     = net_man;
    listeners  = new ArrayList(1);
    commandListener = new ActionHandler();
    lastReqFightTS = System.currentTimeMillis()-REQ_FIGHT_WAIT_TIME/2;
    mAllObjects = new HashMap();
    voted=false;
    arenaWaitingTS=-1;
    switchStrategyTimeOut=30*60*1000;
    switchStrategyTS=System.currentTimeMillis();
    strategy = 0;
  }
  
  /**
   * Method logout
   *
   */
  public void logout()
  {
    MessageC2SLogout msg = new MessageC2SLogout(null);
    
    netMan.addMessage(msg);
  }
  
  /**
   * Returns Arena
   *
   * @return    a  RPObject
   */
  public RPObject getArena()
  {
    RPObject rp_arena = null;
    for (Iterator iter = mAllObjects.values().iterator();iter.hasNext();)
    {
      RPObject rp_obj = (RPObject)iter.next();
      
      try
      {
        if(rp_obj.has(RPCode.var_type) && "arena".equals(rp_obj.get(RPCode.var_type)))
        {
          rp_arena = rp_obj;
          break;
        }
      }
      catch (Attributes.AttributeNotFoundException e)
      {
        marauroad.trace("GameDataModel::getArena","X",e.getMessage());
        e.printStackTrace(System.out);
      }
    }
    return rp_arena;
  }
  
  /**
   * Sets OwnCharacter
   *
   * @param    OwnCharacter        a  RPObject
   */
  public void setOwnCharacterID(String  ownCharacterId)
  {
    this.ownCharacterId = ownCharacterId;
  }
  
  /**
   * Returns OwnCharacter
   *
   * @return    a  RPObject
   */
  public RPObject getOwnCharacter()
  {
    RPObject rp_own_char = null;
    if(ownCharacterId!=null)
    {
      rp_own_char = getObject(ownCharacterId);
    }
    return rp_own_char;
  }
  
  public ActionListener getActionHandler()
  {
    return(commandListener);
  }
  
  public String getStatus()
  {
    String  status;
    try
    {
      status = getArena()==null?"unknown":getArena().get(RPCode.var_status);
    }
    catch (Attributes.AttributeNotFoundException e)
    {
      status="???";
    }
    return(status);
  }
  
  public Map getAllObjects()
  {
    return(mAllObjects);
  }
  
  /**
   *
   */
  public void clearAllObjects()
  {
    mAllObjects.clear();
  }
  
  /**
   * returns all the spectators
   **/
  public List getSpectators()
  {
    List l_spectators = new ArrayList();
    for (Iterator iter = mAllObjects.values().iterator();iter.hasNext();)
    {
      RPObject rp_obj = (RPObject)iter.next();
      try
      {
        if(rp_obj.has(RPCode.var_type) && "character".equals(rp_obj.get(RPCode.var_type)))
        {
          l_spectators.add(rp_obj);
        }
      }
      catch (Attributes.AttributeNotFoundException e)
      {
        marauroad.trace("GameDataModel::getSpectators","X",e.getMessage());
        e.printStackTrace(System.out);
      }
    }
    return(l_spectators);
  }
  
  /**
   * returns all the gladiator in the shop
   **/
  public List getShopGladiators()
  {
    List l_gladiators = new ArrayList();
    for (Iterator iter = mAllObjects.values().iterator();iter.hasNext();)
    {
      RPObject rp_obj = (RPObject)iter.next();
      try
      {
        if(rp_obj.has(RPCode.var_type) && "shop".equals(rp_obj.get(RPCode.var_type)))
        {
          if(rp_obj.hasSlot(RPCode.var_gladiators))
          {
            try
            {
              RPSlot glad_slot = rp_obj.getSlot(RPCode.var_gladiators);
              for(Iterator g_iter = glad_slot.iterator(); g_iter.hasNext();)
              {
                RPObject rp_g =  (RPObject)g_iter.next();
                if(rp_g.has(RPCode.var_type) && "gladiator".equals(RPCode.var_type))
                {
                  l_gladiators.add(rp_g);
                }
              }
            }
            catch (RPObject.NoSlotFoundException e)
            {
              marauroad.trace("GameDataModel::getShopGladiators","X",e.getMessage());
              e.printStackTrace(System.out);
            }
          }
          break;
        }
      }
      catch (Attributes.AttributeNotFoundException e)
      {
        marauroad.trace("GameDataModel::getShopGladiators","X",e.getMessage());
        e.printStackTrace(System.out);
      }
    }
    return(l_gladiators);
  }
  
  public List getFighters()
  {
    List l_gladiators = new ArrayList();
    RPObject rp_arena = getArena();
    if(rp_arena!=null && rp_arena.hasSlot(RPCode.var_gladiators))
    {
      try
      {
        RPSlot glad_slot = rp_arena.getSlot(RPCode.var_gladiators);
        for(Iterator g_iter = glad_slot.iterator(); g_iter.hasNext();)
        {
          RPObject rp_g =  (RPObject)g_iter.next();
          try
          {
            if(rp_g.has(RPCode.var_type) && "gladiator".equals(rp_g.get(RPCode.var_type)))
            {
              l_gladiators.add(rp_g);
            }
            else
            {
              marauroad.trace("GameDataModel::getFighters","D","Object ignored because it is not gladiator: "+rp_g);
            }
          }
          catch (Attributes.AttributeNotFoundException e)
          {
            marauroad.trace("GameDataModel::getFighters","X",e.getMessage());
            e.printStackTrace(System.out);
          }
        }
      }
      catch (RPObject.NoSlotFoundException e)
      {
        marauroad.trace("GameDataModel::getFighters","X",e.getMessage());
        e.printStackTrace(System.out);
      }
    }
    else
    {
      marauroad.trace("GameDataModel::getFighters","D","No slot " +RPCode.var_gladiators + " in " + rp_arena);
    }
    return(l_gladiators);
  }
  
  public void requestFight()
  {
    RPObject gladiator = getFirstOwnGladiator();
    
    if(gladiator!=null)
    {
      int gl_id = RPObject.INVALID_ID.getObjectID();
      try
      {
        gl_id = gladiator.getInt(RPCode.var_object_id);
      }
      catch (Attributes.AttributeNotFoundException e)
      {
        marauroad.trace("The1001Game::requestFight","X","Gladiator has no or invalid object id.");
      }
      
      RPAction action = new RPAction();
      
      action.put(RPCode.var_type,"request_fight");
      action.put(RPCode.var_gladiator_id,gl_id);
      netMan.addMessage(new MessageC2SAction(null,action));
      marauroad.trace("The1001Game::requestFight","D","Fight requested.");
    }
  }
  
//
  //  public static RPObject getGlobalObject(String id)
  //  {
  //    return((RPObject)mGlobalObjects.get(id));
  //  }
  
  private RPObject getObject(String id)
  {
    return((RPObject)mAllObjects.get(id));
  }
  
  /**
   * Method getFirstOwnGladiator
   *
   * @return   a  RPObject
   */
  public RPObject getFirstOwnGladiator()
  {
    RPObject gladiator = null;
    RPObject own_char = getOwnCharacter();
    if(own_char!=null)
    {
      if(own_char.hasSlot(RPCode.var_myGladiators))
      {
        try
        {
          RPSlot g_slot = own_char.getSlot(RPCode.var_myGladiators);
          gladiator = (RPObject)g_slot.iterator().next();
        }
        catch (RPObject.NoSlotFoundException e)
        {
          marauroad.trace("GameDataModel::getFirstOwnGladiator","E",e.getMessage());
          e.printStackTrace(System.out);
        }
      }
    }
    if(gladiator==null)
    {
      marauroad.trace("GameDataModel::getFirstOwnGladiator","D","Own gladiator is not set.");
    }
    return gladiator;
  }
  
  public void sendMessage(String msg)
  {
    RPAction action = new RPAction();
    action.put(RPCode.var_type,RPCode.var_chat);
    action.put(RPCode.var_content,msg);
    netMan.addMessage(new MessageC2SAction(null,action));
    marauroad.trace("The1001Game::sendMessage","D","Chat message sent.");
  }
  
  /**
   * Method setRandomFightMode
   *
   * @param    strategy            an int
   *
   */
  public void setRandomFightMode(int strategy)
  {
    String strg = myName+"["+strategy+"]:"+lastFightMode+"->"+getFightMode()+"->";
    switch(strategy)
    {
      case 0:
        HashSet hs = new HashSet(3);
        hs.add(RPCode.var_scissor);
        hs.add(RPCode.var_rock);
        hs.add(RPCode.var_paper);
        hs.remove(lastFightMode);
        hs.remove(getFightMode());
        lastFightMode = currentFightMode;
        setFightMode((String)hs.iterator().next());
        hs = null;
        break;
      case 1:
        int random_value = random.nextInt(2);
        lastFightMode = currentFightMode;
        if(RPCode.var_scissor.equals(getFightMode()))
        {
          setFightMode(random_value==0?RPCode.var_paper:RPCode.var_rock);
        }
        else if(RPCode.var_paper.equals(getFightMode()))
        {
          setFightMode(random_value==0?RPCode.var_scissor:RPCode.var_rock);
        }
        else
        {
          setFightMode(random_value==0?RPCode.var_scissor:RPCode.var_paper);
        }
        break;
      case 2:
      default:
        random_value = random.nextInt(3);
        lastFightMode = currentFightMode;
        setFightMode(random_value==0?RPCode.var_scissor:(random_value==1?RPCode.var_rock:RPCode.var_paper));
        break;
    }
    //System.out.println(strg+getFightMode());
  }
  
  public String getFightMode()
  {
    return(currentFightMode);
  }
  
  public void setFightMode(String mode)
  {
    if(mode!=null)
    {
      currentFightMode = mode;
      
      RPObject gladiator = getFirstOwnGladiator();
      
      if(gladiator!=null)
      {
        int gl_id = RPObject.INVALID_ID.getObjectID();
        
        try
        {
          gl_id = gladiator.getInt(RPCode.var_object_id);
        }
        catch (Attributes.AttributeNotFoundException e)
        {
          marauroad.trace("The1001Game::setFightMode","X","Gladiator has no or invalid object id.");
        }
        
        RPAction action = new RPAction();
        
        action.put(RPCode.var_gladiator_id,gl_id);
        action.put(RPCode.var_type,"fight_mode");
        action.put("fight_mode",mode);
        netMan.addMessage(new MessageC2SAction(null,action));
        marauroad.trace("The1001Game::setFightMode","D","Fight mode set.");
      }
      else
      {
        marauroad.trace("The1001Game::setFightMode","D","Fight mode is null.");
      }
    }
  }
  
  public void buyGladiator(String gladiator_id)
  {
    RPAction action = new RPAction();
    action.put(RPCode.var_type,RPCode.var_buyGladiator);
    action.put(RPCode.var_choosen_item,gladiator_id);
    netMan.addMessage(new MessageC2SAction(null,action));
    marauroad.trace("The1001Game::buyGladiator","D","Want to buy ["+gladiator_id+"].");
  }
  
  public void vote(String vote)
  {
    RPObject gladiator = getFirstOwnGladiator();
    
    if(gladiator!=null)
    {
      int gl_id = RPObject.INVALID_ID.getObjectID();
      
      try
      {
        gl_id = gladiator.getInt(RPCode.var_object_id);
      }
      catch (Attributes.AttributeNotFoundException e)
      {
        marauroad.trace("The1001Game::vote","X","Gladiator has no or invalid object id.");
      }
      
      RPAction action = new RPAction();
      
      action.put(RPCode.var_gladiator_id,gl_id);
      action.put(RPCode.var_type,RPCode.var_vote);
      action.put(RPCode.var_vote,vote);
      netMan.addMessage(new MessageC2SAction(null,action));
      marauroad.trace("The1001Game::vote","D","Voted ["+vote+"].");
    }
  }
  
  /**
   * adds a new listener
   **/
  public GameDataModelListenerIF addListener(GameDataModelListenerIF lissi)
  {
    if(lissi!=null)
    {
      if(!listeners.contains(lissi))
      {
        listeners.add(lissi);
      }
    }
    return(lissi);
  }
  
  /**
   * removes an already registered listener
   */
  public boolean removeListener(GameDataModelListenerIF lissi)
  {
    boolean ret = false;
    
    if(lissi!=null)
    {
      ret = listeners.remove(lissi);
    }
    return(ret);
  }
  
  protected void fireListeners()
  {
    for (int i = 0; i < listeners.size(); i++)
    {
      ((GameDataModelListenerIF)listeners.get(i)).modelUpdated(this);
    }
  }
  
  public void react(boolean doPrint)
  {
    try
    {
      if(getFirstOwnGladiator()==null)
      {
        List glads_in_shop = getShopGladiators();
        
        if(glads_in_shop.size()>0)
        {
          RPObject first_avail_glad = (RPObject)glads_in_shop.get(Math.abs(random.nextInt()%glads_in_shop.size()));
          buyGladiator(first_avail_glad.get(RPCode.var_object_id));
        }
      }
    }
    catch (Attributes.AttributeNotFoundException e)
    {
      marauroad.trace("GameDataModel::getFirstOwnGladiator","E",e.getMessage());
      e.printStackTrace(System.out);
    }
    if(RPCode.var_waiting.equals(getStatus())||RPCode.var_request_fame.equals(getStatus()))
    {
      if(RPCode.var_waiting.equals(getStatus()))
      {
        if(arenaWaitingTS==-1)
        {
          arenaWaitingTS=System.currentTimeMillis();
        }
      }
      else
      {
        arenaWaitingTS=-1;
      }
      
      if(REQ_FIGHT_WAIT_TIME<(System.currentTimeMillis()-lastReqFightTS)||
           (countPeople()==0) ||
           (arenaWaitingTS>0&& System.currentTimeMillis()-arenaWaitingTS>30*1000)
        )
      {
        requestFight();
        lastReqFightTS = System.currentTimeMillis();
      }
      voted=false;
      currentFightMode=null;
      lastFightMode=null;
    }
    else
    {
      arenaWaitingTS=-1;
    }
    if(RPCode.var_request_fame.equals(getStatus()))
    {
      RPObject rp_arena = getArena();
      int winner_id = -1;
      try
      {
        if(rp_arena.has(RPCode.var_winner))
        {
          winner_id = getArena().getInt(RPCode.var_winner);
        }
      }
      catch(Exception e)
      {
        marauroad.trace("GameDataModel::getFirstOwnGladiator","E",e.getMessage());
        e.printStackTrace(System.out);
      }
      if(!voted)
      {
        if(getFirstOwnGladiator()!=null)
        {
          int own_glad_id = -2;
          try
          {
            own_glad_id = getFirstOwnGladiator().getInt(RPCode.var_object_id);
          }
          catch(Exception e)
          {
            marauroad.trace("GameDataModel::getFirstOwnGladiator","E",e.getMessage());
            e.printStackTrace(System.out);
          }
          if(winner_id==own_glad_id)
          {
            vote(RPCode.var_voted_up);
          }
          else
          {
            vote(Math.random()>0.1?RPCode.var_voted_up:"VOTE_DOWN");
          }
        }
        else
        {
          vote(Math.random()>0.5?RPCode.var_voted_up:"VOTE_DOWN");
        }
        voted = true;
      }
      currentFightMode=null;
      lastFightMode=null;
    }
    else if(RPCode.var_fighting.equals(getStatus()) || (getOwnCharacter()!=null && getOwnCharacter().has(RPCode.var_damage)))
    {
      if(!RPCode.var_fighting.equals(getStatus()))
      {
        System.out.println("BUG!!! Already fighting(damage is there) but the arena is not in fight status!!!");
      }
      voted=false;
      RPObject own_char = getOwnCharacter();
      if(own_char!=null && (own_char.has(RPCode.var_fighting) || own_char.has(RPCode.var_damage)))
      {
        if(!own_char.has(RPCode.var_fighting))
        {
          System.out.println("BUG!!! Already fighting(damage is there) but the fighting attributes is not there!!!");
        }
        try
        {
          int own_damage = 0;
          try
          {
            if(getFirstOwnGladiator()!=null)
            {
              if(getFirstOwnGladiator().has(RPCode.var_damage))
              {
                own_damage = getFirstOwnGladiator().getInt(RPCode.var_damage);
              }
            }
          }
          catch(Exception e)
          {
            marauroad.trace("GameDataModel::getFirstOwnGladiator","E",e.getMessage());
            e.printStackTrace(System.out);
          }
          if(getFightMode()==null || own_damage>0)
          {
            setRandomFightMode(strategy%1);
          }
          if(own_damage==0)
          {
            lastFightMode = null;
          }
        }
        finally
        {
          
        }
      }
    }
    if(Math.random()>0.99)
    {
      String cite = The1001Bot.getCite();
      
      if(cite!=null && cite.length()>0)
      {
        sendMessage(("QOTD \"" +cite+"\"").replace('\t',' ').replace('\n',' '));
      }
    }
    if(System.currentTimeMillis()-switchStrategyTS>=switchStrategyTimeOut)
    {
      marauroad.trace("GameDataModel::getFirstOwnGladiator","D","Strategy switched from "+strategy);
      //time to change strategy...
      strategy = random.nextInt(1);
      marauroad.trace("GameDataModel::getFirstOwnGladiator","D"," to "+strategy);
      switchStrategyTS=System.currentTimeMillis();
    }
    
    if(doPrint)
    {
      System.out.println(dumpToString());
    }
  }
  
  /**
   * Method countPeople
   *
   * @return   an int
   */
  private int countPeople()
  {
    int count = 0;
    for (Iterator iter = getSpectators().iterator(); iter.hasNext();)
    {
      RPObject rp_obj = (RPObject)iter.next();
      try
      {
        String name = rp_obj.get(RPCode.var_name);
        if(name.matches("[Bb][Oo][Tt]_.*|root[0-9][0-9][0-9]"))
        {
          //          System.out.println("Bot detected: " + name);
        }
        else
        {
          count++;
        }
      }
      catch (Attributes.AttributeNotFoundException e)
      {
        marauroad.trace("GameDataModel::getFirstOwnGladiator","E",e.getMessage());
        e.printStackTrace(System.out);
        count++;
      }
    }
    return count;
  }
  
  private String dumpToString()
  {
    int line_length=115;
    String top    = "+"+setStringWidth("-",'-',line_length)+"+\n";
    String middle = "|"+setStringWidth("-",'-',line_length)+"|\n";
    String empty  = "|"+setStringWidth(" ",' ',line_length)+"|\n";
    String bottom = "+"+setStringWidth("-",'-',line_length)+"+\n";
    // String divider="|"+setStringWidth("-",'-',line_length)+"|\n";
    String ret = top;
    String status = getStatus();
    int winner_id = -1;
    
    if(RPCode.var_request_fame.equals(status))
    {
      try
      {
        winner_id = getArena().getInt(RPCode.var_winner);
      }
      catch (Attributes.AttributeNotFoundException e)
      {
        marauroad.trace("GameDataModel::dumpToString","E",e.getMessage());
        e.printStackTrace(System.out);
      }
      status="Request fame(";
      try
      {
        String timeout      = getArena().get(RPCode.var_timeout);
        String thumbs_up    = getArena().get(RPCode.var_thumbs_up);
        String thumbs_down  = getArena().get(RPCode.var_thumbs_down);
        String waiting      = getArena().get(RPCode.var_waiting);
        String fame         = getArena().get(RPCode.var_karma);
        
        status+=fame+"): "+timeout + " Up: "+thumbs_up+" Down: "+thumbs_down+" Wait: "+waiting;
      }
      catch (Attributes.AttributeNotFoundException e)
      {
        marauroad.trace("GameDataModel::dumpToString","E",e.getMessage());
        e.printStackTrace(System.out);
      }
    }
    ret+=setStringWidth("|Arena: "+status,' ',line_length+1)+"|\n";
    ret+="|"+setStringWidth(" ",' ',line_length)+"|\n";
    ret+=middle;
    
    List spectators = getSpectators();
    
    if(spectators!=null&&spectators.size()>0)
    {
      ret+="|"+setStringWidth("Characters ",' ',line_length)+"|\n";
      ret+=middle;
      ret+="|"+setStringWidth(setStringWidth("Name",' ',21)+setStringWidth("Fame",' ',6)+setStringWidth("Karma",' ',6)+setStringWidth("s",' ',3)+setStringWidth("fmode",' ',10)+setStringWidth("freq",' ',10)+setStringWidth("Message",' ',58),' ',line_length)+"|\n";
      ret+=middle;
      for (int i = 0; i < spectators.size(); i++)
      {
        RPObject spectator = (RPObject)spectators.get(i);
        try
        {
          String name = spectator.get(RPCode.var_name);
          String fame = spectator.get(RPCode.var_fame);
          String msg  ="";
          
          if(spectator.has(RPCode.var_text))
          {
            msg  = spectator.get(RPCode.var_text);
          }
          
          String spec_id = spectator.get(RPCode.var_object_id);
          String fight_mode;
          String wait_to_req_fight;
          String karma;
          String strtg;
          RPObject gladiator = null;
          if(ownCharacterId.equals(spec_id))
          {
            gladiator = getFirstOwnGladiator();
            name="*"+name;
            fight_mode = currentFightMode;
            wait_to_req_fight = String.valueOf((REQ_FIGHT_WAIT_TIME - (System.currentTimeMillis()-lastReqFightTS))/1000);
            
            strtg = String.valueOf(strategy);
          }
          else
          {
            name=" "+name;
            fight_mode = "";
            wait_to_req_fight ="";
            karma = "";
            strtg="   ";
            gladiator = null;//getFirstGladiatorOf(spectators[i]);
          }
          karma=(gladiator==null?"":gladiator.get(RPCode.var_karma));
          if(gladiator!=null && winner_id==gladiator.getInt(RPCode.var_karma))
          {
            name="W"+name;
          }
          else
          {
            name=" "+name;
          }
          
          ret+="|"+setStringWidth(setStringWidth(name,' ',21)+setStringWidth(fame,' ',6)+setStringWidth(karma,' ',6)+setStringWidth(strtg,' ',3)+setStringWidth(fight_mode,' ',10)+setStringWidth(wait_to_req_fight,' ',10)+setStringWidth(msg,' ',58),' ',line_length)+"|\n";
        }
        catch (Attributes.AttributeNotFoundException e)
        {
          marauroad.trace("GameDataModel::dumpToString","E",e.getMessage());
          e.printStackTrace(System.out);
        }
      }
      ret+=middle;
      ret+=empty;
    }
    spectators=null;
    
    List fighters = getFighters();
    
    if(fighters!=null&&fighters.size()>0)
    {
      int own_glad_id=-1;
      try
      {
        if(getFirstOwnGladiator()!=null)
        {
          own_glad_id = getFirstOwnGladiator().getInt(RPCode.var_object_id);
        }
      }
      catch (Attributes.AttributeNotFoundException e)
      {
        marauroad.trace("GameDataModel::dumpToString","E",e.getMessage());
        e.printStackTrace(System.out);
      }
      ret+="|"+setStringWidth("Gladiators ",' ',line_length)+"|\n";
      ret+=middle;
      ret+="|"+setStringWidth(setStringWidth("Name",' ',25)+setStringWidth("Karma",' ',8)+setStringWidth("Health",' ',8)+setStringWidth("Damage",' ',8)+setStringWidth("Won",' ',5)+setStringWidth("Lost",' ',5)+setStringWidth("W/(W+L)%",' ',10),' ',line_length)+"|\n";
      ret+=middle;
      for (int i = 0; i < fighters.size(); i++)
      {
        RPObject fighter = (RPObject)fighters.get(i);
        try
        {
          String name   = fighter.get(RPCode.var_name);
          String karma  = fighter.get(RPCode.var_karma);
          String health = fighter.get(RPCode.var_hp);
          String damage ="";
          if(fighter.has(RPCode.var_damage))
          {
            damage = fighter.get(RPCode.var_damage);
          }
          else
          {
            damage = "N/A";
          }
          int won       = fighter.getInt(RPCode.var_num_victory);
          int lost      = fighter.getInt(RPCode.var_num_defeat);
          double winp   = ((double)won*100)/Math.max(won+lost,1);
          
          int i_winp    = (int)winp;
          int id        = fighter.getInt(RPCode.var_object_id);
          
          if(id==own_glad_id)
          {
            name="*"+name;
          }
          else
          {
            name=" "+name;
          }
          if(winner_id==id)
          {
            name="W"+name;
          }
          else
          {
            name=" "+name;
          }
          ret+="|"+setStringWidth(setStringWidth(name,' ',25)+setStringWidth(karma,' ',8)+setStringWidth(health,' ',8)+setStringWidth(damage,' ',8)+setStringWidth(""+won,' ',5)+setStringWidth(""+lost,' ',5)+setStringWidth(""+i_winp,' ',10),' ',line_length)+"|\n";
        }
        catch (Attributes.AttributeNotFoundException e)
        {
          marauroad.trace("GameDataModel::dumpToString","E",e.getMessage());
          e.printStackTrace(System.out);
          marauroad.trace("GameDataModel::dumpToString","E"," object was: "+fighter);
        }
      }
      ret+=middle;
      ret+=empty;
    }
    else
    {
      marauroad.trace("GameDataModel::dumpToString","E","NO GLadiators!!!");
    }
    ret+=bottom;
    return(ret);
  }
  
  private static String setStringWidth(String in, char ch, int length)
  {
    String ret = in==null?"":in;
    while(ret.length()<length){ret+=ch;}
    while(ret.length()>length){ret=ret.substring(0,ret.length()-2);}
    ret = ret.replace('\t',' ');
    return(ret);
  }
  
  private final class ActionHandler
    implements ActionListener
  {
    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
      if(e!=null)
      {
        String command = e.getActionCommand();
        
        if(CMD_FIGHT.equals(command))
        {
          requestFight();
        }
        else if(CMD_SCISSOR.equals(command))
        {
          setFightMode(RPCode.var_scissor);
        }
        else if(CMD_PAPER.equals(command))
        {
          setFightMode(RPCode.var_paper);
        }
        else if(CMD_STONE.equals(command))
        {
          setFightMode(RPCode.var_rock);
        }
        else if(CMD_VOTE_UP.equals(command)||CMD_VOTE_DOWN.equals(command))
        {
          vote(command);
        }
        else
        {
          marauroad.trace("GameDataModel.ActionHandler.actionPerformed","D","Unknown command "+command + ", e="+e);
        }
      }
    }
  }
}
