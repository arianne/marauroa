package marauroa.common.game;

public class SlotIsFullException extends RuntimeException
  {
  private static final long serialVersionUID = -944453052092790508L;

  public SlotIsFullException(String slot)
    {
    super("Slot ["+slot+"] is already full.");
    }
  }
  