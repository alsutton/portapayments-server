package com.portapayments.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.portapayments.server.datamodel.PaymentRequest;
import com.portapayments.server.datamodel.Recipient;
import com.portapayments.server.utils.BitlyUtils;
import com.portapayments.server.utils.EMF;

public class CreateUniversalCodeServlet extends HttpServlet {

	/**
	 * The URL for a blank image
	 */
	
	private static final String BLANK_IMAGE_URL = "http://static.portapayments.com/images/blankcode.png";
	
	/**
	 * Character array for quick number to character conversion.
	 */
	
	private static final char[] NUMBER_CHARS = { '0', '1', '2', '3', '4', '5' };
	
	/**
	 * Generated serial number
	 */
	private static final long serialVersionUID = 9138072095215974828L;

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) 
		throws IOException, ServletException {
		PaymentRequest paymentRequest = new PaymentRequest();
		
		String currency = request.getParameter("c");
		if( currency == null ) {
			log("Currency not found");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		paymentRequest.setCurrency(currency);
		try {
			String note = request.getParameter("n");
			if( note != null && note.length() > 0) {
				StringBuilder noteBuilder = new StringBuilder(note.length());
				for(int i =0 ; i < note.length() ; i++) {
					char c = note.charAt(i);
					if(Character.isWhitespace(c)) {
						noteBuilder.append(' ');
					} else {
						noteBuilder.append(c);
					}
 				}
				paymentRequest.setMemo(note);
			} else {
				paymentRequest.setMemo("Paid via PortaPayments");
			}

			boolean foundData = false;
			
			final EntityManager em = EMF.get().createEntityManager();
			em.persist(paymentRequest);
			em.flush();
			
			StringBuilder amountParam = new StringBuilder(2);
			amountParam.append("a0");
			StringBuilder recipientParam = new StringBuilder(2);
			recipientParam.append("r0");
			for(int i = 0  ; i < 6 ; i++ ) {
				amountParam.setCharAt(1, NUMBER_CHARS[i]);
				String amount = request.getParameter(amountParam.toString());
				if(amount == null || amount.length() == 0) {
					continue;
				}	
				
				recipientParam.setCharAt(1, NUMBER_CHARS[i]);
				String recipient = request.getParameter(recipientParam.toString());
				if(recipient == null || recipient.length() == 0) {
					continue;
				}	

				Recipient paymentRecipient = new Recipient();
				paymentRecipient.setAmount(amount);
				paymentRecipient.setEmailAddress(recipient);
				paymentRecipient.setPaymentRequest(paymentRequest);
				em.persist(recipient);
				foundData = true;
			}
			
			if( !foundData ) {
				log("No data found");
				response.sendRedirect(BLANK_IMAGE_URL);
				return;
			}

			em.flush();
			final String url = BitlyUtils.getShortenedURL(paymentRequest);
			final String encodedData = URLEncoder.encode(url, "UTF-8");
			final StringBuilder googleChartsRedirect = new StringBuilder(256);
			googleChartsRedirect.append("http://chart.apis.google.com/chart?cht=qr&chs=192x192&choe=UTF-8&chl=");
			googleChartsRedirect.append(encodedData);
			response.sendRedirect(googleChartsRedirect.toString());
		} catch(UnsupportedEncodingException e) {
			log("Error during code generation", e);
			throw new ServletException( e );
		}
	}
}
