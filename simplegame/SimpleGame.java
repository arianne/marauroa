/**
 * SimpleGame.java
 *
 * it is just a demonstrating of what marauroa can be, not what it <b>is</b>
 *
 * @author Created by wt
 */

package marauroa.simplegame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import marauroa.JMarauroa;
import marauroa.game.RPAction;
import marauroa.net.Message;
import marauroa.net.MessageC2SAction;
import marauroa.net.MessageS2CPerception;
import marauroa.net.NetworkClientManager;

public class SimpleGame
  extends JFrame implements Runnable
{
  
  private NetworkClientManager netMan;
  private GameDataModel gdm;
  private JMarauroa marauroa;
  
  public SimpleGame(NetworkClientManager netman, JMarauroa marauroa)
  {
    netMan = netman;
    this.marauroa = marauroa;
    gdm = new GameDataModel(3,3);
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
            //gdm.setColorAt();
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
    SimpleGame sg = new SimpleGame(null,null);
    sg.pack();
    sg.show();
    sleep(2);
    sg.gdm.setColorAt(0,0,Color.red);
    sleep(1);
    sg.gdm.setColorAt(1,1,Color.green);
    sleep(1);
    sg.gdm.setColorAt(2,2,Color.red);
    sleep(1);
    sg.gdm.setColorAt(2,0,Color.green);
    sleep(1);
    sg.gdm.setColorAt(0,2,Color.red);
    sleep(1);
    sg.gdm.setColorAt(0,1,Color.green);
    sleep(1);
    sg.gdm.setColorAt(1,2,Color.red);
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
  
  private final class GameDataModel
  {
    private Color colors[][];
    private Vector vUpdateListener;
    private int rows;
    private int columns;
    private GameDataModel(int rows, int columns)
    {
      this.rows = rows;
      this.columns = columns;
      colors = new Color[rows][columns];
      vUpdateListener = new Vector(1,1);
    }
    
    private void fireUpdate(int row, int column, Color color)
    {
      addLog(""+row+","+column+","+color+"\n");
      for (int i = 0; i < vUpdateListener.size(); i++)
      {
        GameUpdateListener ul = (GameUpdateListener)vUpdateListener.elementAt(i);
        ul.updateReceived(row,column,color);
      }
    }
    
    private void addGameUpdateListener(GameUpdateListener ul)
    {
      if(ul!=null)
      {
        vUpdateListener.add(ul);
      }
    }
    
    public void setColorAt(int row, int column,Color color)
    {
      if(!color.equals(colors[row][column]))
      {
        colors[row][column]=color;
        fireUpdate(row,column,color);
      }
    }
    
    private int getColumnsCount()
    {
      return(columns);
    }
    private int getRowsCount()
    {
      return(rows);
    }
    
    private Color getColorAt(int row, int column)
    {
      return(colors[row][column]);
    }
    
  }
  
  private interface GameUpdateListener
  {
    public void updateReceived(int row, int column, Color color);
  }
  
  private final class GameDisplay
    extends JPanel
  {
    private GameDataModel gameDataModel;
    
    private GameDisplay(GameDataModel gdm)
    {
      gameDataModel = gdm;
      gameDataModel.addGameUpdateListener(new GameUpdateListener()
                                          {
            public void updateReceived(int row, int column, Color color)
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
      int r = gameDataModel.getRowsCount();
      int c = gameDataModel.getColumnsCount();
      int cell_width  = w/c;
      int cell_height = h/r;
      int startx = cell_width/8;
      int starty = cell_height/8;
      
      int x = startx;
      for (int i = 0; i < c; i++)
      {
        x=startx+i*cell_width;
        int y = starty;
        for (int j = 0; j < r; j++)
        {
          y=starty+j*cell_height;
          Color color = gameDataModel.getColorAt(j,i);
          if(color!=null)
          {
            g.setColor(color);
            g.fillOval(x,y,cell_width*3/4,cell_height*3/4);
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
        int r = gameDataModel.getRowsCount();
        int c = gameDataModel.getColumnsCount();
        int cell_width  = w/c;
        int cell_height = h/r;
        Point pnt = e.getPoint();
        int column = pnt.x/cell_width + (pnt.x%cell_width>0?1:0)-1;
        int row = pnt.y/cell_height + (pnt.y%cell_height>0?1:0)-1;
        if(netMan!=null)
        {
          RPAction rpaction = new RPAction();
          rpaction.put("object_id","4711");
          rpaction.put("row",String.valueOf(row));
          rpaction.put("column",String.valueOf(column));
          MessageC2SAction msg = new MessageC2SAction(null,rpaction);
          netMan.addMessage(msg);
        }
        addLog("Player choosed [" +row +","+column+"]\n");
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

