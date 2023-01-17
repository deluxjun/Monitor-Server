package com.speno.xmon.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.JsonGenerator.JsonGeneratorAmass;
import com.speno.xmon.codedic.DicCommands;
import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.codedic.DicOrderTypes;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.compress.ZIPcompress;
import com.speno.xmon.dataset.ItemActionList;
import com.speno.xmon.db.DataInserterAction;
import com.speno.xmon.db.DataInserterResource;
import com.speno.xmon.db.DataSelecterAction;
import com.speno.xmon.db.DataSelecterLog;
import com.speno.xmon.db.DataSelecterResource;
import com.speno.xmon.env.ItemAgent;
import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.sender.RequestAgentShort;
import com.speno.xmon.sender.RequestAgentShortHandler;
import com.speno.xmon.util.CommonUtil;
import com.speno.xmon.util.JsonUtil;

//public class CommandServerMessageThread extends xMThreadWorker_Received {
public class CommandServerMessageThread {

	private final static Logger LOG = LoggerFactory.getLogger(CommandServerMessageThread.class);
	
//	private String rcvData;
//	private IoSession session;
//	private String remoteAddressStr ;
	private static DataSelecterAction	dbAction;
	
	private static DataSelecterResource dbResourceSelect;
	private static DataInserterResource dbResource;

	
	public CommandServerMessageThread() {}
	public CommandServerMessageThread(CommandServer server) {
		// TODO Auto-generated constructor stub
	}
	/*
	public CommandServerMessageThread(CommandServerMessageThread t) {}
	public CommandServerMessageThread(IoSession session, SocketAddress remoteAddress, String rcvData) 
	{
		this.rcvData 	= rcvData;
		this.session 	= session; 
		this.remoteAddressStr = remoteAddress.toString();
		this.dbResource				= new DataInserterResource();
	}
	*/
	public static void process(IoSession session2, SocketAddress remoteAddress, byte[] byteJsonData) {
		if(byteJsonData.length ==0){
			LOG.error("byteJsonData null");
		}
		String rcvData;
		try {
			rcvData 					=  new String( ZIPcompress.inflate(xmPropertiesXml.networkCompress, byteJsonData), "UTF-8");
		} catch (Exception e) {
			rcvData 					=  new String( ZIPcompress.inflate(xmPropertiesXml.networkCompress, byteJsonData));
		}
		
		if (dbResource == null)
			dbResource				= new DataInserterResource();
		if (dbResourceSelect == null)
			dbResourceSelect				= new DataSelecterResource();
		if (dbAction == null)
			dbAction				= new DataSelecterAction();
		
		messageProcess(session2, remoteAddress, rcvData);
	}

	
	public static void messageProcess(IoSession session, SocketAddress remoteAddress, String rcvData) {
	
		if(rcvData == null) return;
		if(rcvData.equals("")) return;
		try {	
			
			JSONObject  jsonObject		= new JSONObject(rcvData);
			
			/* Agent Session Create: SessionAgent */
			if(rcvData.indexOf(DicCommands.SessionAgent) > -1)
			{                           
				String agentName = jsonObject.get(DicCommands.SessionAgent).toString();
				if(xmPropertiesXml.htAgentList.containsKey(agentName))
				{
					xmPropertiesXml.htAgentList.get(agentName).setMinaSession(session);
				}
			}
			
			// 20141117, New statistics modules
			if (jsonObject.has(DicOrderAdd.Command)) {
				String command = jsonObject.get(DicOrderAdd.Command).toString();
				if (DicOrderAdd.StatisticsResource.equals(command)) {
					String agentName	= jsonObject.getString(DicOrderAdd.AgentName).trim();
					String consoleId	= jsonObject.get(DicOrderAdd.ConsoleId).toString();
					String rangeStart	= jsonObject.get(DicOrderAdd.RangeStart).toString();
					String rangeEnd		= jsonObject.get(DicOrderAdd.RangeEnd).toString();
					String perUnit		= jsonObject.get(DicOrderAdd.PerUnit).toString();		//Sec, Min, Hour, Day
					String aggreUnit	= jsonObject.get(DicOrderAdd.AggreUnit).toString();	
					String propertyNames= jsonObject.get(DicOrderAdd.PropertyNames).toString();	
					String resourceId	= jsonObject.get(DicOrderAdd.ResourceID).toString();	
					String resourceGroupId = jsonObject.get(DicOrderAdd.ResourceGroupID).toString();
					
					List<String> listAggreUnit = CommonUtil.getListFromString(",", aggreUnit);
					List<String> listPropertyNames = CommonUtil.getListFromString(",", propertyNames);
					List<String> listResourceId = CommonUtil.getListFromString(",", resourceId);
					List<String> listResourceGroupId = CommonUtil.getListFromString(",", resourceGroupId);
					
					StringBuffer errMsg = new StringBuffer();					
					List<List<String>> data = dbResourceSelect.selectResource(agentName, rangeStart, rangeEnd, perUnit, listAggreUnit, listPropertyNames, listResourceId, listResourceGroupId, errMsg);
					if (data == null) {
						sendErrorMessage2(session, consoleId, DicCommands.GENERAL_ERROR, errMsg.toString());
					}
					else if (data.size() < 1) {
						sendErrorMessage2(session, consoleId, DicCommands.NODATA, "No data");
					}
					else {
						JSONObject json = JsonUtil.getJsonFromList(data);
						if (json == null)
							sendErrorMessage2(session, consoleId, DicCommands.GENERAL_ERROR, errMsg.toString());
						else {
							json.put(DicOrderAdd.ConsoleId, consoleId);
							json.put(DicOrderAdd.PerUnit, perUnit);
							json.put(DicOrderAdd.ReturnCode, DicCommands.OK);
							SendMessageToConsole(session, json.toString());
						}
					}
					return;
				}
				else if (DicOrderAdd.StatisticsTPS.equals(command)) {
					return;
				}
				else if (DicOrderAdd.StatisticsResponse.equals(command)) {
					return;
				}
			}
			
			
			String orderType			= jsonObject.get(DicOrderTypes.OrderType).toString().trim();			  
			String msgToconsole	= "";

			/* Server Environment: RequestSet */
			if(orderType.equals(DicOrderTypes.RequestSet))
			{
				//this.addListConsole(session);
				
				String Command 	= jsonObject.get(DicOrderAdd.Command).toString();
				String agentNames = jsonObject.has(DicOrderAdd.TargetAgent) ? jsonObject.get(DicOrderAdd.TargetAgent).toString() : "";
				String Sms = jsonObject.has("AggreUnit") ? jsonObject.get("AggreUnit").toString() : "";
								
				if(Command.equals(DicCommands.Set_InitSession))
				{
					 xmPropertiesXml.chmConsoleEvent.putIfAbsent(session.getRemoteAddress().toString() + xmPropertiesXml.Sep, session); 
					
					Iterator<String> itr = xmPropertiesXml.chmConsoleEvent.keySet().iterator();
					while(itr.hasNext())
					{
						String tempKey = itr.next();
						 LOG.debug("Console ID:" +  tempKey + ", SessionConnected:" + xmPropertiesXml.chmConsoleEvent.get(tempKey).isConnected());
					}
					return;
				}
			
				SendMessageToConsole(session, new JsonGeneratorAmass(jsonObject).ResponseSet(orderType, Command, agentNames,Sms));
				return;
			}
			
			JSONArray jsonOrderArray	=jsonObject.getJSONArray(orderType);
			JSONObject jsonSubObject	= null;
			
			boolean bResourceDeleted = false;
			boolean bActionDeleted = false;
			boolean bLogIdDeleted = false;
			  
			for(int i =0 ; i<jsonOrderArray.length();i ++)
			{
				jsonSubObject =jsonOrderArray.getJSONObject(i);
				String Command 	= jsonSubObject.getString(DicOrderAdd.Command);	//Req_Memory, Trans_Stats
				String ValueUnit = jsonSubObject.getString(DicOrderAdd.ValueUnit);		//KB, MB, GB, Byte, MS
				
				if ( orderType.equals(DicOrderTypes.RequestStats))
				{
					String targetAgent 				= jsonObject.get(DicOrderAdd.TargetAgent).toString();
					String[] agentNames			= targetAgent.split(",");
					String RequestDateTime		= jsonObject.get(DicOrderAdd.RequestDateTime).toString();
					String consoleId					= jsonObject.get(DicOrderAdd.ConsoleId).toString();
					String PerUnit					= jsonSubObject.get(DicOrderAdd.PerUnit).toString();		//Sec, Min, Hour, Day
					
					/*
					 * ##################### AggreUnit #####################
					 * Resource: Avg, Min, Max, Sum, Cnt
					 * Action	 : Avg, Min, Max, CntSuc, CntErr, CntOut
					 */
					String AggreUnit					= jsonSubObject.get(DicOrderAdd.AggreUnit).toString();	
					String[] AggreUnits 			= AggreUnit.split(",");
					
					String RangeStart				= jsonSubObject.get(DicOrderAdd.RangeStart).toString();	//2014-07-15 02:00:00
					String RangeEnd				= jsonSubObject.get(DicOrderAdd.RangeEnd).toString();	//2014-07-15 05:00:00
						  
					if(RequestDateTime.equals("")) {
						return ;
					}
					
					for(int agent =0; agent <agentNames.length; agent++)
					{
						String resourcdID = "";
						String agentName = agentNames[agent];
						
						String[] keys = JSONObject.getNames(jsonSubObject);
						for(int j=0;j<keys.length;j++)
						{
							if(keys[j].equals(DicOrderAdd.ResourceID))	
								resourcdID= jsonSubObject.getString(DicOrderAdd.ResourceID);
						}
						
						if(Command.indexOf(xmPropertiesXml.Sep) > -1)
						{
							String TempCommand	= Command.split(xmPropertiesXml.Sep)[0];
							resourcdID						= Command.split(xmPropertiesXml.Sep)[1];
							Command = TempCommand;
						}
						CommandServerRequestBuilder csReqBuilder = new CommandServerRequestBuilder.Builder()
													.SetAgentName(agentName)
													.SetOrderType(orderType)
													.SetCommandName(Command)
													.SetResourcdID(resourcdID)
													.SetActionName(Command)
													.SetPerUnit(PerUnit)
													.SetAggreUnit(AggreUnits)
													.SetValueUnit(ValueUnit)
													.SetRangeStart(RangeStart)
													.SetRangeEnd(RangeEnd)				
													.SetConsoleId(consoleId)
													.build();
						
						if(Command.startsWith(DicCommands.Trans_Stats))
						{							
//							dbAction.setBuilder(csReqBuilder);
							msgToconsole =  dbAction.Select_XmAggregatedAction(csReqBuilder);
						}
						else if(Command.startsWith(DicCommands.Trans_Unit))
						{
//							dbAction.setBuilder(csReqBuilder);
							msgToconsole =  dbAction.Select_XmTransAction(csReqBuilder);
						}
						else if(Command.startsWith(DicCommands.Log_LogText))
						{
							msgToconsole =  new DataSelecterLog(csReqBuilder).Select_LogText(session.getRemoteAddress().toString());
						}
						else if(Command.equals("Down_LogText"))
						{	
							String path = xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.LogsFolder) +File.separator;	
							path = path + targetAgent+ "_" + AggreUnit + ".log";	
							DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
							Date date = dateFormat.parse(RangeStart);
							
							Date now = new Date();
							if(date.getDay() == now.getDay() && date.getMonth() == now.getMonth()){}
							else{
								path = path + "." + RangeStart;
							}
							
							InputStream inputStream = new FileInputStream(path);
							SendMessageToConsole(session, inputStream );
							return;
						}
						else {
							String propertyName					= jsonSubObject.get(DicOrderAdd.PropertyNames).toString();	
							
							csReqBuilder.setPropertyName(propertyName);
//							dbResourceSelect.setBuilder(csReqBuilder);
							msgToconsole =  dbResourceSelect.Select_XmAggregatedResource(csReqBuilder);
						}
						SendMessageToConsole(session, msgToconsole );
					}
				  }
				  else if(orderType.equals(DicOrderTypes.RequestAgent) )
				  {
					  String targetAgent 				= jsonObject.get(DicOrderAdd.TargetAgent).toString();
					  String consoleId					= jsonObject.get(DicOrderAdd.ConsoleId).toString();
					  
					  String[] agentNames				= targetAgent.split(",");
					  
					  for(int agent =0; agent <agentNames.length; agent++)
					  {
						  jsonObject.put(DicOrderAdd.TargetAgent, agentNames[agent]);
						  jsonObject.put(DicOrderAdd.ConsoleId, consoleId);
						  
						  String command_id = String.valueOf(System.currentTimeMillis());
						  
						  jsonObject.put(DicOrderAdd.ConsoleCmdID, command_id);
						  DicCommands.htConsoleCmdSessionFromAmassServer.put(command_id, session);
						  
						  int ret = new RequestAgentShort().SendRequestCmd(agentNames[agent], jsonObject.toString() );
						  if (ret < 0) {	// 20141029, junsoo, send a error message to console
							  sendErrorMessage(session, Command, DicCommands.GENERAL_ERROR, "An error occurred while sending a command to agent(" +agentNames[agent]+")");
						  }
					  }					  
				  }
				  else if(orderType.equals(DicOrderTypes.RequestAgentSet) )
				  {
					  String targetAgent 				= jsonObject.get(DicOrderAdd.TargetAgent).toString();
					  String consoleId					= jsonObject.get(DicOrderAdd.ConsoleId).toString();
					  
					  String[] agentNames				= targetAgent.split(",");
					  
					  for(int agent =0; agent <agentNames.length; agent++)
					  {
						  jsonObject.put(DicOrderAdd.TargetAgent, agentNames[agent]);
						  jsonObject.put(DicOrderAdd.ConsoleId, consoleId);
						  
						  String command_id = String.valueOf(System.currentTimeMillis());
						  
						  
						  jsonObject.put(DicOrderAdd.ConsoleCmdID, command_id);
						  DicCommands.htConsoleCmdSessionFromAmassServer.put(command_id, session);
						  
						  int ret = new RequestAgentShort().SendRequestCmd(agentNames[agent], jsonObject.toString() );
						  if (ret < 0) {	// 20141029, junsoo, send a error message to console
							  sendErrorMessage(session, Command, DicCommands.GENERAL_ERROR, "An error occurred while sending a command to agent(" +agentNames[agent]+")");
						  }

					  }					  
				  }
				  else if(orderType.equals(DicOrderTypes.ResponseAgent) || orderType.equals(DicOrderTypes.ResponseAgentSet) ) 
				  {
					  	session.setAttribute("using", false);
		            	String consoleCmdID =  jsonObject.get(DicOrderAdd.ConsoleCmdID).toString();
		            	IoSession consoleSession = DicCommands.htConsoleCmdSessionFromAmassServer.get(consoleCmdID);
		            	
		            	if(consoleSession == null) return;
		            	if(consoleSession.isConnected())
		            	{
		            		SendMessageToConsole(consoleSession, jsonObject.toString());
		  	    		  	DicCommands.htConsoleCmdSessionFromAmassServer.remove(consoleCmdID);
		            	}
				  }
				  else if(orderType.equals(DicOrderTypes.ResponseAgentShort))
				  {
					  session.setAttribute("using", false);
					  
					  String returnMsg 	= jsonSubObject.get(DicOrderAdd.ReturnMsg).toString();
					  int returnCode 		= Integer.parseInt(jsonSubObject.get(DicOrderAdd.ReturnCode).toString());

					  if(returnCode != 0)
					  {
						  LOG.error(" Response Error :" + returnMsg);
						  //this.SendMessageToConsole(session, jsonObject.toString() );
						  continue;
					   }

					String agentName = jsonObject.get(DicOrderAdd.AgentName).toString().trim();
					String aggregatedTime = jsonObject.get(DicOrderAdd.SendTime).toString();
					try {
						Date date = SimDate.getDateTimeFormatter_Sec().parse(aggregatedTime);
						if (!date.after(new Date(System.currentTimeMillis() + 60*1000L)))
							dbResource.Insert_XmAggregatedResourceSec(new RequestAgentShortHandler(agentName, aggregatedTime, jsonSubObject).GetItemList());
					} catch (Exception e) {
						LOG.error("Date format error : " + e.getMessage(), e);
					}

				  }
				  else if(orderType.equals(DicOrderTypes.INIT_AGENT))
				  {
					  session.setAttribute("using", false);

					  if (jsonSubObject.has(DicOrderAdd.AgentName)) {
						  String agentName = jsonSubObject.getString(DicOrderAdd.AgentName).trim();
						  if (!bActionDeleted) {
							  new DataInserterAction().deleteActionList(agentName);
							  bActionDeleted = true;
						  }
						  if (!bResourceDeleted) {
							  dbResource.Delete_XmCommandList(agentName);
							  dbResource.Delete_XmCommandSubList(agentName, "", "");
							  bResourceDeleted = true;
						  }
	
						  if(Command.equals(DicOrderAdd.ActionName))
						  {
							  ItemActionList item = new ItemActionList();
							  
							  item.setAgentName(agentName);
							  item.setActionName(jsonSubObject.getString(DicOrderAdd.ActionName).trim());
							  item.setTitle(jsonSubObject.getString(DicOrderAdd.Title).trim());
							  item.setDescription(jsonSubObject.getString(DicOrderAdd.Description).trim());
							  item.setAggreUseYN(jsonSubObject.getString(DicOrderAdd.AggreUseYN).trim());
							  item.setHealthUseYN(jsonSubObject.getString(DicOrderAdd.HealthUseYN).trim());
							  
							  new DataInserterAction().Insert_XmActionList(item);
						  }
						  else if(Command.equals(DicOrderAdd.CommandList))
						  {
							  String USE = jsonSubObject.get("USE").toString().trim();
							  dbResource.InsertValidation_XmCommandList(agentName, USE);
							  
							  JSONArray jsonArray = new DataSelecterResource().Select_XmCommandListAndSub(agentName,"");
							  
							  if((jsonArray != null) && (jsonArray.length() > 0)) {
								  ItemAgent agent = xmPropertiesXml.htAgentList.get(agentName);
								  if (agent != null) {
									  agent.setCommandListAndSubJsonArray(jsonArray);
								  } else {
									  LOG.error("XMonitor and agent's name dismatch : " + agentName);
								  }
							  }
						  } 
						  else if(Command.equals(DicOrderAdd.LogID)){
	//						  if (!bLogIdDeleted) {
	//							  xmPropertiesXml.mapLogId.clear();
	//							  bLogIdDeleted = true;
	//						  }
	
							  String values = jsonSubObject.getString(DicOrderAdd.ValuesComma).trim();
							  if (agentName.length() > 0 && values.length() > 0) {
								  String[] valueArray = values.split(",");
								  for (int j = 0; j < valueArray.length; j++) {
									  valueArray[j] = valueArray[j].trim();
									  LOG.info("LogId setting : " + agentName + "," + valueArray[j]);
								  }
								  xmPropertiesXml.mapLogId.put(agentName, valueArray);
								 
							  }
						  }
					  }
				  }
			  } //End For jsonOrderArray
		      return;
		} catch (Exception e) {			
			LOG.error("remoteAddress:" + remoteAddress.toString() + "\nrcvData: " + rcvData +"", e);
			e.printStackTrace();
		}
		finally
		{
		}
	}
	
	private static void SendMessageToConsole(IoSession session, String msgToconsole) {
		  if(msgToconsole.equals("")) return;
		  
		  byte[] byteJsonDatadd = ZIPcompress.deflate(xmPropertiesXml.networkCompress, msgToconsole);
		  if(xmPropertiesXml.useProtocolCodecFilter)
		  {
			  session.write(byteJsonDatadd);
		  }
		  else
		  {
			  IoBuffer rbuffer = IoBuffer.allocate(byteJsonDatadd.length);
			  rbuffer.put(byteJsonDatadd);
			  rbuffer.flip();
			  session.write(rbuffer);
		  }
	}
	
	private static void SendMessageToConsole(IoSession session, InputStream stream) {
		  
		   try {
			byte[] byteJsonDatadd = ZIPcompress.deflate(xmPropertiesXml.networkCompress, CommonUtil.getBytes(stream));
			  if(xmPropertiesXml.useProtocolCodecFilter)
			  {
				  session.write(byteJsonDatadd);
			  }
			  else
			  {
				  IoBuffer rbuffer = IoBuffer.allocate(byteJsonDatadd.length);
				  rbuffer.put(byteJsonDatadd);
				  rbuffer.flip();
				  session.write(rbuffer);
			  }
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}
	/*
    protected synchronized void addListConsole(IoSession session) {
    	SocketAddress clientAddr = session.getRemoteAddress();
    	if(clientAddr == null ) return;
        if (!containsClient(clientAddr))
        {
        	XConsoleItem xConsole = new XConsoleItem(clientAddr.toString());              
        	CommandServer.xConsoleList.put(clientAddr, xConsole);
            
           // LOGGER.info("[1-1] New View Clients.put:" + clientAddr.toString());
        }
    }
    */
    /*
    //contains
    protected boolean containsClient(SocketAddress clientAddr) {
        return CommandServer.xConsoleList.containsKey(clientAddr);
    }
*/
	public static void logMessageProcess(String agentName, String agentSendDateTime, String logText, String logID) {
		if(xmPropertiesXml.chmConsoleEvent.size() ==0 ) return;
		
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(DicOrderAdd.Command, DicCommands.Log_Event);
			jsonObject.put(DicOrderAdd.AgentName, agentName);
			jsonObject.put(DicOrderAdd.SendTime, agentSendDateTime);
			jsonObject.put(DicOrderTypes.OrderType, DicOrderTypes.ResponseStats);
			jsonObject.put(DicOrderAdd.AgentLogID, logID);
			jsonObject.put("USE", logText);
			String msgToconsole = jsonObject.toString();
			Iterator<String> itr = xmPropertiesXml.chmConsoleEvent.keySet().iterator();
			while(itr.hasNext())
			{
				SendMessageToConsole(xmPropertiesXml.chmConsoleEvent.get(itr.next()), msgToconsole);
				//LOG.debug("Console ID:" +  tempKey + ", SessionConnected:" + xmProperties.htConsoleEventSession.get(tempKey).isConnected());
			}		
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void eventMessageProcess(String agentName
													  , String agentSendDateTime
													  , String eventName
													  , String eventText
													  , int eventLevel) {
		if(xmPropertiesXml.chmConsoleEvent.size() ==0) return;
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(DicOrderAdd.Command, DicCommands.Event_Level);
			jsonObject.put(DicOrderAdd.AgentName, agentName);
			jsonObject.put(DicOrderAdd.SendTime, agentSendDateTime);
			jsonObject.put(DicOrderTypes.OrderType, DicOrderTypes.ResponseStats);
			jsonObject.put(DicOrderAdd.EventLevel, eventLevel);
			
			jsonObject.put(DicOrderAdd.EventName, eventName);
			jsonObject.put(DicOrderAdd.EventText, eventText);
			
			String msgToconsole = jsonObject.toString();
			Iterator<String> itr = xmPropertiesXml.chmConsoleEvent.keySet().iterator();
			while(itr.hasNext()){
				SendMessageToConsole(xmPropertiesXml.chmConsoleEvent.get(itr.next()), msgToconsole);
				//LOG.debug("Console ID:" +  tempKey + ", SessionConnected:" + xmProperties.htConsoleEventSession.get(tempKey).isConnected());
			}		
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 20141029, junsoo, 
	private static void sendErrorMessage(IoSession session, String originalCmd, String errorCode, String errorMessage) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(DicOrderAdd.Command, originalCmd);
			jsonObject.put(DicOrderAdd.ReturnCode, errorCode);
			jsonObject.put(DicOrderAdd.ReturnMsg, errorMessage);
			String msgToconsole = jsonObject.toString();
			SendMessageToConsole(session, msgToconsole);
		}
		catch (JSONException e) {
			LOG.error("An error occurred while sending error message : " + e.getMessage(), e);
		}
	}

	// 20141029, junsoo, 
	private static void sendErrorMessage2(IoSession session, String consoleId, String errorCode, String errorMessage) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(DicOrderAdd.ConsoleId, consoleId);
			jsonObject.put(DicOrderAdd.ReturnCode, errorCode);
			jsonObject.put(DicOrderAdd.ReturnMsg, errorMessage);
			String msgToconsole = jsonObject.toString();
			SendMessageToConsole(session, msgToconsole);
		}
		catch (JSONException e) {
			LOG.error("An error occurred while sending error message : " + e.getMessage(), e);
		}
	}



}
