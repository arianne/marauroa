package marauroa.common.game;

public class SlotAlreadyAddedException extends RuntimeException {
	private static final long serialVersionUID = 5774414964919365640L;

	public SlotAlreadyAddedException(String slot) {
		super("Slot [" + slot + "] already added.");
	}
}
