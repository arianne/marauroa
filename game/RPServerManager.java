package marauroa.game;

import java.util.*;
import java.io.*;

import marauroa.net.*;
import marauroa.marauroad;


public class RPServerManager extends Thread
  {
  private boolean keepRunning;
  
  public RPServerManager()
    {
    super("RPServerManager");
    
    keepRunning=true;    
    }

  public void finish()
    {
    keepRunning=false;
    }
    
  public void run()
    {
    marauroa.marauroad.report("Start thread "+this.getName());
    while(keepRunning)
      {
      }

    marauroa.marauroad.report("End thread "+this.getName());
    }
  }