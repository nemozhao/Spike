package org.spike;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SpikeLoggerFormatter extends Formatter {

	private Date dat = new Date();
	private final static String dateformat = "{0,date,short} {0,time}";
	private Object args[] = new Object[1];

	private MessageFormat formatter;

	@Override
	public String format(LogRecord pRecord) {
		StringBuilder sb = new StringBuilder();
		// Minimize memory allocations here.
		dat.setTime(pRecord.getMillis());
		args[0] = dat;
		StringBuffer text = new StringBuffer();
		if (formatter == null) {
			formatter = new MessageFormat(dateformat);
		}
		formatter.format(args, text, null);
		sb.append(text);
		sb.append(" - ");
		sb.append(pRecord.getLevel().getLocalizedName());
		sb.append(": ");
		sb.append(formatMessage(pRecord));
		sb.append("\n");
		if (pRecord.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				pRecord.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}
}
