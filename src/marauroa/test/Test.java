package marauroa.test;

public class Test {

	public static void assertEquals(Object expected, Object val) {
		if(!expected.equals(val)) {
			throw new FailedException("expected "+expected+" but got "+val);
		}
	}

	public static void fail() {
		throw new FailedException("Forced fail.");
	}

}
