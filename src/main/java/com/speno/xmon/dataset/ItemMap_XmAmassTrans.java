package com.speno.xmon.dataset;

import java.util.Date;
//import java.util.concurrent.atomic.AtomicInteger;



import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.comm.SimDate;

/*
 * Agent 에서 받은 데이타를 저장
 */
public class ItemMap_XmAmassTrans {

	public static final long DELAYTIME_TO_INSERT = 1 * 1000L;
	
	private String AgentName;
	private String ActionName;
	private String AggreServerTime;
	private String TransID;
	private long TransInitTime;
	private long TransCompleteTime = 0L;
	private boolean SuccessFlag	= false;
	private boolean DataPack 		= false;
	private long timeToInsert = 0L;
	private String ext1 = "";
	public String getExt1() {
		return ext1;
	}

	public String getExt2() {
		return ext2;
	}
	private String ext2 = "";
	private String result = DicOrderAdd.AmassSufFix_ActionCompTimeout;
    
    public ItemMap_XmAmassTrans(String AgentName
    											, String ActionName
    											, String TransID
    											, long TransInitTime
    											)
    {
    	this.AgentName 			= AgentName;
    	this.ActionName			= ActionName;
    	this.AggreServerTime		= new SimDate().getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis()));	
    	this.TransID					= TransID;
    	this.TransInitTime			= TransInitTime;
    	this.TransCompleteTime	= 0;
    	this.DataPack = false;
    }

	public ItemMap_XmAmassTrans(String AgentName, String ActionName,
			String TransID, long TransInitTime,String ext1, String ext2) {
		this.AgentName = AgentName;
		this.ActionName = ActionName;
		this.AggreServerTime = new SimDate().getDateTimeFormatter_MS()
				.format(new Date(System.currentTimeMillis()));
		this.TransID = TransID;
		this.TransInitTime = TransInitTime;
		this.TransCompleteTime = 0;
		this.DataPack = false;
		this.ext1 = ext1;
		this.ext2 = ext2;
	}
    public boolean Clean(){
    	this.AgentName 			= "";
    	this.ActionName			= "";
    	this.AggreServerTime		= "";
    	this.TransID					= "";
    	this.TransInitTime			= 0;
    	this.TransCompleteTime	= 0;
    	return true;
    }
    
	public boolean ValidInit() {		
		return DataPack;
	}
	public boolean ValidTrans(String transID) {
		if(this.SuccessFlag) return false;
		if(this.TransID.equals(transID)) return true;
		return false;
	}
	
	public void setActionName(String actionName) {
		ActionName = actionName;
	}
	
	public void setTransInitTime(long transInitTime) {
		TransInitTime = transInitTime;
	}
	public String GetAgentName()				{ return this.AgentName        	;}
	public String GetActionName()				{ return this.ActionName        	;}
	public String GetAggreServerTime()		{ return this.AggreServerTime   	;}
	public String GetTransID()						{ return this.TransID           		;}
	public long GetTransInitTime()				{ return this.TransInitTime     		;}
	public long GetTransCompleteTime()		{ return this.TransCompleteTime	;}
	public boolean GetSuccessFlag()			{ return this.SuccessFlag  	;} 
	
	public void SetTransCompleteTime(long TransCompleteTime, boolean SuccessFlag) {
		this.TransCompleteTime	= TransCompleteTime;
		this.SuccessFlag = SuccessFlag;
		this.DataPack = true;
	}
	public String GetErrorCode() {
		return null;
	}
	public String GetErrorMessage() {
		return null;
	}
	public long getTimeToInsert() {
		return timeToInsert;
	}
	public void setTimeToInsert(long timeToInsert) {
		this.timeToInsert = timeToInsert;
	}
	public void setModifiedTimeToInsert(long timeToInsert) {
		int seconds = (int)(timeToInsert / 1000L);
		this.timeToInsert = seconds * 1000L;
	}
	
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	// default 1초 뒤
	public void setTimeToInsert() {
		this.timeToInsert = System.currentTimeMillis() + DELAYTIME_TO_INSERT;
	}
	@Override
	public String toString() {
		return "ItemMap_XmAmassTrans [AgentName=" + AgentName + ", ActionName="
				+ ActionName + ", TransID=" + TransID + ", TransInitTime="
				+ TransInitTime + ", TransCompleteTime=" + TransCompleteTime
				+ ", SuccessFlag=" + SuccessFlag + ", timeToInsert="
				+ timeToInsert + "]";
	}
	
	
}


//MapItem_AmassTrans
/*
	private Queue_XmAmassTrans getOrCreate(String id) {
		records.containsKey()
		
		Queue_XmAmassTrans rec = records.get(id);
	    if (rec == null) {
	        // record does not yet exist
	    	Queue_XmAmassTrans newRec = new Queue_XmAmassTrans(id);
	        rec = records.putIfAbsent(id, newRec);
	        if (rec == null) {
	            // put succeeded, use new value
	            rec = newRec;
	        }
	    }
	    return rec;
	} 
*/
