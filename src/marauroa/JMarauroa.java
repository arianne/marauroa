package marauroa;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import marauroa.net.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import marauroa.game.RPObject;
import simplegame.SimpleGame;


/**
 * Test client for marauroa
 **/
public class JMarauroa
  extends JFrame
{
  private final static String ACTION_CMD_LOGIN = "login";
  private final static String ACTION_CMD_DISCONNECT = "disconnect";
  private final static String ACTION_CMD_EXIT = "exit";
  private final static String ACTION_CMD_ABOUT = "about";
  private final static String ACTION_CMD_PLAY = "play";
  
  
  private JTextArea reportsTextArea;
  private JComponent glassPane;
  private ActionHandler actionHandler;
  
  private SimpleDateFormat formatter;
  private Date logDate;
  
  private short clientId;
  private NetworkClientManager netMan;
  private RPObject.ID characterID;
  

  public JMarauroa()
  {
    actionHandler = new ActionHandler();
    actionHandler.start();
    setTitle("Marauroa test client");
    setIconImage(new ImageIcon(getClass().getClassLoader().getResource("images/marauroa_ICON.png")).getImage());
    initMenu();
    initComponents();
    clientId=-10;
    formatter = new SimpleDateFormat("[HH:MM:ss.SSS]  ");
    logDate = new Date();
    addWindowListener(new WindowAdapter()
                      {
          public void windowOpened(WindowEvent we)
          {
            actionHandler.add(new ActionEvent(this,0,ACTION_CMD_LOGIN));
          }
          public void windowClosing(WindowEvent p0)
          {
            exit();
          }
        });
    glassPane = new JPanel();
    glassPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    glassPane.setOpaque(false);
    glassPane.addMouseListener(new MouseAdapter(){} );
    glassPane.addKeyListener(new KeyAdapter()
                             {
          public void keyPressed(KeyEvent e)
          {
            if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
            {
            }
          }} );
    glassPane.addFocusListener(new FocusListener()
                               {
          
          public void focusGained(FocusEvent e)
          {
          }
          
          public void focusLost(FocusEvent e)
          {
          }
          
        });
    setGlassPane(glassPane);
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
    JMarauroa marauroa = new JMarauroa();
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
    
    JMenuItem mnu_item_letsplay = new JMenuItem("Let us play!");
    mnu_item_letsplay.setAccelerator(KeyStroke.getKeyStroke("control P"));
    mnu_item_letsplay.addActionListener(actionHandler);
    mnu_item_letsplay.setActionCommand(ACTION_CMD_PLAY);
    mnu_item_letsplay.setMnemonic('P');
    mnu_server.add(mnu_item_letsplay);
    
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
    BackgroundImagePanel main_panel = new BackgroundImagePanel(new BorderLayout());
    reportsTextArea = new JTextArea();
    reportsTextArea.setEditable(false);
    reportsTextArea.setLineWrap(false);
    JScrollPane sp = new JScrollPane(reportsTextArea);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    main_panel.add(sp,BorderLayout.CENTER);
    main_panel.setPreferredSize(new Dimension(580, 200));
    main_panel.setMinimumSize(new Dimension(580, 200));
    
    reportsTextArea.setOpaque(false);
    sp.getViewport().setOpaque(false);
    sp.setOpaque(false);
    sp.setBackground(Color.green);
    reportsTextArea.setBackground(Color.red);
    main_panel.setBackGroundImage(new ImageIcon(getClass().getClassLoader().getResource("images/marauroa_BG.png")).getImage());
    main_panel.setOpaque(false);
    main_panel.setBackground(Color.blue);
    
    setContentPane(main_panel);
  }
  
  
  private void chooseCharacter(String character)
  {
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
        MessageS2CChooseCharacterACK msg_ack = (MessageS2CChooseCharacterACK)msgReply;
        characterID = msg_ack.getObjectID();
        addLog("Character choosen correctly(id is "+characterID+")\n");
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
  public void addLog(String msg)
  {
    logDate.setTime(System.currentTimeMillis());
    String line = formatter.format(logDate) + msg;
    reportsTextArea.append(line);
    reportsTextArea.setCaretPosition(reportsTextArea.getText().length());
  }
  
  private void addLog(Exception  thr)
  {
    ByteArrayOutputStream out= new ByteArrayOutputStream();
    thr.printStackTrace(new PrintStream(out));
    addLog(out.toString());
  }
  
  
  
  private void login()
  {
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
    JOptionPane.showMessageDialog(this,"<html><body><font color=\"blue\">Marauroa</font> client version x.y.z</body></html>","About",JOptionPane.INFORMATION_MESSAGE);
  }
  
  
  private final class ActionHandler
    extends Thread implements ActionListener
  {
    
    private ActionEvent currentEvent;
    private Object monitor;
    private boolean end;
    private JWindow cancelDialog;
    
    
    
    public ActionHandler()
    {
      end = false;
      monitor = new Object();
      currentEvent = null;
    }
    
    private void letsplay()
    {
      SimpleGame sg = new SimpleGame(netMan,JMarauroa.this,characterID);
      sg.pack();
      sg.show();
      new Thread(sg,"Lets play thread...").start();
    }
    
    
    private void process(ActionEvent e)
    {
      if(e!=null)
      {
        lockGui();
        String action_cmd = e.getActionCommand();
        try
        {
          addLog("processing event "+action_cmd+"\n");
          if(ACTION_CMD_LOGIN.equals(action_cmd))
          {
            //login
            login();
          }
          else if(ACTION_CMD_DISCONNECT.equals(action_cmd))
          {
            //disconnect
            disconnect();
          }
          else if(ACTION_CMD_EXIT.equals(action_cmd))
          {
            //exit
            exit();
          }
          else if(ACTION_CMD_ABOUT.equals(action_cmd))
          {
            //about
            about();
          }
          else if(ACTION_CMD_PLAY.equals(action_cmd))
          {
            letsplay();
          }
          else
          {
            addLog("ignored unknown action: " +action_cmd+"\n");
          }
          addLog("event "+action_cmd + " procesed\n");
        }
        catch(Exception thr)
        {
          addLog("Uncaught exception processing : " + action_cmd + "\n");
          addLog(thr);
        }
        finally
        {
          unlockGui();
        }
      }
    }
    
    
    public void actionPerformed(ActionEvent e)
    {
      add(e);
    }
    
    
    public void add(ActionEvent e)
    {
      synchronized(monitor)
      {
        currentEvent = e;
        monitor.notifyAll();
      }
    }
    
    public void run()
    {
      //      System.out.println("Handler started");
      while(!end)
      {
        ActionEvent ae = get();
        if(ae!=null)
        {
          process(ae);
        }
      }
    }
    
    private ActionEvent get()
    {
      synchronized(monitor)
      {
        ActionEvent ret_event;
        if(currentEvent==null)
        {
          //wait until event is set
          try
          {
            monitor.wait();
          }
          catch (InterruptedException e)
          {
            addLog("ActionHandler interrupted!!!");
          }
        }
        ret_event = currentEvent;
        currentEvent = null;
        return(ret_event);
      }
    }
    
    private void unlockGui()
    {
      glassPane.setVisible(false);
    }
    
    private void lockGui()
    {
      glassPane.setVisible(true);
      glassPane.requestFocusInWindow();
    }
    
  }
  
  
  private final class BackgroundImagePanel
    extends JPanel
  {
    private Image backGroundImage;
    
    public BackgroundImagePanel(LayoutManager lm)
    {
      super(lm);
    }
    
    public void setBackGroundImage(Image backGroundImage)
    {
      this.backGroundImage = backGroundImage;
    }
    
    public Image getBackGroundImage()
    {
      return backGroundImage;
    }
    
    protected void paintComponent(Graphics g)
    {
      super.paintComponent(g);
      if(backGroundImage!=null)
      {
        g.drawImage(backGroundImage,0,0,getSize().width-1,getSize().height-1, this);
      }
    }
    
  }
  
  
}

