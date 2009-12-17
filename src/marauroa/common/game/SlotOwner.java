package marauroa.common.game;

import java.util.Iterator;
import java.util.List;

/**
 * interface for RP-classes that own slots
 *
 * @author hendrik
 */
public interface SlotOwner {

	/**
	 * This method returns true if the object has that slot
	 *
	 * @param name
	 *            the name of the slot
	 * @return true if slot exists or false otherwise
	 */
	public boolean hasSlot(String name);

	/**
	 * This method add the slot to the object
	 *
	 * @param name
	 *            the RPSlot name to be added
	 * @throws SlotAlreadyAddedException
	 *             if the slot already exists
	 */
	public abstract void addSlot(String name) throws SlotAlreadyAddedException;

	/**
	 * This method add the slot to the object
	 *
	 * @param slot
	 *            the RPSlot to be added
	 * @throws SlotAlreadyAddedException
	 *             if the slot already exists
	 */
	public void addSlot(RPSlot slot) throws SlotAlreadyAddedException;

	/**
	 * This method is used to remove an slot of the object
	 *
	 * @param name
	 *            the name of the slot
	 * @return the removed slot if it is found or null if it is not found.
	 */
	public abstract RPSlot removeSlot(String name);

	/**
	 * This method returns a slot whose name is name
	 *
	 * @param name
	 *            the name of the slot
	 * @return the slot or null if the slot is not found
	 */
	public RPSlot getSlot(String name);

	/**
	 * Returns a iterator over the slots
	 *
	 * @return an iterator over the slots
	 */
	public Iterator<RPSlot> slotsIterator();

	/**
	 * Returns an unmodifiable list of the slots
	 *
	 * @return a list of the slots
	 */
	public List<RPSlot> slots();

	/**
	 * Returns the RPClass of the attributes
	 *
	 * @return the object RPClass
	 */
	public RPClass getRPClass();

}