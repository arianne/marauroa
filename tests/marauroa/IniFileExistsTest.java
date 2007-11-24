package marauroa;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class IniFileExistsTest {

	@Test
	public void checkIniExists() {
		File ini = new File("server.ini");
		assertTrue("There must be a file named server.ini in the main folder of Marauroa.",ini.exists());
	}

}
