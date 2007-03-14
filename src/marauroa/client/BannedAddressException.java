package marauroa.client;

public class BannedAddressException extends Exception {
	private static final long serialVersionUID = -6977739824675973192L;

	public BannedAddressException() {
		super("Your IP Address has been banned.");
	}
}
