package marauroa.net;

import java.net.*;
import java.util.*;
import java.io.*;


public class NetworkServerManager
  {
  private DatagramSocket socket;
  private boolean keepRunning;
  private MessageFactory msgFactory;
  private List messages;
  
  NetworkServerManager() throws SocketException
    {    
    socket=new DatagramSocket(NetConst.marauroa_PORT);
    msgFactory=MessageFactory.getFactory();
    keepRunning=true;
    messages=Collections.synchronizedList(new LinkedList());
    }    
  
  class NetworkServerManagerRead extends Thread
    {
    NetworkServerManagerRead()
      {
      super("NetworkServerManagerRead");
      }
    
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
          }
        catch(IOException e)
          {
          /* Report the problem */
          }
        }
      }    
    }        
    
  class NetworkServerManagerWrite
    {
    NetworkServerManagerWrite()
      {
      }
    
 	public void write(Message message)
 	  {
 	  try
 	    {
 	    ByteArrayOutputStream out=new ByteArrayOutputStream();
 	    OutputSerializer s=new OutputSerializer(out);
 	 
 	    s.write(message);
 	  
 	    byte[] buffer=out.toByteArray();
 	    DatagramPacket pkt=new DatagramPacket(buffer,buffer.length,message.getAddress());
 	    
 	    socket.send(pkt);
 	    }
 	  catch(IOException e)
 	    { 	 
 	    /* Report the problem */   
 	    }
 	  }
    }    
  }