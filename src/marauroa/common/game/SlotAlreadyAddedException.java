package marauroa.common.game;

public class SlotAlreadyAddedException extends RuntimeException
  {
  public SlotAlreadyAddedException(String slot)
    {
    super("Slot ["+slot+"] already added.");
    }
  }
	