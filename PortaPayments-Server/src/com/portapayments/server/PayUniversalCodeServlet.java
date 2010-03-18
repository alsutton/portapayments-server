package com.portapayments.server;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.portapayments.server.datamodel.PaymentRequest;
import com.portapayments.server.utils.EMF;

public class PayUniversalCodeServlet extends HttpServlet {

	/**
	 * Generated serial ID.
	 */
	private static final long serialVersionUID = -3976716985453783742L;
	
	/**
	 * The URL to send users to if a payment isn't valid.
	 */
	private static final String ERROR_URL = "http://static.portapayments.com/errors/PaymentInvalid.html";

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) 
		throws IOException, ServletException {
		final String	k = request.getParameter("k"),
						i = request.getParameter("i");
		
		if(k == null || i == null) {
			log("Missing k:"+(k==null)+" / i"+(i==null));
			response.sendRedirect(ERROR_URL);
			return;
		}
		
		EntityManager em = EMF.get().createEntityManager();
		try {
			Key paymentKey = KeyFactory.createKey(k, Long.parseLong(i));
			PaymentRequest paymentRequest = em.find(PaymentRequest.class, paymentKey);
			if(paymentRequest == null) {
				log("Payment not found for k:"+k+" / i:"+i);
				response.sendRedirect(ERROR_URL);
				return;				
			}
		} catch(Exception ex) {
			super.log("Exception for k:"+k+" + i:"+i, ex);
			response.sendRedirect(ERROR_URL);
		} finally {
			em.close();
		}
		
	}
}
