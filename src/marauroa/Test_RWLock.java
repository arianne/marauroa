/* $Id: Test_RWLock.java,v 1.4 2004/03/24 15:25:32 arianne_rpg Exp $ */
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

import junit.framework.*;

public class Test_RWLock extends TestCase
  {
  private RWLock lock;
  private class Data extends Thread
    {
    private boolean keepRunning;
    public int data_1;
    public int data_2;
    public int data_3;
    public int data_4;
    public Data()
      {
      data_1=data_2=data_3=data_4=0;
      keepRunning=true;
      start();
      }
    
    public void finish()
      {
      keepRunning=false;
      }
    
    public void run()
      {
      while(keepRunning) 
        {
        lock.requestReadLock();
        assertTrue(data_1==data_2 && data_2==data_3 && data_3==data_4);
        lock.releaseLock();
        }        
      }
    }
  public static Test suite ( ) 
    {
    return new TestSuite(Test_RWLock.class);
    }
	
  public void testLock()
    {
    marauroad.trace("Test_RWLock::testLock","?","This test case try to see if the RW "+
      "lock is working correctly by changing 5 values on the Data class while the lock "+
      "is owned, and then the lock is released and Data class will adquiere it in read "+ 
      "mode and verify that the 5 numbers are the same");      
    marauroad.trace("Test_RWLock::testLock",">");
    lock=new RWLock();

    Data data=new Data();
    int i=1000;
    
    while(i!=0)
      {
      --i;
      lock.requestWriteLock();
      ++data.data_1;
      ++data.data_2;
      ++data.data_3;
      ++data.data_4;
      lock.releaseLock();
      }
    data.finish();
    assertTrue(data.data_1==1000);
    marauroad.trace("Test_RWLock::testLock","<");
    }
  }
