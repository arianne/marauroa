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

