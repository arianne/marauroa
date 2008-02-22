package marauroa.test.ping;

import java.util.Observable;

public class PingModel extends Observable {
	String[] lines=new String[]{ 
			"Hello world?",
			"Hi there.",
			"Nice to meet you",
			"How are you?",
			"Fine",
			"See you later",
			"Bye"
		};
	
	private int current;

	public void initConversation() {
		current=0;
	}
	
	public void next() {
		current++;
	}
	
	public boolean hasNext() {
		return current<lines.length;
	}
	
	public String getLine() {		
		return lines[current];
	}
}
