package marauroa.net;

import marauroa.net.*;
import java.io.*;
import java.lang.Byte;
import junit.framework.*;

public class Test_Messages extends TestCase
  {
  private ByteArrayOutputStream out;
  private ByteArrayInputStream in;
  
  private InputSerializer sin;
  private OutputSerializer sout;

  public static Test suite ( ) 
    {
    return new TestSuite(Test_Messages.class);
	}
	
  public void testMessageC2SChooseCharacter()
    {
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
    
    MessageC2SChooseCharacter result=new MessageC2SChooseCharacter();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_C2S_CHOOSECHARACTER,result.getType());
    assertEquals(clientid,result.getClientID());
    assertEquals(character,result.getCharacter());
    }

  public void testMessageC2SLogin()
    {
    String username="Test username";
    String password="Test password";
    short clientid=14324;
    
    MessageC2SLogin msg=new MessageC2SLogin(null,username,password);
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

    assertEquals(Message.TYPE_C2S_LOGIN,msg.getType());
    assertEquals(clientid,msg.getClientID());
    assertEquals(username,msg.getUsername());
    assertEquals(password,msg.getPassword());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageC2SLogin result=new MessageC2SLogin();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_C2S_LOGIN,result.getType());
    assertEquals(clientid,result.getClientID());
    assertEquals(username,result.getUsername());
    assertEquals(password,result.getPassword());
    }

  public void testMessageC2SLogout()
    {
    short clientid=14324;
    
    MessageC2SLogout msg=new MessageC2SLogout(null);
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

    assertEquals(Message.TYPE_C2S_LOGOUT,msg.getType());
    assertEquals(clientid,msg.getClientID());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageC2SLogout result=new MessageC2SLogout();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_C2S_LOGOUT,result.getType());
    assertEquals(clientid,result.getClientID());
    }
  
  public void testMessageS2CCharacterList()
    {
    short clientid=14324;
    String[] characters={"Test character","Another Test character"};
    
    MessageS2CCharacterList msg=new MessageS2CCharacterList(null,characters);
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

    assertEquals(Message.TYPE_S2C_CHARACTERLIST,msg.getType());
    assertEquals(clientid,msg.getClientID());
    assertEquals(characters,msg.getCharacters());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageS2CCharacterList result=new MessageS2CCharacterList();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_S2C_CHARACTERLIST,result.getType());
    assertEquals(clientid,result.getClientID());
    assertEquals(characters.length,result.getCharacters().length);
    
    for(int i=0;i<characters.length;++i)
      {
      assertEquals(characters[i],result.getCharacters()[i]);
      }
    }  

  public void testMessageS2CChooseCharacterACK()
    {
    short clientid=14324;
    
    MessageS2CChooseCharacterACK msg=new MessageS2CChooseCharacterACK(null);
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

    assertEquals(Message.TYPE_S2C_CHOOSECHARACTER_ACK,msg.getType());
    assertEquals(clientid,msg.getClientID());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageS2CChooseCharacterACK result=new MessageS2CChooseCharacterACK();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_S2C_CHOOSECHARACTER_ACK,result.getType());
    assertEquals(clientid,result.getClientID());
    }
  
  public void testMessageS2CChooseCharacterNACK()
    {
    short clientid=14324;
    
    MessageS2CChooseCharacterNACK msg=new MessageS2CChooseCharacterNACK(null);
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

    assertEquals(Message.TYPE_S2C_CHOOSECHARACTER_NACK,msg.getType());
    assertEquals(clientid,msg.getClientID());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageS2CChooseCharacterNACK result=new MessageS2CChooseCharacterNACK();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_S2C_CHOOSECHARACTER_NACK,result.getType());
    assertEquals(clientid,result.getClientID());
    }
  
  public void testMessageS2CLoginACK()
    {
    short clientid=14324;
    
    MessageS2CLoginACK msg=new MessageS2CLoginACK(null);
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

    assertEquals(Message.TYPE_S2C_LOGIN_ACK,msg.getType());
    assertEquals(clientid,msg.getClientID());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageS2CLoginACK result=new MessageS2CLoginACK();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_S2C_LOGIN_ACK,result.getType());
    assertEquals(clientid,result.getClientID());
    }
   
  public void testMessageS2CLoginNACK()
    {
    byte reason=MessageS2CLoginNACK.USERNAME_WRONG;
    short clientid=14324;
    
    MessageS2CLoginNACK msg=new MessageS2CLoginNACK(null,reason);
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

    assertEquals(Message.TYPE_S2C_LOGIN_NACK,msg.getType());
    assertEquals(reason,msg.getResolutionCode());
    assertEquals(clientid,msg.getClientID());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageS2CLoginNACK result=new MessageS2CLoginNACK();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_S2C_LOGIN_NACK,result.getType());
    assertEquals(reason,msg.getResolutionCode());
    assertEquals(clientid,result.getClientID());
    }

  public void testMessageS2CLogoutACK()
    {
    short clientid=14324;
    
    MessageS2CLogoutACK msg=new MessageS2CLogoutACK(null);
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

    assertEquals(Message.TYPE_S2C_LOGOUT_ACK,msg.getType());
    assertEquals(clientid,msg.getClientID());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageS2CLogoutACK result=new MessageS2CLogoutACK();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_S2C_LOGOUT_ACK,result.getType());
    assertEquals(clientid,result.getClientID());
    }

  public void testMessageS2CLogoutNACK()
    {
    short clientid=14324;
    
    MessageS2CLogoutNACK msg=new MessageS2CLogoutNACK(null);
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

    assertEquals(Message.TYPE_S2C_LOGOUT_NACK,msg.getType());
    assertEquals(clientid,msg.getClientID());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageS2CLogoutNACK result=new MessageS2CLogoutNACK();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_S2C_LOGOUT_NACK,result.getType());
    assertEquals(clientid,result.getClientID());
    }
    
  public void testMessageC2SAction()
    {
    short clientid=14324;
    marauroa.game.RPAction action=new marauroa.game.RPAction();
    action.put("object_id","156123");
    action.put("zone_id","1");
    action.put("action_type","null action");
    
    MessageC2SAction msg=new MessageC2SAction(null,action);
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

    assertEquals(Message.TYPE_C2S_ACTION,msg.getType());
    assertEquals(clientid,msg.getClientID());
    assertEquals(action,msg.getRPAction());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageC2SAction result=new MessageC2SAction();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_C2S_ACTION,result.getType());
    assertEquals(clientid,result.getClientID());
    assertEquals(action,result.getRPAction());
    }

  public void testMessageS2CActionACK()
    {
    short clientid=14324;
    
    MessageS2CActionACK msg=new MessageS2CActionACK(null);
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

    assertEquals(Message.TYPE_S2C_ACTION_ACK,msg.getType());
    assertEquals(clientid,msg.getClientID());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageS2CActionACK result=new MessageS2CActionACK();
    
    try
      {
      sin.readObject(result);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }
    
    assertEquals(Message.TYPE_S2C_ACTION_ACK,result.getType());
    assertEquals(clientid,result.getClientID());
    }
      
  public void testSeveralMessageSameStream()
    {
    short clientid=14324;
    String username="Test username";
    String password="Test password";
    
    MessageC2SLogin msgLogin=new MessageC2SLogin(null,username,password);
    MessageS2CLoginACK msgLoginACK=new MessageS2CLoginACK(null);
    MessageC2SLogout msgLogout= new MessageC2SLogout(null);
    MessageS2CLogoutNACK msgLogoutNACK=new MessageS2CLogoutNACK(null);
    
    msgLogin.setClientID(clientid);
    msgLoginACK.setClientID(clientid);
    msgLogout.setClientID(clientid);
    msgLogoutNACK.setClientID(clientid);
    
    out=new ByteArrayOutputStream();
    sout=new OutputSerializer(out);
    
    try
      {
      sout.write(msgLogin);
      sout.write(msgLoginACK);
      sout.write(msgLogout);
      sout.write(msgLogoutNACK);      
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data out");
      }    

    assertEquals(Message.TYPE_C2S_LOGIN,msgLogin.getType());
    assertEquals(Message.TYPE_S2C_LOGIN_ACK,msgLoginACK.getType());
    assertEquals(Message.TYPE_C2S_LOGOUT,msgLogout.getType());
    assertEquals(Message.TYPE_S2C_LOGOUT_NACK,msgLogoutNACK.getType());
    
    assertEquals(clientid,msgLogin.getClientID());
    assertEquals(clientid,msgLoginACK.getClientID());
    assertEquals(clientid,msgLogout.getClientID());
    assertEquals(clientid,msgLogoutNACK.getClientID());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageC2SLogin resultLogin=new MessageC2SLogin();
    MessageS2CLoginACK resultLoginACK=new MessageS2CLoginACK();
    MessageC2SLogout resultLogout=new MessageC2SLogout();
    MessageS2CLogoutNACK resultLogoutNACK=new MessageS2CLogoutNACK();
    
    try
      {
      sin.readObject(resultLogin);
      sin.readObject(resultLoginACK);
      sin.readObject(resultLogout);
      sin.readObject(resultLogoutNACK);
      }
    catch(IOException e)
      {
      fail("Exception happened when serializing data in");
      }
    catch(java.lang.ClassNotFoundException e)
      {
      fail("Exception happened when serializing data in");
      }

    assertEquals(Message.TYPE_C2S_LOGIN,resultLogin.getType());
    assertEquals(Message.TYPE_S2C_LOGIN_ACK,resultLoginACK.getType());
    assertEquals(Message.TYPE_C2S_LOGOUT,resultLogout.getType());
    assertEquals(Message.TYPE_S2C_LOGOUT_NACK,resultLogoutNACK.getType());
    
    assertEquals(clientid,resultLogin.getClientID());
    assertEquals(clientid,resultLoginACK.getClientID());
    assertEquals(clientid,resultLogout.getClientID());
    assertEquals(clientid,resultLogoutNACK.getClientID());
    }  
  }