package marauroa.game;

public class NoSlotFoundException extends Exception
  {
  public NoSlotFoundException(String slot)
    {
    super("Slot ["+slot+"] not found");
    }
  }
	