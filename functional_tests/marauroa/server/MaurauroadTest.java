package marauroa.server;

import static org.junit.Assert.assertTrue;

import java.io.File;

import marauroa.helper.ResetMarauroaSingleton;

import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class MaurauroadTest {

	@Test
	public void testname() throws Exception {
		String filename = "./functional_tests/marauroa/server/testserver.ini";
		assertTrue(new File(filename).exists());
		String[] args = new String[] { "-c", filename };

		marauroad.main(args);
		// let marauroa start up before we kill it
		Thread.sleep(1000);
		marauroad.getMarauroa().finish();
	}

	@AfterClass
	public static void afterclass() throws IllegalArgumentException, SecurityException,
			IllegalAccessException, NoSuchFieldException {
		ResetMarauroaSingleton.sysoutthreads();
	}

}
