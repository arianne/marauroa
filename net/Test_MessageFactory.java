package marauroa.net;

import marauroa.net.*;
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
    return new TestSuite(Test_Messages.class);
	}
	
  public void testMessageFactory()
    {
    MessageFactory msgFac=MessageFactory.getFactory();
/*
    msgFac.register(Message.TYPE_C2S_LOGIN,new MessageC2SLogin());
    msgFac.register(Message.TYPE_S2C_LOGIN_ACK,new MessageS2CLoginACK());
    msgFac.register(Message.TYPE_S2C_LOGIN_NACK,new MessageS2CLoginNACK());
    
    msgFac.register(Message.TYPE_S2C_CHARACTERLIST,new MessageS2CCharacterList());
  
    msgFac.register(Message.TYPE_C2S_CHOOSECHARACTER,new MessageC2SChooseCharacter());
    msgFac.register(Message.TYPE_S2C_CHOOSECHARACTER_ACK,new MessageS2CChooseCharacterACK());
    msgFac.register(Message.TYPE_S2C_CHOOSECHARACTER_NACK,new MessageS2CChooseCharacterNACK());
  
    msgFac.register(Message.TYPE_C2S_LOGOUT,new MessageC2SLogout());
    msgFac.register(Message.TYPE_S2C_LOGOUT_ACK,new MessageS2CLogoutACK());
    msgFac.register(Message.TYPE_S2C_LOGOUT_NACK,new MessageS2CLogoutNACK());
*/    
    
    String character="Test character";
    short clientid=14324;
    
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

    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
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
  }