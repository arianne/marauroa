package marauroa.ping.client;

import java.util.Observable;
import java.util.Observer;

public class SpeakerView implements Observer {
	private Speaker model;
	
	public SpeakerView(Speaker speaker) {
	  model=speaker;		
	  model.addObserver(this);
	}

	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

}
