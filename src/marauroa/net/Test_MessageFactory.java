package marauroa.net;

import marauroa.net.*;
import marauroa.game.*;
import java.io.*;
import java.lang.Byte;
import junit.framework.*;

public class Test_MessageFactory extends TestCase
  {
  private ByteArrayOutputStream out;
  private ByteArrayInputStream in;
  
  private InputSerializer sin;
  private OutputSerializer sout;

  public static Test suite ( ) 
    {
    return new TestSuite(Test_MessageFactory.class);
	}
	
  public void testMessageFactoryRandomMessage()
    {
    MessageFactory msgFac=MessageFactory.getFactory();
   
    String character="Test character";
    int clientid=14324;
    
    MessageC2SChooseCharacter msg=new MessageC2SChooseCharacter(null,character);
    msg.setClientID(clientid);
    
    out=new ByteArrayOutputStream();
    sout=new OutputSerializer(out);
    
    try
      {
      sout.write(msg);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data out");
      }    

    assertEquals(Message.TYPE_C2S_CHOOSECHARACTER,msg.getType());
    assertEquals(clientid,msg.getClientID());
    assertEquals(character,msg.getCharacter());
    
    try
      {
      Message result=msgFac.getMessage(out.toByteArray(),null);

      assertNotNull(result);    
      assertEquals(Message.TYPE_C2S_CHOOSECHARACTER,result.getType());
    
      MessageC2SChooseCharacter realResult=(MessageC2SChooseCharacter)result;
      assertEquals(Message.TYPE_C2S_CHOOSECHARACTER,realResult.getType());
      assertEquals(clientid,realResult.getClientID());
      assertEquals(character,realResult.getCharacter());
      }
    catch(IOException e)
      {
      fail(e.getMessage());
      }
    }

  public void testMessageFactoryFailOnUnregisteredMessage()
    {
    MessageFactory msgFac=MessageFactory.getFactory();
   
    String character="Test character";
    int clientid=14324;
    
    MessageC2SChooseCharacter msg=new MessageC2SChooseCharacter(null,character);
    msg.setClientID(clientid);
    
    out=new ByteArrayOutputStream();
    sout=new OutputSerializer(out);
    
    try
      {
      sout.write(msg);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data out");
      }    

    byte[] outarray=out.toByteArray();
    outarray[1]=Message.TYPE_INVALID;
    
    try
      {
      Message result=msgFac.getMessage(outarray,null);
      fail("Expected IOException");
      }
    catch(IOException e)
      {      
      }
    }

  public void testMessageFactoryFailOnWrongProtocol()
    {
    MessageFactory msgFac=MessageFactory.getFactory();
   
    String character="Test character";
    int clientid=14324;
    
    MessageC2SChooseCharacter msg=new MessageC2SChooseCharacter(null,character);
    msg.setClientID(clientid);
    
    out=new ByteArrayOutputStream();
    sout=new OutputSerializer(out);
    
    try
      {
      sout.write(msg);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data out");
      }    

    byte[] outarray=out.toByteArray();
    /* We make protocol to differ from the expected */
    outarray[0]=NetConst.NETWORK_PROTOCOL_VERSION-1;
    
    try
      {
      Message result=msgFac.getMessage(outarray,null);
      fail("Expected IOException");
      }
    catch(IOException e)
      {      
      }
    }

  public void testMessageFactoryAllMessages()
    {
    MessageFactory msgFac=MessageFactory.getFactory();
    
    Message msg_0=new MessageC2SAction(null,new marauroa.game.RPAction());
    Message msg_1=new MessageC2SChooseCharacter(null,"Test character");
    Message msg_2=new MessageC2SLogin(null,"Test username","Test password");
    Message msg_3=new MessageC2SLogout(null);
    Message msg_4=new MessageS2CCharacterList(null,new String[0]);
    Message msg_5=new MessageS2CChooseCharacterACK(null, new RPObject.ID(0));
    Message msg_6=new MessageS2CChooseCharacterNACK(null);
    Message msg_7=new MessageS2CLoginACK(null);
    Message msg_8=new MessageS2CLoginNACK(null,MessageS2CLoginNACK.SERVER_IS_FULL);
    Message msg_9=new MessageS2CLogoutACK(null);
    Message msg_10=new MessageS2CLogoutNACK(null);
    Message msg_11=new MessageS2CActionACK(null);
    
    ByteArrayOutputStream out_0=new ByteArrayOutputStream();
    ByteArrayOutputStream out_1=new ByteArrayOutputStream();
    ByteArrayOutputStream out_2=new ByteArrayOutputStream();
    ByteArrayOutputStream out_3=new ByteArrayOutputStream();
    ByteArrayOutputStream out_4=new ByteArrayOutputStream();
    ByteArrayOutputStream out_5=new ByteArrayOutputStream();
    ByteArrayOutputStream out_6=new ByteArrayOutputStream();
    ByteArrayOutputStream out_7=new ByteArrayOutputStream();
    ByteArrayOutputStream out_8=new ByteArrayOutputStream();
    ByteArrayOutputStream out_9=new ByteArrayOutputStream();
    ByteArrayOutputStream out_10=new ByteArrayOutputStream();
    ByteArrayOutputStream out_11=new ByteArrayOutputStream();
    
    try
      {
      sout=new OutputSerializer(out_0);
      sout.write(msg_0);
      sout=new OutputSerializer(out_1);
      sout.write(msg_1);
      sout=new OutputSerializer(out_2);
      sout.write(msg_2);
      sout=new OutputSerializer(out_3);
      sout.write(msg_3);
      sout=new OutputSerializer(out_4);
      sout.write(msg_4);
      sout=new OutputSerializer(out_5);
      sout.write(msg_5);
      sout=new OutputSerializer(out_6);
      sout.write(msg_6);
      sout=new OutputSerializer(out_7);
      sout.write(msg_7);
      sout=new OutputSerializer(out_8);
      sout.write(msg_8);
      sout=new OutputSerializer(out_9);
      sout.write(msg_9);
      sout=new OutputSerializer(out_10);
      sout.write(msg_10);
      sout=new OutputSerializer(out_11);
      sout.write(msg_11);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data out");
      }    

    assertEquals(Message.TYPE_C2S_ACTION,msg_0.getType());
    assertEquals(Message.TYPE_C2S_CHOOSECHARACTER,msg_1.getType());
    assertEquals(Message.TYPE_C2S_LOGIN,msg_2.getType());
    assertEquals(Message.TYPE_C2S_LOGOUT,msg_3.getType());
    assertEquals(Message.TYPE_S2C_CHARACTERLIST,msg_4.getType());
    assertEquals(Message.TYPE_S2C_CHOOSECHARACTER_ACK,msg_5.getType());
    assertEquals(Message.TYPE_S2C_CHOOSECHARACTER_NACK,msg_6.getType());
    assertEquals(Message.TYPE_S2C_LOGIN_ACK,msg_7.getType());
    assertEquals(Message.TYPE_S2C_LOGIN_NACK,msg_8.getType());
    assertEquals(Message.TYPE_S2C_LOGOUT_ACK,msg_9.getType());
    assertEquals(Message.TYPE_S2C_LOGOUT_NACK,msg_10.getType());
    assertEquals(Message.TYPE_S2C_ACTION_ACK,msg_11.getType());

    try
      {
      Message result_0=msgFac.getMessage(out_0.toByteArray(),null);
      Message result_1=msgFac.getMessage(out_1.toByteArray(),null);
      Message result_2=msgFac.getMessage(out_2.toByteArray(),null);
      Message result_3=msgFac.getMessage(out_3.toByteArray(),null);
      Message result_4=msgFac.getMessage(out_4.toByteArray(),null);
      Message result_5=msgFac.getMessage(out_5.toByteArray(),null);
      Message result_6=msgFac.getMessage(out_6.toByteArray(),null);
      Message result_7=msgFac.getMessage(out_7.toByteArray(),null);
      Message result_8=msgFac.getMessage(out_8.toByteArray(),null);
      Message result_9=msgFac.getMessage(out_9.toByteArray(),null);
      Message result_10=msgFac.getMessage(out_10.toByteArray(),null);
      Message result_11=msgFac.getMessage(out_11.toByteArray(),null);

      assertEquals(Message.TYPE_C2S_ACTION,result_0.getType());
      assertEquals(Message.TYPE_C2S_CHOOSECHARACTER,result_1.getType());
      assertEquals(Message.TYPE_C2S_LOGIN,result_2.getType());
      assertEquals(Message.TYPE_C2S_LOGOUT,result_3.getType());
      assertEquals(Message.TYPE_S2C_CHARACTERLIST,result_4.getType());
      assertEquals(Message.TYPE_S2C_CHOOSECHARACTER_ACK,result_5.getType());
      assertEquals(Message.TYPE_S2C_CHOOSECHARACTER_NACK,result_6.getType());
      assertEquals(Message.TYPE_S2C_LOGIN_ACK,result_7.getType());
      assertEquals(Message.TYPE_S2C_LOGIN_NACK,result_8.getType());
      assertEquals(Message.TYPE_S2C_LOGOUT_ACK,result_9.getType());
      assertEquals(Message.TYPE_S2C_LOGOUT_NACK,result_10.getType());
      assertEquals(Message.TYPE_S2C_ACTION_ACK,result_11.getType());
      }
    catch(IOException e)
      {
      fail(e.getMessage());
      }
    }
  }