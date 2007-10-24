package marauroa.server;

import marauroa.common.Log4J;
import marauroa.common.Logger;

/**
 * logs uncaught exceptions to the logging system before
 * the exception is propagated to the Java VM which will
 * kill the thread. The Java VM only logs to stderr so
 * there would be no information in the logfile without
 * this class.
 */
public class MarauroaUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
	private static Logger logger = Log4J.getLogger(MarauroaUncaughtExceptionHandler.class);
	private Thread.UncaughtExceptionHandler next;

	public void uncaughtException(Thread thread, Throwable exception) {
		logger.error("Exception in thread " + thread.getName(), exception);
		System.err.println("Exception in thread " + thread.getName());
		exception.printStackTrace();
		if (next != null) {
			next.uncaughtException(thread, exception);
		}
	}

	/**
	 * installs this uncaught exception handler
	 */
	public static void setup() {
		MarauroaUncaughtExceptionHandler handler = new MarauroaUncaughtExceptionHandler();
		handler.next = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}
	
	public static void main(String[] args) {
		setup();
		Object o = null;
		o.equals(o);
		System.out.println("x");
	}
}
