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
  
  private void gameLogic(Map world_objects, RPObject.ID myID, NetworkClientManager netMan, Message msg) throws Exception
    {
    Random rand=new Random();

    RPObject player=(RPObject)world_objects.get(myID);
    if(!player.has("fighting") && !player.has("requested"))
      {
      System.out.println("Requesting fight");
      RPAction action=new RPAction();
      action.put("type","request_fight");
      action.put("gladiator_id",player.getSlot("!gladiators").get().get("id"));
      
      MessageC2SAction mesAct=new MessageC2SAction(msg.getAddress(),action);
      netMan.addMessage(mesAct);          
      }
    else if(player.has("fighting"))
      {
      System.out.println("Player is on Arena");
      RPObject arena=null;
      Iterator it=world_objects.values().iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        if(object.get("type").equals("arena"))
          {
          arena=object;
          break;
          }
        }          
      
      if(arena!=null && arena.get("status").equals("fighting"))
        {
        System.out.println("Player changes fight mode");
        
        String[] modes={"rock","paper","scissor"};
        
        RPAction action=new RPAction();
        action.put("type","fight_mode");
        action.put("fight_mode",modes[Math.abs(rand.nextInt()%3)]);
        action.put("gladiator_id",player.getSlot("!gladiators").get().get("id"));
        
        MessageC2SAction mesAct=new MessageC2SAction(msg.getAddress(),action);
        netMan.addMessage(mesAct);          
        }
      }
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
        
      RPObject.ID myID=null;

      Message msgCC=new MessageC2SChooseCharacter(address,character);

      msgCC.setClientID(clientid);
      netMan.addMessage(msgCC);
      while(recieved!=5)
        {
        Message msg=null;

        while(msg==null) msg=netMan.getMessage();
        if(msg instanceof MessageS2CChooseCharacterACK)
          {
          myID=((MessageS2CChooseCharacterACK)msg).getObjectID();
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
          MessageC2SPerceptionACK reply=new MessageC2SPerceptionACK(msg.getAddress());
          reply.setClientID(clientid);
          netMan.addMessage(reply);
          
          MessageS2CPerception msgPer=(MessageS2CPerception)msg;
          System.out.println(this.getName()+" -- Recieved perception: "+msgPer.getTypePerception());
          handler.apply(msgPer,world_objects);
          
          gameLogic(world_objects, myID, netMan, msg);
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
        
        new nullClient(username,password,character).start();
        return;
        }

      int num=28;
      nullClient test[]=new nullClient[num];
      
      test[0]=new nullClient("miguel","qwerty","miguel");
      test[1]=new nullClient("prueba","qwerty","prueba");
      test[2]=new nullClient("bot_8","nopass","bot_8");
      test[3]=new nullClient("bot_9","nopass","bot_9");
      test[4]=new nullClient("bot_10","nopass","bot_10");
      test[5]=new nullClient("bot_11","nopass","bot_11");

      test[6]=new nullClient("overload_0","overload","overload_0");
      test[7]=new nullClient("overload_1","overload","overload_1");
      test[8]=new nullClient("overload_2","overload","overload_2");
      test[9]=new nullClient("overload_3","overload","overload_3");
      test[10]=new nullClient("overload_4","overload","overload_4");
      test[11]=new nullClient("overload_5","overload","overload_5");
      test[12]=new nullClient("overload_6","overload","overload_6");
      test[13]=new nullClient("overload_7","overload","overload_7");
      test[14]=new nullClient("overload_8","overload","overload_8");
      test[15]=new nullClient("overload_9","overload","overload_9");
      test[16]=new nullClient("overload_10","overload","overload_10");
      test[17]=new nullClient("overload_11","overload","overload_11");
      test[18]=new nullClient("overload_12","overload","overload_12");
      test[19]=new nullClient("overload_13","overload","overload_13");
      test[20]=new nullClient("overload_14","overload","overload_14");
      test[21]=new nullClient("overload_15","overload","overload_15");
      test[22]=new nullClient("overload_16","overload","overload_16");
      test[23]=new nullClient("overload_17","overload","overload_17");
      test[24]=new nullClient("overload_18","overload","overload_18");
      test[25]=new nullClient("overload_19","overload","overload_19");
      test[26]=new nullClient("overload_20","overload","overload_20");
      test[27]=new nullClient("overload_21","overload","overload_21");
      
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
    
    
