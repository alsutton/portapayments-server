package com.portapayments.server;

import java.io.IOException;
import java.util.Currency;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.portapayments.server.datamodel.PaymentRequest;
import com.portapayments.server.gateways.PayPal;
import com.portapayments.server.gateways.PayPal.PayPalExceptionWithErrorCode;
import com.portapayments.server.utils.EMF;

public class PayUniversalCodeServlet extends HttpServlet {

	/**
	 * Generated serial ID.
	 */
	private static final long serialVersionUID = -3976716985453783742L;
	
	/**
	 * The URL to send users to if a payment isn't valid.
	 */
	private static final String ERROR_URL = "http://portapayments.appspot.com/InvalidPayment.jsp";

	/**
	 * The URL to send users to if a payment isn't valid.
	 */
	private static final String ENTER_AMOUNT_URL = "/EnterAmount.jsp";

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) 
		throws IOException, ServletException {
		final String	k = request.getParameter("k"),
						i = request.getParameter("i"),
						u = request.getParameter("u");
		
		if(k == null || i == null) {
			log("Missing k:"+(k==null)+" / i"+(i==null));
			response.sendRedirect(ERROR_URL);
			return;
		}
		
		String email = "<UNKNOWN>";
		EntityManager em = EMF.get().createEntityManager();
		try {
			Key paymentKey = KeyFactory.createKey(k, Long.parseLong(i));
			PaymentRequest paymentRequest = em.find(PaymentRequest.class, paymentKey);
			if(paymentRequest == null) {
				log("Payment not found for k:"+k+" / i:"+i);
				response.sendRedirect(ERROR_URL);
				return;				
			}
			
			email = paymentRequest.getRecipients().get(0).getRecipient();
			long amount;
			if(paymentRequest.getAmount() == null) {
					final String amountParameter = request.getParameter("amount");
					if(amountParameter == null) {
						sendToEnterAmountPage(request, response, paymentRequest);
						return;
					}
					 
				try {
					amount = parseAmount(amountParameter);
				} catch (Exception ex) {
					request.setAttribute("ErrorMessage", "The amount you entered is not valid.");
					sendToEnterAmountPage(request,response,paymentRequest);
					return;
				}
			} else {
				amount = paymentRequest.getAmount();
			}

			response.sendRedirect(PayPal.getPaymentRedirect(u, paymentRequest, amount, request.getRemoteAddr()));
		} catch(PayPalExceptionWithErrorCode ex) {
			final String payPalError = ex.getTranslatedMessage();
			if(payPalError == null) {
				Logger.getAnonymousLogger().log(Level.SEVERE, "Unknown error during processing  k:"+k+" + i:"+i+" c: "+ex.getErrorCode()+" to:"+email);
			} else {
				Logger.getAnonymousLogger().log(Level.SEVERE, payPalError+" during processing  k:"+k+" + i:"+i+" c: "+ex.getErrorCode()+" to:"+email);
				request.setAttribute("ppError", payPalError);
			}
			request.getRequestDispatcher("/InvalidPayment.jsp").forward(request, response);
		} catch(Exception ex) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Exception for k:"+k+" + i:"+i, ex);
			response.sendRedirect(ERROR_URL);
		} finally {
			em.close();
		}		
	}
	
	/**
	 * Convert an amount string into an amount long
	 * 
	 * @param amount The value as a string.
	 * @return The value as a long.
	 */
	
	private long parseAmount(final String amount) {
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
	
	/**
	 * Set up the neccessary items for the payment page
	 * @throws IOException 
	 * @throws ServletException 
	 */
	
	private void sendToEnterAmountPage(final HttpServletRequest request,
			final HttpServletResponse response, final PaymentRequest paymentRequest) 
		throws ServletException, IOException {
		final String currencyCode = paymentRequest.getCurrency();
		if(currencyCode != null) {
			request.setAttribute("CurrencyCode", currencyCode);
			try {
				Currency currency = Currency.getInstance(paymentRequest.getCurrency());
				request.setAttribute("Currency", currency.getSymbol(request.getLocale()));					
			} catch(Exception ex) {
				request.setAttribute("Currency", paymentRequest.getCurrency());
			}
		}
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(ENTER_AMOUNT_URL);
		dispatcher.forward(request, response);		
	}
}
