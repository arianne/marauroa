package marauroa.common.game;

/**
 * This enum represent the possible values returned by the create account
 * process. Caller should verify that the process ended in OK_ACCOUNT_CREATED.
 * 
 * @author miguel
 */
public enum Result {
	/** Account was created correctly. */
	OK_CREATED(true, "Account was created correctly."),
	/**
	 * Account was not created because one of the important parameters was
	 * missing.
	 */
	FAILED_EMPTY_STRING(false, "Account was not created because one of the important parameters was missing."),
	/**
	 * Account was not created because an invalid characters( letter, sign,
	 * number ) was used
	 */
	FAILED_INVALID_CHARACTER_USED (false, "Account was not created because an invalid characters( letter, sign, number ) was used"),
	/**
	 * Account was not created because any of the parameters are either too long
	 * or too short.
	 */
	FAILED_STRING_SIZE(false, "Account was not created because any of the parameters are either too long or too short"),
	/** Account was not created because because this account already exists. */
	FAILED_PLAYER_EXISTS(false, "Account was not created because because this account already exists"),
	/** Account was not created because there was an unspecified exception. */
	FAILED_EXCEPTION(false, "Account was not created because there was an unspecified exception."),
	/**
	 * The template passed to the create character method is not valid because
	 * it fails to pass the RP rules.
	 */
	FAILED_INVALID_TEMPLATE(false, "The template passed to the create character method is not valid because it fails to pass the RP rules.");
	
	/**
	 * Textual description of the result 
	 */
	private String text;
	/**
	 * True if the account is successfully created. 
	 */
	private boolean created;

	Result(boolean created, String text) {
		this.text=text;
	}

	/**
	 *  Returns true if the account is successfuly created.
	 * @return true if the account is successfuly created.
	 */
	public boolean failed() {
		return !created;
	}

	/**
	 * Returns a textual description of the result.
	 * @return a textual description of the result.
	 */
	public String getText() {
		return text;
	}
}
