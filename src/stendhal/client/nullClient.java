package stendhal.client;

import java.text.SimpleDateFormat;
import java.net.*;
import java.io.*;
import java.util.*;
import marauroa.*;
import marauroa.net.*;
import marauroa.game.*;

public class nullClient extends Thread
  {
  private String username;
  private String password;
  private String character;
  
  private boolean synced;
  private RPObject myRPObject;
  private Map<RPObject.ID,RPObject> world_objects;
  private Random rand;
  private NetworkClientManager netMan;
  
  public nullClient(String u, String p, String c) throws SocketException
    {
    Configuration.setConfigurationFile("stendhal.ini");
    
    username=u;
    password=p;
    character=c;
    
    synced=false;
    
    myRPObject=null;
    world_objects=new LinkedHashMap<RPObject.ID,RPObject>();
    rand=new Random();
    netMan=new NetworkClientManager("127.0.0.1");
    }
   
  public void run()
    {
    try
      {
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
        else if(msg instanceof MessageS2CLoginNACK)
          {
          throw new Exception(msg.toString());
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
        
      
      PerceptionHandler handler=new PerceptionHandler(new DefaultPerceptionListener()
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
        
        public int onException(Exception e, marauroa.net.MessageS2CPerception perception)
          {
          e.printStackTrace();
          System.out.println(perception);
          
          System.exit(0);
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
        
      boolean cond=true;
      while(cond)  
        {
        Message msg=null;
        while(msg==null) msg=netMan.getMessage();
        if(msg instanceof MessageS2CPerception)
          {
          System.out.println("Sending Perception ACK");
          MessageC2SPerceptionACK reply=new MessageC2SPerceptionACK(msg.getAddress());
          reply.setClientID(clientid);
          netMan.addMessage(reply);
          
          MessageS2CPerception msgPer=(MessageS2CPerception)msg;
          handler.apply(msgPer,world_objects);
          
          RPAction action=new RPAction();
          action.put("type","add");
          action.put("a","1");
          action.put("b","2");
          
          MessageC2SAction msgAction=new MessageC2SAction(msg.getAddress(),action);
          msgAction.setClientID(clientid);
          netMan.addMessage(msgAction);
          }
        else if(msg instanceof MessageS2CTransferREQ)        
          {
          System.out.println("Transfer REQ: "+((MessageS2CTransferREQ)msg));
          List<TransferContent> items=((MessageS2CTransferREQ)msg).getContents();
          
          for(TransferContent item: items)
            {
            item.ack=true;
            }
          
          MessageC2STransferACK reply=new MessageC2STransferACK(msg.getAddress(),items);
          reply.setClientID(clientid);
          netMan.addMessage(reply);
          }
        else if(msg instanceof MessageS2CTransfer)        
          {
          System.out.println("Transfer: "+((MessageS2CTransfer)msg));
          List<TransferContent> items=((MessageS2CTransfer)msg).getContents();
          
          for(TransferContent item: items)
            {
            System.out.println(item.data);
            for(int byteContent:  item.data)
              {
              System.out.print(byteContent+",");
              }
              
            System.out.println();
            }
          }
        else
          {
          System.out.println("Other Message: "+msg);
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

      System.out.println("Logout --> Login");
      }
    catch(Exception e)
      {
      e.printStackTrace();
      System.exit(0);
      }
    }
  
  public static void main (String[] args)
    {
    try
      {
//      if(args.length>0)
//        {
//        int i=0;
//        String username=null;
//        String password=null;
//        String character=null;
//     
//        while(i!=args.length)
//          {
//          if(args[i].equals("-u"))
//            {
//            username=args[i+1];
//            }
//          else if(args[i].equals("-p"))
//            {
//            password=args[i+1];
//            }
//          else if(args[i].equals("-c"))
//            {
//            character=args[i+1];
//            }
//            
//          i++;
//          }        
//          
//        System.out.println("Parameter operation");
//        new nullClient(username,password,character).start();
//        return;
//        }
//      else
        {
        new nullClient("miguel","password","mIgUeL").start();
        }
      }
    catch(Exception e)
      {
      e.printStackTrace();
      System.exit(1);
      }
    }
  }
  
