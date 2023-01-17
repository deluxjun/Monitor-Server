package com.speno.xmon.dataset;

import java.util.Date;
import java.util.HashMap;

import com.speno.xmon.agent.ILogger;
import com.speno.xmon.agent.LoggerDummy;
import com.speno.xmon.comm.SimDate;

public class ItemAmassTrans {	
	
	private final static ILogger LOG = new LoggerDummy();
	
	/*
	 * Agent에서 집계 서버로 보내기 전 담는 Queue Item
	 */

	private String tranjectionID;
	private String CommandFull;
	private String actionName;
	private Long  longTransDateTime; 
	private String errorCode;
    private String errorMessage;       
    
	private String putDateTime;  //Agent 에서 Put 받은 시각
	private HashMap<String,String> attributeExt;
	
	public ItemAmassTrans(String tranjectionID
									, String CommandFull
									, String actionName
									, long longTransDateTime
									, String errorCode
									, String errorMessage
									, HashMap<String,String> m)
    {
    	this.tranjectionID			= tranjectionID;
    	this.CommandFull			= CommandFull;
    	this.actionName				= actionName;
    	this.longTransDateTime	= longTransDateTime;
    	this.errorCode				= errorCode;
    	this.errorMessage			= errorMessage;
    	
    	this.putDateTime			= SimDate.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis()));
    	
    	this.attributeExt					= m;
    }

	public void Print() {
		String temp = "--------------------------------------------------------------------------\n" 
				        + "tranjectionID\t\t\t:"				+ this.tranjectionID		+ "\n"
				        + "Command\t:"						+ this.CommandFull	+ "\n"
						+ "actionName\t:"					+ this.actionName		+ "\n"
						+ "longTransDateTime\t\t\t:"	+ this.longTransDateTime	+ "\n"
						+ "errorCode\t\t:"					+ this.errorCode			+ "\n"
						+ "errorMessage\t\t\t\t:"			+ this.errorMessage	+ "\n"
						+ "putDateTime\t\t\t\t:"			+ this.putDateTime		+ "\n"
						+ "--------------------------------------------------------------------------";
		LOG.info(temp);
	}
	public String getCommand() {
		return this.CommandFull;
	}
	public String getActionName() {
		return this.actionName;
	}
	public String getTransactionID() {
		return this.tranjectionID;
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
	
	public HashMap<String, String> getattributeExt() {
		return this.attributeExt;
	}
}

