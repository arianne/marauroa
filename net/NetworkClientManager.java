package marauroa.net;

import java.net.*;
import java.util.*;
import java.io.*;

import marauroa.marauroad;

/** The NetworkClientManager is in charge of sending and recieving the packages 
 *  from the network. */
public class NetworkClientManager
  {
  private DatagramSocket socket;
  private InetSocketAddress address;
  private short clientid;
  
  private MessageFactory msgFactory;
  
  /** Constructor that opens the socket on the marauroa_PORT and start the thread
      to recieve new messages from the network. */
  public NetworkClientManager(String host) throws SocketException
    {
    clientid=0;
    address=new InetSocketAddress(host,NetConst.marauroa_PORT);
    socket=new DatagramSocket();
    socket.setSoTimeout(100);
       
    msgFactory=MessageFactory.getFactory();
    }
  
  /** This method notify the thread to finish it execution */
  public void finish()
    {
    socket.close();
    }

  /** This method returns a message if it is available or null
   *  @return a Message*/
  public Message getMessage()
    {
    byte[] buffer=new byte[NetConst.UDP_PACKET_SIZE];
    DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
    
    try
      {
      socket.receive(packet);          
      Message msg=msgFactory.getMessage(packet.getData(),(InetSocketAddress)packet.getSocketAddress());      
      System.out.println("NetworkClientManager: receive message("+msg.getType()+") from "+msg.getClientID());
      
      if(msg.getType()==Message.TYPE_S2C_LOGIN_ACK)
        {
        clientid=msg.getClientID();        
        }

      return msg;
      }
    catch(java.net.SocketTimeoutException e)
      {
      /* We need the thread to check from time to time if user has requested
       * an exit */
      return null;
      }
    catch(IOException e)
      {
      /* Report the exception */
      marauroad.report(e.getMessage());
      return null;
      }
     }
    
  /** This method add a message to be delivered to the client the message is pointed to.
   *  @param msg the message to ve delivered. */
  public synchronized void addMessage(Message msg)
    {
    try
      {
      /* We enforce the remote endpoint */
      msg.setAddress(address);
      msg.setClientID(clientid);
      
      ByteArrayOutputStream out=new ByteArrayOutputStream();
      OutputSerializer s=new OutputSerializer(out);
 
      System.out.println("NetworkClientManager: send message("+msg.getType()+") from "+msg.getClientID());
      s.write(msg);
   
      byte[] buffer=out.toByteArray();
      DatagramPacket pkt=new DatagramPacket(buffer,buffer.length,msg.getAddress());
     
      socket.send(pkt);
      }
    catch(IOException e)
      { 	 
      /* Report the exception */
      marauroad.report(e.getMessage());
      }
    }       
  }