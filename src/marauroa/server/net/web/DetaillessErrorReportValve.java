package marauroa.server.net.web;

import org.apache.catalina.valves.ErrorReportValve;

public class DetaillessErrorReportValve extends ErrorReportValve {

	public DetaillessErrorReportValve() {
		super();
		setShowReport(false);
		setShowServerInfo(false);
	}
}
