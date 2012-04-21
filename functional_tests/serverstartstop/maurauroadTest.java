package serverstartstop;

import helper.ResetMarauroaSingleton;

import java.io.File;

import marauroa.server.marauroad;

import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
public class maurauroadTest {

	@Test
	public void testname() throws Exception {
		String filename = "./functional_tests/serverstartstop/testserver.ini";
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
