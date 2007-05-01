package marauroa.server.game.container.test;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the account validation
 *
 * @author hendrik
 */
public class TestCharacternameValidation {
	private static Logger logger = Logger.getLogger(TestCharacternameValidation.class);

	public boolean checkImpersonation(String username) {
		String name = username;
		name = name.replaceAll("[ _.,;.\\-\\\\ \"§$%&/()='<>|*+~#]", " ");
		if (name.startsWith(" ") || name.endsWith(" ") || (name.indexOf("gm ") > -1) || (name.indexOf(" gm") > -1)
		        || name.startsWith("gm") || name.endsWith("gm") || (name.indexOf("  ") > -1)) {
			logger.warn("Possible impersonation: " + username);
			return false;
		}
		
		return true;
	}
	
	@Test
	public void checkImpersonation() {
		Assert.assertTrue(checkImpersonation("hendrik"));
		Assert.assertTrue(checkImpersonation("hendrik1"));
		Assert.assertTrue(checkImpersonation("Hendrik"));
		Assert.assertTrue(checkImpersonation("hEndrik1"));
		Assert.assertFalse(checkImpersonation("hendrik_"));
		Assert.assertFalse(checkImpersonation("_hendrik"));
		Assert.assertFalse(checkImpersonation("hendrikgm"));
	}
}
