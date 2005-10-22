package marauroa.common.game;

public class SlotIsFullException extends RuntimeException
  {
  public SlotIsFullException(String slot)
    {
    super("Slot ["+slot+"] is already full.");
    }
  }
  