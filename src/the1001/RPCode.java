/* $Id: RPCode.java,v 1.2 2003/12/12 18:57:56 arianne_rpg Exp $ */
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
package the1001;

import marauroa.game.*;
import marauroa.*;

public class RPCode
  {
  private static the1001RPRuleProcessor ruleProcessor;
  
  public static void setCallback(the1001RPRuleProcessor rpu)
    {
    ruleProcessor=rpu;
    }
  
  /** The buy action means that object represented by id wants to buy the item
   *  of type item_type ( gladiator or item ) represented by item_id */
  public static RPAction.Status Buy(RPObject.ID id, String item_type, String item_id)
    {
    marauroad.trace("RPCode::Buy",">");
    
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();
     
      RPObject shop=zone.getHeroesHouse();
     
      if(item_type=="gladiators" || item_type=="items")
        {
        }
      else
        {
        }
    
      return null;
      }
    finally
      {
      marauroad.trace("RPCode::Buy","<");
      }
    }
  }