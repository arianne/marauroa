<!--
/*  $Id: index.jsp,v 1.1 2004/05/07 15:27:49 root777 Exp $ */
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
-->
<%@ page import="marauroa.game.JDBCPlayerDatabase" %>
<%@ page import="the1001.objects.Gladiator" %>
<%@ page import="the1001.objects.Player" %>
<%@ page import="marauroa.game.Transaction" %>
<%@ page import="marauroa.game.PlayerDatabaseFactory" %>
<%@ page import="marauroa.game.PlayerDatabase" %>
<%@ page import="marauroa.game.RPObject" %>
<%@ page import="marauroa.Configuration" %>

<%!
  static PlayerDatabase playerDatabase;
  static
  {
    try
    {
      Configuration.setConfigurationFile("/opt/tomcat/webapps/test/WEB-INF/conf/marauroa.ini");
      playerDatabase = PlayerDatabaseFactory.getDatabase("JDBCPlayerDatabase");
    }
    catch(Exception e)
    {
      playerDatabase = null;
    }
  }
%>
<html>
  <body>
  <%
  String mode       = request.getParameter("mode");
  String real_name  = request.getParameter("realname");
  String email      = request.getParameter("email");
  String user_name  = request.getParameter("username");
  String password   = request.getParameter("password");
  String char_model = request.getParameter("charmodel");
  String glad_name  = request.getParameter("gladname");
  String glad_model = request.getParameter("gladmodel");
  
  if(mode!=null && mode.equalsIgnoreCase("ca"))
  {
    if(real_name==null || user_name.length()==0){%>
      Invalid real name <%= real_name %>.
      <%return;}
    
    if(email==null || email.length()==0 || !email.matches(".*@.*\\..*")){%>
      Invalid email <%= email %>.
      <%return;}
    
    if(user_name==null || user_name.length()==0 || !playerDatabase.validString(user_name)){%>
      Invalid user name <%= user_name %>.
      <%return;}
    
    if(password==null || password.length()==0 || !playerDatabase.validString(password)){%>
      Invalid password.
      <%return;}
    
    if(char_model==null || char_model.length()==0 || !playerDatabase.validString(char_model)){%>
      Invalid character model: <%= char_model %>.
      <%return;}
    
    if(glad_name==null || glad_name.length()==0 || !playerDatabase.validString(glad_name)){%>
      Invalid gladiator name <%= glad_name %>.
      <%return;}
    
    if(glad_model==null || glad_model.length()==0 || !playerDatabase.validString(glad_model)){%>
      Invalid gladiator model: <%= glad_model %>.
      <%return;}
    try
    {
      Transaction transaction = playerDatabase.getTransaction();
      if(playerDatabase.hasPlayer(transaction,user_name)){%>
      Player <%= user_name %> already exists.
      <%return;}
      
      //at this point we have all data
      playerDatabase.addPlayer(transaction,user_name,password,email);
      RPObject.ID player_id = playerDatabase.getValidRPObjectID(transaction);
      RPObject player=new Player(player_id,user_name);
      player.put("look",char_model);
      RPObject.ID glad_id = playerDatabase.getValidRPObjectID(transaction);
      Gladiator gladiator=new Gladiator(glad_id);
      gladiator.put("name",glad_name);
      gladiator.put("look",glad_model);
      player.getSlot("!gladiators").add(gladiator);
      playerDatabase.addCharacter(transaction, user_name,user_name,player);
      transaction.commit();
      if (playerDatabase.hasPlayer(transaction,user_name)){%>
       Account created. All is ok.<br>
       <% if(playerDatabase.verifyAccount(transaction,user_name,password)){%>       
       Your account is created and active.<br>
       Download the client and play.<br>
       <%}else{%>
       Your account is currently not active.<br>
       You will receive an email with instructions how to activate your account.<br>
      <%}%>
       <a href="">Back.</a>
      <%}else{%>
        Something went wrong.
      <%
      }      
    }
    catch (Exception e)
    {%>
      <pre>
      Error creating new account: <%= e.getMessage() %>
      </pre>
    <%}
  }
  else
  {
    %>
    <form name="accountForm" method="POST" action="/test/index.jsp">
      <input type="hidden" name="cmd" value="newaccount"/>
      <table width="90%">
        <tr><td width="30%">Real name: </td><td><input type="text" name="realname" size="60" maxlength="60"/></td></tr>
        <tr><td>Email <i>(*)</i>: </td><td><input type="text" name="email" size="60" maxlength="60"/></td></tr>
      </table><p>Username is not modifiable once choosen, so choose wisely.<br>
      <table width="90%">
        <tr><td width="30%">Username: </td><td><input type="text" name="username" size="20" maxlength="20"/></td></tr>
        <tr><td>Password: </td><td><input type="password" size="20" name="password"/></td></tr>
        <tr><td>Character model: </td><td><select name="charmodel" onChange="changeCharacter_picture(this.selectedIndex)"><option value="billgates">billgates</option><option value="bobafett">bobafett</option><option value="faerie">faerie</option><option value="harry">harry</option><option value="gladiator">gladiator</option><option value="pknight">pknight</option><option value="orc">orc</option><option value="yohko">yohko</option></select></td></tr>
        <tr><td>Gladiator name: </td><td><input type="text" name="gladname" size="20" maxlength="20"/></td></tr>
        <tr><td>Gladiator model: </td><td><select name="gladmodel" onChange="changeGladiator_picture(this.selectedIndex)"><option value="billgates">billgates</option><option value="bobafett">bobafett</option><option value="faerie">faerie</option><option value="harry">harry</option><option value="gladiator">gladiator</option><option value="pknight">pknight</option><option value="orc">orc</option><option value="yohko">yohko</option></select></td></tr>
      </table>
      <p>
      <input type="hidden" name="mode" value="ca">
      <input type="submit" value="Create account"/>
     </form>
    <%
  }
  %>
  </body>
</html>
