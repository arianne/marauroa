package marauroa.common.game;

public class RPObjectNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4649823545471552977L;

	public RPObjectNotFoundException(RPObject.ID id) {
		super("RP Object [" + id + "] not found");
	}
}
