package marauroa;
import junit.framework.*;

/**
 * TestSuite that runs all the sample tests
 */
public class RunTests
  {
  public static void main (String[] args) 
    {
 	//junit.swingui.TestRunner.run(RunTests.class);
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
	
	suite.addTest(new TestSuite(marauroa.net.Test_Messages.class));
	suite.addTest(new TestSuite(marauroa.net.Test_MessageFactory.class));
	
	suite.addTest(new TestSuite(marauroa.net.Test_NetworkServerManager.class));
	
	suite.addTest(new TestSuite(marauroa.game.Test_PlayerDatabase.class));
	suite.addTest(new TestSuite(marauroa.game.Test_PlayerEntryContainer.class));

	return suite;
	}
  }