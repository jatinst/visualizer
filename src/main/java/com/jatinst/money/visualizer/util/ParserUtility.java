package com.jatinst.money.visualizer.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jatinst.money.visualizer.exception.StmtParserException;

public class ParserUtility {

	// TODO - add new exception just for utils
	public static Date getDateFromMMSlashDD(String dateStr, String fourdigitYear) throws StmtParserException {
		SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
		if (dateStr == null || fourdigitYear == null) {
			throw new StmtParserException("Date argument cannot be null");
		}
		String dateStrFull = dateStr.trim() + "/" + fourdigitYear.trim();
		try {
			return sdf.parse(dateStrFull);
		} catch (ParseException e) {
			throw new StmtParserException("Could not create Date from passed arguments " + dateStr + " and "
					+ fourdigitYear, e);
		}
	}
}
