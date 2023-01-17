package com.speno.xmon.aggregate.builder;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class AggregatedActionItem {
	
	private int successCount 			= 0;
	private int errorCount				= 0;
	private int timeOutCount			= 0;
	
	private int responseMaxTime		= 0;
	private int responseMinTime		= 0;
	private int responseAvgTime		= 0;
	
	private String agentName 			= "";
	private String actionName			= "";
	private String aggregatedTime	= "";
	
	//private final static Logger LOG = LoggerFactory.getLogger(AggregatedActionItem.class);
	
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
		return this.successCount;
	}
	public int GetErrorCount() {
		return this.errorCount;
	}
	public int GetTimeOutCount() {
		return this.timeOutCount;
	}

	public int GetResponseAvgTime() {
		return this.responseAvgTime;
	}
	public int GetResponseMaxTime() {
		return this.responseMaxTime;
	}
	public int GetResponseMinTime() {
		return this.responseMinTime;
	}
	
	public void SetAggregatedTime(String AggregatedTime) {
		this.aggregatedTime = AggregatedTime;
	}
	public String GetAggregatedTime() {
		return this.aggregatedTime;
	}
	public void SetSuccessCount(int sucCnt) {
		this.successCount = sucCnt;
	}
	public void SetErrorCount(int errCnt) {
		this.errorCount = errCnt;
	}
	
	public void setTimeOutCount(int timeOutCount) {
		this.timeOutCount = timeOutCount;
	}
	public void SetResponseAvgTime(int avgTime) {
		this.responseAvgTime = avgTime;
		
	}
	public void SetResponseMinTime(int minTime) {
		this.responseMinTime = minTime;
		
	}
	public void SetResponseMaxTime(int maxTime) {
		this.responseMaxTime = maxTime;
		
	}
	public void Clear() {
		this.successCount 		= 0;
		this.errorCount				= 0;
		this.timeOutCount			= 0;
		
		this.responseMaxTime	= 0;
		this.responseMinTime	= 0;
		this.responseAvgTime	= 0;
		
		this.agentName 		= "";
		this.actionName			= "";
		this.aggregatedTime	= "";		
	}

}
