package marauroa.common.game;

public class SlotAlreadyAddedException extends Exception
  {
  public SlotAlreadyAddedException(String slot)
    {
    super("Slot ["+slot+"] already added.");
    }
  }
	