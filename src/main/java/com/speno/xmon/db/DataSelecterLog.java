package com.speno.xmon.db;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.codedic.DicOrderTypes;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.db.collectLog.Tail;
import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.listener.CommandServerRequestBuilder;

/**
 * 20141031, junsoo, log4j로 바꿔서 당일 것만 조회됨.
 * @author speno
 *
 */
public class DataSelecterLog {

	private SimDate sd = null;
	private final String agentName;
	private final String commandName;
	
	private final String logId;
	private final int getLineCnt;
	
	private final String perUnit;
	private final List<String> aggreUnit;
	
	private final String aggreUnitMix;
	
	private String rangeStart;
	private String rangeEnd;
	private String consoleId;
	

	
    public DataSelecterLog(CommandServerRequestBuilder builder) 
    {
    	this.sd 						= new SimDate();
    	this.agentName 		= builder.getAgentName();
    	this.commandName	= builder.getCommandName();
    	//this.actionName			= builder.getActionName();
        
    	this.perUnit 				= builder.getPerUnit();
    	this.aggreUnit			= builder.getAggreUnit();
        this.rangeStart 			= builder.getRangeStart();
        this.rangeEnd 			= builder.getRangeEnd();
        this.consoleId			= builder.getConsoleId();
        
        this.logId					= builder.getAggreUnit().get(0);
        this.getLineCnt			= Integer.parseInt(builder.getValueUnit());
        
    	this.aggreUnitMix		= builder.GetMixOfAggreUnit(this.aggreUnit);
	}
	public String Select_LogText(String remoteAddress)  
	{
		JSONObject jsonObjectResponse 		= new JSONObject();
	  	JSONObject jsonObjectSubResponse = null;
	  	JSONArray jsonArrayResponse 			= new JSONArray();
	  	
		String logName = xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.LogsFolder) + File.separator +  this.agentName + "_" + this.logId + ".log";
		String logKey = remoteAddress + xmPropertiesXml.Sep + logName;

		File fileLog		= new File(logName);
    	String ReturnMsg = "";
    	int ReturnCode =0;
		try 
		{
			if(!fileLog.exists()) {
				jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "LogFile Not Found:" + logName);
			  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, "-2");
			}

			Tail tempTail  = null;
			if( xmPropertiesXml.chmConsoleLogTail.containsKey(logKey)) {
				tempTail =  xmPropertiesXml.chmConsoleLogTail.get(logKey);
			}
			else {
				tempTail = new Tail( logName, this.logId + "_" + this.rangeStart);
				if(tempTail.bFileExists){
					tempTail.bSystemOut = false;
					xmPropertiesXml.chmConsoleLogTail.put(logKey, tempTail);
				}
				else {
					ReturnMsg = "cannot find the file";
					ReturnCode =  -5;
				}
			}
			
			if (tempTail == null)
				return "";
			
			if(this.getLineCnt == -1)
			{
				tempTail.stopTail();
				xmPropertiesXml.chmConsoleLogTail.remove(logKey, tempTail);				
				return "";
			}
			
			String Line = 	tempTail.getQueueLogFileLine(this.getLineCnt);

			if(Line==null || Line.equals(""))
			{
				if(ReturnCode != -5)
				{
					return "";
				}
			}
	    	
		  	jsonObjectResponse.put(DicOrderAdd.AgentName, this.agentName);
		  	jsonObjectResponse.put(DicOrderAdd.SendTime, sd.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis())));	        
		  	
		  	jsonObjectResponse.put(DicOrderTypes.OrderType,	DicOrderTypes.ResponseStats);
		  	jsonObjectResponse.put(DicOrderAdd.Command, 	this.commandName);
		  	jsonObjectResponse.put(DicOrderAdd.AggreUnit, 	this.aggreUnitMix);
		  	jsonObjectResponse.put(DicOrderAdd.PerUnit, 		this.perUnit);
		  	
		  	jsonObjectResponse.put(DicOrderAdd.RangeStart,	this.rangeStart);
		  	jsonObjectResponse.put(DicOrderAdd.RangeEnd, 	this.rangeEnd);
		  	jsonObjectResponse.put(DicOrderAdd.ConsoleId, 	this.consoleId);
 
			jsonObjectSubResponse 	= new JSONObject();
			jsonObjectSubResponse.put("USE", Line);
			jsonArrayResponse.put(jsonObjectSubResponse);
	    	
			jsonObjectResponse.put(DicOrderTypes.ResponseStats, jsonArrayResponse);
			
		  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, ReturnMsg);
		  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, ReturnCode);
		  	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		} 
		finally
		{
		}
	
		return jsonObjectResponse.toString();
	}
}
