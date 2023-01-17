package com.speno.xmon.agent;

import java.util.Hashtable;

public interface ILogger {
	
	public static String LINE = System.getProperty("line.separator");
	
	public void init(Hashtable attrs) throws Exception;
	
	public void debug(String message);
	public void error(String message);
	public void warn(String message);
	public void info(String message);
	
//	public void info(String defMsg, String ins1, String ins2);
//	public void debug(String defMsg, String ins1, String ins2);
//	public void error(String defMsg, String ins1, String ins2);

}
