/* $Id: RPAction.java,v 1.9 2003/12/08 01:12:19 arianne_rpg Exp $ */
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
package marauroa.game;

/** This class represent an action. Please refer to Actions Explained document */
public class RPAction extends Attributes
  {
  public final static Status STATUS_SUCCESS=new Status(Status.SUCCESS);
  public final static Status STATUS_FAIL=new Status(Status.FAIL);
  public final static Status STATUS_INCOMPLETE=new Status(Status.INCOMPLETE);
  
  protected int actionType;
  
  /** This class represent the status of the action */
  public static class Status
    {
    public final static byte SUCCESS=0;
    public final static byte FAIL=1;
    public final static byte INCOMPLETE=2;
    private byte val;
    
    /** Constructor
     *  @param val the status of the action */
    public Status(byte val)
      {
      this.val=val;
      }
    
    /** Constructor
     *  @param val the status of the action */
    public Status(String val)
      {
      if(val.equalsIgnoreCase("success")) this.val=SUCCESS;
      if(val.equalsIgnoreCase("fail")) this.val=FAIL;
      if(val.equalsIgnoreCase("incomplete")) this.val=INCOMPLETE;
      }
    
    /** This method returns the status of the action
     *  @return the status of the action */
    public byte getStatus()
      {
      return val;
      }
    
    /** This method returns true of both object are equal.
     *  @param status another Status object
     *  @return true if they are equal, or false otherwise. */
    public boolean equals(Object status)
      {
      return val==((Status)status).val;
      }
    
    /** This method returns a String that represent the object
     *  @return a string representing the object.*/
    public String toString()
      {
      if(val==0) return "success";
      if(val==1) return "fail";
      if(val==2) return "incomplete";
      
      return "-incoherent status-";
      }
    }
  
  /** Constructor */
  public RPAction()
    {
    actionType=0;
    }
  
  public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
    {
    super.writeObject(out);
    }
  
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    }
  }
