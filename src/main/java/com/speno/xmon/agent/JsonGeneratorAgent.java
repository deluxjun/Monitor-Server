package com.speno.xmon.agent;


import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.speno.xmon.codedic.DicCommands;
import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.codedic.DicOrderTypes;
import com.speno.xmon.codedic.DicValueUnit;
import com.speno.xmon.codedic.DicXmlProperties;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.dataset.ItemAmassEvent;
import com.speno.xmon.dataset.ItemAmassLogs;
import com.speno.xmon.dataset.ItemAmassTrans;
import com.speno.xmon.dataset.MapQue_Amass;

public class JsonGeneratorAgent implements ILogListener{
	private static ILogger LOG = new LoggerDummy();
	
	public void setLogger(ILogger log) {
		LOG = log;
	}
	
	public JsonGeneratorAgent() {
    	MainXAgent.addLogListener(this);
	}

	/*
	 * etc. command: 
	 * Req_Memory_USE ...
	 * Req_XvarmArchive^CAS01
	 * Req_XvarmArchive^CAS01^USE
	 * 
	 */
	public synchronized JSONArray resourceValue(JSONArray jsonArray, String command, String valueUnit, String[] propertyNames) 
	{
		
        JSONObject jsonSubObject 	= null;
        try {
        	//Req_JavaHeap&JavaHeap&USE
			String[] cmd = command.split(xmProperties.Sep);
			if(!(cmd.length >=2  && cmd.length <=3 ) ) 
			{
				LOG.error("command Error:" + command);
				return null;
			}

			IxMon mon = MainXAgent.getResource(cmd[0]);
			   
			jsonSubObject 	= new JSONObject();
			jsonSubObject.put(DicOrderAdd.Command, command);
	        jsonSubObject.put(DicValueUnit.ValueUnit, valueUnit);

			if(mon == null){
				jsonSubObject.put(DicOrderAdd.ReturnMsg, "Resource Null, Request CMD:" + command);
				jsonSubObject.put(DicOrderAdd.ReturnCode, -1);
				
				jsonArray.put(jsonSubObject);
				return jsonArray;
			}
	        
			jsonSubObject.put(DicOrderAdd.ReturnMsg, "");
			jsonSubObject.put(DicOrderAdd.ReturnCode, "0");
			
			long val =0;
			String extKey = "";
			
			for(int p=0; p<propertyNames.length;p++)
			{
				val  =  mon.getValue(cmd[1], propertyNames[p] );
				jsonSubObject.put(propertyNames[p], val );
				
				HashMap<String, String> extMap = mon.getExtMap();
				if(extMap == null) continue;
				Iterator<String> itr = extMap.keySet().iterator();
				
				while(itr.hasNext())
				{
					extKey = itr.next();
					if(extKey.equals("")) continue;
					jsonSubObject.put(DicOrderAdd.Exten  + extKey, extMap.get(extKey) );
				}
			}
			jsonArray.put(jsonSubObject);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonArray;
	}
	  
    /*
     * Agent API 의 Put 으로 들어온 Transaction RawQueue 데이타를  집계서버로 전송
     */
	public String ResponseAgentShort(String comdType) 
	{
	    JSONObject	jsonObject		= null;
		try {
			
			String sDateTime_Send = SimDate.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis()));
			jsonObject = new JSONObject();
			
			jsonObject.put(DicOrderAdd.AgentName, 	xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AgentMyName));
			jsonObject.put(DicOrderAdd.SendTime,		sDateTime_Send);
			jsonObject.put(DicOrderTypes.OrderType,	DicOrderTypes.ResponseAgentShort);
			
//			long start = System.currentTimeMillis();
			String json = "";
			if(comdType.equals(DicCommands.Trans_Unit))
			{
				jsonObject.put(DicOrderAdd.Command,	DicCommands.Trans_Unit);
				json = this.genQueueData_Trans(jsonObject);//, MapQue_AmassAction.QueAmass_Trans);
			}
			else if(comdType.equals(DicCommands.Log_Persist))
			{
				jsonObject.put(DicOrderAdd.Command,		DicCommands.Log_Persist);
				json = this.genLog_Persist(jsonObject);
			}
			else if(comdType.equals(DicCommands.Event_Level))
			{
				jsonObject.put(DicOrderAdd.Command,		DicCommands.Event_Level);
				json = this.genEvent_Level(jsonObject);
			}

//			LOG.debug("the time preparing send data : " + (System.currentTimeMillis()-start) + "ms");
			
			return json;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			LOG.error(e.getMessage());
			return "";
		}
	}
	synchronized private String genEvent_Level(JSONObject jsonOb_Agent) 
	{ 
	    JSONArray	jsonArray		= null;
	    JSONObject	jsonOb_Que	= null;
	    
		String notify = "Queue EventLevel";
		jsonArray   = new JSONArray();
		
		int CurrentGettingQueueCnt            	= 0;
		ItemAmassEvent queueItem 	= null;
    	try
		{
			String eventName			= "";
			String eventText				= "";
			int eventLevel					= 0;
			String eventDateTime		= "";
			String returnMsg 			= "";
			String returnCode			= "";
			
			while((queueItem = MapQue_Amass.putEvent.getEvent()) != null)
//			while(!MapQue_Amass.putEvent.isEmpty())
			{
//				if ( MapQue_Amass.putEvent.size() == 0)
//				{					
//					//LOG.info( notify + " 'size==0' Get Length :"  + CurrentGettingQueueCnt + "~ 0" );
//					CurrentGettingQueueCnt = 0;
//					break;
//				}
//
//				//보낼때 개수 제한
//				if ( Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.MaxJsonDataCount )) < CurrentGettingQueueCnt )
//				{ 
//					LOG.info(notify + " CurrentGettingCnt/CurrentQueueCnt: "  + CurrentGettingQueueCnt + " / " +MapQue_Amass.putEvent.size() );
//					CurrentGettingQueueCnt = 0;
//					break;
//				}
//				queueItem = MapQue_Amass.putEvent.getEvent();
//				if(queueItem == null)
//				{ 
//					LOG.error(notify + " queEventLevel is null " + MapQue_Amass.putEvent.size());
//					CurrentGettingQueueCnt = 0;
//					break;
//				}
//				else
//				{
//					CurrentGettingQueueCnt ++;
//				}

				CurrentGettingQueueCnt ++;

				/*
			 	this.eventName			= eventName;
		    	this.eventText			= eventText;
		    	this.eventLevel			= eventLevel;
		    	this.longEventTime		= transDateTime;
		    	this.errorCode			= errorCode;
		    	this.errorMessage		= errorMessage;
				*/
				
				eventName		= queueItem.getEventName();
				eventText			= queueItem.getEventText();
				eventLevel			= queueItem.getEventLevel();
				eventDateTime	= String.valueOf(queueItem.getLongEventDateTime());
				returnMsg 			= queueItem.getReturnMsg();
				returnCode			= queueItem.getReturnCode();
				
				if(eventText.equals("") || eventDateTime.equals("") || eventName.equals("")) continue;
				
				jsonOb_Que = new JSONObject();
				jsonOb_Que.put(DicOrderAdd.EventName,			eventName);
				jsonOb_Que.put(DicOrderAdd.EventText,			eventText);
				jsonOb_Que.put(DicOrderAdd.EventLevel,			eventLevel);
				
				jsonOb_Que.put(DicOrderAdd.EventDateTime,		eventDateTime);
				
				jsonOb_Que.put(DicOrderAdd.ReturnMsg,			returnMsg);
				jsonOb_Que.put(DicOrderAdd.ReturnCode,			returnCode);
				
				//LOG.info(("CurrentQueueCnt:" + CurrentGettingQueueCnt);
				jsonArray.put(jsonOb_Que);
			}//End While
			
			if(jsonArray.length() ==0) return "";
			else
			{
				//LOG.info(notify + " Get Length:"  + CurrentGettingQueueCnt + "~ Remain:" + MapQue_Amass.putEvent.size() );
				jsonOb_Agent.put(DicOrderTypes.ResponseAgentShort, jsonArray);    
				return jsonOb_Agent.toString();
			}
		}
		catch(Exception e)
		{
			LOG.error( "putEvent:" + MapQue_Amass.putEvent.size() );
		    e.printStackTrace();
		}
		return "";   	
	}

	synchronized private String genLog_Persist(JSONObject jsonOb_Agent )
	{ 
	    JSONArray	jsonArray		= null;
	    JSONObject	jsonOb_Que	= null;
	    
		String notify = "Queue Log";
		jsonArray   = new JSONArray();
		
		int CurrentGettingQueueCnt            	= 0;
		ItemAmassLogs queueItem 	= null;
    	try
		{
			String command				= "";
			String logID					= "";
			String logText					= "";
			String transDateTime		= "";
			String returnMsg 			= "";
			String returnCode			= "";
			
			while((queueItem = MapQue_Amass.putLogText.getLogText()) != null)
//			while(!MapQue_Amass.putLogText.isEmpty())
			{
//				if ( MapQue_Amass.putLogText.size() == 0)
//				{					
//					//LOG.info( notify + " 'size==0' Get Length :"  + CurrentGettingQueueCnt + "~ 0" );
//					CurrentGettingQueueCnt = 0;
//					break;
//				}
//
//				//보낼때 개수 제한
//				if ( Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.MaxJsonDataCount )) < CurrentGettingQueueCnt )
//				{ 
//					LOG.info(notify + " CurrentGettingCnt/CurrentQueueCnt: "  + CurrentGettingQueueCnt + " / " +MapQue_Amass.putLogText.size() );
//					CurrentGettingQueueCnt = 0;
//					break;
//				}
//				queueItem = MapQue_Amass.putLogText.getLogText();
//				if(queueItem == null)
//				{ 
//					LOG.error(notify + " queLogText is null " + MapQue_Amass.putLogText.size());
//					CurrentGettingQueueCnt = 0;
//					break;
//				}
//				else
//				{
//					CurrentGettingQueueCnt ++;
//				}
				CurrentGettingQueueCnt ++;
				
				command			= queueItem.getCommand();
				logID					= queueItem.getLogID();
				logText				= queueItem.getLogText();
				transDateTime	= String.valueOf(queueItem.getLongTransTime());
				returnMsg 			= queueItem.getReturnMsg();
				returnCode			= queueItem.getReturnCode();
				
				if(logID.equals("") || transDateTime.equals("") || command.equals("")) continue;

				jsonOb_Que = new JSONObject();
				jsonOb_Que.put(DicOrderAdd.Command,				command);
				jsonOb_Que.put(DicOrderAdd.LogID,			logID);
				jsonOb_Que.put(DicOrderAdd.LogText,		logText);
				jsonOb_Que.put(DicOrderAdd.TransDateTime,		transDateTime);
				
				jsonOb_Que.put(DicOrderAdd.ReturnMsg,			returnMsg);
				jsonOb_Que.put(DicOrderAdd.ReturnCode,			returnCode);
				
				//LOG.info("CurrentQueueCnt:" + CurrentGettingQueueCnt);
				jsonArray.put(jsonOb_Que);
			}//End While
			
			if(jsonArray.length() ==0) return "";
			else
			{
				//LOG.info(notify + " Get Length:"  + CurrentGettingQueueCnt + "~ Remain:" + MapQue_Amass.putLogText.size() );
				jsonOb_Agent.put(DicOrderTypes.ResponseAgentShort, jsonArray);    
				return jsonOb_Agent.toString();
			}
		}
		catch(Exception e)
		{
			LOG.error( "putLogText:" + MapQue_Amass.putLogText.size());
		    e.printStackTrace();
		}
		return "";   	
	}
	
	synchronized private String genQueueData_Trans(JSONObject jsonOb_Agent )
	{ 
	    JSONArray	jsonArray		= null;
	    JSONObject	jsonOb_Que	= null;
	    
		String notify = "Queue Trans";
		jsonArray   = new JSONArray();
		
		int CurrentGettingQueueCnt            	= 0;
		ItemAmassTrans queueItem 	= null;
    	try
		{
			String command				= "";
			String actionName			= "";
			String transactionID		= "";
			String transDateTime		= "";
			String returnMsg 			= "";
			String returnCode			= "";

			while((queueItem = MapQue_Amass.putTransData.getTrans()) != null){
//			while(!MapQue_Amass.putTransData.isEmpty()){
//				if ( MapQue_Amass.putTransData.size() == 0){					
//					//LOG.info( notify + " 'size==0' Get Length :"  + CurrentGettingQueueCnt + "~ 0" );
//					CurrentGettingQueueCnt = 0;
//					break;
//				}

				//보낼때 개수 제한
//				if ( Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.MaxJsonDataCount )) < CurrentGettingQueueCnt ){ 
//					LOG.info(notify + " CurrentGettingCnt/CurrentQueueCnt: "  + CurrentGettingQueueCnt + " / " +MapQue_Amass.putTransData.size() );
//					CurrentGettingQueueCnt = 0;
//					break;
//				}
//				queueItem = MapQue_Amass.putTransData.getTrans();
//				if(queueItem == null){ 
//					LOG.error(notify + " queAmassTrans is null " + MapQue_Amass.putTransData.size());
//					CurrentGettingQueueCnt = 0;
//					break;
//				}
//				else{
//					CurrentGettingQueueCnt ++;
//				}
				
					CurrentGettingQueueCnt ++;
				
				command			= queueItem.getCommand();
				transactionID		= queueItem.getTransactionID();
				actionName		= queueItem.getActionName();
				transDateTime	= String.valueOf(queueItem.getLongTransTime());
				returnMsg 			= queueItem.getReturnMsg();
				returnCode			= queueItem.getReturnCode();
				Map<String, String > map = queueItem.getattributeExt();
				
				
				if(transactionID.equals("") || transDateTime.equals("") || command.equals("")) continue;
				if(!actionName.equals("CompOK")){
					HashMap<String, String> actionMap = DicCommands.AgentMyActionsList.get(actionName);
					if (actionMap == null)
						continue;
					String agrYN = actionMap.get(DicXmlProperties.AggreUseYN);
					if(agrYN == null || agrYN.equals("N")){
						continue;
					}
				}
				
				jsonOb_Que = new JSONObject();
				jsonOb_Que.put(DicOrderAdd.Command,			command);
				jsonOb_Que.put(DicOrderAdd.TransactionID,		transactionID);
				jsonOb_Que.put(DicOrderAdd.ActionName,			actionName);
				
				jsonOb_Que.put(DicOrderAdd.TransDateTime,	transDateTime);
				
				jsonOb_Que.put(DicOrderAdd.ReturnMsg,			returnMsg);
				jsonOb_Que.put(DicOrderAdd.ReturnCode,			returnCode);
				//ext send
				if(map != null){
					int i = 1;
					for(String key : map.keySet()){
					String value = map.get(key);					
					jsonOb_Que.put("ext" + i++, value);					
					}
				}
				
				//LOG.info(("CurrentQueueCnt:" + CurrentGettingQueueCnt);
				jsonArray.put(jsonOb_Que);
			}//End While
			
			if(jsonArray.length() ==0) return "";
			else{
				//LOG.info(notify + " Get Length:"  + CurrentGettingQueueCnt + "~ Remain:" + MapQue_Amass.putTransData.size() );
				jsonOb_Agent.put(DicOrderTypes.ResponseAgentShort, jsonArray);    
				// FIXME: junsoo, 검증용으로 데이터 개수를 전달 
				jsonOb_Agent.put("TOTAL", jsonArray.length());
				return jsonOb_Agent.toString();
			}
		}
		catch(Exception e){
			LOG.error( "putTransData:" + MapQue_Amass.putTransData.size() );
		    e.printStackTrace();
		}
		return "";   	
	}
	
}
