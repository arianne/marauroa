/* $Id: SimpleGame.java,v 1.15 2003/12/08 01:12:19 arianne_rpg Exp $ */
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
package simplegame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import marauroa.JMarauroa;
import marauroa.game.Attributes;
import marauroa.game.RPActionFactory;
import marauroa.game.RPObject;
import marauroa.game.RPObjectFactory;
import marauroa.game.RPSlot;
import marauroa.net.Message;
import marauroa.net.MessageC2SAction;
import marauroa.net.MessageC2SPerceptionACK;
import marauroa.net.MessageS2CPerception;
import marauroa.net.NetworkClientManager;
import simplegame.actions.ChallengeAction;
import simplegame.actions.ChallengeAnswer;
import simplegame.actions.GetCharacterListAction;
import simplegame.actions.MoveAction;
import simplegame.objects.CharacterList;
import simplegame.objects.GameBoard;

public class SimpleGame
  extends JFrame implements Runnable
{
  
  private NetworkClientManager netMan;
  private SimpleGameDataModel gdm;
  private JMarauroa marauroa;
  private int ownCharacterID;
  private int otherCharacterID;
  
  public SimpleGame(NetworkClientManager netman, JMarauroa marauroa,RPObject.ID characterID)
  {
    netMan = netman;
    
    //register our objects
    RPObjectFactory.getFactory().register(CharacterList.TYPE_CHARACTER_LIST,CharacterList.class);
    RPObjectFactory.getFactory().register(CharacterList.TYPE_CHARACTER_LIST_ENTRY,CharacterList.CharEntry.class);
    RPObjectFactory.getFactory().register(GameBoard.TYPE_GAME_BOARD,GameBoard.class);
    
    //register our actions
    RPActionFactory.getFactory().register(ChallengeAction.ACTION_CHALLENGE,ChallengeAction.class);
    RPActionFactory.getFactory().register(ChallengeAnswer.ACTION_CHALLENGE_ANSWER,ChallengeAnswer.class);
    RPActionFactory.getFactory().register(MoveAction.ACTION_MOVE,MoveAction.class);
    RPActionFactory.getFactory().register(GetCharacterListAction.ACTION_GETCHARLIST,GetCharacterListAction.class);
    
    this.marauroa = marauroa;
    this.ownCharacterID=characterID.getObjectID();
    this.otherCharacterID=-1;
    gdm = new SimpleGameDataModel(3);
    initComponents();
    addWindowListener(new WindowAdapter()
                      {
          public void windowClosing(WindowEvent e)
          {
            System.exit(0);
          }
        });
  }
  
  private void initComponents()
  {
    setContentPane(new GameDisplay(gdm));
  }
  
  public void run()
  {
    while(true)
    {
      if(netMan!=null)
      {
        Message msg = netMan.getMessage();
        if(msg!=null)
        {
          if(msg instanceof MessageS2CPerception)
          {
            MessageC2SPerceptionACK replyMsg=new MessageC2SPerceptionACK(msg.getAddress());
            replyMsg.setClientID(msg.getClientID());
            netMan.addMessage(replyMsg);
            MessageS2CPerception perception = (MessageS2CPerception)msg;
            List modified_objects = perception.getModifiedRPObjects();
            if(modified_objects!=null && modified_objects.size()>0)
            {
              //the only object we should see hier is the player object itself.
              RPObject obj = (RPObject)modified_objects.get(0);
              addLog(obj.toString()+"\n");
              
              // now it can be one of three things:
              // 1. GameBoard in "hand" slot
              // 2. CharacterList in "ear" slot
              // 3. other RPPlayer in "challenge" slot
              
              try //gameboard
              {
                GameBoard gb = (GameBoard)obj.getSlot("hand").get();
                int size = gb.getSize();
                for (int k = 0; k < size; k++)
                {
                  for (int l = 0; l < size; l++)
                  {
                    int id = gb.getRPCharacterAt(k,l);
                    gdm.setRPCharacterAt(k,l,id);
                  }
                }
              }
              catch (Exception e1)
              {
                try //charcterlist
                {
                  CharacterList clist = (CharacterList)obj.getSlot("ear").get();
                  addLog(""+clist+"\n");
                }
                catch(Exception e2)
                {
                  try
                  {
                    RPObject other_player = obj.getSlot("challenge").get();
                    addLog(""+other_player+"\n");
                  }
                  catch(Exception e3)
                  {
                  }
                }
              }
            }
          }
        }
      }
      else
      {
        sleep(50);
      }
    }
  }
  
  
  private static void sleep(long timeout)
  {
    try
    {
      Thread.sleep(timeout*1000);
    }
    catch (InterruptedException e)
    {
    }
  }
  
  
  private final class GameDisplay
    extends JPanel
  {
    private SimpleGameDataModel gameDataModel;
    
    private GameDisplay(SimpleGameDataModel gdm)
    {
      gameDataModel = gdm;
      gameDataModel.addGameUpdateListener(new SimpleGameDataModel.GameUpdateListener()
                                          {
            public void updateReceived(int row, int column, int characterID)
            {
              repaint();
            }
          });
      setPreferredSize(new Dimension(300,300));
      setMinimumSize(new Dimension(300,300));
      addMouseListener(new PlayerListener());
    }
    
    protected void paintComponent(Graphics g)
    {
      super.paintComponent(g);
      int w = getWidth();
      int h = getHeight();
      int board_size = gameDataModel.getSize();
      int cell_width  = w/board_size;
      int cell_height = h/board_size;
      int startx = cell_width/8;
      int starty = cell_height/8;
      
      int x = startx;
      for (int i = 0; i < board_size; i++)
      {
        x=startx+i*cell_width;
        int y = starty;
        for (int j = 0; j < board_size; j++)
        {
          y=starty+j*cell_height;
          Color color = null;
          if(otherCharacterID!=-1)
          {
            if(otherCharacterID==gameDataModel.getRPCharacterAt(j,i))
            {
              color = Color.red;
            }
            else if(ownCharacterID==gameDataModel.getRPCharacterAt(j,i))
            {
              color = Color.green;
            }
            if(color!=null)
            {
              g.setColor(color);
              g.fillOval(x,y,cell_width*3/4,cell_height*3/4);
            }
          }
        }
      }
    }
    
    private final class PlayerListener
      extends MouseAdapter
    {
      public void mouseClicked(MouseEvent e)
      {
        if(otherCharacterID==-1)
        {
          addLog("get the list of players...\n");
          GetCharacterListAction rpaction = new GetCharacterListAction();
          MessageC2SAction msg = new MessageC2SAction(null,rpaction);
          netMan.addMessage(msg);
        }
        else
        {
          int w = getWidth();
          int h = getHeight();
          int board_size = gameDataModel.getSize();
          int cell_width  = w/board_size;
          int cell_height = h/board_size;
          Point pnt = e.getPoint();
          int column = pnt.x/cell_width + (pnt.x%cell_width>0?1:0)-1;
          int row = pnt.y/cell_height + (pnt.y%cell_height>0?1:0)-1;
          if(netMan!=null)
          {
            MoveAction rpaction = new MoveAction();
            rpaction.put("object_id", ""+ownCharacterID);
            rpaction.setRow(row);
            rpaction.setColumn(column);
            MessageC2SAction msg = new MessageC2SAction(null,rpaction);
            netMan.addMessage(msg);
          }
          addLog("Player makes move on [" +row +","+column+"]\n");
        }
      }
    }
  }
  
  private void addLog(String msg)
  {
    if(marauroa!=null)
    {
      marauroa.addLog(msg);
    }
    else
    {
      System.out.print(msg);
    }
  }
  
}

