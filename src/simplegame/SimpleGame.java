/* $Id: SimpleGame.java,v 1.26 2003/12/17 17:21:59 arianne_rpg Exp $ */
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
import java.io.InputStream;
import java.util.List;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import marauroa.JMarauroa;
import marauroa.game.RPObject;
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
import sun.audio.AudioPlayer;

public class SimpleGame
  extends JFrame implements Runnable, MetaEventListener
{
  
  private NetworkClientManager netMan;
  private SimpleGameDataModel gdm;
  private JMarauroa marauroa;
  private int ownCharacterID;
  private int otherCharacterID;
  private static Sequencer player;
  private boolean continueMusicPlay;
  private boolean continueGamePlay;
  
  public SimpleGame(NetworkClientManager netman, JMarauroa marauroa,RPObject.ID characterID)
  {
    netMan = netman;
    
    this.marauroa = marauroa;
    this.ownCharacterID=characterID.getObjectID();
    this.otherCharacterID=-1;
    gdm = new SimpleGameDataModel(3);
    initComponents();
    addWindowListener(new WindowAdapter()
                      {
          public void windowClosing(WindowEvent e)
          {
            continueMusicPlay = false;
            continueGamePlay  = false;
          }
        });
  }
  
  private void initComponents()
  {
    setContentPane(new GameDisplay(gdm));
  }
  
  public void run()
  {
    continueGamePlay = true;
    try
    {    
    while(continueGamePlay)
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
            if(modified_objects.size()>0)
            {
              //the only object we should see hier is the player object itself.
              RPObject obj = (RPObject)modified_objects.get(0);
              addLog(obj.toString()+"\n");
              
              if(obj.hasSlot("hand"))
                {
                /** CASE 1: Game already started. */
                GameBoard gameBoard=(GameBoard)obj.getSlot("hand").get();
                
                int size = gameBoard.getSize();
                for(int i=0;i<size;++i)
                  {
                  for(int j=0;j<size;++j)
                    {
                    int id = gameBoard.getRPCharacterAt(i,j);
                    gdm.setRPCharacterAt(i,j,id);
                    }
                  }
                               
                if(gameBoard.getWinnerID()!=-1)
                  {
                  String msgResolution;
                  if(gameBoard.getWinnerID()==ownCharacterID)
                    {
                    msgResolution = "You won.";
                    }
                  else
                    {
                    msgResolution = "You lost.";
                    }
                  JOptionPane.showMessageDialog(marauroa,msgResolution,msgResolution,JOptionPane.INFORMATION_MESSAGE);
                  continueGamePlay  = false;
                  continueMusicPlay = false;
                  
                  SimpleGame.this.dispose();
                  }                
                }
              else if(obj.hasSlot("challenge"))
                {
                /* CASE 2: Player is challenged by another player. We choose the first player. */
                CharacterList characterList=(CharacterList)obj.getSlot("challenge").get();
                addLog(""+characterList+"\n");
                
                String player=characterList.iterator().next();                                
                otherCharacterID = characterList.getId(player);
                
                ChallengeAnswer answer = new ChallengeAnswer();
                answer.setWho(ownCharacterID);
                answer.setWhom(otherCharacterID);
                answer.setAccept(true);
                MessageC2SAction m_action = new MessageC2SAction(null,answer);
                netMan.addMessage(m_action);
                
                addLog("Accepted challenge from " + otherCharacterID);
                playMidi();
                }
              else if(obj.hasSlot("ear"))
                {
                CharacterList characterList=(CharacterList)obj.getSlot("ear").get();
                Iterator iterator = characterList.iterator();
                if(iterator!=null)
                {
                  addLog("Received character list with following chars:\n");
                  Object[]      message = new Object[2];
                  message[0] = "Characters:";
                  
                  JComboBox cb_characters = new JComboBox();
                  cb_characters.setEditable(false);
                  message[1] = cb_characters;
                  
                  
                  while(iterator.hasNext())
                  {
                    CharacterList.CharEntry  entry = iterator.next();
                    cb_characters.addItem(entry);
                    addLog(entry.getId()+"|"+entry.getName()+"|"+entry.getStatus()  +"\n");
                  }
                  // Options
                  String[] options = {"Challenge"};
                  int result = JOptionPane.showOptionDialog(
                    this,                             // the parent that the dialog blocks
                    message,                                    // the dialog message array
                    "Choose the character to challenge...", // the title of the dialog window
                    JOptionPane.DEFAULT_OPTION,                 // option type
                    JOptionPane.INFORMATION_MESSAGE,            // message type
                    new ImageIcon("wurst.png"),                 // optional icon, use null to use the default icon
                    options,                                    // options string array, will be made into buttons
                    options[0]                                  // option that should be made into a default button
                  );
                  if(result!=JOptionPane.CLOSED_OPTION)
                  {
                    CharacterList.CharEntry  entry = (CharacterList.CharEntry)cb_characters.getSelectedItem();
                    otherCharacterID = Integer.parseInt(entry.getId());
                    ChallengeAction c_action = new ChallengeAction();
                    c_action.setWho(ownCharacterID);
                    c_action.setWhom(otherCharacterID);
                    netMan.addMessage(new MessageC2SAction(null,c_action));
                  }
                }
                addLog(""+clist+"\n");
                playMidi();
                }
              
//              	/** BUG: OMG, should test that slot doesn't exist with hasSlot */
//                if(otherCharacterID==-1)
//                {
//                  try
//                  {
//                    CharacterList clist = (CharacterList)obj.getSlot("challenge").get();
//                    addLog(""+clist+"\n");
//                    otherCharacterID = Integer.parseInt(clist.iterator().next().getId());
//                    ChallengeAnswer answer = new ChallengeAnswer();
//                    answer.setWho(ownCharacterID);
//                    answer.setWhom(otherCharacterID);
//                    answer.setAccept(true);
//                    MessageC2SAction m_action = new MessageC2SAction(null,answer);
//                    netMan.addMessage(m_action);
//                    addLog("Accepted challenge from " + otherCharacterID);
//                    playMidi();
//                  }
//                  catch(Exception e2)
//                  {
//                    try //charcterlist
//                    {
//                      CharacterList clist = (CharacterList)obj.getSlot("ear").get();
//                      CharacterList.CharEntryIterator iterator = clist.iterator();
//                      if(iterator!=null)
//                      {
//                        addLog("Received character list with following chars:\n");
//                        Object[]      message = new Object[2];
//                        message[0] = "Characters:";
//                        
//                        JComboBox cb_characters = new JComboBox();
//                        cb_characters.setEditable(false);
//                        message[1] = cb_characters;
//                        
//                        
//                        while(iterator.hasNext())
//                        {
//                          CharacterList.CharEntry  entry = iterator.next();
//                          cb_characters.addItem(entry);
//                          addLog(entry.getId()+"|"+entry.getName()+"|"+entry.getStatus()  +"\n");
//                        }
//                        // Options
//                        String[] options = {"Challenge"};
//                        int result = JOptionPane.showOptionDialog(
//                          this,                             // the parent that the dialog blocks
//                          message,                                    // the dialog message array
//                          "Choose the character to challenge...", // the title of the dialog window
//                          JOptionPane.DEFAULT_OPTION,                 // option type
//                          JOptionPane.INFORMATION_MESSAGE,            // message type
//                          new ImageIcon("wurst.png"),                 // optional icon, use null to use the default icon
//                          options,                                    // options string array, will be made into buttons
//                          options[0]                                  // option that should be made into a default button
//                        );
//                        if(result!=JOptionPane.CLOSED_OPTION)
//                        {
//                          CharacterList.CharEntry  entry = (CharacterList.CharEntry)cb_characters.getSelectedItem();
//                          otherCharacterID = Integer.parseInt(entry.getId());
//                          ChallengeAction c_action = new ChallengeAction();
//                          c_action.setWho(ownCharacterID);
//                          c_action.setWhom(otherCharacterID);
//                          netMan.addMessage(new MessageC2SAction(null,c_action));
//                        }
//                      }
//                      addLog(""+clist+"\n");
//                      playMidi();
//                    }
//                    catch(Exception e3)
//                    {
//                      //                    e3.printStackTrace();
//                    }
//                  }
//                }
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
  catch(Exception e)
    {    
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
        playWav(getClass().getClassLoader().getResourceAsStream("sounds/human_spell.wav"));
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
  
  public static void playWav(final InputStream wavstream)
  {
    AudioPlayer.player.start(wavstream);
    //    new Thread(new Runnable()
    //               {
    //          public void run()
    //          {
    //            try
    //            {
    //              AudioPlayer.player.start(wavstream);
    //              Sequence theSound = MidiSystem.getSequence(midistream);
    //              if(player==null)
    //              {
    //                player = MidiSystem.getSequencer();
    //                player.open();
    //              }
    //              else
    //              {
    //                player.stop();
    //              }
    //              player.setSequence(theSound);
    //              player.start();
    //              //player.close();
    //            }
    //            catch (Exception ex)
    //            {
    //              ex.printStackTrace();
    //            }
    //          }},"midi player").start();
  }
  
  public void playMidi()
  {
    new Thread(new Runnable()
               {
          public void run()
          {
            try
            {
              if(player==null)
              {
                InputStream is_midi = getClass().getClassLoader().getResourceAsStream("sounds/1.mid");
                Sequence theSound = MidiSystem.getSequence(is_midi);
                player = MidiSystem.getSequencer();
                player.open();
                player.setSequence(theSound);
                player.addMetaEventListener(SimpleGame.this);
                continueMusicPlay = true;
              }
              player.start();
              //player.close();
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
          }},"midi player").start();
  }
  
  
  //implement metaEventListener
  public void meta(MetaMessage event)
  {
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
    System.out.println("MIDI:"+String.valueOf(event.getType()));
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
    if (event.getType() == 47)
    {
      System.out.println("song ended");
      // Sequencer is done playing
      if (continueMusicPlay && player != null)
      {
        player.setTickPosition(0);
        playMidi();
      }
    }
  }
  
}





