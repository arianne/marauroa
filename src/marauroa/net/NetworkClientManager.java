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
    public byte remaining;
    public byte[] content;
    public InetSocketAddress address;
    }    
  
  private Map pendingPackets;

  /** Constructor that opens the socket on the marauroa_PORT and start the thread
      to recieve new messages from the network. */
  public NetworkClientManager(String host) throws SocketException
    {
    clientid=0;
    address=new InetSocketAddress(host,NetConst.marauroa_PORT);
    socket=new DatagramSocket();
    socket.setSoTimeout(100);
    
    msgFactory=MessageFactory.getFactory();
    pendingPackets=new HashMap();
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
    try
      {          
      Iterator it=pendingPackets.entrySet().iterator();
      while(it.hasNext())
        {
        Map.Entry entry=(Map.Entry)it.next();
        PacketContainer message=(PacketContainer)entry.getValue();
        if(message.remaining==0)
          {
          Message msg=msgFactory.getMessage(message.content,message.address);      
          System.out.println("NetworkClientManager: receive message("+msg.getType()+") from "+msg.getClientID());
      
          if(msg.getType()==Message.TYPE_S2C_LOGIN_ACK)
            {
            clientid=msg.getClientID();        
            }
              
          pendingPackets.remove(new Byte(message.signature));
          return msg;
          }
        }
      }
    catch(IOException e)
      {
      /* Report the exception */
      marauroad.report(e.getMessage());
      return null;
      }

    byte[] buffer=new byte[NetConst.UDP_PACKET_SIZE];
    DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
    int i=0;
    
    try
      {
      /** We want to avoid this to block the whole client recieving messages */
      while(i<5)
        {
        ++i;
        
        socket.receive(packet);          
        byte[] data=packet.getData();
      
        /* A multipart message. We try to read the rest now. 
         * We need to check on the list if the message exist and it exist we add this one. */
        byte total=data[0];
        byte position=data[1];
        byte signature=data[2];
        System.out.println("NetworkClientManager: receive multipart message("+signature+"): "+position+1+" of "+total);

        if(!pendingPackets.containsKey(new Byte(signature)))
          {
          /** This is the first packet */
          PacketContainer message=new PacketContainer();
          message.signature=signature;
          message.remaining=(byte)(total-1);
          message.address=(InetSocketAddress)packet.getSocketAddress();
          message.content=new byte[(NetConst.UDP_PACKET_SIZE-3)*total];
          
          System.arraycopy(data,3,message.content,(NetConst.UDP_PACKET_SIZE-3)*position,data.length-3);      
          pendingPackets.put(new Byte(signature),message);
          }
        else
          {
          PacketContainer message=(PacketContainer)pendingPackets.get(new Byte(signature));
          --message.remaining;
          
          if(message.remaining<0)
            {
            System.out.println("ERROR: We confused the messages");
            return null;
            }

          System.arraycopy(data,3,message.content,(NetConst.UDP_PACKET_SIZE-3)*position,data.length-3);      
          }
        }
        
      return null;        
      }
    catch(java.net.SocketTimeoutException e)
      {
      /* We need the thread to check from time to time if user has requested an exit */
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