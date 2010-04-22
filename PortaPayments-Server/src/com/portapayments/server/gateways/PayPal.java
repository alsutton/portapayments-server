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
	
	private static final String PAYPAL_URL = "https://svcs.paypal.com/AdaptivePayments/Pay";

	/**
	 * The URL stub for passing the payment authentication to PayPal
	 */

	private final static String PAY_URL_STUB = "https://www.paypal.com/webscr?cmd=_ap-payment&paykey=";

	/**
	 * The account which receives all the fee payments.
	 */
	
	private static String FEES_RECIPIENT;
	static {
		try {
			FEES_RECIPIENT = URLEncoder.encode("payments@funkyandroid.com", "UTF-8");
		} catch(Exception ex) {
			FEES_RECIPIENT = "payments@funkyandroid.com";			
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
			fees = 2;
		}
		
		Properties headers = new Properties();		
		headers.put("X-PAYPAL-SECURITY-USERID", "payments_api1.funkyandroid.com"); 
		headers.put("X-PAYPAL-SECURITY-PASSWORD","8PHXACWXATXPW9QA"); 
		headers.put("X-PAYPAL-SECURITY-SIGNATURE","A3F8ibcD.y4vlg9hgBrTNX-nZaVPAF0lgltCEaVuHALO5vzjj6fhxS8I");
		headers.put("X-PAYPAL-REQUEST-DATA-FORMAT", "NV"); 
		headers.put("X-PAYPAL-RESPONSE-DATA-FORMAT", "NV");  
		headers.put("X-PAYPAL-APPLICATION-ID", "APP-8AR47415GG027162L");

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
			Logger.getAnonymousLogger().log(Level.SEVERE, "Error during post", e);
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
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
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
		
		private static final Map<String, String> errorMessages = new HashMap<String,String>();
		static {
			errorMessages.put("500000","There is a system error");
			errorMessages.put("520002","Internal error");
			errorMessages.put("520003","User name/password is incorrect");
			errorMessages.put("520005","Merchant account is locked");
			errorMessages.put("520006","This call is not defined in the database");	
			errorMessages.put("520009","PayPal did not recognise the payment recipient.");	
			errorMessages.put("529038","There was an error while making this payment");	
			errorMessages.put("539012","The preapproval key has not been authorized yet");	
			errorMessages.put("539041","The email account is based in a country that is not enabled to receive payments");	
			errorMessages.put("539043","The email account is based in a country that is not enabled to send payments");	
			errorMessages.put("540031","You don't have permission to may the payment.");
			errorMessages.put("559044","Account setting on the receiver prohibited the payment");	
			errorMessages.put("560027","The argument value is unsupported");	
			errorMessages.put("569000","Split payments are not supported at this time");	
			errorMessages.put("569013","The preapproval key has been canceled");	
			errorMessages.put("569016","Preapproval PIN functionality is not enabled");	
			errorMessages.put("569017","The preapproval key has been suspended");	
			errorMessages.put("569018","Preapproved payments have been disabled");	
			errorMessages.put("569019","The preapproval has been suspended due to too many PIN failures");	
			errorMessages.put("569042","The email account is not confirmed by PayPal");	
			errorMessages.put("579007","The maximum number of receivers is 6");	
			errorMessages.put("579010","If a preapproval key is specified, the sender’s email address must be, too");	
			errorMessages.put("579014","The preapproval key specifies a different sender than the payment request");	
			errorMessages.put("579017","The amount for the primary receiver must be greater than or equal to the total of other chained receiver amounts");	
			errorMessages.put("579024","The preapproval key cannot be used before the start date or after the end date");	
			errorMessages.put("579025","The preapproval key cannot be used on this weekday");	
			errorMessages.put("579026","The preapproval key cannot be used on this day of the month");	
			errorMessages.put("579027","The preapproval key specifies a different currency than the payment request");	
			errorMessages.put("579028","The payment amount exceeds the maximum amount per payment");	
			errorMessages.put("579030","The number of payments made this period exceeds the maximum number of payments per period");	
			errorMessages.put("579031","The total amount of all payments exceeds the maximum total amount for all payments");	
			errorMessages.put("579033","The sender and each receiver must have different accounts");	
			errorMessages.put("579040","The receivers cannot belong to the same PayPal account");	
			errorMessages.put("579042","The tracking ID already exists and cannot be duplicated");	
			errorMessages.put("579045","The email account exceeds the receiving limit");	
			errorMessages.put("579047","The email account exceeds the purse limit");	
			errorMessages.put("579048","The email account exceeds the sending limit");	
			errorMessages.put("580001","Invalid request");	
			errorMessages.put("580023","Invalid request");
			errorMessages.put("580027","The argument is unsupported");	
			errorMessages.put("580028","A URL is malformed");	
			errorMessages.put("580029","Invalid request");	
			errorMessages.put("589009","This payment cannot be processed because no payment source is available");	
			errorMessages.put("589023","If a fractional amount is rounded due to currency conversion, funds could be lost");	
			errorMessages.put("589039","The email address is invalid. It may not be registered in PayPal’s system yet.");
		}
		
		
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
		
		/**
		 * Get the PayPal translated error message
		 */
		public String getTranslatedMessage() {
			if(errorCode == null) {
				return null;
			}
			return errorMessages.get(errorCode);
		}
	}	

}
