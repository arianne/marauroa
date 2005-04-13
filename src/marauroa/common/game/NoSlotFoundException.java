package marauroa.common.game;

public class NoSlotFoundException extends RuntimeException
  {
  public NoSlotFoundException(String slot)
    {
    super("Slot ["+slot+"] not found");
    }
  }
	