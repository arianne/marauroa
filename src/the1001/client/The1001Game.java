/* $Id: The1001Game.java,v 1.19 2004/04/26 22:08:23 arianne_rpg Exp $ */
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

import javax.swing.*;
import marauroa.net.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.SocketException;
import java.util.Map;
import marauroa.game.RPObject;
import marauroa.game.RPZone;
import marauroa.marauroad;
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
  private JLabel statusLine;
  private boolean continueGamePlay;
  private transient GameDataModel gm;
  private JTextArea chatTextArea;
  private OggPlayer player;
  private boolean loggedOut;
  
  private The1001Game(NetworkClientManager netman)
  {
    netMan = netman;
    initComponents();
    setTitle("Gladiators (the1001)");
    addWindowListener(new MWindowListener());
    player = new OggPlayer();
  }
  
  private void initComponents()
  {
    JPanel main_panel = new JPanel(new BorderLayout());
    
    gm = new GameDataModel(netMan);
    statusLine = new JLabel("<html><body>Launching <font color=blue>Gladiators</font>...</body></html>");
    
    The1001Game3D g3d = new The1001Game3D(gm);
    
    g3d.setSize(500,500);
    main_panel.add(g3d,BorderLayout.CENTER);
    chatTextArea = new JTextArea();
    chatTextArea.setEditable(false);
    chatTextArea.setLineWrap(false);
    
    JScrollPane sp = new JScrollPane(chatTextArea);
    
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    sp.setPreferredSize(new Dimension(500,120));
    
    final JTextField tf = new JTextField(40);
    
    tf.addActionListener(new ActionListener()
                         {
          public void actionPerformed(ActionEvent ae)
          {
            String text = tf.getText();
            
            if(!"".equals(text))
            {
              gm.sendMessage(text);
              tf.setText("");
            }
          }
        });
    
    JButton btn = new JButton("Send");
    
    btn.addActionListener(new ActionListener()
                          {
          public void actionPerformed(ActionEvent ae)
          {
            String text = tf.getText();
            
            if(!"".equals(text))
            {
              gm.sendMessage(text);
              tf.setText("");
            }
          }
        });
    
    JPanel pnl_chat = new JPanel();
    
    pnl_chat.add(tf);
    pnl_chat.add(btn);
    
    JPanel all_chat = new JPanel(new BorderLayout());
    
    all_chat.add(sp,BorderLayout.CENTER);
    all_chat.add(statusLine,BorderLayout.NORTH);
    all_chat.add(pnl_chat,BorderLayout.SOUTH);
    main_panel.add(all_chat,BorderLayout.SOUTH);
    setContentPane(main_panel);
  }
  
  /**
   * adds a message into reportPane
   */
  private void addChatMessage(String name,String msg)
  {
    String text = name+":"+msg;
    
    chatTextArea.append(text+"\n");
    chatTextArea.setCaretPosition(chatTextArea.getText().length());
  }
  
  public void run()
  {
    int time_out_max_count = 20;
    int timeout_count = 0;
    continueGamePlay = true;
    loggedOut = false;
    
    boolean synced = false;
    try
    {
      int previous_timestamp=0;
      while(continueGamePlay)
      {
        if(netMan!=null)
        {
          Message msg = netMan.getMessage();
          
          if(msg!=null)
          {
            if(msg instanceof MessageS2CPerception)
            {
              timeout_count = 0;
              MessageC2SPerceptionACK replyMsg=new MessageC2SPerceptionACK(msg.getAddress());
              
              replyMsg.setClientID(msg.getClientID());
              netMan.addMessage(replyMsg);
              
              MessageS2CPerception perception = (MessageS2CPerception)msg;
              boolean full_perception = perception.getTypePerception()==RPZone.Perception.SYNC;
              
              if(!synced)
              {
                synced=full_perception;
                marauroad.trace("The1001Game::messageLoop","D",synced?"Synced.":"Unsynced!");
              }
              if(full_perception)
              {
                previous_timestamp=perception.getPerceptionTimestamp()-1;
              }
              marauroad.trace("The1001Game::messageLoop","D",full_perception?"TOTAL PRECEPTION":"DELTA PERCEPTION");
              
              if(synced)
              {
                if(previous_timestamp+1!=perception.getPerceptionTimestamp())
                {
                  marauroad.trace("The1001Game::messageLoop","D","We are out of sync. Waiting for sync perception");
                  marauroad.trace("The1001Game::messageLoop","D","Expected "+previous_timestamp+" but we got "+perception.getPerceptionTimestamp());
                  synced=false;
                  /* TODO: Try to regain sync by getting more messages in the hope of getting the out of order perception */
                }
              }
              
              if(synced)
              {
                Map world_objects = gm.getAllObjects();
                if(full_perception)
                {
                  gm.clearAllObjects();
                  //full perception contains all objects???
                }
                try
                {
                  previous_timestamp=perception.applyPerception(world_objects,previous_timestamp,null);
                  RPObject my_object = perception.getMyRPObject();
                  if(my_object!=null)
                  {
                    gm.setOwnCharacterID(my_object.get(RPCode.var_object_id));
                  }
                }
                catch (MessageS2CPerception.OutOfSyncException e)
                {
                  e.printStackTrace();
                  synced=false;
                }
                gm.react(false);
                gm.fireListeners();
              }
              else
              {
                marauroad.trace("The1001Bot::messageLoop","D","Waiting for sync...");
              }
            }
            else if(msg instanceof MessageS2CLogoutACK)
            {
              loggedOut=true;
              marauroad.trace("The1001Game::messageLoop","D","Logged out...");
              sleep(20);
              System.exit(-1);
            }
            else if(msg instanceof MessageS2CActionACK)
            {
              MessageS2CActionACK msg_act_ack = (MessageS2CActionACK)msg;
              marauroad.trace("The1001Bot::messageLoop","D",msg_act_ack.toString());
            }
            else
            {
              // something other than
            }
          }
          else
          {
            timeout_count++;
            if(timeout_count>=time_out_max_count)
            {
              marauroad.trace("The1001Game::messageLoop","D","TIMEOUT. EXIT.");
              System.exit(1);
            }
            sleep(1);
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
      marauroad.trace("The1001Bot::messageLoop","X",e.getMessage());
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
   *
   */
  public static void main(String[] args)
  {
    if(args.length!=3)
      showSplash(1000);
    login(args);
  }
  
  /**
   * Method showSplash
   *
   * @param    duration         a long
   *
   */
  private static void showSplash(long duration)
  {
    JWindow window = new JWindow();
    
    window.getContentPane().add(new JLabel(new ImageIcon(Resources.getImageUrl("Logo.png"))));
    window.pack();
    
    Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
    
    window.setLocation((int)(screen_size.getWidth()/2-window.getWidth()/2),(int)(screen_size.getHeight()/2-window.getHeight()/2));
    window.show();
    try
    {
      Thread.sleep(duration);
    }
    catch (InterruptedException e)
    {
    }
    window.setVisible(false);
  }
  
  private static void connectAndChooseCharacter(String hostname, String user, String pwd)
  {
    NetworkClientManager net_man;
    int client_id = -1;
    
    try
    {
      net_man=new NetworkClientManager(hostname);
      
      MessageC2SLogin msg=new MessageC2SLogin(null,user,pwd);
      
      net_man.addMessage(msg);
      
      boolean complete=false;
      int recieved=0;
      String[] characters=null;
      String[] serverInfo=null;
      
      client_id=-1;
      while(!complete && recieved<20)
      {
        Message message=net_man.getMessage();
        
        ++recieved;
        if(message!=null)
        {
          marauroad.trace("The1001Game::connectAndChooseCharacter","D","new message, waiting for "+Message.TYPE_S2C_LOGIN_ACK + ", receivied "+message.getType());
          switch(message.getType())
          {
            case Message.TYPE_S2C_LOGIN_NACK:
              complete=true;
              break;
            case Message.TYPE_S2C_LOGIN_ACK:     // 10
              client_id=message.getClientID();
              break;
            case Message.TYPE_S2C_CHARACTERLIST: // 2
              characters=((MessageS2CCharacterList)message).getCharacters();
              break;
            case Message.TYPE_S2C_SERVERINFO:    // 7
              serverInfo=((MessageS2CServerInfo)message).getContents();
              break;
          }
        }
        complete = complete || ((serverInfo!=null) && (characters!=null) && (client_id!=-1));
      }
      if(!complete)
      {
        JOptionPane.showMessageDialog(null,"Failed connect to server, exiting.","Server",JOptionPane.ERROR_MESSAGE);
        System.exit(-1);
      }
      marauroad.trace("The1001Game::connectAndChooseCharacter","D","characters: "+characters);
      if(characters!=null && characters.length>0)
      {
        chooseCharacter(net_man, client_id, characters[0]);
      }
      else
      {
        JOptionPane.showMessageDialog(null,"No characters received from server - wrong username/password?");
        System.exit(-1);
      }
    }
    catch(MessageFactory.InvalidVersionException e)
    {
      marauroad.trace("The1001Game::messageLoop","X","Not able to connect to server because you are using an outdated client");
      System.exit(-1);
    }
    catch(SocketException e)
    {
      marauroad.trace("The1001Game::connectAndChooseCharacter","X",e.getMessage());
    }
  }
  
  private static void chooseCharacter(NetworkClientManager netman, int client_id, String character)
  {
    Message msg=new MessageC2SChooseCharacter(null,character);
    
    msg.setClientID(client_id);
    netman.addMessage(msg);
    
    Message message=null;
    boolean complete=false;
    int recieved=0;
    try
    {
      while(!complete && recieved<20)
      {
        message=netman.getMessage();
        recieved++;
        if(message!=null)
        {
          marauroad.trace("The1001Game::chooseCharacter","D","new message, waiting for "+Message.TYPE_S2C_CHOOSECHARACTER_ACK + ", receivied "+message.getType());
          if(message.getType()==Message.TYPE_S2C_CHOOSECHARACTER_ACK)
          {
            The1001Game game = new The1001Game(netman);
            
            game.pack();
            game.show();
            new Thread(game,"Game thread...").start();
            complete = true;
          }
          else if(message.getType()==Message.TYPE_S2C_CHOOSECHARACTER_NACK)
          {
            marauroad.trace("The1001Game::chooseCharacter","E","server nacks the character, exiting...");
            System.exit(-1);
          }
        }
      }
    }
    catch(MessageFactory.InvalidVersionException e)
    {
      marauroad.trace("The1001Game::messageLoop","X","Not able to connect to server because you are using an outdated client");
      System.exit(-1);
    }
    if(!complete)
    {
      JOptionPane.showMessageDialog(null,"The server doesn't answer, exiting.","Communication failure",JOptionPane.ERROR_MESSAGE);
      System.exit(-1);
    }
  }
  
  private static void login(String args[])
  {
    String pwd = "";
    String hostname = "";
    String user_name = "";
    
    if(args.length==3)
    {
      hostname = args[0];
      user_name = args[1];
      pwd = args[2];
    }
    else
    {
      // Messages
      Object[]      message = new Object[6];
      
      message[0] = "Server to login:";
      
      JComboBox cb_server = new JComboBox();
      
      cb_server.addItem("marauroa.ath.cx");
      cb_server.addItem("127.0.0.1");
      cb_server.addItem("tribus.dyndns.org");
      cb_server.addItem("192.168.100.100");
      cb_server.addItem("localhost");
      cb_server.setEditable(true);
      message[1] = cb_server;
      message[2] = "User:";
      
      JComboBox cb_user = new JComboBox();
      
      cb_user.addItem("Test Player");
      cb_user.addItem("Another Test Player");
      cb_user.setEditable(true);
      message[3] = cb_user;
      message[4] = "Password:";
      
      JPasswordField pf_pwd = new JPasswordField();
      
      pf_pwd.setText("Test Password");
      message[5] = pf_pwd;
      
      // Options
      String[] options = {"Connect","Cancel",};
      int result = JOptionPane.showOptionDialog(
        null,                             // the parent that the dialog blocks
        message,                                    // the dialog message array
        "Login to...", // the title of the dialog window
        JOptionPane.DEFAULT_OPTION,                 // option type
        JOptionPane.INFORMATION_MESSAGE,            // message type
        new ImageIcon("wurst.png"),                 // optional icon, use null to use the default icon
        options,                                    // options string array, will be made into buttons
        options[0]                                  // option that should be made into a default button
      );
      
      switch(result)
      {
        case 0: // connect
          {
            if(cb_server.getSelectedItem()!=null)
            {
              hostname = String.valueOf(cb_server.getSelectedItem());
            }
            else
            {
              hostname = String.valueOf(cb_server.getEditor().getItem());
            }
            if(cb_user.getSelectedItem()!=null)
            {
              user_name = String.valueOf(cb_user.getSelectedItem());
            }
            else
            {
              user_name = String.valueOf(cb_user.getEditor().getItem());
            }
            pwd = new String(pf_pwd.getPassword());
          }
          break;
        case 1: // cancel
          marauroad.trace("The1001Game::chooseCharacter","E","User dont want to login, exiting...");
          System.exit(-1);
          break;
        default:
          break;
      }
    }
    connectAndChooseCharacter(hostname, user_name,pwd);
  }
  
  private static void disconnect(NetworkClientManager net_man)
  {
    if(net_man!=null)
    {
      Message msg=new MessageC2SLogout(null);
      
      net_man.addMessage(msg);
      net_man.finish();
    }
  }
  private final class MWindowListener
    implements WindowListener
  {
    /**
     * Invoked when a window has been closed as the result
     * of calling dispose on the window.
     */
    public void windowClosed(WindowEvent e)
    {
    }
    
    /**
     * Invoked the first time a window is made visible.
     */
    public void windowOpened(WindowEvent e)
    {
    }
    
    /**
     * Invoked when the user attempts to close the window
     * from the window's system menu.  If the program does not
     * explicitly hide or dispose the window while processing
     * this event, the window close operation will be cancelled.
     */
    public void windowClosing(WindowEvent e)
    {
      disconnect(netMan);
      System.exit(-1);
    }
    
    /**
     * Invoked when a Window is no longer the active Window. Only a Frame or a
     * Dialog can be the active Window. The native windowing system may denote
     * the active Window or its children with special decorations, such as a
     * highlighted title bar. The active Window is always either the focused
     * Window, or the first Frame or Dialog that is an owner of the focused
     * Window.
     */
    public void windowDeactivated(WindowEvent e)
    {
    }
    
    /**
     * Invoked when a window is changed from a normal to a
     * minimized state. For many platforms, a minimized window
     * is displayed as the icon specified in the window's
     * iconImage property.
     * @see java.awt.Frame#setIconImage
     */
    public void windowIconified(WindowEvent e)
    {
    }
    
    /**
     * Invoked when the Window is set to be the active Window. Only a Frame or
     * a Dialog can be the active Window. The native windowing system may
     * denote the active Window or its children with special decorations, such
     * as a highlighted title bar. The active Window is always either the
     * focused Window, or the first Frame or Dialog that is an owner of the
     * focused Window.
     */
    public void windowActivated(WindowEvent e)
    {
    }
    
    /**
     * Invoked when a window is changed from a minimized
     * to a normal state.
     */
    public void windowDeiconified(WindowEvent e)
    {
    }
  }
}
