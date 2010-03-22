package com.portapayments.server.gateways;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.portapayments.server.datamodel.PaymentRequest;

public class PayPal {

	/**
	 * The endpoint URL for paypal requests.
	 */
	
	private static final String PAYPAL_URL = "https://svcs.paypal.com/AdaptivePayments/Pay";

	/**
	 * Method to make a payment between a sender and receiver
	 * @throws IOException 
	 * 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	
	public static String makePayment(final String sender, final PaymentRequest request) 
		throws IOException {
		return makePayment(sender, request, request.getAmount());
	}

	/**
	 * Method to make a payment between a sender and receiver
	 * 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	
	public static String makePayment(final String sender, final PaymentRequest request, final String amountString ) 
		throws IOException {
		double amount = Double.parseDouble(amountString)*100;
		amount = Math.ceil(amount);
		amount /= 100;
		
		Properties headers = new Properties();		
		headers.put("X-PAYPAL-SECURITY-USERID", "payments_api1.funkyandroid.com"); 
		headers.put("X-PAYPAL-SECURITY-PASSWORD","8PHXACWXATXPW9QA"); 
		headers.put("X-PAYPAL-SECURITY-SIGNATURE","A3F8ibcD.y4vlg9hgBrTNX-nZaVPAF0lgltCEaVuHALO5vzjj6fhxS8I");
		headers.put("X-PAYPAL-REQUEST-DATA-FORMAT", "NV"); 
		headers.put("X-PAYPAL-RESPONSE-DATA-FORMAT", "NV");  
		headers.put("X-PAYPAL-APPLICATION-ID", "APP-80W284485P519543T");
		

		StringBuilder requestBody = new StringBuilder();
        requestBody.append("senderEmail=");
        requestBody.append(sender);  
		requestBody.append("&actionType=PAY&currencyCode=");
		requestBody.append(request.getCurrency());
		requestBody.append("&feesPayer=EACHRECEIVER");
		
		requestBody.append("&receiverList.receiver(0).email=");
		requestBody.append(request.getRecipient());
		requestBody.append("&receiverList.receiver(0).amount=");
		requestBody.append(amount);
		
		double fees = (amount*100)/400;
		fees = Math.ceil(fees);
		fees /= 100;
		
		requestBody.append("&receiverList.receiver(1).email=payments@funkyandroid.com&receiverList.receiver(1).amount=");
		requestBody.append(fees);
		requestBody.append("&receiverList.receiver(1).primary=false&returnUrl=http://appengine.portapayments.mobi/ppm/PayOK.jsp");
		requestBody.append("&cancelUrl=http://appengine.portapayments.mobi/ppm/PayCancelled.jsp");
		requestBody.append("&requestEnvelope.errorLanguage=en_US");
		requestBody.append("&clientDetails.ipAddress=127.0.0.1");
		requestBody.append("&memo="+request.getMemo());
		requestBody.append("&clientDetails.applicationId=PortaPayments");
        
        Map<String,String> results = postData(headers, requestBody.toString());
        if(results == null) {
        	throw new PayPalException("PayPal was unable to start the transaction.");
        }
        
        final String ack = results.get("responseEnvelope.ack");
        if(ack != null && "Failure".equals(ack)) {
        	throw new PayPalExceptionWithErrorCode("PayPal generated an error ", results.get("error(0).errorId"));
        }
        
        return results.get("payKey");
	}
	

	public static Map<String,String> postData(final Properties headers, final String data) 
		throws IOException {
		int retries = 3;
		while( retries > 0) {
			HttpURLConnection connection = 
				setupConnection(PayPal.PAYPAL_URL, headers, null);

			Map<String,String> results = sendHttpPost(connection, data);
			if(results != null) {
				return results;
			}
			retries--;
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				; // Do nothing, an interrupted sleep is not a problem
			}
		}
		return null;
	}

	public static  Map<String,String> sendHttpPost(final HttpURLConnection connection, final String data) {
		BufferedReader reader = null;
		
		try {
			OutputStream os = connection.getOutputStream();
			os.write(data.toString().getBytes("UTF-8"));
			os.close();
			int status = connection.getResponseCode();
			if (status != 200) {
				Logger.getAnonymousLogger().log(Level.SEVERE,  "HTTP Error code " + status + " received, transaction not submitted");
				return null;
			} else {
				reader = new BufferedReader(new InputStreamReader(connection
						.getInputStream()));
			}

			return getResults(reader);
		} catch (Exception e) {
			System.out.println(e);
		} finally {

			try {
				if (reader != null)
					reader.close();
				if (connection != null)
					connection.disconnect();
			} catch (Exception e) {
				; // Do nothing
			}

		}
		
		return null;
	}

	private static HttpURLConnection setupConnection(String endpoint,
			Properties headers, Properties connectionProps) throws IOException {
		HttpURLConnection connection = null;

		URL url = new URL(endpoint);
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);

		Object[] keys = headers.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			connection.setRequestProperty((String) keys[i],
					(String) headers.get(keys[i]));
		}

		return connection;
	}
	
	/**
	 * Create a Map of the results from a post
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	
	private static Map<String,String> getResults(final Reader reader) 
		throws IllegalStateException, IOException {
        Map<String,String> results = new HashMap<String,String>();

    	int character;
    	String key = "X";
    	StringBuilder builder = new StringBuilder();
    	while((character = reader.read()) != -1) {
    		char c = (char) character;
    		if(c == '=') {
    			key = builder.toString();
    			builder.delete(0, builder.length());
    		} else if (c == '&') {
    			results.put(key, builder.toString());
    			builder.delete(0, builder.length());
    		} else {
    			builder.append(c);
    		}
    	}

        return results;
	}
	
	/**
	 * Exception thrown if something is goes wrong in the app/PayPal communication
	 */
	
	public static class PayPalException extends RuntimeException {
		//569057 - Recipient invalid
		/**
		 * Generated serial ID
		 */
		private static final long serialVersionUID = -1416134527103317337L;

		PayPalException(final String message) {
			super(message);
		}
	}
	
	/**
	 * Exception thrown if PayPal reports an error that contains an error code
	 */
	
	public static final class PayPalExceptionWithErrorCode extends PayPalException {
		/**
		 * Generated serial ID.
		 */
		private static final long serialVersionUID = -4204297047002736812L;
		
		/**
		 * The error code from PayPal
		 */
		private String errorCode;
		
		PayPalExceptionWithErrorCode(final String message, final String errorCode) {
			super(message);
			this.errorCode = errorCode;
		}

		/**
		 * Get the error code.
		 * 
		 * @return The error code from PayPal.
		 */
		public String getErrorCode() {
			return errorCode;
		}
	}	

}
