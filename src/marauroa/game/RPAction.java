/* $Id: RPAction.java,v 1.17 2004/07/07 10:07:20 arianne_rpg Exp $ */
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

/** This class represent an Action. Please refer to "Actions Explained" document */
public class RPAction extends Attributes
  {
  public final static Status STATUS_SUCCESS=new Status(Status.SUCCESS);
  public final static Status STATUS_FAIL=new Status(Status.FAIL);
  public final static Status STATUS_INCOMPLETE=new Status(Status.INCOMPLETE);
  
  public static Status Success()
    {
    return STATUS_SUCCESS;
    }
    
  public static Status Fail(String message)
    {
    return new Status(Status.FAIL,message);
    }
    
  /** This class represent the status of the action */
  public static class Status
    {
    public final static byte SUCCESS=0;
    public final static byte FAIL=1;
    public final static byte INCOMPLETE=2;
    private byte val;
    private String reason;
    
    /** Constructor
     *  @param val the status of the action */
    public Status(byte val)
      {
      this.val=val;
      }
    
    /** Constructor
     *  @param val the status of the action 
     *  @param reason explaining the status of the action. */
    public Status(byte val, String reason)
      {
      this.val=val;
      this.reason=reason;
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
      
    public int hashCode()
      {
      return val;
      }
    
    /** This method returns a String that represents the status value of the object
     *  @return a string representing the status value ("success", "fail: " + reason + incomplete") */
    public String toString()
      {
      if(val==0) return "success";
      if(val==1) return "fail: "+reason;
      if(val==2) return "incomplete";
      // TODO: throw exception instead 
      return "-incoherent status-";
      }
    }
    
  /** Constructor */
  public RPAction()
    {
    super(RPClass.getBaseRPActionDefault());
    }  
  }
