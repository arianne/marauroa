package marauroa.common.game;

public class NoSlotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1195512407123041835L;

	public NoSlotFoundException(String slot) {
		super("Slot [" + slot + "] not found");
	}
}
