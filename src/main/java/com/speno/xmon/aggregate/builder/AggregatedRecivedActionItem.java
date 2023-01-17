package com.speno.xmon.aggregate.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregatedRecivedActionItem {
	
	private AtomicInteger successCount 			= new AtomicInteger( 0 );
	private AtomicInteger errorCount					= new AtomicInteger( 0 );
	private AtomicInteger timeOutCount			= new AtomicInteger( 0 );
	
	private AtomicInteger responseSumTime		= new AtomicInteger( 0 );
	private AtomicInteger responseMaxTime		= new AtomicInteger( 0 );
	private AtomicInteger responseMinTime		= new AtomicInteger( 0 );
	
	
	private String agentName 			= "";
	private String actionName			= "";
	private String aggregatedTime	= "";
	private List<String> transIDs 		= new ArrayList<String>();
	
	private final static Logger LOG = LoggerFactory.getLogger(AggregatedRecivedActionItem.class);
	
	synchronized public long SuccessCount_Increment(long TransInitTime, long CompleteTime) {

		long longResponseEachTime =  this.GetResponseTime( TransInitTime, CompleteTime);
		if(longResponseEachTime < 1)
		{
			LOG.debug("Error Response  TransInitTime:" + TransInitTime + ", CompleteTime:" + CompleteTime );
		}
		int responseEachTime = (int)longResponseEachTime;
		if(responseMaxTime.get() <=  responseEachTime)
			this.responseMaxTime.set(responseEachTime);
		
		if(responseMinTime.get() == 0) this.responseMinTime.set(responseEachTime);
		
		if(responseMinTime.get() >  responseEachTime)
			this.responseMinTime.set(responseEachTime); 
		
		this.responseSumTime.getAndAdd(responseEachTime);
		this.successCount.getAndIncrement();
		return longResponseEachTime;
		/*
		LOG.debug("TransInitTime:" + TransInitTime 
		                   + ", CompleteTime:" +CompleteTime 
		                   + ", responseEachTime:" + responseEachTime);
		*/
		
	}
	synchronized private long GetResponseTime(long initTime, long completeTime) 
	{
		return completeTime - initTime; 
	}
	public void ErrorCount_incrument() {
		this.errorCount.getAndIncrement();
	}
	public void TimeoutCount_incrument() {
		this.timeOutCount.getAndIncrement();
	}
	public List<String> getTransID() {
		return transIDs;
	}
	public void SetTransID(String transID) {
		synchronized(this.transIDs)
		{
			this.transIDs.add(transID);
		}
	}
	public String GetAgentName() {
		return this.agentName;
	}
	public void SetAgentName(String AgentName) {
		this.agentName = AgentName;
	}
	public String GetActionName() {		
		return this.actionName;
	}
	public void SetActionName(String ActionName) {		
		this.actionName = ActionName;
	}
	public int GetSuccessCount() {
		return this.successCount.get();
	}
	public int GetErrorCount() {
		return this.errorCount.get();
	}
	public int GetTimeOutCount() {
		return this.timeOutCount.get();
	}

	public int GetResponseAvgTime() {
		return this.responseSumTime.get() / this.successCount.get();
	}
	public int GetResponseMaxTime() {
		return this.responseMaxTime.get();
	}
	public int GetResponseMinTime() {
		return this.responseMinTime.get();
	}
	
	public void SetAggregatedTime(String AggregatedTime) {
		this.aggregatedTime = AggregatedTime;
	}
	public String GetAggregatedTime() {
		return this.aggregatedTime;
	}

}
