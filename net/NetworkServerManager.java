package marauroa.net;

import java.net.*;
import java.util.*;
import java.io.*;

import marauroa.marauroad;

/** The NetworkServerManager is the active entity of the marauroa.net package,
 *  it is in charge of sending and recieving the packages from the network. */
public class NetworkServerManager
  {
  private DatagramSocket socket;
  private boolean keepRunning;  
  private boolean isfinished;
  
  private MessageFactory msgFactory;
  private List messages;  
  private NetworkServerManagerRead readManager;
  private NetworkServerManagerWrite writeManager;
  
  /** Constructor that opens the socket on the marauroa_PORT and start the thread
      to recieve new messages from the network. */
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
  
  /** This method notify the thread to finish it execution */
  public synchronized void finish()
    {
    keepRunning=false;
    
    while(isfinished==false)
      {
      /* Tricky wait of doing a sleep. Does exist sleep for no threads? */
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
 
  /** This method returns a Message from the list or block for timeout milliseconds
   *  until a message is available or null if timeout happens.
   *  @param timeout timeout time in milliseconds
   *  @return a Message or null if timeout happens */
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

  /** This method blocks until a message is available 
   *  @return a Message*/
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
    
  /** This method add a message to be delivered to the client the message is pointed to.
   *  @param msg the message to ve delivered. */
  public synchronized void addMessage(Message msg)
    {
    writeManager.write(msg);
    }       
  
  /** The active thread in charge of recieving messages from the network. */
  class NetworkServerManagerRead extends Thread
    {
    public NetworkServerManagerRead()
      {
      super("NetworkServerManagerRead");
      }
    
    /** Method that execute the reading */
    public void run()
      {
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
          /* We need the thread to check from time to time if user has requested
           * an exit */
          }
        catch(IOException e)
          {
          /* Report the exception */
          marauroad.report(e.getMessage());
          }
        }

      isfinished=true;              
      }    
    }        
    
  /** A wrapper class for sending messages to clients */
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