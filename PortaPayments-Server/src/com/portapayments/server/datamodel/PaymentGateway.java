package com.portapayments.server.datamodel;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue; 
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.google.appengine.api.datastore.Key;

@Entity
public class PaymentGateway {
	/**
	 * Key for the entity.
	 */
	
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Key key; 

    /**
     * The name of the payment gateway
     */
    
    private String displayName;
    
    /**
     * The name of the class which handles payments
     */
    
    private String paymentHandler;

    /**
     * The payment requests configured for this gatepay
     */
    @OneToMany(mappedBy="gateway")
    private List<PaymentRequest> paymentRequests;
    
	public Key getKey() {
		return key;
	}

	public void setKey(final Key key) {
		this.key = key;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPaymentHandler() {
		return paymentHandler;
	}

	public void setPaymentHandler(String paymentHandler) {
		this.paymentHandler = paymentHandler;
	}

	public List<PaymentRequest> getPaymentRequests() {
		return paymentRequests;
	}

	public void setPaymentRequests(List<PaymentRequest> paymentRequests) {
		this.paymentRequests = paymentRequests;
	}	
}
