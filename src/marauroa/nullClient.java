package marauroa;

import java.text.SimpleDateFormat;
import java.net.*;
import java.io.*;
import java.util.*;
import marauroa.net.*;
import marauroa.game.*;

public class nullClient extends Thread
  {
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
      PrintStream out=new PrintStream(new FileOutputStream(getName()+"_testClient.txt"));
      
      Map world_objects=new LinkedHashMap();
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
        
      PerceptionHandler handler=new PerceptionHandler();
      while(cond)
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();
        if(msg instanceof MessageS2CPerception)
          {
          System.out.println("foo bar");
          MessageC2SPerceptionACK reply=new MessageC2SPerceptionACK(msg.getAddress());
          reply.setClientID(clientid);
          netMan.addMessage(reply);
          
          MessageS2CPerception msgPer=(MessageS2CPerception)msg;
          handler.apply(msgPer,world_objects);
          }

        StringBuffer world=new StringBuffer("World content: \n");
    
        Iterator world_it=world_objects.values().iterator();
        while(world_it.hasNext())
          {
          RPObject object=(RPObject)world_it.next();
          world.append("  "+object.toString()+"\n");
          }
        out.println(world.toString());
        out.flush();
        }
          
      Message msgL=new MessageC2SLogout(address);

      msgL.setClientID(clientid);
      netMan.addMessage(msgL);
      while(recieved!=6)
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
      int num=6;
      nullClient test[]=new nullClient[num];
      
      test[0]=new nullClient("miguel","qwerty","miguel");
      test[1]=new nullClient("prueba","qwerty","prueba");
      test[2]=new nullClient("bot_8","nopass","bot_8");
      test[3]=new nullClient("bot_9","nopass","bot_9");
      test[4]=new nullClient("bot_10","nopass","bot_10");
      test[5]=new nullClient("bot_11","nopass","bot_11");
      
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
    
    
