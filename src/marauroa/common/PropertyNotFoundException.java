package marauroa.common;

public class PropertyNotFoundException extends RuntimeException {
	private static final long serialVersionUID = -8979389593134638942L;

	public PropertyNotFoundException(String property) {
		super("Property [" + property + "] not found");
	}
}
