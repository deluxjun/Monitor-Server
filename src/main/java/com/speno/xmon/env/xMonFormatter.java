package com.speno.xmon.env;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

 public  class xMonFormatter extends Formatter {
	private String message;
	public xMonFormatter(String message)
	{
		this.message = message;
	}

	@Override
	public String format(LogRecord record) {
		return message + ":" + record.getMessage() + "\n";
	}

}
