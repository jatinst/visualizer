package com.jatinst.money.visualizer.reader.impl;

import java.util.List;

import com.jatinst.money.visualizer.model.RawTransactionMessage;
import com.jatinst.money.visualizer.reader.api.RawTransFileReader;
import com.jatinst.money.visualizer.reader.api.RawTransactionListener;

public class GenericCSVFileReader implements RawTransFileReader {

	@Override
	public List<RawTransactionMessage> loadTransactions(String filePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadTransactions(String filePath,
			RawTransactionListener listener) {
		// TODO Auto-generated method stub
		
	}
}
