/* $Id: The1001Game.java,v 1.1 2004/02/15 19:21:06 root777 Exp $ */
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import marauroa.JMarauroa;
import marauroa.game.Attributes;
import marauroa.game.RPAction;
import marauroa.game.RPObject;
import marauroa.game.RPSlot;
import marauroa.marauroad;
import marauroa.net.Message;
import marauroa.net.MessageC2SAction;
import marauroa.net.MessageC2SPerceptionACK;
import marauroa.net.MessageS2CPerception;
import marauroa.net.NetworkClientManager;
import the1001.RPCode;

/**
 *
 *
 *@author Waldemar Tribus
 */
public class The1001Game
extends JFrame implements Runnable
{
	private final static long serialVersionUID = 4714;
	private transient NetworkClientManager netMan;
	private JMarauroa marauroa;
	private transient RPObject ownCharacter;
	private int ownCharacterID;
	private JLabel statusLine;
	private boolean continueGamePlay;
	private transient GameDataModel gm;
	private JButton btnRqFight;
	
	
	public The1001Game(NetworkClientManager netman, JMarauroa marauroa,RPObject.ID characterID)
	{
		netMan = netman;
		
		this.marauroa = marauroa;
		ownCharacter=null;
		this.ownCharacterID=characterID.getObjectID();
		initComponents();
		setTitle("Gladiators (the1001)");
	}
	
	private void initComponents()
	{
		JPanel main_panel = new JPanel(new BorderLayout());
		gm = new GameDataModel(netMan);
//		GameDisplay  gd = new GameDisplay(gm);
//		main_panel.add(gd,BorderLayout.WEST);
		btnRqFight = new JButton("Request fight");
		btnRqFight.addActionListener(new ActionListener()
																 {
					public void actionPerformed(ActionEvent e)
					{
						gm.requestFight();
					}
				});
		btnRqFight.setEnabled(true);
		statusLine = new JLabel("<html><body>Launching <font color=blue>Gladiators</font>...</body></html>");
		main_panel.add(btnRqFight,BorderLayout.NORTH);
		main_panel.add(statusLine,BorderLayout.SOUTH);
		The1001Game3D g3d = new The1001Game3D(gm);
		g3d.setSize(500,500);
		main_panel.add(g3d,BorderLayout.CENTER);
//		main_panel.add(g3d);
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
						for (int i = 0; i < modified_objects.size(); i++)
						{
							RPObject obj = (RPObject)modified_objects.get(i);
							if("arena".equals(obj.get("type")))
							{
								String name = obj.get("name");
								String status = obj.get("status");
//								gm.setWaiting("waiting".equalsIgnoreCase(status));
								System.out.println("Arena: " + name + " " + status );
								try
								{
									RPSlot slot = obj.getSlot("gladiators");
									for (Iterator iter = slot.iterator(); iter.hasNext() ; )
									{
										RPObject gladiator = (RPObject)iter.next();
										if("gladiator".equalsIgnoreCase(gladiator.get("type")))
										{
											gm.addFighter(gladiator);
										}
										else
										{
											marauroad.trace("the1001client.messageLoop","D","Ignored wrong object in arena");
										}
									}
								}
								catch (RPObject.NoSlotFoundException e)
								{
									marauroad.trace("The1001Game::messageLoop","X","Arena has no slot gladiators");
								}
							}
							else if("character".equals(obj.get("type")))
							{
								gm.addSpectator(obj);
								int id = obj.getInt("object_id");
								if(ownCharacterID==id)
								{
									RPSlot glad_slot   = obj.getSlot("gladiators");
									RPObject gladiator = glad_slot.get();
									gm.setGladiator(gladiator);
								}
							}
							else
							{
								marauroad.trace("The1001Game::messageLoop","D","Ignored wrong object in perception");
							}
						}
						
						List deleted_objects = perception.getDeletedRPObjects();
						for (int i = 0; i < deleted_objects.size(); i++)
						{
							RPObject obj = (RPObject)deleted_objects.get(i);
							gm.deleteSpectator(obj);
							gm.deleteFighter(obj);
						}
						repaint();
					}
				}
				else
				{
					sleep(5);
				}
			}
		}
		catch(Exception e)
		{
			marauroad.trace("The1001Game::messageLoop","X",e.getMessage());
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
	
	/**
	 * it was the attempt to create a plain swing gladitors client.
	 * was replaced by The1001Game3D, but can be used parallel to 3d client
	 * represents the gameboard
	 **/
	private final class GameDisplay
	extends JPanel
	{
		private final static long serialVersionUID = 4715;
		private transient GameDataModel gm;
		
		private GameDisplay(GameDataModel gm)
		{
			this.gm = gm;
			setPreferredSize(new Dimension(600,600));
			setMinimumSize(new Dimension(600,600));
		}
		
		protected void paintComponent(Graphics g)
		{
			g.setColor(getBackground());
			g.fillRect(0,0,getWidth(),getHeight());
			paintArena(g);
			paintSpectators(g);
		}
		
		protected void paintArena(Graphics g)
		{
			g.fillOval(getWidth()/2-2,getHeight()/2-2,4,4);
			int width  = getWidth()*2/3;
			int height = getHeight()*2/3;
			int x_d = (getWidth()-width)/2;
			int y_d = (getHeight()-height)/2;
			g.setColor(Color.green.brighter());
			g.drawOval(x_d,y_d,width,height);
			g.setColor(Color.green.darker().darker());
			g.fillOval(x_d+10,y_d+10,width-20,height-20);
			paintFighters(g);
		}
		
		protected void paintSpectators(Graphics g)
		{
			RPObject spectators[] = gm.getSpectators();
			double angle = 2*Math.PI/spectators.length;
			double radius = (getWidth()*2/3+getWidth()/16)/2;
			for (int i = 0;i<spectators.length; i++)
			{
				double sin = Math.sin(angle*(double)i);
				double cos = Math.cos(angle*(double)i);
				int x = (int)(radius*cos)+getWidth()/2;
				int y = (int)(radius*sin)+getHeight()/2;
				paintSpectator(x,y,spectators[i],g);
			}
		}
		
		protected void paintFighters(Graphics g)
		{
			RPObject fighters[] = gm.getFighters();
			double angle = 2*Math.PI/fighters.length;
			double radius = getWidth()/6;
			for (int i = 0;i<fighters.length; i++)
			{
				double sin = Math.sin(angle*(double)i);
				double cos = Math.cos(angle*(double)i);
				int x = (int)(radius*cos)+getWidth()/2;
				int y = (int)(radius*sin)+getHeight()/2;
				paintFighter(x,y,fighters[i],g);
			}
		}
		
		/**
		 * Method paintFighter
		 *
		 * @param    x_f                 an int
		 * @param    y_f                 an int
		 * @param    w_f                 an int
		 * @param    h_f                 an int
		 * @param    fighter             a  RPObject
		 * @param    g                   a  Graphics
		 *
		 */
		private void paintFighter(int x_f, int y_f, RPObject fighter, Graphics g)
		{
			int radius = getWidth()/18;
			boolean own_gladiator = fighter.equals(gm.getGladiator());
			if(own_gladiator)
			{
				g.setColor(Color.blue);
			}
			else
			{
				g.setColor(Color.red);
			}
			g.fillOval(x_f-radius/2,y_f-radius/2,radius,radius);
			if(own_gladiator)
			{
				g.setColor(Color.blue.brighter());
			}
			else
			{
				g.setColor(Color.white);
			}
			try
			{
				g.drawString(String.valueOf(fighter.get("name")),x_f-radius/2,y_f-radius/2);
			}
			catch (Attributes.AttributeNotFoundException e)
			{
				marauroad.trace("the1001client::paintFighter","X",e.getMessage());
			}
		}
		
		
		protected void paintSpectator(int x, int y, RPObject spectator, Graphics g)
		{
			int radius = getWidth()/18;
			g.setColor(Color.gray);
			g.fillOval(x-radius/2,y-radius/2,radius,radius);
			g.setColor(Color.white);
			try
			{
				g.drawString(String.valueOf(spectator.get("name")),x-radius/2,y-radius/2);
			}
			catch (Attributes.AttributeNotFoundException e)
			{
				marauroad.trace("the1001client::paintSpectator","X",e.getMessage());
			}
		}
	}
	
}

