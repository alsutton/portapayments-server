package com.portapayments.server.datamodel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue; 
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.google.appengine.api.datastore.Key;

@Entity
public class Recipient {
	/**
	 * Key for the entity.
	 */
	
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Key key; 

    /**
     * The payment request this is part of
     */
    
    @ManyToOne
    private PaymentRequest paymentRequest;
    
    /**
     * The email address of the recipient
     */
    
    private String emailAddress;

    /**
     * The amount his recipient will receive
     */
    
    private String amount;
    
	public Key getKey() {
		return key;
	}

	public void setKey(final Key key) {
		this.key = key;
	}
	
	public PaymentRequest getPaymentRequest() {
		return paymentRequest;
	}

	public void setPaymentRequest(final PaymentRequest paymentRequest) {
		this.paymentRequest = paymentRequest;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(final String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(final String amount) {
		this.amount = amount;
	}    	
}
