package marauroa.server.game;

import marauroa.common.game.RPObject;

/** This enum represent the possible values returned by the create account process.
 *  Caller should verify that the process ended in OK_ACCOUNT_CREATED.
 * @author miguel
 */
public class AccountResult {
	public static enum Result {
		/** Account was created correctly. */
		OK_ACCOUNT_CREATED,
		/** Account was not created because one of the important parameters was missing. */
		FAILED_EMPTY_STRING,
		/** Account was not created because an invalid characters( letter, sign, number ) was used */
		FAILED_INVALID_CHARACTER_USED,
		/** Account was not created because any of the parameters are either too long or too short. */
		FAILED_STRING_SIZE,
		/** Account was not created because because this account already exists. */
		FAILED_PLAYER_EXISTS,
		/** Account was not created because there was an unspecified exception. */
		FAILED_EXCEPTION
	}

	private Result result;
	private String username;
	private RPObject template;

	public AccountResult(Result result, String username, RPObject template) {
		this.result=result;
		this.username=username;
		this.template=template;
	}

	public Result getResult() {
		return result;
	}

	public String getUsername() {
		return username;
	}

	public RPObject getTemplate() {
		return template;
	}
}

