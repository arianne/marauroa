package mapacman.client;

import java.text.SimpleDateFormat;
import java.net.*;
import java.io.*;
import java.util.*;
import marauroa.Configuration;
import marauroa.net.*;
import marauroa.game.*;
import mapacman.*;

public class nullClient extends Thread
  {
  static class ClientMap
    {
    char[] content;
    int sizey;
    int sizex;
    
    public ClientMap(byte[] mapData) throws Exception
      {
      ByteArrayInputStream bi=new ByteArrayInputStream(mapData);
      InputSerializer ser=new InputSerializer(bi);
      
      sizey=ser.readInt();
      for(int i=0;i<sizey;++i)
        {
        String line=ser.readString();
        if(i==0)
          {
          content=new char[sizey*line.length()];
          sizex=line.length();
          }
          
        for(int j=0;j<sizex;j++)
          {
          content[i*sizex+j]=line.charAt(j);
          }
        }
      }
    
    public char get(int x,int y)
      {
      return content[y*sizex+x];
      }
    
    public int getSizeX()
      {
      return sizex;
      }

    public int getSizeY()
      {
      return sizey;
      }
    
    public void print(PrintStream out, Map objects) throws Exception
      {
      for(int i=0;i<sizey;i++)
        {
        for(int j=0;j<sizex;j++)
          {
          char type=get(j,i);
          if(type=='.' || type=='0' || type=='+')
            {
            boolean printed=false;
            Iterator it=objects.values().iterator();
            while(it.hasNext())
              {
              RPObject obj=(RPObject)it.next();
              if(obj.getInt("x")==j && obj.getInt("y")==i)
                {
                if(obj.get("type").equals("player"))
                  {
                  printed=true;
                  out.print('C');
                  }
                else if(obj.get("type").equals("ball"))
                  {
                  printed=true;
                  out.print('.');
                  }
                }
              }
            
            if(!printed && type=='+')
              {
              out.print('+');
              }
            else if(!printed)
              {
              out.print(' ');
              }
            }
          else
            {
            out.print(type);
            }
          }
        out.println();
        }
      }
    }

  private String username;
  private String password;
  private String character;
  
  public nullClient(String u, String p, String c)
    {
    username=u;
    password=p;
    character=c;
    }
   
  public void run()
    {
    try
      {
      Configuration.setConfigurationFile("mapacman.ini");
      PrintStream out=new PrintStream(new FileOutputStream(getName()+"_testClient.txt"));
      
      Map world_objects=new LinkedHashMap();
      ClientMap map_objects=null;
      RPObject myRPObject=null;
      Random rand=new Random();
      
      NetworkClientManager netMan=new NetworkClientManager("127.0.0.1");
      InetSocketAddress address=new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT);

      netMan.addMessage(new MessageC2SLogin(address,username,password));

      int clientid=-1;
      int recieved=0;

      while(recieved!=3)
        {
        Message msg=null;

        while(msg==null) msg=netMan.getMessage();
        if(msg instanceof MessageS2CLoginACK)
          {
          clientid=msg.getClientID();
          ++recieved;
          }
        else if(msg instanceof MessageS2CCharacterList)
          {
          ++recieved;
          }
        else if(msg instanceof MessageS2CServerInfo)
          {
          ++recieved;
          }
        else
          {
          throw new Exception();
          }
        }

      Message msgCC=new MessageC2SChooseCharacter(address,character);

      msgCC.setClientID(clientid);
      netMan.addMessage(msgCC);
      while(recieved!=5)
        {
        Message msg=null;

        while(msg==null) msg=netMan.getMessage();
        if(msg instanceof MessageS2CChooseCharacterACK)
          {
          ++recieved;
          }
        else if(msg instanceof MessageS2CMap)
          {
          byte[] mapData=((MessageS2CMap)msg).getMapData();
          map_objects=new ClientMap(mapData);
          ++recieved;
          }
        else if(msg instanceof MessageS2CPerception)
          {
          }
        else
          {
          throw new Exception();
          }
        }
        
      
      boolean cond=true;
      boolean outofsync=true;
      int previous_timestamp=0;

      Date timestamp=new Date();
      SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        
      while(cond)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();
        if(msg instanceof MessageS2CPerception)
          {
          MessageC2SPerceptionACK reply=new MessageC2SPerceptionACK(msg.getAddress());
          reply.setClientID(clientid);
          netMan.addMessage(reply);
          
          MessageS2CPerception msgPer=(MessageS2CPerception)msg;
          if(msgPer.getTypePerception()==1 && outofsync)
            {
            outofsync=false;
            previous_timestamp=msgPer.getPerceptionTimestamp()-1;
            }
          else if(outofsync==true)
            {
            System.out.println("|"+Long.toString(System.currentTimeMillis())+"| Got Perception - "+msgPer.getTypePerception()+" - "+msgPer.getPerceptionTimestamp());
            }
          
          if(outofsync==false)
            {
            if(previous_timestamp+1!=msgPer.getPerceptionTimestamp())
              {
              System.out.println("We are out of sync. Waiting for sync perception");
              System.out.println("Expected"+(previous_timestamp+1)+" but we got "+msgPer.getPerceptionTimestamp());
              outofsync=true;
              /* TODO: Try to regain sync by getting more messages in the hope of getting the out of order perception */
              }
            else
              {
              timestamp.setTime(System.currentTimeMillis());
              String ts = formatter.format(timestamp);

              System.out.println(ts+" "+"Got Perception - "+msgPer.getTypePerception()+" - "+msgPer.getPerceptionTimestamp());
              out.println(ts+" "+msgPer.getTypePerception()+" - "+msgPer.getPerceptionTimestamp());
              
              previous_timestamp=msgPer.applyPerception(world_objects,previous_timestamp,null);
              if(msgPer.getMyRPObject()!=null)
                {
                myRPObject=msgPer.getMyRPObject();
                }


              /** Code here game **/
              int x=myRPObject.getInt("x");
              int y=myRPObject.getInt("y");
              String dir=myRPObject.get("dir");
                
              RPAction turn=new RPAction();
              turn.put("type","turn");
/**
    If Not bWall(Ghost.X - 1, Ghost.Y) Then i = i + 1 'Left
    If Not bWall(Ghost.X + 1, Ghost.Y) Then i = i + 1 'Right
    If Not bWall(Ghost.X, Ghost.Y - 1) Then i = i + 1 'Up
    If Not bWall(Ghost.X, Ghost.Y + 1) Then i = i + 1 'Down
    
    Select Case i
    Case 1
        'Dead end
        'Here we reverse ghost direction
    Case 2
        'Corner or straight
        'Go off in the direction which does not make us go
        'back (check iLastX and iLastY parameters)
    Case Else
        'Intersection of some sort
        'Check which route brings us closest to pacman
        'and move that way
    End If
**/
              int i=0;
              if(map_objects.get(x,y-1)=='*') i++;
              if(map_objects.get(x,y+1)=='*') i++;
              if(map_objects.get(x-1,y)=='*') i++;
              if(map_objects.get(x+1,y)=='*') i++;
              
              System.out.println("Case :"+i);
              
              switch(i)
                {
                case 1:
                  if(dir.equals("S")) {turn.put("dir","N");break;}
                  if(dir.equals("N")) {turn.put("dir","S");break;}
                  if(dir.equals("E")) {turn.put("dir","W");break;}
                  if(dir.equals("W")) {turn.put("dir","E");break;}
                case 2:
                  String hor_dirs[]={"W","E"};
                  String ver_dirs[]={"N","S"};
                  
                  if((dir.equals("N") && map_objects.get(x,y-1)=='*')||(dir.equals("S") && map_objects.get(x,y+1)=='*'))
                    {
                    turn.put("dir",hor_dirs[Math.abs(rand.nextInt()%2)]);
                    break;
                    }
                  if((dir.equals("W") && map_objects.get(x-1,y)=='*')||(dir.equals("E") && map_objects.get(x+1,y)=='*'))
                    {
                    turn.put("dir",ver_dirs[Math.abs(rand.nextInt()%2)]);
                    break;
                    }
                case 3:
                  String dirs[]={"N","S","W","E"};
                  turn.put("dir",dirs[Math.abs(rand.nextInt()%4)]);
                  break;
                }

              if(turn.size()>1)
                {
                Message msgTurn=new MessageC2SAction(msg.getAddress(),turn);
                msgTurn.setClientID(clientid);
                netMan.addMessage(msgTurn);
                }
                
              map_objects.print(System.out,world_objects);
              }
            }
          }
        }
          
      Message msgL=new MessageC2SLogout(address);

      msgL.setClientID(clientid);
      netMan.addMessage(msgL);
      while(recieved!=5)
        {
        Message msg=null;

        while(msg==null) msg=netMan.getMessage();
        if(msg instanceof MessageS2CLogoutACK)
          {
          ++recieved;
          }
        else if(msg instanceof MessageS2CPerception)
          {
          }
        else
          {
          throw new Exception();
          }
        }
      }
    catch(Exception e)
      {
      e.printStackTrace();
      }
    }
    
  public static void main (String[] args)
    {
    try
      {
      int num=1;
      nullClient test[]=new nullClient[num];
      
      test[0]=new nullClient("miguel","qwerty","miguel");
//      test[1]=new TestClient("prueba","qwerty","prueba");
//      test[2]=new TestClient("bot_8","nopass","bot_8");
//      test[3]=new TestClient("bot_9","nopass","bot_9");
//      test[4]=new TestClient("bot_10","nopass","bot_10");
//      test[5]=new TestClient("bot_11","nopass","bot_11");
      
      for(int i=0;i<num;++i)
        {
        test[i].start();
        }
      }
    catch(Exception e)
      {
      e.printStackTrace();
      }
    }
  }
    
    
