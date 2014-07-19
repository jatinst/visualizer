package com.jatinst.money.visualizer.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class with some basic fields that every statement message has Also has an
 * extras map to handle custom key-value pairs
 */
public class RawTransactionMessage {
	private final Date transdate;
	private final BigDecimal transAmount;
	private final String transDescription;
	private Map<String, Object> extras;

	public RawTransactionMessage(Date transdate, BigDecimal transAmount,
			String transDescription) {
		this.transdate = transdate;
		this.transAmount = transAmount;
		this.transDescription = transDescription;
	}

	public Date getTransdate() {
		return transdate;
	}

	public BigDecimal getTransAmount() {
		return transAmount;
	}

	public String getTransDescription() {
		return transDescription;
	}

	/**
	 * Add extra fields that are not standard in a raw transaction read from the file
	 * @param extraKey the name of the extra field
	 * @param extraValue the value of the extra field
	 */
	public void addExtra(String extraKey, Object extraValue) {
		if (extras == null) { // create the map if it does not exist
			extras = new HashMap<String, Object>();
		}
		extras.put(extraKey, extraValue);
	}

	/**
	 * 
	 * @param extraKey the key for the extra
	 * @return the value for the given extraKey, or null if none exists for that key
	 */
	public Object getExtra(String extraKey) {
		return extras == null ? null : extras.get(extraKey);
	}

	/**
	 * @return the entire extras map, or null if no extras exist
	 */
	public Map<String, Object> getExtras() {
		return extras;
	}

}
