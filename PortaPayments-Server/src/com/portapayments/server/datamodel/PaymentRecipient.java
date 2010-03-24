package com.portapayments.server.datamodel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class PaymentRecipient {
	/**
	 * Key for the entity.
	 */
	
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Key key; 

    /**
     * The email address of the recipient
     */
    
    private String recipient;

    /**
     * The amount his recipient will receive
     */
    
    private Long amount;

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}
	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(final Long amount) {
		this.amount = amount;
	}
}
