import junit.framework.*;

/**
 * TestSuite that runs all the sample tests
 *
 */
public class RunTests
  {
  public static void main (String[] args) 
    {
//	junit.swingui.TestRunner.run(marauroa.net.Test_SerializerByte.class);
	junit.textui.TestRunner.run(suite());
	}
  
  public static Test suite ( ) 
    {
	TestSuite suite= new TestSuite("All marauroa Tests");
	suite.addTest(new TestSuite(marauroa.net.Test_SerializerByte.class));
	suite.addTest(new TestSuite(marauroa.net.Test_SerializerShort.class));
	suite.addTest(new TestSuite(marauroa.net.Test_SerializerInt.class));
	suite.addTest(new TestSuite(marauroa.net.Test_SerializerByteArray.class));	
	suite.addTest(new TestSuite(marauroa.net.Test_SerializerString.class));	

	return suite;
	}
  }