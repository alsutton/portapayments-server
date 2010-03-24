package com.portapayments.server.datamodel;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

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
    
    private String gateway;
    
    /**
     * The email address of the recipient
     */
    
    @OneToMany(cascade=CascadeType.ALL)
    private List<PaymentRecipient> recipients;

    /**
     * The amount his recipient will receive
     */
    
    private Long amount;

	public Key getKey() {
		return key;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(final String gateway) {
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
	
	public List<PaymentRecipient> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<PaymentRecipient> recipients) {
		this.recipients = recipients;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}
}
