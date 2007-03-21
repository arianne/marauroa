package marauroa.common.game;

/** This enum represent the possible values returned by the create account process.
 *  Caller should verify that the process ended in OK_ACCOUNT_CREATED.
 * @author miguel
 */
public enum Result {
	/** Account was created correctly. */
	OK_CREATED,
	/** Account was not created because one of the important parameters was missing. */
	FAILED_EMPTY_STRING,
	/** Account was not created because an invalid characters( letter, sign, number ) was used */
	FAILED_INVALID_CHARACTER_USED,
	/** Account was not created because any of the parameters are either too long or too short. */
	FAILED_STRING_SIZE,
	/** Account was not created because because this account already exists. */
	FAILED_PLAYER_EXISTS,
	/** Account was not created because there was an unspecified exception. */
	FAILED_EXCEPTION,
	/** The template passed to the create character method is not valid because
	 * it fails to pass the RP rules.
	 */
	FAILED_INVALID_TEMPLATE
}

