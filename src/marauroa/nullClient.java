package marauroa;

import java.net.*;
import java.io.*;
import java.util.*;
import marauroa.net.*;
import marauroa.game.*;

class TestClient
  {
  public TestClient()
    {
    }
   
  public void run()
    {
    try
      {
      System.setOut(new PrintStream(new FileOutputStream("testClient.txt")));
      
      Map world_objects=new LinkedHashMap();
      NetworkClientManager netMan=new NetworkClientManager("127.0.0.1");
      InetSocketAddress address=new InetSocketAddress("127.0.0.1",NetConst.marauroa_PORT);

      netMan.addMessage(new MessageC2SLogin(address,"prueba","qwerty"));

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

      Message msgCC=new MessageC2SChooseCharacter(address,"prueba");

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
          
          if(outofsync==false)
            {
            if(previous_timestamp+1!=msgPer.getTimestamp())
              {
              System.err.println("We are out of sync. Waiting for sync perception");
              System.err.println("Expected"+previous_timestamp+" but we got "+msgPer.getTimestamp());
              outofsync=true;
              /* TODO: Try to regain sync by getting more messages in the hope of getting the out of order perception */                
              }
            else
              {
              previous_timestamp=msgPer.getTimestamp();
              System.err.println("Got Perception - "+msgPer.getTypePerception()+" - "+msgPer.getTimestamp());
              System.out.println(msgPer.getTypePerception()+" - "+msgPer.getTimestamp());
          
              Iterator it;
              it=msgPer.getDeletedRPObjects().iterator();
              while(it.hasNext())
                {
                RPObject object=(RPObject)it.next();
                System.out.println("D: "+object);
                world_objects.remove(object.get("id"));            
                }
              
              it=msgPer.getModifiedDeletedRPObjects().iterator();
              while(it.hasNext())
                {
                RPObject object=(RPObject)it.next();
                System.out.println("MD: "+object);
                RPObject w_object=(RPObject)world_objects.get(object.get("id"));    
                w_object.applyDifferences(null,object);        
                }
    
              it=msgPer.getModifiedAddedRPObjects().iterator();
              while(it.hasNext())
                {
                RPObject object=(RPObject)it.next();
                System.out.println("MA: "+object);
                RPObject w_object=(RPObject)world_objects.get(object.get("id"));    
                w_object.applyDifferences(object,null);        
                }
    
              it=msgPer.getAddedRPObjects().iterator();
              while(it.hasNext())
                {
                RPObject object=(RPObject)it.next();
                System.out.println("A: "+object);
                world_objects.put(object.get("id"),object);            
                }   
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
        System.out.println(world.toString());
        System.out.flush();
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
      TestClient test=new TestClient();
      test.run();
      }
    catch(Exception e)
      {
      e.printStackTrace();
      }
    }
  }
    
    
