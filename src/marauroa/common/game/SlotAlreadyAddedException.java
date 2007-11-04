package marauroa.common.game;

/**
 * Thrown in case the object already has a slot with this name
 */
public class SlotAlreadyAddedException extends RuntimeException {

	private static final long serialVersionUID = 5774414964919365640L;

	/**
	 * creates a new SlotAlreadyAddedException
	 *
	 * @param slot name of preexisting slot
	 */
	public SlotAlreadyAddedException(String slot) {
		super("Slot [" + slot + "] already added.");
	}
}
