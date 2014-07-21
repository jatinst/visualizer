package com.jatinst.money.visualizer.reader.api;

import java.util.List;

import com.jatinst.money.visualizer.exception.ReaderException;
import com.jatinst.money.visualizer.exception.StmtParserException;
import com.jatinst.money.visualizer.model.RawTransactionMessage;

public interface RawTransFileReader {

	/**
	 * For smaller files, get the entire list of transactions in one go
	 * @param filePath the location on disk of the raw file
	 * @return List of RawTransactionMessage objects from the file
	 * @throws StmtParserException 
	 */
	public List<RawTransactionMessage> loadTransactions(String filePath) throws ReaderException, StmtParserException;
	
	/**
	 * For large files, will call the listener to pass the rawTransaction messages
	 * @param filePath 
	 * @param listener
	 */
	public void loadTransactions(String filePath, RawTransactionListener listener) throws ReaderException;
	
}
