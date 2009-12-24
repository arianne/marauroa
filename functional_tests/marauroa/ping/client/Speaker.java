package marauroa.ping.client;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import marauroa.common.game.RPObject;

/**
 * Model of a Speaker.
 * It has two states: Idle or speaking.
 * It keeps a count of the amount of lines spoken so far and what it has
 * said.
 * 
 * @author miguel
 *
 */
public class Speaker extends Observable {
	/**
	 * Idle or Speaking
	 */
	String state;
	
	/**
	 * Amount of sentences.
	 */
	int linesSpoken;
	
	/**
	 * The sentences.
	 */
	List<String> lines;
	
	
	public Speaker() {
	  super();
	  
	  lines=new LinkedList<String>();
	}

	public void changeState(String newState) {
		state=newState;
		notifyObservers();
	}
	
	public void hasSpoken(String text) {
		lines.add(text);
		linesSpoken++;
	}
	
	public void onAddedChanges(RPObject changes) {
		// do nothing, but method is required by interface
	}

	public void onDeletedChanges(RPObject changes) {
		// do nothing, but method is required by interface
	}
}
