/*
 * Utility.java
 *
 * Created on 11. September 2005, 10:16
 *
 */

package marauroa.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Some generic utility methods.
 *
 * @author Matthias Totz
 */
public class Utility
{
  /** no instance allowed */
  private Utility()
  {}
  
  /**
   * adds some leading '0' to the sting until the length <i>maxDigits</i> is
   * reached
   */
  public static String addLeadingZeros(String number, int maxDigits)
  {
    while (number.length() < maxDigits)
    {
      number = "0"+number;
    }
    return number;
  }
  
  /** creates a nice hex-dump of the byte array */
  public static String dumpByteArray(byte[] byteArray)
  {
    return dumpInputStream(new ByteArrayInputStream(byteArray));
  }

  /** creates a nice hex-dump of the byte array */
  public static String dumpInputStream(InputStream byteStream)
  {
    StringBuilder result = new StringBuilder();
    try
    {
      int index = 0;
      StringBuilder chars = new StringBuilder();

      int theByte = byteStream.read();
      result.append(addLeadingZeros(Integer.toHexString(index),8)).append(' ');
      index++;

      while (theByte != -1)
      {
        result.append(addLeadingZeros(Integer.toHexString(theByte),2)).append(' ');

        // show chars < 32 and > 127 as '.'
        if (theByte > 31 && theByte < 128)
        {
          chars.append((char) (theByte));
        }
        else
        {
          chars.append('.');
        }

        
        if (index > 0 && (index % 16 == 0))
        {
          result.append(chars)
                 .append('\n')
                 .append(addLeadingZeros(Integer.toHexString(index),8)).append(' ');

          chars = new StringBuilder();
        }
        index++;
        theByte = byteStream.read();
      }
      return result.toString();
    }
    catch (Exception e)
    {
      return result.toString()+"\nException: "+e.getMessage();
    }
  }
}
