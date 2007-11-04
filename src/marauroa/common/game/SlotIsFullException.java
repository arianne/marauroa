package marauroa.common.game;

/**
 * thrown in case the slot is full
 */
public class SlotIsFullException extends RuntimeException {

	private static final long serialVersionUID = -944453052092790508L;

	/**
	 * creates a new SlotIsFullException
	 *
	 * @param slot name of slot
	 */
	public SlotIsFullException(String slot) {
		super("Slot [" + slot + "] is already full.");
	}
}
