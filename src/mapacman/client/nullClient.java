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
    
    public ClientMap(List mapData) throws Exception
      {
      /** TODO: Intepret and apply the mapData */
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
  
  private boolean synced;
  private RPObject myRPObject;
  private Map world_objects;
  private ClientMap map_objects;
  private Random rand;      
  private NetworkClientManager netMan;
  
  public nullClient(String u, String p, String c) throws SocketException
    {
    username=u;
    password=p;
    character=c;
    
    synced=false;
    
    myRPObject=null;
    world_objects=new LinkedHashMap();
    map_objects=null;
    rand=new Random();
    netMan=new NetworkClientManager("127.0.0.1");
    }
   
  public void run()
    {
    try
      {
      Configuration.setConfigurationFile("mapacman.ini");
      PrintStream out=new PrintStream(new FileOutputStream(getName()+"_testClient.txt"));
      
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
          List mapData=((MessageS2CMap)msg).getMapObjects();
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

      Date timestamp=new Date();
      SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      
      PerceptionHandler handler=new PerceptionHandler(new PerceptionHandler.DefaultPerceptionListener()
        {
        public int onSynced()
          {
          synced=true;
          return 0;
          }
        
        public int onUnsynced()
          {
          synced=false;
          return 0;
          }
        
        public boolean onMyRPObject(boolean changed,RPObject object)          
          {
          if(changed)
            {
            myRPObject=object;
            }
            
          return false;
          }
        });
        
        
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
          handler.apply(msgPer,world_objects);
          
          if(synced)
            {
            map_objects.print(System.out,world_objects);
            gameLogic(myRPObject,map_objects);
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
  
  private void gameLogic(RPObject myRPObject, ClientMap map_objects) throws Attributes.AttributeNotFoundException
    {
    /** Code here game **/
    int x=myRPObject.getInt("x");
    int y=myRPObject.getInt("y");
    String dir=myRPObject.get("dir");
       
    RPAction turn=new RPAction();
    turn.put("type","turn");
    turn.put("dir",dir);
      
    boolean changed=true;
    boolean do_crosspaths=false;

    while(changed)
      {
      changed=false;
      
      if((turn.get("dir").equals("N") && map_objects.get(x,y-1)=='*'))
        {
        System.out.println("Collision at N --> Goes W");
        turn.put("dir","W");
        changed=true;
        }
      else if((turn.get("dir").equals("W") && map_objects.get(x-1,y)=='*'))
        {
        System.out.println("Collision at W --> Goes S");
        turn.put("dir","S");
        changed=true;
        }
      else if((turn.get("dir").equals("S") && map_objects.get(x,y+1)=='*'))
        {
        System.out.println("Collision at S --> Goes E");
        turn.put("dir","E");
        changed=true;
        }
      else if((turn.get("dir").equals("E") && map_objects.get(x+1,y)=='*'))
        {
        System.out.println("Collision at E --> Goes N");
        turn.put("dir","N");
        changed=true;
        }
      
      if(!changed && !dir.equals(turn.get("dir")) && (                
        ("NS".indexOf(dir)!=-1)==("NS".indexOf(turn.get("dir"))!=-1) || 
        ("WE".indexOf(dir)!=-1)==("WE".indexOf(turn.get("dir"))!=-1)))
        {
        System.out.println("Backtracking detected --> Rotating");
        String newdir=turn.get("dir");
        if(newdir.equals("N"))
          {
          turn.put("dir","W");
          }
        else if(newdir.equals("W"))
          {
          turn.put("dir","S");
          }
        else if(newdir.equals("S"))
          {
          turn.put("dir","E");
          }
        else if(newdir.equals("E"))
          {
          turn.put("dir","N");
          }                  
        }
        
                        
        int i=0;
        if(map_objects.get(x,y-1)=='*') i++;
        if(map_objects.get(x,y+1)=='*') i++;
        if(map_objects.get(x-1,y)=='*') i++;
        if(map_objects.get(x+1,y)=='*') i++;
        
        if(!do_crosspaths && (i==0 || i==1))
          {
          System.out.println("Crosspaths detected --> Randomizing");
          if(dir.equals("N"))
            {
            String randdir[]={"N","W","W","E","E"};
            turn.put("dir",randdir[Math.abs(rand.nextInt()%randdir.length)]);
            do_crosspaths=true;
            }
          else if(dir.equals("S"))
            {
            String randdir[]={"S","W","E","W","E"};
            turn.put("dir",randdir[Math.abs(rand.nextInt()%randdir.length)]);
            do_crosspaths=true;
            }
          else if(dir.equals("W"))
            {
            String randdir[]={"W","N","S","N","S"};
            turn.put("dir",randdir[Math.abs(rand.nextInt()%randdir.length)]);
            do_crosspaths=true;
            }
          else if(dir.equals("E"))
            {
            String randdir[]={"N","S","N","S","E"};
            turn.put("dir",randdir[Math.abs(rand.nextInt()%randdir.length)]);
            do_crosspaths=true;
            }
          }
        }

    if(turn.size()>1 && !turn.get("dir").equals(dir))
      {
      Message msgTurn=new MessageC2SAction(null,turn);
      netMan.addMessage(msgTurn);
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
  