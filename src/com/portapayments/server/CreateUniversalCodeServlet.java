package com.portapayments.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.portapayments.server.datamodel.PaymentRecipient;
import com.portapayments.server.datamodel.PaymentRequest;
import com.portapayments.server.utils.BitlyUtils;
import com.portapayments.server.utils.EMF;

public class CreateUniversalCodeServlet extends HttpServlet {

	/**
	 * The PayPal Gateway
	 */
	
	private String PAYPAL_GATEWAY = "PayPal";
	
	/**
	 * The URL for a blank image
	 */
	
	private static final String BLANK_IMAGE_URL = "http://static.portapayments.com/images/blankcode.png";
	
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

			String recipient = request.getParameter("r0");
			if( recipient == null || recipient.length() ==0 ) {
				log("No data found");
				response.sendRedirect(BLANK_IMAGE_URL);
				return;
			}
			
			PaymentRecipient paymentRecipient = new PaymentRecipient();
			paymentRecipient.setRecipient(recipient);

			String amount = request.getParameter("a0");
			if(amount != null && amount.length() > 0) {
				long amountValue = calculateAmount(amount);
				paymentRecipient.setAmount(amountValue);
				paymentRequest.setAmount(amountValue);
			}	

			List<PaymentRecipient> recipients = new ArrayList<PaymentRecipient>();
			recipients.add(paymentRecipient);

			paymentRequest.setRecipients(recipients);
			paymentRequest.setGateway(PAYPAL_GATEWAY);
				
			final EntityManager em = EMF.get().createEntityManager();
			try {				
				em.persist(paymentRequest);
			} finally {
				em.close();
			}
	
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

	/**
	 * Converts a string amount into a 
	 * @param amount
	 * @return
	 */
	private Long calculateAmount(String amount) {
		int pointIdx = amount.indexOf('.');
		if(pointIdx == -1) {
			long value = Long.parseLong(amount);
			return value * 100;
		}
		
		String major = amount.substring(0, pointIdx);
		String minor = amount.substring(pointIdx+1);
		if( minor.length() > 2) {
			throw new RuntimeException("2 digit minor values only");
		}
		
		long value = Long.parseLong(major) * 100;
		value += Long.parseLong(minor);
		return value;
	}
}
