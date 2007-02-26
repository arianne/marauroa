package marauroa.client;

public class CreateAccountFailedException extends Exception {
	private static final long serialVersionUID = -6977739824675973192L;

	public CreateAccountFailedException(String reason) {
		super("Create account failed: "+reason);
	}
}
