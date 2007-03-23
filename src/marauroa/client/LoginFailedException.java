package marauroa.client;

public class LoginFailedException extends Exception {

	private static final long serialVersionUID = -6977739824675973192L;

	public LoginFailedException(String reason) {
		super("Login failed: " + reason);
	}
}
