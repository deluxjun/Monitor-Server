package com.speno.xmon.aggregate.builder;

import java.util.concurrent.atomic.AtomicLong;

public class AggregatedRecivedResourceItem {
	
	private AtomicLong valueLong 	= new AtomicLong( 0 );

	private String agentName 			= "";
	private String commandName	= "";
	private String resourceID	= "";
	private String propertyName		= "";
	private String aggregatedTime	= "";
	private String valueUnit				= "";
	private String extMapToString	= "";
	//synchronized 
	
	public AggregatedRecivedResourceItem(String agentName) {
		this.agentName = agentName;
	}
	public long GetValueLong() {
		return this.valueLong.get();
	}
	public void SetValueLong(long val) {
		this.valueLong.set(val);
	}
	public String GetAgentName() {
		return this.agentName;
	}
	public void SetAggregatedTime(String AggregatedTime) {
		this.aggregatedTime = AggregatedTime;
	}
	public String GetAggregatedTime() {
		return this.aggregatedTime;
	}
	public void SetCommandName(String cmd) {
		this.commandName = cmd;
	}
	public String GetCommandName() {
		return this.commandName ;
	}
	public String GetResourceID() {
		return this.resourceID ;
	}
	public void SetResourceID(String resourceID) {
		this.resourceID  = resourceID;
	}

	public void SetPropertyName(String subcmd) {
		this.propertyName = subcmd;
	}
	public String GetPropertyName() {
		return this.propertyName;
	}
	public void SetValueUnit(String valueUnit) {
		this.valueUnit = valueUnit;
	}
	public String GetValueUnit() {
		return this.valueUnit;
	}
	public String GetExtMapToString(){
		return this.extMapToString;
	}
	public void SetExtMap(String extMapToString) {
		this.extMapToString = extMapToString;
	}


}
