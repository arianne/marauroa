/* $Id: GameDataModel.java,v 1.13 2004/03/26 21:55:26 root777 Exp $ */
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
  public final static int REQ_FIGHT_WAIT_TIME = 2*60*1000; // two minutes
  public final static String ARENA_MODE_WAITING  = RPCode.var_waiting;
  public final static String ARENA_MODE_FIGHTING = RPCode.var_fighting;
  public final static String ARENA_MODE_REQ_FAME = RPCode.var_request_fame;
  // private RPObject ownGladiator;
  private RPObject ownCharacter;
  private RPObject arena;
  private Map spectators;
  private Map fighters;
  private Map shopGladiators;
  private Map myGladiators;
  private transient NetworkClientManager netMan;
  private List listeners;
  private ActionListener commandListener;
  private String status;
  private String currentFightMode;
  private long lastReqFightTS;
  private boolean voted;
  private Random random=new Random(System.currentTimeMillis());
  public GameDataModel(NetworkClientManager net_man)
    {
    netMan     = net_man;
    spectators = new LinkedHashMap(8);
    fighters   = new LinkedHashMap(2);
    shopGladiators = new LinkedHashMap(4);
    myGladiators   = new LinkedHashMap(1);
    listeners  = new ArrayList(1);
    commandListener = new ActionHandler();
    lastReqFightTS = System.currentTimeMillis()-REQ_FIGHT_WAIT_TIME/2;
    voted=false;
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
   * Sets Arena
   *
   * @param    Arena               a  RPObject
   */
  public void setArena(RPObject arena)
    {
    this.arena = arena;
    }
  
  /**
   * Returns Arena
   *
   * @return    a  RPObject
   */
  public RPObject getArena()
    {
    return arena;
    }
  
  /**
   * Sets OwnCharacter
   *
   * @param    OwnCharacter        a  RPObject
   */
  public void setOwnCharacter(RPObject ownCharacter)
    {
    this.ownCharacter = ownCharacter;
    }
  
  /**
   * Returns OwnCharacter
   *
   * @return    a  RPObject
   */
  public RPObject getOwnCharacter()
    {
    return ownCharacter;
    }
  
  public ActionListener getActionHandler()
    {
    return(commandListener);
    }
  
  public void setStatus(String mode)
    {
    this.status = mode;
    }
  
  public String getStatus()
    {
    return(status);
    }
  
  /**
   * adds a new spectator to world.
   * if the spectator is already there then the old instance
   * will be replaced by a new one
   **/
  public void addSpectator(RPObject spectator)
    {
    synchronized(spectators)
      {
      try
        {
        spectators.put(spectator.get(RPCode.var_object_id),spectator);
        fireListeners();
        }
      catch (Attributes.AttributeNotFoundException e)
        {
        marauroad.trace("The1001Game::addSpectator","X",e.getMessage());
        }
      }
    }
  
  /**
   * adds a new spectator to world.
   * if the spectator is already there then the old instance
   * will be replaced by a new one
   **/
  public void addMyGladiator(RPObject gladiator)
    {
    synchronized(myGladiators)
      {
      try
        {
        myGladiators.put(gladiator.get(RPCode.var_object_id),gladiator);
        fireListeners();
        }
      catch (Attributes.AttributeNotFoundException e)
        {
        marauroad.trace("The1001Game::addMyGladiator","X",e.getMessage());
        }
      }
    }
  
  /**
   * adds a new spectator to world.
   * if the spectator is already there then the old instance
   * will be replaced by a new one
   **/
  public void addShopGladiator(RPObject gladiator)
    {
    synchronized(gladiator)
      {
      try
        {
        shopGladiators.put(gladiator.get(RPCode.var_object_id),gladiator);
        fireListeners();
        }
      catch (Attributes.AttributeNotFoundException e)
        {
        marauroad.trace("The1001Game::addShopGladiator","X",e.getMessage());
        }
      }
    }
  
  /**
   * Method dumpList
   *
   * @param    spectators          a  List
   *
   */
  private void dumpList(Collection rpobjects)
    {
    for (Iterator iter = rpobjects.iterator(); iter.hasNext();)
      {
      marauroad.trace("#","D",""+iter.next());
      }
    }
  
  /**
   * deletes the spectator from world, if he was there.
   **/
  public void deleteShopGladiator(RPObject gladiator)
    {
    synchronized(shopGladiators)
      {
      try
        {
        if(shopGladiators.remove(gladiator.get(RPCode.var_object_id))!=null)
          {
          fireListeners();
          }
        }
      catch (Attributes.AttributeNotFoundException e)
        {
        marauroad.trace("The1001Game::deleteShopGladiator","X",e.getMessage());
        }
      }
    fireListeners();
    }
  
  /**
   * deletes the spectator from world, if he was there.
   **/
  public void deleteMyGladiator(RPObject gladiator)
    {
    synchronized(myGladiators)
      {
      try
        {
        if(myGladiators.remove(gladiator.get(RPCode.var_object_id))!=null)
          {
          fireListeners();
          }
        }
      catch (Attributes.AttributeNotFoundException e)
        {
        marauroad.trace("The1001Game::deleteMyGladiator","X",e.getMessage());
        }
      }
    fireListeners();
    }
  
  /**
   * deletes the spectator from world, if he was there.
   **/
  public void deleteSpectator(RPObject spectator)
    {
    synchronized(spectators)
      {
      try
        {
        if(spectators.remove(spectator.get(RPCode.var_object_id))!=null)
          {
          fireListeners();
          }
        }
      catch (Attributes.AttributeNotFoundException e)
        {
        marauroad.trace("The1001Game::deleteSpectator","X",e.getMessage());
        }
      }
    fireListeners();
    }
  
  /**
   * returns all the spectators
   **/
  public RPObject[] getSpectators()
    {
    synchronized(spectators)
      {
      RPObject[] spectators_a = new RPObject[spectators.size()];

      return((RPObject[])spectators.values().toArray(spectators_a));
      }
    }
  
  /**
   * returns all the gladiator in the shop
   **/
  public RPObject[] getShopGladiators()
    {
    synchronized(shopGladiators)
      {
      RPObject[] gladiators_a = new RPObject[shopGladiators.size()];

      return((RPObject[])shopGladiators.values().toArray(gladiators_a));
      }
    }
  
  /**
   * adds a new fighter to arena.
   * if the fighter is already there then the old instance
   * will be replaced by a new one
   **/
  public void addFighter(RPObject fighter)
    {
    synchronized(fighters)
      {
      try
        {
        fighters.put(fighter.get(RPCode.var_object_id),fighter);
        fireListeners();
        }
      catch (Attributes.AttributeNotFoundException e)
        {
        marauroad.trace("The1001Game::addFighter","X",e.getMessage());
        }
      }
    }
  
  /**
   * deletes the fighter from world, if he was there.
   **/
  public void deleteFighter(RPObject fighter)
    {
    try
      {
      if(fighters.remove(fighter.get(RPCode.var_object_id))!=null)
        {
        fireListeners();
        }
      }
    catch (Attributes.AttributeNotFoundException e)
      {
      marauroad.trace("The1001Game::deleteFighter","X",e.getMessage());
      }
    }
  
  public RPObject[] getFighters()
    {
    synchronized(fighters)
      {
      RPObject[] fighters_a = new RPObject[fighters.size()];

      return((RPObject[])fighters.values().toArray(fighters_a));
      }
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
  
  /**
   * Method getFirstOwnGladiator
   *
   * @return   a  RPObject
   */
  public RPObject getFirstOwnGladiator()
    {
    RPObject gladiator = null;

    if(myGladiators.size()>0)
      {
      gladiator = (RPObject)myGladiators.values().iterator().next();
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
  
  public void setRandomFightMode()
    {
    if(RPCode.var_scissor.equals(getFightMode()))
      {
      setFightMode(Math.random()>0.5?RPCode.var_paper:RPCode.var_rock);
      }
    else if(RPCode.var_paper.equals(getFightMode()))
      {
      setFightMode(Math.random()>0.5?RPCode.var_scissor:RPCode.var_rock);
      }
    else
      {
      setFightMode(Math.random()>0.5?RPCode.var_scissor:RPCode.var_paper);
      }
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
        gl_id = gladiator.getInt("object_id");
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
  
  private void fireListeners()
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
        RPObject glads_in_shop[] = getShopGladiators();

        if(glads_in_shop.length>0)
          {
          RPObject first_avail_glad = glads_in_shop[Math.abs(random.nextInt()%glads_in_shop.length)];

          buyGladiator(first_avail_glad.get(RPCode.var_object_id));
          }
        }
      }
    catch (Attributes.AttributeNotFoundException e)
      {
      e.printStackTrace();
      }
    if(RPCode.var_waiting.equals(getStatus())||RPCode.var_request_fame.equals(getStatus()))
      {
      if(REQ_FIGHT_WAIT_TIME<(System.currentTimeMillis()-lastReqFightTS))
        {
        requestFight();
        lastReqFightTS = System.currentTimeMillis();
        }
      voted=false;
      currentFightMode=null;
      }
    else if(RPCode.var_request_fame.equals(getStatus()))
      {
      if(!voted)
        {
        vote(Math.random()>0.5?RPCode.var_voted_up:"VOTE_DOWN");
        voted = true;
        }
      currentFightMode=null;
      }
    else if(RPCode.var_fighting.equals(getStatus()))
      {
      voted=false;
      if(ownCharacter.has(RPCode.var_fighting))
        {
        try
          {
          int damage = 0;

          if(ownCharacter.has(RPCode.var_damage))
            {
            damage = ownCharacter.getInt(RPCode.var_damage);
            }
          if(getFightMode()==null || damage>0)
            {
            setRandomFightMode();
            }
          }
        catch (Attributes.AttributeNotFoundException e)
          {
          e.printStackTrace();
          }
        }
      }
    if(Math.random()>0.99)
      {
      String cite = The1001Bot.getCite();

      if(cite!=null && cite.length()>0)
        {
        sendMessage(("cite: " +cite).replace('\t',' ').replace('\n',' '));
        }
      }
    if(doPrint)
      {
      System.out.println(dumpToString());
      }
    }
  
  public String dumpToString()
    {
    String top    = "+"+setStringWidth("-",'-',80)+"+\n";
    String middle = "|"+setStringWidth("-",'-',80)+"|\n";
    String empty  = "|"+setStringWidth(" ",' ',80)+"|\n";
    String bottom = "+"+setStringWidth("-",'-',80)+"+\n";
    // String divider="|"+setStringWidth("-",'-',80)+"|\n";
    String ret = top;
    String status = getStatus()==null?"":getStatus();
    
    if(RPCode.var_request_fame.equals(status))
      {
      try
        {
        String timeout      = getArena().get(RPCode.var_timeout);
        String thumbs_up    = getArena().get(RPCode.var_thumbs_up);
        String thumbs_down  = getArena().get(RPCode.var_thumbs_down);
        String waiting      = getArena().get(RPCode.var_waiting);
        String fame         = getArena().get(RPCode.var_karma);

        status="Request fame("+fame+"): "+timeout + " Up: "+thumbs_up+" Down: "+thumbs_down+" Wait: "+waiting;
        }
      catch (Attributes.AttributeNotFoundException e) {}
      }
    ret+=setStringWidth("|Arena: "+status,' ',81)+"|\n";
    ret+="|"+setStringWidth(" ",' ',80)+"|\n";
    ret+=middle;
    
    RPObject [] spectators = getSpectators();

    if(spectators!=null&&spectators.length>0)
      {
      int own_char_id=-1;

      try
        {
        if(getOwnCharacter()!=null)
          {
          own_char_id = getOwnCharacter().getInt(RPCode.var_object_id);
          }
        }
      catch (Attributes.AttributeNotFoundException e) {}
      ret+="|"+setStringWidth("Characters ",' ',80)+"|\n";
      ret+=middle;
      ret+="|"+setStringWidth(setStringWidth("Name",' ',25)+setStringWidth("Fame",' ',6)+setStringWidth("Karma",' ',6)+setStringWidth("fmode",' ',10)+setStringWidth("freq",' ',10)+setStringWidth("Message",' ',23),' ',80)+"|\n";
      ret+=middle;
      for (int i = 0; i < spectators.length; i++)
        {
        try
          {
          String name = spectators[i].get(RPCode.var_name);
          String fame = spectators[i].get(RPCode.var_fame);
          String msg  ="<>";

          if(spectators[i].has(RPCode.var_text))
            {
            msg  = spectators[i].get(RPCode.var_text);
            }

          int id      = spectators[i].getInt(RPCode.var_object_id);
          String fight_mode;
          String wait_to_req_fight;
          String karma;

          if(id==own_char_id)
            {
            name="*"+name;
            fight_mode = currentFightMode;
            wait_to_req_fight = String.valueOf((REQ_FIGHT_WAIT_TIME - (System.currentTimeMillis()-lastReqFightTS))/1000);
            karma=(getFirstOwnGladiator()==null?"":getFirstOwnGladiator().get(RPCode.var_karma));
            }
          else
            {
            name=" "+name;
            fight_mode = "";
            wait_to_req_fight ="";
            karma = "";
            }
          ret+="|"+setStringWidth(setStringWidth(name,' ',25)+setStringWidth(fame,' ',6)+setStringWidth(karma,' ',6)+setStringWidth(fight_mode,' ',10)+setStringWidth(wait_to_req_fight,' ',10)+setStringWidth(msg,' ',29),' ',80)+"|\n";
          }
        catch (Attributes.AttributeNotFoundException e) {}
        }
      ret+=middle;
      ret+=empty;
      }
    spectators=null;

    RPObject [] fighters = getFighters();

    if(fighters!=null&&fighters.length>0)
      {
      int own_glad_id=-1;

      try
        {
        if(getFirstOwnGladiator()!=null)
          {
          own_glad_id = getFirstOwnGladiator().getInt(RPCode.var_object_id);
          }
        }
      catch (Attributes.AttributeNotFoundException e) {}
      ret+="|"+setStringWidth("Gladiators ",' ',80)+"|\n";
      ret+=middle;
      ret+="|"+setStringWidth(setStringWidth("Name",' ',25)+setStringWidth("Karma",' ',8)+setStringWidth("Health",' ',8)+setStringWidth("Damage",' ',8)+setStringWidth("Won",' ',5)+setStringWidth("Lost",' ',5)+setStringWidth("W/(W+L)%",' ',10),' ',80)+"|\n";
      ret+=middle;
      for (int i = 0; i < fighters.length; i++)
        {
        try
          {
          String name   = fighters[i].get(RPCode.var_name);
          String karma  = fighters[i].get(RPCode.var_karma);
          String health = fighters[i].get(RPCode.var_hp);
          String damage ="";

          if(fighters[i].has(RPCode.var_damage))
            {
            damage = fighters[i].get(RPCode.var_damage);
            }

          int won       = fighters[i].getInt(RPCode.var_num_victory);
          int lost      = fighters[i].getInt(RPCode.var_num_defeat);
          double winp   = ((double)won)/(won+lost+0.0001);

          winp*=100;

          int i_winp    = (int)winp;
          int id        = fighters[i].getInt(RPCode.var_object_id);

          if(id==own_glad_id)
            {
            name="*"+name;
            }
          else
            {
            name=" "+name;
            }
          ret+="|"+setStringWidth(setStringWidth(name,' ',25)+setStringWidth(karma,' ',8)+setStringWidth(health,' ',8)+setStringWidth(damage,' ',8)+setStringWidth(""+won,' ',5)+setStringWidth(""+lost,' ',5)+setStringWidth(""+i_winp,' ',10),' ',80)+"|\n";
          }
        catch (Attributes.AttributeNotFoundException e)
          {
          e.printStackTrace(System.out);
          System.out.println("object was: "+fighters[i]);
          }
        }
      ret+=middle;
      ret+=empty;
      }
    ret+=bottom;
    return(ret);
    }
  
  public static String setStringWidth(String in, char ch, int length)
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
          marauroad.trace("GameDataModel","D","Unknown command "+command + ", e="+e);
          }
        }
      }
    }
  }
