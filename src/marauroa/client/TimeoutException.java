package marauroa.client;

public class TimeoutException extends Exception {
	private static final long serialVersionUID = -6977739824675973192L;

	public TimeoutException() {
		super("Timeout happened while waiting server reply");
	}
}
