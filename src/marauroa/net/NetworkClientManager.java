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
  private int clientid;
  
  private MessageFactory msgFactory;
  
  static private class PacketContainer
    {
    public byte signature;
    public byte position;
    public byte total;
    public byte[] content;
    }    

  private List pendingPackets;

  /** Constructor that opens the socket on the marauroa_PORT and start the thread
      to recieve new messages from the network. */
  public NetworkClientManager(String host) throws SocketException
    {
    clientid=0;
    address=new InetSocketAddress(host,NetConst.marauroa_PORT);
    socket=new DatagramSocket();
    socket.setSoTimeout(100);
       
    msgFactory=MessageFactory.getFactory();
    pendingPackets=new LinkedList();
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
      byte[] data=packet.getData();
      
      /* We look the two first byte to see if it is a full message or not */
      if(data[0]==1)
        {
        byte[] messageData=new byte[data.length-3];
        System.arraycopy(data,3,messageData,0,data.length-3);
        
        Message msg=msgFactory.getMessage(messageData,(InetSocketAddress)packet.getSocketAddress());      
        System.out.println("NetworkClientManager: receive message("+msg.getType()+") from "+msg.getClientID());
      
        if(msg.getType()==Message.TYPE_S2C_LOGIN_ACK)
          {
          clientid=msg.getClientID();        
          }

        return msg;
        }
      else
        {
        /* A multipart message. We try to read the rest now. 
         * We need to check on the list if the message exist and it exist we add this one. */
        System.out.println("NetworkClientManager: receive multipart message ("+data[0]+")");
//        PacketContainer container=new PacketContainer();
//        container.total=data[0];
//        container.position=data[1];
//        container.signature=data[2];
//
//        container.content=new byte[data.length-3];
//        System.arraycopy(data,3,container.content,0,data.length-3);      

        return null;
        }
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