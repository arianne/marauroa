/**
 * JMarauroa2.java
 * derived from JMarauroa
 * dont no if someone like it
 * @author trwa@tribus.dyndns.org
 */

package marauroa;


import marauroa.net.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * Test client for marauroa
 **/
public class JMarauroa2
  extends JFrame
{
  
  private JButton actionButton;
  private JButton chooseCharacterButton;
  private JButton connectButton;
  private JButton disconnectButton;
  private JTextArea reportsTextArea;
  
  private short clientId;
  private NetworkClientManager netMan;
  
  private SimpleDateFormat formatter;
  private Date logDate;
  
  
  
  public JMarauroa2()
  {
    setTitle("Marauroa test client");
    //    setIconImage(new ImageIcon("marauroa.png").getImage());
    initComponents();
    clientId=-10;
    formatter = new SimpleDateFormat("[HH:MM:ss.SSS]  ");
    logDate = new Date();
    addWindowListener(new WindowAdapter()
                      {
          public void windowClosing(WindowEvent p0)
          {
            exitForm(p0);
          }
        });
  }
  
  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    JMarauroa2 marauroa = new JMarauroa2();
    marauroa.pack();
    marauroa.show();
  }
  
  
  /** Exit the Application */
  private void exitForm(WindowEvent evt)
  {
    if(disconnectButton.isEnabled())
    {
      disconnectButton.doClick();
      // ;-)
    }
    System.exit(0);
  }
  
  
  /**
   * This method is called from within the init() method to
   * initialize the form.
   */
  private void initComponents()
  {
    JPanel main_panel = new JPanel(new BorderLayout());
    reportsTextArea = new JTextArea();
    
    connectButton = new JButton("Connect");
    connectButton.setMnemonic('C');
    chooseCharacterButton = new JButton("Character");
    chooseCharacterButton.setMnemonic('h');
    actionButton = new JButton("Action");
    actionButton.setMnemonic('A');
    disconnectButton = new JButton("Disconnect");
    disconnectButton.setMnemonic('D');
    
    reportsTextArea.setEditable(false);
    reportsTextArea.setLineWrap(false);
    
    connectButton.addActionListener(new ActionListener()
                                    {
          public void actionPerformed(ActionEvent evt)
          {
            connectButtonActionPerformed(evt);
          }
        });
    
    chooseCharacterButton.setEnabled(false);
    chooseCharacterButton.addActionListener(new ActionListener()
                                            {
          public void actionPerformed(ActionEvent evt)
          {
            chooseCharacterButtonActionPerformed(evt);
          }
        });
    
    actionButton.setEnabled(false);
    actionButton.addActionListener(new ActionListener()
                                   {
          public void actionPerformed(ActionEvent evt)
          {
            actionButtonActionPerformed(evt);
          }
        });
    
    disconnectButton.setEnabled(false);
    disconnectButton.addActionListener(new ActionListener()
                                       {
          public void actionPerformed(ActionEvent evt)
          {
            disconnectButtonActionPerformed(evt);
          }
        });
    
    JScrollPane sp = new JScrollPane(reportsTextArea);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    main_panel.add(sp,BorderLayout.CENTER);
    
    JPanel btn_panel = new JPanel(new FlowLayout());
    btn_panel.add(connectButton);
    btn_panel.add(chooseCharacterButton);
    btn_panel.add(actionButton);
    btn_panel.add(disconnectButton);
    
    main_panel.add(btn_panel,BorderLayout.SOUTH);
    main_panel.setPreferredSize(new Dimension(580, 200));
    main_panel.setMinimumSize(new Dimension(580, 200));
    
    setContentPane(main_panel);
  }
  
  private void disconnectButtonActionPerformed(ActionEvent evt)
  {
    /* TODO: Write here the code to send Logout message */
    Message msg=new MessageC2SLogout(null);
    msg.setClientID(clientId);
    netMan.addMessage(msg);
    netMan.finish();
    
    connectButton.setEnabled(true);
    chooseCharacterButton.setEnabled(false);
    actionButton.setEnabled(false);
    disconnectButton.setEnabled(false);
  }
  
  private void actionButtonActionPerformed(ActionEvent evt)
  {
    /* TODO: Write here the code to send Action message */
  }
  
  private void chooseCharacterButtonActionPerformed(ActionEvent evt)
  {
    /* TODO: Write here the code to send Choose Character message */
    addLog("MrBean choosen\n");
    Message msg=new MessageC2SChooseCharacter(null,"MrBean");
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
    chooseCharacterButton.setEnabled(false);
    actionButton.setEnabled(true);
  }
  
  private void connectButtonActionPerformed(ActionEvent evt)
  {
    /* Todo: Write here the code to send Login message */
    addLog("Starting network client manager\n");
    try
    {
      netMan=new NetworkClientManager("192.168.100.100");
    }
    catch(java.net.SocketException e)
    {
      ByteArrayOutputStream out= new ByteArrayOutputStream();
      e.printStackTrace(new PrintStream(out));
      
      addLog("Exception starting network client manager\n");
      addLog(out.toString()+"\n");
    }
    
    MessageC2SLogin msg=new MessageC2SLogin(null,"Test Player","Test Password");
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
    while(is_correct_login && (msgReply==null))
    {
      msgReply=netMan.getMessage();
      
      if(msgReply==null) continue;
      
      if(msgReply.getType()==Message.TYPE_S2C_CHARACTERLIST)
      {
        addLog("Character list received\n");
        String[] characters=((MessageS2CCharacterList)msgReply).getCharacters();
        for(int i=0;i<characters.length;++i)
        {
          addLog("- "+characters[i]+"\n");
        }
      }
    }
    disconnectButton.setEnabled(true);
    chooseCharacterButton.setEnabled(true);
    connectButton.setEnabled(false);
  }
  
  /**
   * adds a message into reportPane
   */
  private void addLog(String msg)
  {
    logDate.setTime(System.currentTimeMillis());
    String line = formatter.format(logDate) + msg;
    reportsTextArea.append(line);
    reportsTextArea.setCaretPosition(reportsTextArea.getText().length());
  }
  
}

