package com.speno.xmon.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.comm.SimDate;

public class CommandServerRequestBuilder {	
	
	private SimDate sd = null;
	public final static String perUnitSec	= "SEC";
	public final static String perUnitMin	= "MIN";
	public final static String perUnitHour	= "HOUR";
	public final static String perUnitDay	= "DAY";
	
	/*
	 * aggreUnit 
	 */
	public final static String aggreUnitAvg	= "Avg";
	public final static String aggreUnitMin	= "Min";
	public final static String aggreUnitMax	= "Max";
	
	public final static String aggreUnitCnt	= "Cnt";
	public final static String aggreUnitSum	= "Sum";
	
	public final static String aggreUnitSuc	= "Success";// "Suc"; //Success
	public final static String aggreUnitErr	= "Error";
	public final static String aggreUnitOut	= "Out";
	
	
	private final String orderType;
	private final String agentName;
	private final String commandName;
	private final String resourcdID;
	private String propertyName;
	private final String actionName;
	private final String perUnit;
	private final String valueUnit;
	private final List<String> aggreUnit;
	private final String rangeStart;
	private final String rangeEnd;
	private final String consoleId;
	
	private String receivedDateTime;
	
	public CommandServerRequestBuilder(Builder builder)
	{
		this.sd 						= new SimDate();
    	this.orderType			= builder.orderType;
    	this.agentName 		= builder.agentName;
    	this.commandName	= builder.commandName;
    	this.resourcdID			= builder.resourcdID;
    	this.propertyName			= builder.propertyName;
    	this.actionName			= builder.actionName;
        
    	this.perUnit 				= builder.perUnit;
    	this.valueUnit				= builder.valueUnit;
    	this.aggreUnit			= builder.aggreUnit;
        this.rangeStart 			= builder.rangeStart;
        this.rangeEnd 			= builder.rangeEnd;
        this.consoleId			= builder.consoleId;
        
        this.receivedDateTime = sd.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis()));
	}
	public String getOrderType() {
		return orderType;
	}
	public String getAgentName() {
		return agentName;
	}
	public String getCommandName() {
		return commandName;
	}
	public String getResourcdID() {
		return resourcdID;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	public String getActionName() {
		return actionName;
	}
	public String getPerUnit() {
		return perUnit;
	}
	public String getValueUnit() {
		return valueUnit;
	}
	public List<String> getAggreUnit() {
		return aggreUnit;
	}
	public String getRangeStart() {
		return rangeStart;
	}
	public String getRangeEnd() {
		return rangeEnd;
	}
	public String getReceivedDateTime() {
		return receivedDateTime;
	}
	public String getConsoleId(){
		return consoleId;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}


	public static  class Builder
	{
		private String orderType;
		private String agentName;
		
		private String commandName;
		private String resourcdID;
		private String propertyName;
		
		private String actionName;
		
		private String perUnit;
		private String valueUnit;
		private List<String> aggreUnit;
		private String rangeStart;
		private String rangeEnd;
		private String consoleId;
		
	    public Builder( ) {}
		public Builder SetAgentName(String agentName) {
			this.agentName = agentName;
			return this;
		}
	    public Builder SetOrderType( String orderType )
	    {
	        this.orderType = orderType;
	        return this;
	    }
	    public Builder SetCommandName( String commandName )
	    {
	        this.commandName = commandName;
	        return this;
	    }
	    public Builder SetResourcdID( String resourcdID )
	    {
	        this.resourcdID = resourcdID;
	        return this;
	    }
	    
	    public void setPropertyName(String propertyName) {
			this.propertyName = propertyName;
		}
		public Builder SetActionName( String commandName )
	    {
	    	this.actionName	= commandName;
	    	String[] temp 		= commandName.split("_");
	    	if(temp.length == 3) this.actionName = temp[2];
	    	/*
	    	if(commandName.endsWith(DicKeysOrderAde.Amass_ActionType_Inquiry))
	    		this.actionName = DicKeysOrderAde.Amass_ActionType_Inquiry;
	    	
	    	else if(commandName.endsWith(DicKeysOrderAde.Amass_ActionType_Register))
	    		this.actionName = DicKeysOrderAde.Amass_ActionType_Register;
	    	*/
	        return this;
	    }
	    public Builder SetPerUnit( String perUnit )
	    {
	        this.perUnit = perUnit;
	        return this;
	    }
		public Builder SetAggreUnit(String[] aggreUnit) {
			this.aggreUnit = new ArrayList<String>();
			 for(int i =0; i< aggreUnit.length; i++)
			 {
				 this.aggreUnit.add(aggreUnit[i].trim());
			 }
			return this;
		}
	    public Builder SetValueUnit( String valueUnit )
	    {
	        this.valueUnit = valueUnit;
	        return this;
	    }
		
	    public Builder SetRangeStart( String rangeStart )
	    {
	        this.rangeStart = rangeStart;
	        return this;
	    }
	    public Builder SetRangeEnd( String rangeEnd )
	    {
	        this.rangeEnd = rangeEnd;
	        return this;
	    }
		public Builder SetConsoleId(String consoleId) {
			this.consoleId = consoleId;
			return this;
		}
	    public CommandServerRequestBuilder build(  )
	    {
	        return new CommandServerRequestBuilder(this );	            
	    }

	}

	/*
	 * *********** PerUnit 으로 TableName 가져오기 **************
	 */
	public static String GetTableNameOfPerUnit(String amassPreFix, String perUnit) {
		String tableName = "";
		String tempPerUnit = perUnit.trim().toUpperCase();
		
		if(amassPreFix.equals(DicOrderAdd.AmassPreFix_Trans))
		{
			if(tempPerUnit.equals(perUnitSec))
			{
				tableName = " XmAggregatedActionSec ";
			}
			else if(tempPerUnit.equals(perUnitMin))
			{
				tableName = " XmAggregatedActionMin ";
			} 
			else if(tempPerUnit.equals(perUnitHour))
			{
				tableName = " XmAggregatedActionHour ";
			}
			else if(tempPerUnit.equals(perUnitDay))
			{
				tableName = " XmAggregatedActionDay ";
			}
		}
		else	if(amassPreFix.equals(DicOrderAdd.AmassPreFix_Resource))
		{
			if(tempPerUnit.equals(perUnitSec))
			{
				tableName = " XmAggregatedResourceSec ";
			}
			else if(tempPerUnit.equals(perUnitMin))
			{
				tableName = " XmAggregatedResourceMin ";
			} 
			else if(tempPerUnit.equals(perUnitHour))
			{
				tableName = " XmAggregatedResourceHour ";
			}
			else if(tempPerUnit.equals(perUnitDay))
			{
				tableName = " XmAggregatedResourceDay ";
			}
		}
		
		return tableName;
	}
	/*
	 * *********** AggreUnit 으로 컬럼명 가져오기 **************
	 */
	public static List<String> GetColumnNameOfAggreUnit(String amassPreFix, List<String> aggreUnit) {
		List<String> ListAggreUnit = new ArrayList<String>();
		if(amassPreFix.equals(DicOrderAdd.AmassPreFix_Trans))
		{
			if(aggreUnit.contains(aggreUnitAvg))
			{
				ListAggreUnit.add(" ResponseAvgTime as avg  ");
			}
			if(aggreUnit.contains(aggreUnitMin))
			{
				ListAggreUnit.add(" ResponseMinTime as min ");
			}
			if(aggreUnit.contains(aggreUnitMax))
			{
				ListAggreUnit.add(" ResponseMaxTime as max ");
			}
			if(aggreUnit.contains(aggreUnitSuc))
			{
				ListAggreUnit.add(" SuccessCount as Success  ");
			}
			if(aggreUnit.contains(aggreUnitErr))
			{
				ListAggreUnit.add(" ErrorCount  as	Error ");
			}
			if(aggreUnit.contains(aggreUnitOut))
			{
				ListAggreUnit.add(" TimeOutCount as	Out ");
			}
		}
		else if(amassPreFix.equals(DicOrderAdd.AmassPreFix_Resource))
		{
			if(aggreUnit.contains(aggreUnitAvg))
			{
				ListAggreUnit.add(" PropertyAvgValue  as avg  ");
			}
			if(aggreUnit.contains(aggreUnitMin))
			{
				ListAggreUnit.add(" PropertyMinValue as	 min ");
			}
			if(aggreUnit.contains(aggreUnitMax))
			{
				ListAggreUnit.add(" PropertyMaxValue as max ");
			}
			if(aggreUnit.contains(aggreUnitCnt))
			{
				ListAggreUnit.add(" count(*)           		 as		cnt  ");
			}
			if(aggreUnit.contains(aggreUnitSum))
			{
				ListAggreUnit.add(" sum(PropertyAvgValue) as	sum ");
			}
		}

		return ListAggreUnit;
	}
	
	public static String GetGroupBy(String amassPreFix, List<String> aggreUnit) {
		List<String> ListAggreUnit = new ArrayList<String>();
		if(amassPreFix.equals(DicOrderAdd.AmassPreFix_Trans))
		{
			if(aggreUnit.contains(aggreUnitAvg))
			{
				ListAggreUnit.add(" ResponseAvgTime ");
			}
			if(aggreUnit.contains(aggreUnitMin))
			{
				ListAggreUnit.add(" ResponseMinTime ");
			}
			if(aggreUnit.contains(aggreUnitMax))
			{
				ListAggreUnit.add(" ResponseMaxTime ");
			}
			if(aggreUnit.contains(aggreUnitSuc))
			{
				ListAggreUnit.add(" SuccessCount ");
			}
			if(aggreUnit.contains(aggreUnitErr))
			{
				ListAggreUnit.add(" ErrorCount ");
			}
			if(aggreUnit.contains(aggreUnitOut))
			{
				ListAggreUnit.add(" TimeOutCount ");
			}
		}
		else if(amassPreFix.equals(DicOrderAdd.AmassPreFix_Resource))
		{
			if(aggreUnit.contains(aggreUnitAvg))
			{
				ListAggreUnit.add(" PropertyAvgValue ");
			}
			if(aggreUnit.contains(aggreUnitMin))
			{
				ListAggreUnit.add(" PropertyMinValue ");
			}
			if(aggreUnit.contains(aggreUnitMax))
			{
				ListAggreUnit.add(" PropertyMaxValue ");
			}
		}
		
		StringBuffer returnStr = new StringBuffer();
		for (String string : ListAggreUnit) {
			returnStr.append(string + ",");
		}
		returnStr.setLength(returnStr.length()-1);

		return returnStr.toString();
	}
	
	public String GetMixOfAggreUnit(List<String> aggreUnit) {
		String ret = "";
		Iterator<String> iter = aggreUnit.iterator();
		while(iter.hasNext())
		{
			ret += iter.next();
			if(iter.hasNext()) ret += ",";
		}
		return ret;
	}
	public String GetConditionOfAggreUnit(List<String> aggreUnit2) {
		String ret = "";
		Iterator<String> iter = aggreUnit.iterator();
		while(iter.hasNext())
		{
			ret += "'"+ iter.next().trim() +"'";
			if(iter.hasNext()) ret += ",";
		}
		return ret;
	}



}


