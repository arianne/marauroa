/* $Id: createaccount.java,v 1.1 2004/01/30 18:59:22 arianne_rpg Exp $ */
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
    int i=0;
    PrintWriter out=null;
    
    String username=null;
    String password=null;
    String character=null;
    String character_model=null;
    String gladiator=null;
    String gladiator_model=null;
        
    while(i!=args.length)
      {
      System.out.println(args[i]);
      if(args[i].equals("-u"))
        {
        username=args[i+1];
        }
      else if(args[i].equals("-p"))
        {
        password=args[i+1];
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
    
    if(username==null) return;
    if(password==null) return;
    if(character==null) return;
    if(character_model==null) return;
    if(gladiator==null) return;
    if(gladiator_model==null) return;
      
    try
      {      
      out=new PrintWriter(new FileOutputStream("createaccount_log.txt"));
      out.println("Trying to create username("+username+"), password("+password+"), character("+character+"),"
        +"character_model("+character_model+"), gladiator("+gladiator+"), gladiator_model("+gladiator_model+")");
      
      PlayerDatabase playerDatabase=PlayerDatabaseFactory.getDatabase("JDBCPlayerDatabase");
      
      if(!playerDatabase.validString(username)) return;
      if(!playerDatabase.validString(password)) return;
      if(!playerDatabase.validString(character)) return;
      if(!playerDatabase.validString(character_model)) return;       
      if(!playerDatabase.validString(gladiator)) return;
      if(!playerDatabase.validString(gladiator_model)) return;       

      if(username.length()<10) return;
      if(password.length()<10) return;
      if(character.length()<10) return;
      if(character_model.length()<10) return;
      if(gladiator.length()<10) return;
      if(gladiator_model.length()<10) return;
        
      if(playerDatabase.hasPlayer(username))
        {
        return;
        }

      playerDatabase.addPlayer(username,password);

      RPObject object=new RPObject();
      object.put("object_id","-1");
      object.put("type","character");
      object.put("name",character);
      object.put("look",character_model);
      
      object.addSlot(new RPSlot("gladiators"));
      Gladiator gladiator_obj=new Gladiator(new RPObject.ID(-1));
      gladiator_obj.put("name",gladiator);
      gladiator_obj.put("look",gladiator_model);
      
      object.getSlot("gladiators").add(gladiator_obj);
      
      playerDatabase.addCharacter(username,character,object);
      
      out.println("Correctly created");
      }
    catch(Exception e)
      {
      out.println("Failed: "+e.getMessage());
      }    
    finally
      {
      if(out!=null)
        {
        out.close();
        }
      }
    }
  }