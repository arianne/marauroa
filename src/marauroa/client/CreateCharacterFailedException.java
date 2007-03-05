package marauroa.client;

public class CreateCharacterFailedException extends Exception {

	public CreateCharacterFailedException(String reason) {
		super("Create character failed: "+reason);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
