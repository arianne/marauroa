/* $Id: SimpleGame.java,v 1.48 2004/02/07 20:41:00 root777 Exp $ */
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

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
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
	private final static long serialVersionUID = 4712;
	private transient NetworkClientManager netMan;
	private transient SimpleGameDataModel gdm;
	private transient JMarauroa marauroa;
	private transient RPObject ownCharacter;
	private int ownCharacterID;
	private int otherCharacterID;
	private static Sequencer player;
	private boolean continueMusicPlay;
	private boolean continueGamePlay;
	private JLabel statusLine;
	
	public SimpleGame(NetworkClientManager netman, JMarauroa marauroa,RPObject.ID characterID)
	{
		netMan = netman;
		
		this.marauroa = marauroa;
		ownCharacter=null;
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
		setTitle("Tic-Tac-Toe");
	}
	
	private void initComponents()
	{
		JPanel main_panel = new JPanel(new BorderLayout());
		GameDisplay  gd = new GameDisplay(gdm);
		main_panel.add(gd,BorderLayout.CENTER);
		statusLine = new JLabel("<html><body>Launching <font color=blue>Tic-Tac-Toe</font>...</body></html>");
		main_panel.add(statusLine,BorderLayout.SOUTH);
		setContentPane(main_panel);
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
					if(msg!=null && msg instanceof MessageS2CPerception)
					{
						MessageC2SPerceptionACK replyMsg=new MessageC2SPerceptionACK(msg.getAddress());
						replyMsg.setClientID(msg.getClientID());
						netMan.addMessage(replyMsg);
						
						MessageS2CPerception perception = (MessageS2CPerception)msg;
						List modified_objects = perception.getModifiedRPObjects();
						if(modified_objects.size()>0)
						{
							//the only object we should see here is the player object itself.
							RPObject obj = (RPObject)modified_objects.get(0);
							ownCharacter=obj;
							addLog(obj.toString()+"\n");
							
							if(obj.hasSlot("hand"))
							{
								/** CASE 1: Game already started. */
								GameBoard gameBoard= new GameBoard(obj.getSlot("hand").get());
								
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
									statusLine.setText(msgResolution);
									JOptionPane.showMessageDialog(marauroa,msgResolution,msgResolution,JOptionPane.INFORMATION_MESSAGE);
									continueGamePlay  = false;
									continueMusicPlay = false;
									
									SimpleGame.this.dispose();
								}
							}
							else if(obj.hasSlot("challenge"))
							{
								/* CASE 2: Player is challenged by another player. We choose the first player. */
								CharacterList characterList=new CharacterList(obj.getSlot("challenge").get());
								addLog(""+characterList+"\n");
								
								String player=(String)characterList.CharacterIterator().next();
								otherCharacterID = characterList.getId(player);
								
								ChallengeAnswer answer = new ChallengeAnswer();
								answer.setWho(ownCharacterID);
								answer.setWhom(otherCharacterID);
								answer.setAccept(true);
								MessageC2SAction m_action = new MessageC2SAction(null,answer);
								netMan.addMessage(m_action);
								statusLine.setText("Accepted challenge from " + otherCharacterID);
								addLog("Accepted challenge from " + otherCharacterID);
								playMidi();
							}
							else if(obj.hasSlot("ear") && otherCharacterID==-1)
							{
								if(obj.getSlot("ear").size()==0)
								{
									addLog("Waiting for another player to join\n");
									statusLine.setText("Waiting for another player to join");
									addLog("get the UPDATED list of players...\n");
									GetCharacterListAction rpaction = new GetCharacterListAction();
									netMan.addMessage(new MessageC2SAction(null,rpaction));
								}
								else
								{
									RPObject baseObject=(RPObject)obj.getSlot("ear").get();
									CharacterList characterList=new CharacterList(baseObject);
									System.out.println(characterList.toString());
									Iterator iterator = characterList.CharacterIterator();
									
									addLog("Received character list with following chars:\n");
									Object[] message = new Object[2];
									message[0]="Characters:";
									
									JComboBox cb_characters = new JComboBox();
									cb_characters.setEditable(false);
									message[1] = cb_characters;
									
									while(iterator.hasNext())
									{
										String player = (String)iterator.next();
										cb_characters.addItem(player);
										addLog(player+"\n");
									}
									if(cb_characters.getItemCount()>0)
									{
										// Options
										String[] options = {"Challenge"};
										int result = JOptionPane.showOptionDialog(
																															this,                                       // the parent that the dialog blocks
																															message,                                    // the dialog message array
																															"Choose the character to challenge...",     // the title of the dialog window
																															JOptionPane.DEFAULT_OPTION,                 // option type
																															JOptionPane.INFORMATION_MESSAGE,            // message type
																															new ImageIcon("wurst.png"),                 // optional icon, use null to use the default icon
																															options,                                    // options string array, will be made into buttons
																															options[0]                                  // option that should be made into a default button
																														 );
										
										String player=(String)cb_characters.getSelectedItem();
										if(result!=JOptionPane.CLOSED_OPTION &&player!=null && !player.equals(""))
										{
											addLog("Challenge player...\n");
											statusLine.setText("Challenge player...");
											otherCharacterID = CharacterList.getId(player);
											System.out.println(ownCharacterID+" challenged "+otherCharacterID);
											ChallengeAction c_action=new ChallengeAction();
											c_action.setWho(ownCharacterID);
											c_action.setWhom(otherCharacterID);
											netMan.addMessage(new MessageC2SAction(null,c_action));
										}
										else
										{
											addLog("get the UPDATED list of players...\n");
											GetCharacterListAction rpaction = new GetCharacterListAction();
											netMan.addMessage(new MessageC2SAction(null,rpaction));
										}
									}
									//                                  else
									//                                  {
									//                                      statusLine.setText("No characters received from server...");
									//                                      sleep(500);
									//                                      addLog("get the UPDATED list of players...\n");
									//                                      GetCharacterListAction rpaction = new GetCharacterListAction();
									//                                      netMan.addMessage(new MessageC2SAction(null,rpaction));
									//                                  }
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
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * causes the calling thread to sleep the specified amount of <b>seconds</b>
	 * @param timeout the amount of seconds to sleep
	 **/
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
	
	/**
	 * represents the gameboard
	 **/
	private final class GameDisplay
	extends JPanel
	{
		private final static long serialVersionUID = 4713;
		private transient SimpleGameDataModel gameDataModel;
		
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
				if(ownCharacter==null || (ownCharacter!=null && !ownCharacter.hasSlot("hand")))
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
								InputStream is_midi = getClass().getClassLoader().getResourceAsStream("sounds/bgmusic_001.mid");
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





