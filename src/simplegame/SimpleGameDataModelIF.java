/**
 * SimpleGameDataModelIF.java
 *
 * @author Waldemar Tribus
 */

package simplegame;

public interface SimpleGameDataModelIF
{
  public void addGameUpdateListener(GameUpdateListener ul);
  public void setRPCharacterAt(int row, int column, int characterID);
  public int getSize();
  public int getRPCharacterAt(int row, int column);
  public int getWinner();
  public static interface GameUpdateListener
  {
    public void updateReceived(int row, int column, int characterID);
  }
}

