package com.portapayments.server;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CreateMobileCodeServlet extends HttpServlet {

	/**
	 * Generated serial number
	 */
	private static final long serialVersionUID = 9138072095215974828L;

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) 
		throws IOException, ServletException {
		String recipient = request.getParameter("r0");
		String amount = request.getParameter("a0");
		String currency = request.getParameter("c");
		
		String page = "/CreateCodeMobile.jsp";
		if( recipient == null	|| recipient.length() == 0
		||	amount == null 		|| amount.length() == 0
		||	currency == null	|| currency.length() == 0 ) {
			page = "/CreateCodeMobile-error.jsp";
		}
		
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(page);
		dispatcher.forward(request, response);
	}
}
