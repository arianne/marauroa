/**
 * JMarauroa.java
 * derived from JMarauroa
 * dont no if someone like it
 * @author Waldemar Tribus
 */

package marauroa;


import javax.swing.*;
import marauroa.net.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Test client for marauroa
 **/
public class JMarauroa extends JFrame
{
  private final static String ACTION_CMD_LOGIN = "login";
  private final static String ACTION_CMD_DISCONNECT = "disconnect";
  private final static String ACTION_CMD_EXIT = "exit";
  private final static String ACTION_CMD_ABOUT = "about";
  
  
  //  private JButton actionButton;
  //  private JButton chooseCharacterButton;
  //  private JButton connectButton;
  //  private JButton disconnectButton;
  private JTextArea reportsTextArea;
  
  private short clientId;
  private NetworkClientManager netMan;
  
  private SimpleDateFormat formatter;
  private Date logDate;
  
  private ActionHandler actionHandler;
  
  
  public JMarauroa()
  {
    setTitle("Marauroa test client");
    //    setIconImage(new ImageIcon("marauroa.png").getImage());
    initMenu();
    initComponents();
    clientId=-10;
    formatter = new SimpleDateFormat("[HH:MM:ss.SSS]  ");
    logDate = new Date();
    addWindowListener(new WindowAdapter()
                      {
          public void windowOpened(WindowEvent we)
          {
            login();
          }
          public void windowClosing(WindowEvent p0)
          {
            exit();
          }
        });
  }
  
  private void connectAndChooseCharacter(String hostname, String user, String pwd)
  {
    addLog("Starting network client manager\n");
    try
    {
      netMan=new NetworkClientManager(hostname);
    }
    catch(SocketException e)
    {
      addLog("Exception starting network client manager\n");
      addLog(e);
    }
    
    MessageC2SLogin msg=new MessageC2SLogin(null,user,pwd);
    netMan.addMessage(msg);
    
    boolean is_correct_login=false;
    Message msgReply=null;
    while(msgReply==null)
    {
      msgReply=netMan.getMessage();
      
      if(msgReply==null) continue;
      
      if(msgReply.getType()==Message.TYPE_S2C_LOGIN_NACK)
      {
        addLog("Login reject because "+((MessageS2CLoginNACK)msgReply).getResolution()+"\n");
      }
      
      if(msgReply.getType()==Message.TYPE_S2C_LOGIN_ACK)
      {
        addLog("Login successful\n");
        clientId=msgReply.getClientID();
        addLog("Recieved clientid: "+clientId+"\n");
        is_correct_login=true;
      }
    }
    
    msgReply=null;
    String[] characters = null;
    while(is_correct_login && (msgReply==null))
    {
      msgReply=netMan.getMessage();
      
      if(msgReply==null) continue;
      
      if(msgReply.getType()==Message.TYPE_S2C_CHARACTERLIST)
      {
        addLog("Character list received\n");
        characters =((MessageS2CCharacterList)msgReply).getCharacters();
        for(int i=0;i<characters.length;++i)
        {
          addLog("- "+characters[i]+"\n");
        }
      }
    }
    
    if(characters!=null && characters.length>0)
    {
      Object[]      message = new Object[2];
      message[0] = "Characters:";
      
      JComboBox cb_characters = new JComboBox();
      for (int i = 0; i < characters.length; i++)
      {
        cb_characters.addItem(characters[i]);
      }
      
      message[1] = cb_characters;
      
      // Options
      String[] options = {"Choose"};
      int result = JOptionPane.showOptionDialog(
        this,                             // the parent that the dialog blocks
        message,                                    // the dialog message array
        "Choose your character...", // the title of the dialog window
        JOptionPane.DEFAULT_OPTION,                 // option type
        JOptionPane.INFORMATION_MESSAGE,            // message type
        new ImageIcon("wurst.png"),                 // optional icon, use null to use the default icon
        options,                                    // options string array, will be made into buttons
        options[0]                                  // option that should be made into a default button
      );
      switch(result)
      {
        case 0: // choose character
          {
            String character = String.valueOf(cb_characters.getSelectedItem());
            chooseCharacter(character);
          }
          break;
        case 1: // cancel
          break;
        default:
          break;
      }
      
    }
    else
    {
      JOptionPane.showMessageDialog(this,"No characters received from server");
    }
    
  }
  
  
  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    JMarauroa2 marauroa = new JMarauroa2();
    marauroa.pack();
    Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension own_dimen = marauroa.getSize();
    int x = screen_size.width/2 - own_dimen.width/2;
    int y = screen_size.height/2 - own_dimen.height/2;
    marauroa.setLocation(x,y);
    marauroa.show();
  }
  
  
  /** Exit the Application */
  private void exit()
  {
    System.exit(0);
  }
  
  private void initMenu()
  {
    if(actionHandler==null)
    {
      actionHandler = new ActionHandler();
    }
    JMenuBar menubar = new JMenuBar();
    JMenu mnu_server = new JMenu("Server");
    mnu_server.setMnemonic('S');
    JMenuItem mnu_item_connect = new JMenuItem("Login");
    mnu_item_connect.setAccelerator(KeyStroke.getKeyStroke("control L"));
    mnu_item_connect.setMnemonic('C');
    mnu_item_connect.addActionListener(actionHandler);
    mnu_item_connect.setActionCommand(ACTION_CMD_LOGIN);
    mnu_server.add(mnu_item_connect);
    
    JMenuItem mnu_item_disconnect = new JMenuItem("Disconnect");
    mnu_item_disconnect.setAccelerator(KeyStroke.getKeyStroke("control D"));
    mnu_item_disconnect.addActionListener(actionHandler);
    mnu_item_disconnect.setActionCommand(ACTION_CMD_DISCONNECT);
    mnu_item_disconnect.setMnemonic('D');
    mnu_server.add(mnu_item_disconnect);
    
    JMenuItem mnu_item_exit = new JMenuItem("Exit");
    mnu_item_exit.setAccelerator(KeyStroke.getKeyStroke("control X"));
    mnu_item_exit.addActionListener(actionHandler);
    mnu_item_exit.setActionCommand(ACTION_CMD_EXIT);
    mnu_item_exit.setMnemonic('X');
    mnu_server.add(mnu_item_exit);
    menubar.add(mnu_server);
    
    JMenu menu_help = new JMenu("Help");
    menu_help.setMnemonic('H');
    JMenuItem mnu_item = new JMenuItem("About");
    mnu_item.setMnemonic('A');
    mnu_item.addActionListener(actionHandler);
    mnu_item.setActionCommand(ACTION_CMD_ABOUT);
    menu_help.add(mnu_item);
    menubar.add(menu_help);
    
    setJMenuBar(menubar);
  }
  
  
  
  /**
   * This method is called from within the init() method to
   * initialize the form.
   */
  private void initComponents()
  {
    JPanel main_panel = new JPanel(new BorderLayout());
    reportsTextArea = new JTextArea();
    reportsTextArea.setEditable(false);
    reportsTextArea.setLineWrap(false);
    JScrollPane sp = new JScrollPane(reportsTextArea);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    main_panel.add(sp,BorderLayout.CENTER);
    main_panel.setPreferredSize(new Dimension(580, 200));
    main_panel.setMinimumSize(new Dimension(580, 200));
    setContentPane(main_panel);
  }
  
  
  private void chooseCharacter(String character)
  {
    /* TODO: Write here the code to send Choose Character message */
    addLog(character+" choosen\n");
    Message msg=new MessageC2SChooseCharacter(null,character);
    msg.setClientID(clientId);
    
    netMan.addMessage(msg);
    
    Message msgReply=null;
    while(msgReply==null)
    {
      msgReply=netMan.getMessage();
      
      if(msgReply==null) continue;
      
      if(msgReply.getType()==Message.TYPE_S2C_CHOOSECHARACTER_ACK)
      {
        addLog("Character choosen correctly\n");
      }
      
      if(msgReply.getType()==Message.TYPE_S2C_CHOOSECHARACTER_NACK)
      {
        addLog("Character not choosen. Error.\n");
      }
    }
  }
  
  
  /**
   * adds a message into reportPane
   */
  private void addLog(String msg)
  {
  	System.out.println(msg);
    logDate.setTime(System.currentTimeMillis());
    String line = formatter.format(logDate) + msg;
    reportsTextArea.append(line);
    reportsTextArea.setCaretPosition(reportsTextArea.getText().length());
  }
  
  private void addLog(Throwable  thr)
  {
    ByteArrayOutputStream out= new ByteArrayOutputStream();
    thr.printStackTrace(new PrintStream(out));
  	System.out.println(out.toString());
    addLog(out.toString());
  }
  
  
  
  private void login()
  {
    // In a ComponentDialog, you can show as many message components and
    // as many options as you want:
    
    // Messages
    Object[]      message = new Object[6];
    message[0] = "Server to login:";
    
    
    JComboBox cb_server = new JComboBox();
    cb_server.addItem("127.0.0.1");
    cb_server.addItem("192.168.100.100");
    cb_server.addItem("localhost");
    cb_server.setEditable(true);
    message[1] = cb_server;
    
    message[2] = "User:";
    
    JTextField tf_user = new JTextField();
    tf_user.setText("Test Player");
    message[3] = tf_user;
    
    message[4] = "Password:";
    
    JPasswordField pf_pwd = new JPasswordField();
    pf_pwd.setText("Test Password");
    message[5] = pf_pwd;
    
    
    // Options
    String[] options = {"Connect","Cancel",};
    int result = JOptionPane.showOptionDialog(
      this,                             // the parent that the dialog blocks
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
          String hostname = null;
          if(cb_server.getSelectedItem()!=null)
          {
            hostname = String.valueOf(cb_server.getSelectedItem());
          }
          else
          {
            hostname = String.valueOf(cb_server.getEditor().getItem());
          }
          String user = tf_user.getText();
          String pwd  = new String(pf_pwd.getPassword());
          connectAndChooseCharacter(hostname, user,pwd);
          
        }
        break;
      case 1: // cancel
        break;
      default:
        break;
    }
  }
  
  private void disconnect()
  {
    if(netMan!=null)
    {
      Message msg=new MessageC2SLogout(null);
      msg.setClientID(clientId);
      netMan.addMessage(msg);
      netMan.finish();
      JOptionPane.showMessageDialog(this,"Disconnected");
      netMan = null;
    }
    else
    {
      JOptionPane.showMessageDialog(this,"Not connected!!!");
    }
  }
  
  private void about()
  {
    JOptionPane.showMessageDialog(this,"<html><body><font color=\"blue\">Marauroa</font> client version 0.01"
      +"<p>Marauroa is a multiplayer role playing game <br>"
      +"and a framework for further development completly based<br>"
      +"on Java. The goal is to get an Arianne compatible client<br>"
      +"and server.<br><br>"
      +"<font color=\"blue\">http://marauroa.sf.net</font></body></html>","About",JOptionPane.INFORMATION_MESSAGE);
  }
  
  
  private final class ActionHandler
    implements ActionListener
  {
    
    public void actionPerformed(ActionEvent e)
    {
      if(ACTION_CMD_LOGIN.equals(e.getActionCommand()))
      {
        //login
        login();
      }
      else if(ACTION_CMD_DISCONNECT.equals(e.getActionCommand()))
      {
        //disconnect
        disconnect();
      }
      else if(ACTION_CMD_EXIT.equals(e.getActionCommand()))
      {
        //exit
        exit();
      }
      else if(ACTION_CMD_ABOUT.equals(e.getActionCommand()))
      {
        //about
        about();
      }
      else
      {
        addLog("ignored unknown action: " +e);
      }
    }
    
  }
  
}

