/**
 * SimpleGameDataModelIF.java
 *
 * @author Waldemar Tribus
 */

package simplegame;

public interface SimpleGameDataModelIF
{
  public void addGameUpdateListener(GameUpdateListener ul);
  public void setColorAt(int row, int column,byte color);
  public int getColumnsCount();
  public int getRowsCount();
  public byte getColorAt(int row, int column);
  public byte checkWinCondition();
  public static interface GameUpdateListener
  {
    public void updateReceived(int row, int column, byte color);
  }
}

