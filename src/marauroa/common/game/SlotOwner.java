package marauroa.common.game;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.TimeoutConf;
import marauroa.common.game.Definition.DefinitionClass;

/**
 * interface for RP-classes that own slots
 *
 * @author hendrik
 */
public abstract class SlotOwner extends Attributes {

	/** a list of slots that this object contains */
	protected List<RPSlot> slots;

	public SlotOwner(RPClass rpclass) {
		super(rpclass);
		slots = new LinkedList<RPSlot>();
	}

	

	@Override
	public Object fill(Attributes attr) {
		Object res = super.fill(attr);
		slots.clear();
		if (attr instanceof SlotOwner) {
			SlotOwner slotOwner = (SlotOwner) attr;
			for (RPSlot slot : slotOwner.slots) {
				RPSlot added = (RPSlot) slot.clone();
				added.setOwner(this);
				slots.add(added);
			}
		}
		return res;
	}



	/**
	 * This method returns true if the object has that slot
	 *
	 * @param name
	 *            the name of the slot
	 * @return true if slot exists or false otherwise
	 */
	public boolean hasSlot(String name) {
		for (RPSlot slot : slots) {
			if (slot.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

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
	public abstract void addSlot(RPSlot slot) throws SlotAlreadyAddedException;

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
	public abstract RPSlot getSlot(String name);

	/**
	 * Returns a iterator over the slots
	 *
	 * @return an iterator over the slots
	 */
	public abstract Iterator<RPSlot> slotsIterator();

	/**
	 * Returns an unmodifiable list of the slots
	 *
	 * @return a list of the slots
	 */
	public abstract List<RPSlot> slots();

	abstract void assignSlotID(RPObject object);

	abstract void usedSlotID(int int1);

	abstract void setContainer(SlotOwner owner, RPSlot slot);

	abstract SlotOwner getContainerOwner();

	abstract SlotOwner getContainerBaseOwner();

	protected void serializeRPSlots(marauroa.common.net.OutputSerializer out,
			DetailLevel level) throws IOException {
		/*
		 * We compute the amount of slots to serialize first. We don't serialize
		 * hidden or private slots unless detail level is full.
		 */
		int size = 0;
		for (RPSlot slot : slots) {
			if (shouldSerialize(DefinitionClass.RPSLOT, slot.getName(), level)) {
				size++;
			}
		}

		/*
		 * Now write it.
		 */
		out.write(size);
		for (RPSlot slot : slots) {
			Definition def = getRPClass().getDefinition(DefinitionClass.RPSLOT, slot.getName());

			if (shouldSerialize(def, level)) {
				slot.writeObject(out, level);
			}
		}
	}


	protected void deserializeRPSlots(marauroa.common.net.InputSerializer in)
			throws IOException {
		/*
		 * First we load slots
		 */
		int size = in.readInt();
		if (size > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of " + String.valueOf(size) + " size");
		}

		slots = new LinkedList<RPSlot>();

		for (int i = 0; i < size; ++i) {
			RPSlot slot = new RPSlot();
			slot.setOwner(this);
			slot = (RPSlot) in.readObject(slot);
			slots.add(slot);
		}
	}
}