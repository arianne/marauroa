/**
 * SimpleGame.java
 *
 * it is just a demonstrating of what marauroa can be, not what it <b>is</b>
 *
 * @author Created by wt
 */

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
import marauroa.game.RPObject;
import marauroa.net.Message;
import marauroa.net.MessageC2SAction;
import marauroa.net.MessageS2CPerception;
import marauroa.net.NetworkClientManager;
import simplegame.actions.MoveAction;
import simplegame.objects.GameBoard;

public class SimpleGame
  extends JFrame implements Runnable
{
  
  private NetworkClientManager netMan;
  private SimpleGameDataModel gdm;
  private JMarauroa marauroa;
  private RPObject.ID ownCharacterID;
  private RPObject.ID otherCharacterID;
  
  public SimpleGame(NetworkClientManager netman, JMarauroa marauroa,RPObject.ID characterID)
  {
    netMan = netman;
    this.marauroa = marauroa;
    this.ownCharacterID=characterID;
    otherCharacterID = new RPObject.ID(4711);
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
            MessageS2CPerception perception = (MessageS2CPerception)msg;
            List modified_objects = perception.getModifiedRPObjects();
            if(modified_objects!=null && modified_objects.size()>0)
            {
              for (int i = 0; i < modified_objects.size(); i++)
              {
                RPObject obj = (RPObject)modified_objects.get(i);
                addLog(obj.toString()+"\n");
                try
                {
                  try
                  {
                    RPObject gb = obj.getSlot("hand").get(0);
                    int size = Integer.parseInt(gb.get("size"));
                    for (int k = 0; k < size; k++)
                    {
                      for (int l = 0; l < size; l++)
                      {
                        int id = GameBoard.getRPCharacterAt(gb,k,l);
                        gdm.setRPCharacterAt(k,l,id);
                      }
                    }
                  }
                  catch (RPObject.NoSlotFoundException e)
                  {
                    e.printStackTrace();
                  }
                }
                catch (Attributes.AttributeNotFoundException e)
                {
                  e.printStackTrace();
                  //e.printStackTrace();
                }
              }
            }
          }
        }
      }
      else
      {
        sleep(5);
      }
    }
  }
  
  public static void main(String argv[])
  {
    SimpleGame sg = new SimpleGame(null,null,null);
    sg.pack();
    sg.show();
    sleep(2);
    sg.gdm.setRPCharacterAt(0,0,1);
    sleep(1);
    sg.gdm.setRPCharacterAt(1,1,0);
    sleep(1);
    sg.gdm.setRPCharacterAt(2,2,1);
    sleep(1);
    sg.gdm.setRPCharacterAt(2,0,0);
    sleep(1);
    sg.gdm.setRPCharacterAt(0,2,1);
    sleep(1);
    sg.gdm.setRPCharacterAt(0,1,0);
    sleep(1);
    sg.gdm.setRPCharacterAt(1,2,1);
    sleep(1);
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
          if(otherCharacterID!=null)
          {
            if(otherCharacterID.getObjectID()==gameDataModel.getRPCharacterAt(j,i))
            {
              color = Color.red;
            }
            else if(ownCharacterID.getObjectID()==gameDataModel.getRPCharacterAt(j,i))
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
          rpaction.put("object_id", ""+ownCharacterID.getObjectID());
          rpaction.setRow(row);
          rpaction.setColumn(column);
          MessageC2SAction msg = new MessageC2SAction(null,rpaction);
          netMan.addMessage(msg);
        }
        addLog("Player makes move on [" +row +","+column+"]\n");
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

