package com.jatinst.money.visualizer.reader.api;

import java.util.List;

import com.jatinst.money.visualizer.exception.StmtParserException;
import com.jatinst.money.visualizer.model.RawTransactionMessage;

public interface TransactionParser {
	public List<RawTransactionMessage> parseTransactions(List<String> pageTexts) throws StmtParserException;

}
