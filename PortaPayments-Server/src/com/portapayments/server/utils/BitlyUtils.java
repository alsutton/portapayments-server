package com.portapayments.server.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import net.sf.json.JSONObject;

import com.portapayments.server.datamodel.PaymentRequest;

public class BitlyUtils {

	/**
	 * The bit.ly URL prefix
	 */
	
	private static final String BITLY_URL_PREFIX = "http://api.bit.ly/shorten?version=2.0.1&longUrl=";
	private static final int BITLY_PREFIX_LENGTH = BITLY_URL_PREFIX.length();
		
	/**
	 * The bit.ly URL suffix
	 */
	
	private static final String BITLY_URL_SUFFIX = "&login=andappstore&apiKey=R_45f8f76299b17957323dabf6f57cfa84";
	private static final int BITLY_SUFFIX_LENGTH = BITLY_URL_SUFFIX.length();

	/**
	 * The key in the JSON response from bit.ly which holds the shortened URL.
	 */
	
	private static final String BITLY_URL_KEY = "shortUrl";
	
	/**
	 * The prefix for payment requests.
	 */
	
	private static final String PAYMENT_URL_PREFIX = "http://portapayments.appspot.com/PayUniversalCode";

	/**
	 * Private constructor to prevent instanciation.
	 */
	
	private BitlyUtils() {
		super();
	}
	
	/**
	 * Create the URL for a specific version and shorten it.
	 */
	
	public static String getShortenedURL(final PaymentRequest pr) 
		throws IOException {
		StringBuilder url = new StringBuilder(128);
		url.append(PAYMENT_URL_PREFIX);
		url.append("?i=");
		url.append(pr.getKey().getId());
		url.append("&k=");
		url.append(URLEncoder.encode(pr.getKey().getKind(),"UTF-8"));
		return getShortenedURL(url.toString());
	}
	
	/**
	 * Get a shortened URL using bit.ly
	 * 
	 * @param url The URL to shorten
	 * @return The shortened URL
	 */
	
	private static final String getShortenedURL(final String url) 
		throws IOException {
		final String encodedURL = URLEncoder.encode(url, "UTF-8");
		
		final StringBuilder bitlyURL = new StringBuilder(BITLY_PREFIX_LENGTH+BITLY_SUFFIX_LENGTH+encodedURL.length());
		bitlyURL.append(BITLY_URL_PREFIX);
		bitlyURL.append(encodedURL);
		bitlyURL.append(BITLY_URL_SUFFIX);
		
		final URLConnection connection = new URL(bitlyURL.toString()).openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		
		final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		try {
			StringBuilder jsonResponse = new StringBuilder(255);
			String response;
			while ((response = in.readLine()) != null) {
				jsonResponse.append(response);
			}
			JSONObject object = JSONObject.fromObject(jsonResponse.toString());
			JSONObject results = object.getJSONObject("results");
			JSONObject siteDetails = results.getJSONObject(url);
			return siteDetails.getString(BITLY_URL_KEY);
		} finally {
			in.close();
		}
	}
}
