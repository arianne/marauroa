package marauroa;

import junit.framework.*;

public class Test_Configuration extends TestCase
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_Configuration.class);
	}
	
  public void testConfiguration()
    {
    try
      {    
      Configuration conf=Configuration.getConfiguration();

      String result=conf.get("test_ATestString");
      assertEquals(result,"ATestString");
    
      conf.set("test_ATestString", "AnotherTestString");

      result=conf.get("test_ATestString");
      assertEquals(result,"AnotherTestString");

      conf.set("test_ATestString", "ATestString");
      conf.store();
      }
    catch(Configuration.PropertyFileNotFoundException e)
      {
      fail(e.getMessage());
      }
    catch(Configuration.PropertyNotFoundException e)
      {
      fail(e.getMessage());
      }
    }
  }