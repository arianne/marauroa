/* $Id: Test_Messages.java,v 1.9 2004/01/01 12:56:54 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.net;

import marauroa.net.*;
import marauroa.game.*;
import marauroa.*;
import java.io.*;
import java.lang.Byte;
import junit.framework.*;
import java.util.LinkedList;

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
    marauroad.trace("Test_Messages::testMessageC2SChooseCharacter","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageC2SChooseCharacter",">");
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
    marauroad.trace("Test_Messages::testMessageC2SChooseCharacter","<");
    }

  public void testMessageC2SLogin()
    {
    marauroad.trace("Test_Messages::testMessageC2SLogin","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageC2SLogin",">");
    String username="Test username";
    String password="Test password";
    int clientid=14324;
    
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
    marauroad.trace("Test_Messages::testMessageC2SLogin","<");
    }

  public void testMessageC2SLogout()
    {
    marauroad.trace("Test_Messages::testMessageC2SLogout","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageC2SLogout",">");
    int clientid=14324;
    
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
    marauroad.trace("Test_Messages::testMessageC2SLogout","<");
    }
  
  public void testMessageS2CCharacterList()
    {
    marauroad.trace("Test_Messages::testMessageS2CCharacterList","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageS2CCharacterList",">");
    int clientid=14324;
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
    marauroad.trace("Test_Messages::testMessageS2CCharacterList","<");
    }  

  public void testMessageS2CChooseCharacterACK()
    {
    marauroad.trace("Test_Messages::testMessageS2CChooseCharacterACK","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageS2CChooseCharacterACK",">");
    int clientid=14324;
    
    MessageS2CChooseCharacterACK msg=new MessageS2CChooseCharacterACK(null,new RPObject.ID(0));
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
    marauroad.trace("Test_Messages::testMessageS2CChooseCharacterACK","<");
    }
  
  public void testMessageS2CChooseCharacterNACK()
    {
    marauroad.trace("Test_Messages::testMessageS2CChooseCharacterNACK","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageS2CChooseCharacterNACK",">");
    int clientid=14324;
    
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
    marauroad.trace("Test_Messages::testMessageS2CChooseCharacterNACK","<");
    }
  
  public void testMessageS2CLoginACK()
    {
    marauroad.trace("Test_Messages::testMessageS2CLoginACK","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageS2CLoginACK",">");
    int clientid=14324;
    
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
    
    marauroad.trace("Test_Messages::testMessageS2CLoginACK","<");
    }
   
  public void testMessageS2CLoginNACK()
    {
    marauroad.trace("Test_Messages::testMessageS2CLoginNACK","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageS2CLoginNACK",">");
    byte reason=MessageS2CLoginNACK.USERNAME_WRONG;
    int clientid=14324;
    
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
    
    marauroad.trace("Test_Messages::testMessageS2CLoginNACK","<");
    }

  public void testMessageS2CLogoutACK()
    {
    marauroad.trace("Test_Messages::testMessageS2CLogoutACK","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageS2CLogoutACK",">");
    int clientid=14324;
    
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
    marauroad.trace("Test_Messages::testMessageS2CLogoutACK","<");
    }

  public void testMessageS2CLogoutNACK()
    {
    marauroad.trace("Test_Messages::testMessageS2CLogoutNACK","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageS2CLogoutNACK",">");
    int clientid=14324;
    
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
    marauroad.trace("Test_Messages::testMessageS2CLogoutNACK","<");
    }
    
  public void testMessageC2SAction()
    {
    marauroad.trace("Test_Messages::testMessageC2SAction","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageC2SAction",">");
    int clientid=14324;
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

    marauroad.trace("Test_Messages::testMessageC2SAction","<");
    }

  public void testMessageS2CActionACK()
    {
    marauroad.trace("Test_Messages::testMessageS2CActionACK","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageS2CActionACK",">");
    int clientid=14324;
    
    MessageS2CActionACK msg=new MessageS2CActionACK(null,123416);
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

    marauroad.trace("Test_Messages::testMessageS2CActionACK","<");
    }
      
  public void testMessageS2CPerception()
    {
    marauroad.trace("Test_Messages::testMessageS2CPerception","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageS2CPerception",">");
    int clientid=14324;
    
    MessageS2CPerception msg=new MessageS2CPerception(null, RPZone.Perception.TOTAL, new LinkedList(), new LinkedList());
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

    assertEquals(Message.TYPE_S2C_PERCEPTION,msg.getType());
    assertEquals(clientid,msg.getClientID());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageS2CPerception result=new MessageS2CPerception();
    
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
    
    assertEquals(Message.TYPE_S2C_PERCEPTION,result.getType());
    assertEquals(clientid,result.getClientID());
    marauroad.trace("Test_Messages::testMessageS2CPerception","<");
    }

  public void testMessageS2CServerInfo()
    {
    marauroad.trace("Test_Messages::testMessageS2CServerInfo","?","This test case try to "+
      "serialize the message and deserialize it and then check it is equal");
    marauroad.trace("Test_Messages::testMessageS2CServerInfo",">");
    int clientid=14324;
    String[] contents={"hi","world"};
    
    MessageS2CServerInfo msg=new MessageS2CServerInfo(null,contents);
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

    assertEquals(Message.TYPE_S2C_SERVERINFO,msg.getType());
    assertEquals(clientid,msg.getClientID());
    
    in=new ByteArrayInputStream(out.toByteArray());
    sin=new InputSerializer(in);
    
    MessageS2CServerInfo result=new MessageS2CServerInfo();
    
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
    
    assertEquals(Message.TYPE_S2C_SERVERINFO,result.getType());
    assertEquals(clientid,result.getClientID());
    marauroad.trace("Test_Messages::testMessageS2CServerInfo","<");
    }
    
  public void testSeveralMessageSameStream()
    {
    marauroad.trace("Test_Messages::testSeveralMessageSameStream","?", "This test case try to serialize"+
      " several messages into the same stream and then deserialize them");
    marauroad.trace("Test_Messages::testSeveralMessageSameStream",">");
    int clientid=14324;
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

    marauroad.trace("Test_Messages::testSeveralMessageSameStream","<");
    }  
  }