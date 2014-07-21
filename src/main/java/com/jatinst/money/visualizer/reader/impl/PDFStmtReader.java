package com.jatinst.money.visualizer.reader.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import com.jatinst.money.visualizer.exception.ReaderException;
import com.jatinst.money.visualizer.exception.StmtParserException;
import com.jatinst.money.visualizer.model.RawTransactionMessage;
import com.jatinst.money.visualizer.reader.api.RawTransFileReader;
import com.jatinst.money.visualizer.reader.api.RawTransactionListener;
import com.jatinst.money.visualizer.reader.api.TransactionParser;

/**
 * Uses the Apache PDFBox, most code is based on http://thottingal.in/blog/2009/06/24/pdfbox-extract-text-from-pdf/ as
 * well as other tutorials The logic to read bank statements is custom
 */
public class PDFStmtReader implements RawTransFileReader {
	private static final Logger logger = Logger.getLogger(PDFStmtReader.class);

	//Using composition here, the parser can change as needed
	private TransactionParser transParser = new WellsFargoParser();
	
	@Override
	public List<RawTransactionMessage> loadTransactions(String filePath) throws ReaderException, StmtParserException {

		File file = new File(filePath);
		if (!file.isFile()) {
			logger.error("File " + filePath + " does not exist.");
			throw new ReaderException("File " + filePath + " does not exist.");
		}

		PDFParser parser;
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		List<String> pageTexts = new ArrayList<String>();
		try {
			parser = new PDFParser(new FileInputStream(file));
		} catch (IOException e) {
			logger.error("Unable to open PDF Parser for file " + filePath, e);
			throw new ReaderException("Unable to open PDF Parser for file " + filePath, e);
		}
		try {
			/**
			 * So, the assumption here is that the file is not going to be so large that we run out of memory. I could
			 * obviously optimize things, but the Internet seems to suggest that PDfBox can handle large documents fine.
			 * And how large can a Bank statement be? (Hope this does not come back to bite me!)
			 */
			parser.parse();
			cosDoc = parser.getDocument();
			pdfStripper = new PDFTextStripper();
			pdDoc = new PDDocument(cosDoc);

			int numPages = pdDoc.getNumberOfPages();
			/**
			 * Not sure if reading page by page helps memory at all, but it does make it easier to deal with things
			 * later
			 */
			for (int i = 1; i <= numPages; i++) {
				pdfStripper.setStartPage(i);
				pdfStripper.setEndPage(i);
				String pageText = pdfStripper.getText(pdDoc);
				pageTexts.add(pageText);
			}
		} catch (Exception e) {
			logger.error("An exception occured in parsing the PDF Document.", e);
			throw new ReaderException("An exception occured in parsing the PDF Document.", e);
		} finally {
			try {
				if (cosDoc != null)
					cosDoc.close();
				if (pdDoc != null)
					pdDoc.close();
			} catch (Exception e) {
				logger.warn("Error in closing PDF objects", e);
			}
		}
		return transParser.parseTransactions(pageTexts);
	}

	@Override
	public void loadTransactions(String filePath, RawTransactionListener listener) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws Exception {
		String testFile = "src/test/resources/jan-2014.pdf";

		logger.info("Trying to read file : " + testFile);
		/*
		 * //String pdfText = pdfToText(testFile);
		 * 
		 * logger.info("Got back the text from pdf as:"); logger.info(pdfText);
		 */

		PDFStmtReader reader = new PDFStmtReader();

		reader.loadTransactions(testFile);
		
		//this is ugly, but I am just testing...
/*		String[] args1 = new String[] {"-html", "src/test/resources/jan-2014.pdf", "1.html"};
		ExtractText.main(args1);*/
	}

	@SuppressWarnings("unused")
	private static String pdfToText(String fileName) {
		PDFParser parser;
		String parsedText = null;

		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		File file = new File(fileName);
		if (!file.isFile()) {
			System.err.println("File " + fileName + " does not exist.");
			return null;
		}
		try {
			parser = new PDFParser(new FileInputStream(file));
		} catch (IOException e) {
			System.err.println("Unable to open PDF Parser. " + e.getMessage());
			return null;
		}
		try {
			parser.parse();
			cosDoc = parser.getDocument();
			pdfStripper = new PDFTextStripper();
			pdDoc = new PDDocument(cosDoc);
			pdfStripper.setStartPage(1);
			pdfStripper.setEndPage(2);
			parsedText = pdfStripper.getText(pdDoc);
		} catch (Exception e) {
			System.err.println("An exception occured in parsing the PDF Document." + e.getMessage());
		} finally {
			try {
				if (cosDoc != null)
					cosDoc.close();
				if (pdDoc != null)
					pdDoc.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return parsedText;

	}

}
