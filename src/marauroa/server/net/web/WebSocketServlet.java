/***************************************************************************
 *                   (C) Copyright 2010-2023 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.net.web;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A websocket dispatcher servlet
 *
 * @author hendrik
 */
public class WebSocketServlet extends HttpServlet {
	private static final long serialVersionUID = -1812859392719209598L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/wsinternal");
		WebSocketRequestWrapper requestWrapper = new WebSocketRequestWrapper(request);
		dispatcher.forward(requestWrapper, response);
	}

}
