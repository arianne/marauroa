package marauroa;

import java.text.SimpleDateFormat;
import java.net.*;
import java.io.*;
import java.util.*;
import marauroa.net.*;
import marauroa.game.*;

class TestClient extends Thread
  {
  private String username;
  private String password;
  private String character;
  
  public TestClient(String u, String p, String c)
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
      while(recieved!=4)
        {
        Message msg=null;

        while(msg==null) msg=netMan.getMessage();
        if(msg instanceof MessageS2CChooseCharacterACK)
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
            previous_timestamp=msgPer.getTimestamp()-1;
            }
          else if(outofsync==true)
            {
            System.out.println("|"+Long.toString(new Date().getTime())+"| Got Perception - "+msgPer.getTypePerception()+" - "+msgPer.getTimestamp());
            }
          
          if(outofsync==false)
            {
            if(previous_timestamp+1!=msgPer.getTimestamp())
              {
              System.out.println("We are out of sync. Waiting for sync perception");
              System.out.println("Expected"+(previous_timestamp+1)+" but we got "+msgPer.getTimestamp());
              outofsync=true;
              /* TODO: Try to regain sync by getting more messages in the hope of getting the out of order perception */                
              }
            else
              {
              timestamp.setTime(System.currentTimeMillis());
              String ts = formatter.format(timestamp);

              previous_timestamp=msgPer.getTimestamp();
              System.out.println(ts+" "+"Got Perception - "+msgPer.getTypePerception()+" - "+msgPer.getTimestamp());
              out.println(ts+" "+msgPer.getTypePerception()+" - "+msgPer.getTimestamp());
              
              previous_timestamp=msgPer.applyPerception(world_objects,previous_timestamp,null);          
              }       
            }
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
      TestClient test[]=new TestClient[num];
      
      test[0]=new TestClient("miguel","qwerty","miguel");
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
    
    
