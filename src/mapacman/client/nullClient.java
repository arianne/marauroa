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
  class ClientMap
    {
    char[] content;
    int sizey;
    int sizex;
    
    public ClientMap(List mapData) throws Exception
      {
      /** TODO: Intepret and apply the mapData */
      int maxv=0, maxh=0;
      Iterator it=mapData.iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        if(object.getInt("x")>maxh) maxh=object.getInt("x");
        if(object.getInt("y")>maxv) maxv=object.getInt("y");
        }
        
      content=new char[(maxh+1)*(maxv+1)];
      sizex=maxh+1;
      sizey=maxv+1;
      
      it=mapData.iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        if(object.get("type").equals("block"))
          {
          put(object.getInt("x"),object.getInt("y"),'*');
          }
        }
      }
      
    private void put(int x,int y, char value)
      {
      content[y*sizex+x]=value;
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
          if(type!='*')
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
    Configuration.setConfigurationFile("mapacman.ini");
    
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
          throw new Exception(msg.toString());
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
            System.out.println(">");
            gameLogic(myRPObject,map_objects);
            System.out.println("<");
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
      if(args.length>0)
        {
        int i=0;
        String username=null;
        String password=null;
        String character=null;
     
        while(i!=args.length)
          {
          if(args[i].equals("-u"))
            {
            username=args[i+1];
            }
          else if(args[i].equals("-p"))
            {
            password=args[i+1];
            }
          else if(args[i].equals("-c"))
            {
            character=args[i+1];
            }
            
          i++;
          }        
        System.out.println("Parameter operation");
        new nullClient(username,password,character).start();
        return;
        }

      System.out.println("Non parameter operation");
      int num=5;
      nullClient test[]=new nullClient[num];
      
      test[0]=new nullClient("prueba","qwerty","prueba");
      test[1]=new nullClient("bot_0","nopass","bot_0");
      test[2]=new nullClient("bot_1","nopass","bot_1");
      test[3]=new nullClient("bot_2","nopass","bot_2");
      test[4]=new nullClient("bot_3","nopass","bot_3");
      
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
  
