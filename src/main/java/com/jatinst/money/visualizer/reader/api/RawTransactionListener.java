package com.jatinst.money.visualizer.reader.api;

import com.jatinst.money.visualizer.model.RawTransactionMessage;

public interface RawTransactionListener {
	
	public void onMessage(RawTransactionMessage transMsg);
}
