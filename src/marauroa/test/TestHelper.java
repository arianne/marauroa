package marauroa.test;

public class TestHelper {

	public static void assertEquals(Object expected, Object val) {
		if (!expected.equals(val)) {
			throw new FailedException("expected " + expected + " but got " + val);
		}
	}

	public static void fail() {
		throw new FailedException("Forced fail.");
	}

	public static void assertNotNull(Object result) {
		if (result == null) {
			throw new FailedException("Unexpected null value");
		}
	}

}
