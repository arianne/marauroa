package marauroa;

/**
 * The launcher of the whole Marauroa Server.
 *
 */
public class marauroad
  {
  private static marauroa.net.NetworkServerManager netMan;
  
  public static void main (String[] args) 
    {
    println("Marauroa server       - An open source MMORPG Server -");
    println("(C) 2003 Miguel Angel Blanch Lardin");
    println();
    println("This program is free software; you can redistribute it and/or modify");
    println("it under the terms of the GNU General Public License as published by");
    println("the Free Software Foundation; either version 2 of the License, or");
    println("(at your option) any later version.");
    println();
    println("This program is distributed in the hope that it will be useful,");
    println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
    println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
    println("GNU General Public License for more details.");
    println();
    println("You should have received a copy of the GNU General Public License");
    println("along with this program; if not, write to the Free Software");
    println("Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA");
    
    init();
	}
	
  private static void init()
    {
    try
      {
      netMan=new marauroa.net.NetworkServerManager();
      }
    catch(java.net.SocketException e)
      {
      report(e.getMessage());
      }
    }
    
  private static void finish()
    {
    netMan.finish();
    }
	
  public static void println(String text)
    {
    System.out.println(text);
    }

  public static void println()
    {
    System.out.println();
    }
  
  public static void report(String text)
    {
    System.out.println(text);
    }
  }