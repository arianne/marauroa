/* $Id: createaccount.java,v 1.18 2004/04/15 20:35:42 arianne_rpg Exp $ */
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
package marauroa;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import marauroa.game.*;
import the1001.objects.*;

class createaccount
  {
  public static void main (String[] args)
    {
    System.exit(createaccount(args));
    }

  public static int createaccount(String[] args)
    {
    int i=0;
    PrintWriter out=null;
    String username=null;
    String password=null;
    String email=null;
    String character=null;
    String character_model=null;
    String gladiator=null;
    String gladiator_model=null;
        
    while(i!=args.length)
      {
      if(args[i].equals("-u"))
        {
        username=args[i+1];
        }
      else if(args[i].equals("-p"))
        {
        password=args[i+1];
        }
      else if(args[i].equals("-e"))
        {
        email=args[i+1];
        }
      else if(args[i].equals("-c"))
        {
        character=args[i+1];
        }
      else if(args[i].equals("-cm"))
        {
        character_model=args[i+1];
        }
      else if(args[i].equals("-g"))
        {
        gladiator=args[i+1];
        }
      else if(args[i].equals("-gm"))
        {
        gladiator_model=args[i+1];
        }
      else if(args[i].equals("-h"))
        {
        // TODO: Write help
        }
      ++i;
      }
    if(username==null) return (1);
    if(password==null) return (1);
    if(email==null) return (1);
    if(character==null) return (1);
    if(character_model==null) return (1);
    if(gladiator==null) return (1);
    if(gladiator_model==null) return (1);
    
    Transaction trans=null;
      
    try
      {      
      out=new PrintWriter(new FileOutputStream("C:/Apache Group/Apache2/logs/createaccount_log.txt",true));
      out.println(new Date().toString()+": Trying to create username("+username+"), password("+password+"), character("+character+"),"
        +"character_model("+character_model+"), gladiator("+gladiator+"), gladiator_model("+gladiator_model+")");
      out.flush();
      
      JDBCPlayerDatabase playerDatabase=(JDBCPlayerDatabase)PlayerDatabaseFactory.getDatabase("JDBCPlayerDatabase");

      trans=playerDatabase.getTransaction();
      out.println("Checking for valid string");
      out.flush();
      if(playerDatabase.validString(username)==false) 
        {
        out.println("String not valid: "+username);
        return (2);
        }
      if(playerDatabase.validString(password)==false)
        {
        out.println("String not valid: "+password);
        return (2);
        }
      if(playerDatabase.validString(email)==false)
        {
        out.println("String not valid: "+email);
        return (2);
        }
      if(playerDatabase.validString(character)==false)
        {
        out.println("String not valid: "+character);
        return (2);
        }
      if(playerDatabase.validString(character_model)==false)       
        {
        out.println("String not valid: "+character_model);
        return (2);
        }
      if(playerDatabase.validString(gladiator)==false)
        {
        out.println("String not valid: "+gladiator);
        return (2);
        }
      if(playerDatabase.validString(gladiator_model)==false)       
        {
        out.println("String not valid: "+gladiator_model);
        return (2);
        }
      out.println("Checking string size");
      if(username.length()>10 || username.length()<4) 
        {
        out.println("String size not valid: "+username);
        return (3);
        }
      if(password.length()>10 || password.length()<1) 
        {
        out.println("String size not valid: "+password);
        return (3);
        }
      if(email.length()>50 || email.length()<5) 
        {
        out.println("String size not valid: "+password);
        return (3);
        }
      if(character.length()>20 || character.length()<4) 
        {
        out.println("String size not valid: "+character);
        return (3);
        }
      if(character_model.length()>10 || character_model.length()<1) 
        {
        out.println("String size not valid: "+character_model);
        return (3);
        }
      if(gladiator.length()>20 || gladiator.length()<4) 
        {
        out.println("String size not valid: "+gladiator);
        return (3);
        }
      if(gladiator_model.length()>10 || gladiator_model.length()<1) 
        {
        out.println("String size not valid: "+gladiator_model);
        return (3);
        }
      out.println("Checking if player exists");
      if(playerDatabase.hasPlayer(trans, username))
        {
        out.println("ERROR: Player exists");
        return (4);
        }
      out.println("Adding player");
      playerDatabase.addPlayer(trans,username,password,email);

      RPObject object=new Player(playerDatabase.getValidRPObjectID(trans),character);

      object.put("look",character_model);
      
      Gladiator gladiator_obj=new Gladiator(playerDatabase.getValidRPObjectID(trans));

      gladiator_obj.put("name",gladiator);
      gladiator_obj.put("look",gladiator_model);
      object.getSlot("!gladiators").add(gladiator_obj);
      out.println("Adding character");
      playerDatabase.addCharacter(trans, username,character,object);
      out.println("Correctly created");
      trans.commit();
      }
    catch(Exception e)
      {
      out.println("Failed: "+e.getMessage());
      e.printStackTrace(out);
      
      try
        {
        trans.rollback();
        }
      catch(Exception ae)
        {
        out.println("Failed Rollback: "+ae.getMessage());
        }
      return (5);
      }    
    finally
      {
      if(out!=null)
        {
        out.flush();
        out.close();
        }
      }
    return (0);
    }
  }