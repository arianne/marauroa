package marauroa.net;

import java.net.*;
import java.util.*;
import java.io.*;


public class NetworkServerManager
  {
  private DatagramSocket socket;
  private boolean keepRunning;
  
  NetworkServerManager() throws SocketException
    {    
    socket=new DatagramSocket(NetConst.marauroa_PORT);
    keepRunning=true;
    }    
  
  class NetworkServerManagerRead  extends Thread
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
          }
        catch(IOException e)
          {
          }
      
        //Message msg=MessageFactory.getClass(packet.getData(),packet.getSocketAddress()); 
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
 	    }
 	  catch(IOException e)
 	    { 	 
 	    /* Report the problem */   
 	    }
 	  }
    }    
  }