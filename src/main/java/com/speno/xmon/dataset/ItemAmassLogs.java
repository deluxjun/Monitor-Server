package com.speno.xmon.dataset;

import java.util.Date;

import com.speno.xmon.agent.ILogger;
import com.speno.xmon.agent.LoggerDummy;
import com.speno.xmon.comm.SimDate;

public class ItemAmassLogs {	
	
	private final static ILogger LOG = new LoggerDummy();
	
	/*
	 * Agent에서 집계 서버로 보내기 전 담는 Queue Item
	 */
	private String logID;
	private String actionName;
	private String logText;
	private Long  longTransDateTime; 
	private String errorCode;
    private String errorMessage;       
    
	private String putDateTime;  //Agent 에서 Put 받은 시각
	 

	
	//logID, logText, currentTimeMillis, errorMessage, errorCode
	public ItemAmassLogs(String logID
									   , String actionName
									   , String logText
									   , long longTransDateTime
									   , String errorCode
									   , String errorMessage)
    {
    	this.logID						= logID;
    	this.actionName				= actionName;
    	this.logText					= logText;        
    	this.longTransDateTime	= longTransDateTime;
    	this.errorCode				= errorCode;
    	this.errorMessage			= errorMessage;
    	
    	this.putDateTime			= SimDate.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis()));
    }

	public void Print() {
		String temp = "--------------------------------------------------------------------------\n" 
				        + "logID\t\t\t:"							+ this.logID					+ "\n"                
						+ "actionName\t:"					+ this.actionName			+ "\n"
						+ "logText\t\t\t:"						+ this.logText					+ "\n"
						+ "errorCode\t\t:"					+ this.errorCode				+ "\n"
						+ "errorMessage\t\t\t\t:"			+ this.errorMessage		+ "\n"
						+ "putDateTime\t\t\t\t:"			+ this.putDateTime			+ "\n"
			
						+ "--------------------------------------------------------------------------";
		//info 에서 레벨 낮춤 YYS
		LOG.info(temp);
	
	}

	public String getLogID() {
		return this.logID;
	}
	public String getCommand() {
		return this.actionName;
	}
	public String getLogText() {
		return this.logText;
	}
	public long getLongTransTime() {
		return this.longTransDateTime;
	}
	public String getReturnCode() {
		return this.errorCode;
	}

	public String getReturnMsg() {
		return this.errorMessage;
	}

	public String getTransTime() {
		return this.putDateTime;
	}
}

