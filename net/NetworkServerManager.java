package marauroa.net;

import java.net.*;
import java.util.*;
import java.io.*;

import marauroa.marauroad;


public class NetworkServerManager
  {
  private DatagramSocket socket;
  private boolean keepRunning;  
  private boolean isfinished;
  
  private MessageFactory msgFactory;
  private List messages;  
  private NetworkServerManagerRead readManager;
  private NetworkServerManagerWrite writeManager;
  
  public NetworkServerManager() throws SocketException
    {    
    socket=new DatagramSocket(NetConst.marauroa_PORT);
    socket.setSoTimeout(1000);
       
    msgFactory=MessageFactory.getFactory();
    keepRunning=true;
    isfinished=false;
    
    messages=Collections.synchronizedList(new LinkedList());
    
    readManager=new NetworkServerManagerRead();
    readManager.start();
    
    writeManager=new NetworkServerManagerWrite();
    }
  
  public synchronized void finish()
    {
    keepRunning=false;
    
    while(isfinished==false)
      {
      try
        {
        wait(100);
        }
      catch(java.lang.InterruptedException e)
        {
        }
      }
    
    socket.close();
    }
    
  private synchronized void newMessageArrived()
    {
    notify();
    }
 
  public synchronized Message getMessage(int timeout)
    {
    if(messages.size()==0)
      {
      try
        {
        wait(timeout);
        }
      catch(InterruptedException e)
        {
        marauroad.report(e.getMessage());
        }
      }
    
    if(messages.size()==0)
      {
      return null;      
      }
    else
      {  
      return (Message)messages.remove(0); 
      }
    }

  public synchronized Message getMessage()
    {
    while(messages.size()==0)
      {
      try
        {
        wait();
        }
      catch(InterruptedException e)
        {
        marauroad.report(e.getMessage());
        }
      }
      
    return (Message)messages.remove(0);
    }
    
  public synchronized void addMessage(Message msg)
    {
    writeManager.write(msg);
    }       
  
  class NetworkServerManagerRead extends Thread
    {
    NetworkServerManagerRead()
      {
      super("NetworkServerManagerRead");
      }
    
    public void run()
      {
      marauroad.report("Start thread "+this.getName());

      while(keepRunning)
        {
        byte[] buffer=new byte[NetConst.UDP_PACKET_SIZE];
        DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
        
        try
          {
          socket.receive(packet);          

          Message msg=msgFactory.getMessage(packet.getData(),(InetSocketAddress)packet.getSocketAddress());
          
          messages.add(msg);
          newMessageArrived();
          }
        catch(java.net.SocketTimeoutException e)
          {
          }
        catch(IOException e)
          {
          /* Report the exception */
          marauroad.report(e.getMessage());
          }
        }
        
      marauroad.report("End thread "+this.getName());
      isfinished=true;              
      }    
    }        
    
  class NetworkServerManagerWrite
    {
    private String name;
    
    NetworkServerManagerWrite()
      {
      name="NetworkServerManagerWrite";
      }
    
 	public void write(Message message)
 	  {
 	  try
 	    {
 	    if(keepRunning)
 	      {
 	      ByteArrayOutputStream out=new ByteArrayOutputStream();
 	      OutputSerializer s=new OutputSerializer(out);
 	 
 	      s.write(message);
 	   
 	      byte[] buffer=out.toByteArray();
   	      DatagramPacket pkt=new DatagramPacket(buffer,buffer.length,message.getAddress());
 	     
 	      socket.send(pkt);
 	      }
 	    }
 	  catch(IOException e)
 	    { 	 
        /* Report the exception */
        marauroad.report(e.getMessage());
 	    }
 	  }
    }    
  }