package com.speno.xmon.dataset;

import java.util.Date;
import java.util.HashMap;

import com.speno.xmon.comm.SimDate;

public class ItemAmassEvent {

	//private final static Logger LOG = LoggerFactory.getLogger(ItemAmassEvent.class);
	
	/*
	 * Agent에서 집계 서버로 보내기 전 담는 Queue Item
	 */

	private String eventName;
	private String eventText;
	private int eventLevel;
	private Long  longEventTime; 
	private String errorCode;
    private String errorMessage;       
    
	private String putDateTime;  //Agent 에서 Put 받은 시각
	private HashMap<String,String> attributeExt;
	
	public ItemAmassEvent(String eventName
			                          , String eventText
			                          , int eventLevel
			                          , long eventDateTime
			                          , String errorCode
			                          , String errorMessage
			                          , HashMap<String, String> m) {
		
    	this.eventName			= eventName;
    	this.eventText			= eventText;
    	this.eventLevel			= eventLevel;
    	this.longEventTime		= eventDateTime;
    	this.errorCode			= errorCode;
    	this.errorMessage		= errorMessage;
    	
    	this.putDateTime		= SimDate.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis()));
    	
    	this.attributeExt			= m;
	}
	
	public String getEventName() {
		return this.eventName;
	}
	public String getEventText() {
		return this.eventText;
	}
	public int getEventLevel() {
		return this.eventLevel;
	}
	public long getLongEventDateTime() {
		return this.longEventTime;
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
