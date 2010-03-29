package com.portapayments.server.gateways;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.portapayments.server.datamodel.PaymentRecipient;
import com.portapayments.server.datamodel.PaymentRequest;

public class PayPal {

	/**
	 * The endpoint URL for paypal requests.
	 */
	
	private static final String PAYPAL_URL = "https://svcs.sandbox.paypal.com/AdaptivePayments/Pay";

	/**
	 * The URL stub for passing the payment authentication to PayPal
	 */

	private final static String PAY_URL_STUB = "https://www.sandbox.paypal.com/webscr?cmd=_ap-payment&paykey=";

	/**
	 * The account which receives all the fee payments.
	 */
	
	private static String FEES_RECIPIENT;
	static {
		try {
			FEES_RECIPIENT = URLEncoder.encode("fees_1269271952_biz@portapayments.com", "UTF-8");
		} catch(Exception ex) {
			FEES_RECIPIENT = "fees_1269271952_biz@portapayments.com";			
		}
	}
	
	/**
	 * The URL the user is sent to after payments have been approved.
	 */
	
	private static String PAY_OK_URL;
	static {
		try {
			PAY_OK_URL = URLEncoder.encode("http://appengine.portapayments.com/payOK.html", "UTF-8");
		} catch(Exception ex) {
			PAY_OK_URL = "http://appengine.portapayments.com/payOK.html";
		}
	}
	
	/**
	 * 
	 */
	
	private static String PAY_CANCELLED_URL;
	static {
		try {
			PAY_CANCELLED_URL = URLEncoder.encode("http://appengine.portapayments.com/payCancelled.html", "UTF-8");
		} catch(Exception ex) {
			PAY_CANCELLED_URL = "http://appengine.portapayments.com/payCancelled.html";
		}
	}
	
	/**
	 * Method to make a payment between a sender and receiver
	 * @throws IOException 
	 * 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	
	public static String getPaymentRedirect(final PaymentRequest request, final String senderIPAddress) 
		throws IOException {
		return getPaymentRedirect(null, request, request.getAmount(), senderIPAddress);
	}

	/**
	 * Method to make a payment between a sender and receiver
	 * @throws IOException 
	 * 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	
	public static String getPaymentRedirect(final String sender, final PaymentRequest request, final String senderIPAddress) 
		throws IOException {
		return getPaymentRedirect(sender, request, request.getAmount(), senderIPAddress);
	}

	/**
	 * Method to make a payment between a sender and receiver
	 * 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	
	public static String getPaymentRedirect(final String sender, final PaymentRequest request, 
			final long amount, final String senderIPAddress) 
		throws IOException {
		long fees = amount/400;
		if(fees < 2) {
			fees++;
		}
		
		Properties headers = new Properties();		
		headers.put("X-PAYPAL-SECURITY-USERID", "fees_1269271952_biz_api1.portapayments.com"); 
		headers.put("X-PAYPAL-SECURITY-PASSWORD","1269271966"); 
		headers.put("X-PAYPAL-SECURITY-SIGNATURE","Al9Hkv5q.OAYUs-uvhYKQIFnf1nGADCHk2izI4Wim9rQSxb0LJa.NzVL");
		headers.put("X-PAYPAL-REQUEST-DATA-FORMAT", "NV"); 
		headers.put("X-PAYPAL-RESPONSE-DATA-FORMAT", "NV");  
		headers.put("X-PAYPAL-APPLICATION-ID", "APP-80W284485P519543T");

		StringBuilder requestBody = new StringBuilder();
		if(sender != null) {
	        requestBody.append("senderEmail=");
	        requestBody.append(sender);
	        requestBody.append('&');
		}
		requestBody.append("actionType=PAY&currencyCode=");
		requestBody.append(request.getCurrency());
		requestBody.append("&feesPayer=EACHRECEIVER");
		
		
		PaymentRecipient recipient = request.getRecipients().get(0);
		requestBody.append("&receiverList.receiver(0).email=");
		requestBody.append(URLEncoder.encode(recipient.getRecipient(), "UTF-8"));
		requestBody.append("&receiverList.receiver(0).amount=");
		addAmountFromLong(requestBody, amount);			
		requestBody.append("&receiverList.receiver(0).primary=true");
		requestBody.append("&receiverList.receiver(1).email=");
		requestBody.append(FEES_RECIPIENT);
		requestBody.append("&receiverList.receiver(1).amount=");
		addAmountFromLong(requestBody, fees);
		requestBody.append("&receiverList.receiver(1).primary=false");
		
		requestBody.append("&returnUrl=");
		requestBody.append(PAY_OK_URL);
		requestBody.append("&cancelUrl=");
		requestBody.append(PAY_CANCELLED_URL);
		requestBody.append("&requestEnvelope.errorLanguage=en_US");
		requestBody.append("&clientDetails.ipAddress=");
		requestBody.append(senderIPAddress);
		if(request.getMemo() != null) {
			requestBody.append("&memo=");
			requestBody.append(URLEncoder.encode(request.getMemo(), "UTF-8"));
		}
		requestBody.append("&clientDetails.applicationId=PortaPayments");
        
		System.out.println(requestBody.toString());
        final Map<String,String> results = postData(headers, requestBody.toString());
        if(results == null) {
        	throw new PayPalException("PayPal was unable to start the transaction.");
        }
        
        final String ack = results.get("responseEnvelope.ack");
        if(ack != null && "Failure".equals(ack)) {
        	StringBuilder errorMessage = new StringBuilder("PayPal generated an error");
        	String errorMessageText = results.get("error(0).message");
        	if(errorMessageText != null) {
        		errorMessage.append(" : ");
        		errorMessage.append(errorMessageText);
        	}
        	throw new PayPalExceptionWithErrorCode(errorMessage.toString(), results.get("error(0).errorId"));
        }
        
        final String payKey = results.get("payKey");
        return PayPal.PAY_URL_STUB+payKey;
	}
	
	/**
	 * Adds an amount to a string buffer.
	 * 
	 */
	
	private static void addAmountFromLong(final StringBuilder builder, final long amount) {
		builder.append(amount / 100);
		builder.append('.');
		
		long minorAmount = amount % 100;
		if(minorAmount < 10) {
			builder.append('0');
		}
		builder.append(minorAmount);
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
