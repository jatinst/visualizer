package com.jatinst.money.visualizer.reader.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jatinst.money.visualizer.exception.StmtParserException;
import com.jatinst.money.visualizer.model.RawTransactionMessage;
import com.jatinst.money.visualizer.reader.api.TransactionParser;
import com.jatinst.money.visualizer.util.ParserUtility;

/**
 * TODO - a better parser would actually know how to read the table from the PDF, this is using a bunch of Regex to try
 * to figure out transactions. Not sure if this will work all the time! Class that knows how to deal with the text
 * retrieved from a WellsFargo statement TODO - make the code figure out the date from the statement Date.
 */
public class WellsFargoParser implements TransactionParser {
	private static final String TRANSACTIONS_BEGIN_MARKER = "Transaction history";
	private static final String TRANSACTIONS_END_MARKER = "Ending balance on";
	public static final String DEPOSIT_PATTERN = "(\\d+?/\\d+?) (.*) ([\\d,]*.\\d\\d)";
	public static final String CHECK_PATTERN = "(\\d+?/\\d+?)(.* Check .*?) *?([\\d,]*\\.\\d\\d) *?([\\d,]*\\.\\d\\d)*(.*)";

	private static final Logger logger = Logger.getLogger(WellsFargoParser.class);
	private static final String HUNDREDS_SEPARATOR = ",";

	@Override
	public List<RawTransactionMessage> parseTransactions(List<String> pageTexts) throws StmtParserException {
		/*
		 * for(String page: pageTexts) { logger.info("page text: \n" + page); }
		 */
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

	private List<RawTransactionMessage> splitTransactions(String transString) throws StmtParserException {
		// lets split this String into lines using the line delimiter
		// since PDFBox uses the system line separator when creating the text, we will use the same to
		// split it
		String[] lines = transString.split(System.lineSeparator());

		String datePatternStr = "(\\d+?/\\d+?) .*";

		Pattern pattern = Pattern.compile(datePatternStr);

		boolean foundTransHistStart = false;
		boolean foundFirstTrans = false;
		List<String> transaction = null;
		List<RawTransactionMessage> rawTransList = new ArrayList<RawTransactionMessage>();
		for (String line : lines) {
			if (line.contains(TRANSACTIONS_BEGIN_MARKER)) {
				foundTransHistStart = true;
			}

			if (foundTransHistStart) {
				if (logger.isTraceEnabled()) {
					logger.trace("Found transaction line:" + line);
				}
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					if (!foundFirstTrans) {
						foundFirstTrans = true; // we found our first Transaction!
					}
					if (logger.isTraceEnabled()) {
						logger.trace("Found match for pattern " + datePatternStr + " for line : " + line);
					}
					// For Wells Fargo stmt extracted text, every transaction line that starts with a Date is a new
					// Transaction. But some transactions can span multiple lines, so we need to create an array for
					// each transaction
					// When we see the begin of transaction, we send the previous transaction (if it exists)
					// into the extractor and create a new transaction for this line( or set of lines)
					if (transaction != null) { // we have a previous Transaction
						rawTransList.add(extractTransaction(transaction));
					}
					// now create a new one for this and add the current line to it
					transaction = new ArrayList<String>();
					transaction.add(line);
				} else { // this is a continuing transaction
					if (foundFirstTrans) { // all the steps below only apply once we have found our first transaction
											// start
						if (transaction == null) { // should not happen
							logger.error("Error in parsing, no transaction start found before continuing line!");
							throw new StmtParserException(
									"Error in parsing, no transaction start found before continuing line!");
						}
						transaction.add(line);
					}
				}
				if (line.contains(TRANSACTIONS_END_MARKER))
					break; // we are done looping
			}
		}

		return null;
	}

	private RawTransactionMessage extractTransaction(List<String> transaction) throws StmtParserException {
		RawTransactionMessage toReturn = null;
		if (transaction.size() == 1) {
			String transLine = transaction.get(0);
			toReturn = extractOneLineTrans(transLine);
		} else {
			toReturn = extractMultiLineTrans(transaction);
		}
		return toReturn;
	}

	private RawTransactionMessage extractOneLineTrans(String transLine) {
		// one line in the stmt, could be a deposit or check withdrawal for Wells Fargo
		Pattern depPattern = Pattern.compile(DEPOSIT_PATTERN); // matches both Check and regular deposits
		Pattern checkPattern = Pattern.compile(CHECK_PATTERN);

		String dateStr, descStr, amountStr;
		RawTransactionMessage.Type transType;
		Matcher depMatcher = depPattern.matcher(transLine);
		if (depMatcher.find()) {
			// lets check that this is not a Check withdrawal
			Matcher checkMatcher = checkPattern.matcher(transLine);
			if (!checkMatcher.find()) { // this is a deposit
				dateStr = depMatcher.group(1);
				descStr = depMatcher.group(2);
				amountStr = depMatcher.group(3);
				transType = RawTransactionMessage.Type.ADDITION;
				if (logger.isDebugEnabled()) {
					logger.debug("Found Deposit, Date = " + dateStr + "; Trans_desc = " + descStr + "; trans_amt = "
							+ amountStr);
				}
			} else { // this is a check withdrawal
				dateStr = checkMatcher.group(1);
				descStr = checkMatcher.group(2);
				amountStr = checkMatcher.group(3);
				transType = RawTransactionMessage.Type.SUBTRACTION;
				if (logger.isDebugEnabled()) {
					logger.debug("Found Check withdrawal, Date = " + dateStr + "; Trans_desc = " + descStr
							+ "; trans_amt = " + amountStr);
				}
			}
		} else {
			logger.error("Could not match line with any predefined transaction types" + transLine);
			// TODO - handle such errors in a well defined way, allowing the user to manually parse?
			return null;
		}
		// try converting the obtained values into expected values
		Date transDate = null;
		BigDecimal amount;
		try {
			transDate = ParserUtility.getDateFromMMSlashDD(dateStr, "2014");
			if (amountStr == null) {
				throw new StmtParserException("Got amount as null using Regex, cannot proceed for line" + transLine);
			}
			amount = new BigDecimal(amountStr.replace(HUNDREDS_SEPARATOR, ""));
		} catch (StmtParserException | NumberFormatException e) {
			logger.error("Error in creating data for transaction for" + transLine, e);
			// TODO - handle such errors in a well defined way, allowing the user to manually parse?
			return null;
		}
		// TODO - fix hardcoded year at some point
		return new RawTransactionMessage(transDate, amount, descStr, transType);
	}

	private RawTransactionMessage extractMultiLineTrans(List<String> transaction) {
		// TODO Auto-generated method stub
		return null;
	}
}
