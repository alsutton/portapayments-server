package com.portapayments.server.datamodel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class PaymentRequest {
	/**
	 * Key for the entity.
	 */
	
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Key key; 

    /**
     * The currency for the payment request
     */
    
    private String currency;

    /**
     * A note for this payment.
     */
    
    private String memo;
    
    /**
     * The payment gateway to use for this payment
     */
    
    private PaymentGateway gateway;
    
    /**
     * The email address of the recipient
     */
    
    private String recipient;

    /**
     * The amount his recipient will receive
     */
    
    private String amount;

	public Key getKey() {
		return key;
	}

	public PaymentGateway getGateway() {
		return gateway;
	}

	public void setGateway(PaymentGateway gateway) {
		this.gateway = gateway;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}
}
