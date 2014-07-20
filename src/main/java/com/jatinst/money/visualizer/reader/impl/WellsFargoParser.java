package com.jatinst.money.visualizer.reader.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.jatinst.money.visualizer.model.RawTransactionMessage;
import com.jatinst.money.visualizer.reader.api.TransactionParser;

/**
 * Class that knows how to deal with the text retrieved from a WellsFargo statement
 */
public class WellsFargoParser implements TransactionParser {
	private static final String TRANSACTIONS_BEGIN_MARKER = "Transaction history";
	private static final String TRANSACTIONS_END_MARKER = "Ending balance on";

	private static final Logger logger = Logger.getLogger(WellsFargoParser.class);

	@Override
	public List<RawTransactionMessage> parseTransactions(List<String> pageTexts) {
		boolean foundTransStart = false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pageTexts.size(); i++) {
			String pageText = pageTexts.get(i);
			if (!pageText.contains(TRANSACTIONS_BEGIN_MARKER)) {
				continue;
			} else { // found the start of Transaction history
				logger.debug("Found the Transaction history table start on page " + (i + 1));
				foundTransStart = true;
			}

			if (foundTransStart) {
				sb.append(pageText);
				if (pageText.contains(TRANSACTIONS_END_MARKER)) {
					break; // we are done looping
				}
			}
		}
		return splitTransactions(sb.toString());
	}

	private List<RawTransactionMessage> splitTransactions(String transString) {
		//lets split this String into lines using the line delimiter
		
		String[] lines = transString.split(System.lineSeparator());
		
		boolean foundTransStart = false;
		for(String line: lines) {
			if(line.contains(TRANSACTIONS_BEGIN_MARKER)) {
				foundTransStart = true;
			} 
			
			if(foundTransStart) {
				logger.debug(line);
				if(line.contains(TRANSACTIONS_END_MARKER))
					break; //we are done looping
			}
		}
		
		return null;
	}

}
