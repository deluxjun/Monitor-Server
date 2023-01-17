package com.speno.xmon.listener;

import java.io.File;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.codedic.DicCommands;
import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.codedic.DicOrderTypes;
import com.speno.xmon.compress.ZIPcompress;
import com.speno.xmon.dataset.ItemMap_XmAmassTrans;
import com.speno.xmon.db.DataInserterAction;
import com.speno.xmon.db.DataInserterEventLevel;
import com.speno.xmon.env.xmPropertiesXml;

public class AmassServerThread {

	private final static Logger LOG = LoggerFactory.getLogger(AmassServerThread.class);
	private static DataInserterAction dbTransction	= null;
	
	private static ConcurrentHashSet<String> logSet = new ConcurrentHashSet<String>();
	
	AmassServer server;

	public AmassServerThread(AmassServer amassServer) {
		server = amassServer;
	}
	
//	SocketAddress remoteAddress;
//	byte[] byteJsonData;
//	
//	public void setRemoteAddress(SocketAddress remoteAddress) {
//		this.remoteAddress = remoteAddress;
//	}
//
//	public void setByteJsonData(byte[] byteJsonData) {
//		this.byteJsonData = byteJsonData;
//	}
//
//	public void run() {
//		process(remoteAddress, byteJsonData);
//	}
	
	public void process(SocketAddress remoteAddress,  byte[] rcvData) {
		String reciveJsonData;
		long start = System.currentTimeMillis();
		try {
			reciveJsonData	= new String(ZIPcompress.inflate(xmPropertiesXml.networkCompress, rcvData), "UTF-8"); 
		} catch (Exception e) {
			reciveJsonData	= new String(ZIPcompress.inflate(xmPropertiesXml.networkCompress, rcvData)); 
		}
		
		// print debug
		LOG.debug("[" + Thread.currentThread().getName() +"] received: " + reciveJsonData);
		
		try {
			this.ParsingToQueue(new JSONObject(reciveJsonData), xmPropertiesXml.GetAgentIP(remoteAddress));
		}
		catch (Exception e) {
			LOG.error(e.getMessage() + " revData:" + rcvData);
			e.printStackTrace();
		}
		
		LOG.debug("amass processing time : " + (System.currentTimeMillis()-start) + "ms");
//		System.out.println("amass processing time : " + (System.currentTimeMillis()-start) + "ms");
	}

	private void ParsingToQueue(JSONObject receiveJsonData, String  agentIP) throws JSONException {

		String agentName = receiveJsonData.get(DicOrderAdd.AgentName).toString();
		String agentSendDateTime = receiveJsonData.get(DicOrderAdd.SendTime).toString();
		String OrderType = receiveJsonData.get(DicOrderTypes.OrderType).toString();
		String command = receiveJsonData.get(DicOrderAdd.Command).toString();

		if (agentName.equals("")) {
			LOG.error("agentName is empty:" + OrderType + "\n" + receiveJsonData.toString());
			return;
		}
		if (!OrderType.equals(DicOrderTypes.ResponseAgentShort)) {
			LOG.error("Not allowed Order Type:" + OrderType + "\n" + receiveJsonData.toString());
			return;
		}

		JSONArray returnArray = receiveJsonData.getJSONArray(OrderType);
		if (returnArray == null) {
			LOG.error("OrderType Array is Null:" + OrderType + "\n" + receiveJsonData.toString());
			return;
		}

		if (command.equals(DicCommands.Trans_Unit)) {
			// FIXME: junsoo, 검증
			try {
				String strTotal = receiveJsonData.get("TOTAL").toString();
				int total = Integer.parseInt(strTotal);
				// System.out.println(total + "," + returnArray.length());
				if (returnArray.length() != total)
					System.out.println("Different size : "	+ returnArray.length() + "/" + total);
			} catch (Exception e) {
			}

			if (dbTransction == null)
				dbTransction = new DataInserterAction();
			this.PutQueue_Amass_TransUnitArray(agentName, agentSendDateTime,returnArray);
		} else if (command.equals(DicCommands.Log_Persist)) {
			this.PutQueue_AmassLog_ReturnArray(agentName, agentSendDateTime,returnArray);
		} else if (command.equals(DicCommands.Event_Level)) {
			this.PutQueue_AmassEvent_ReturnArray(agentName, agentSendDateTime,returnArray);
		} else {
			LOG.error("Command Error:" + command);
		}
	}

	private void PutQueue_AmassEvent_ReturnArray(String agentName, String agentSendDateTime, JSONArray returnArray) throws JSONException {
		  JSONObject returnEachObject	=null;
		  
		  String eventName="", eventText="";
		  
		  for(int i=0; i<returnArray.length();i++)
		  {		
				returnEachObject = returnArray.getJSONObject(i);
				
				int eventLevel		= returnEachObject.getInt(DicOrderAdd.EventLevel);
				long  longEventDateTime	= Long.parseLong(returnEachObject.get(DicOrderAdd.EventDateTime).toString());
				
				eventName	= returnEachObject.getString(DicOrderAdd.EventName);
				eventText		= returnEachObject.getString(DicOrderAdd.EventText);
				 if ( (eventName.equals("")) || (longEventDateTime== 0) ) 
				 {
					 LOG.error(" EventLevel Error  -> LogID:" + eventLevel + ", transDateTime:" + longEventDateTime);
					 continue;
				 }
				 CommandServerMessageThread.eventMessageProcess(agentName, agentSendDateTime, eventName, eventText, eventLevel);
				 DataInserterEventLevel.insertEvent(agentName, eventName, eventText, eventLevel, longEventDateTime, agentSendDateTime);
				 
			} // End For		
		
	}
	
	private void PutQueue_AmassLog_ReturnArray(String agentName
																		  , String agentSendDateTime
																	      , JSONArray returnArray) throws JSONException {
		  JSONObject returnEachObject	=null;
		  String logText = "";
		  for(int i=0; i<returnArray.length();i++)
		  {		
				returnEachObject = (JSONObject) returnArray.get(i);
				 
				String command						= returnEachObject.get(DicOrderAdd.Command).toString();
				String[] commandPreSubFix 	= command.split("_");
					
				if(commandPreSubFix.length < 2 ) continue;
				if(!commandPreSubFix[0].equals(DicOrderAdd.AmassPreFix_Log)) continue;
				
				String LogID				= returnEachObject.get(DicOrderAdd.AgentLogID).toString();
				long  transDateTime	= Long.parseLong(returnEachObject.get(DicOrderAdd.AgentTransDateTime).toString());
				
				 if ( (LogID.equals("")) || (transDateTime== 0) ) 
				 {
					 LOG.error(" LogWrite Error  -> LogID:" + LogID + ", transDateTime:" + transDateTime);
					 continue;
				 }
	
				if(commandPreSubFix[1].equals(DicOrderAdd.AmassSufFix_Log)) {
					 try{
//				            String fileName = xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.LogsFolder) + File.separator + agentName + "_" + LogID + "_" + sd.GetDateTime(sd.DateTimeFormatter_Day) + ".log";
//				            file = new File(fileName);

						 logText = returnEachObject.getString(DicOrderAdd.AgentLogText);

				    		Iterator<String> itr = xmPropertiesXml.LogCaptureTextList.iterator();
				    		String ftText = "";
				    		while(itr.hasNext()) {
				    			ftText = itr.next();

				    			// 콘솔로 event 전송!!
				    			Matcher m = Pattern.compile(ftText).matcher(logText);
				    			if (m.find()){
				    				CommandServerMessageThread.logMessageProcess(agentName, agentSendDateTime, logText, LogID);
				    				break;
				    			}
				    		}
				    		
				            String appenderName = agentName + "_" + LogID;

				            // print log 레벨 내림 YYS
				    		Logger agentLogger = getLogger(appenderName);
				    		agentLogger.info(logText);
					 }
					 catch(Exception e) {
						 LOG.error(e.getMessage());
						 e.printStackTrace();
					 }
				}
			} // End For		
		
	}

	private void PutQueue_Amass_TransUnitArray(String agentName, String agentSendDateTime, JSONArray returnArray) throws JSONException {

		JSONObject returnEachObject	=null;
		  
		String[] commandPreSubFix 	= null;

//		System.out.println("Trans Count : " + returnArray.length());
		LOG.debug("[" + agentName + "] Trans Count : " + returnArray.length());
		for(int i=0; i<returnArray.length();i++) {		
			server.tempInitCnt.incrementAndGet();
			LOG.debug("@Transaction count:" + server.tempInitCnt.get() + " ErrCnt:"  + server.tempErrCnt.get() + " Size:" + server.MapSize());
		  
			returnEachObject = returnArray.getJSONObject(i);			
			String command		= returnEachObject.get(DicOrderAdd.Command).toString();
			commandPreSubFix 	= command.split("_");
			
			if(commandPreSubFix.length < 2 )
			{
				LOG.error(" Transaction length Error  -> " + returnEachObject.toString());
				continue;
			}
			if(!commandPreSubFix[0].equals(DicOrderAdd.AmassPreFix_Trans)) 
			{
				LOG.error(" Transaction Error  -> " + returnEachObject.toString());
				continue;
			}
			
			String transactionID	= returnEachObject.get(DicOrderAdd.AgentTransactionID).toString();
			long  transDateTime		= Long.parseLong(returnEachObject.get(DicOrderAdd.AgentTransDateTime).toString());
			
			
			 if ( (transactionID.equals("")) || (transDateTime== 0) ) 
			 {
				 LOG.error(" Transaction Error  -> transactionID:" + transactionID + ", transDateTime:" + transDateTime);
				 continue;
			 }
			 
			if(commandPreSubFix[1].equals(DicOrderAdd.AmassSufFix_ActionInit)) {
				String tempkey =agentName  + xmPropertiesXml.Sep + transactionID;
				ItemMap_XmAmassTrans itemExisting =  server.MapAmassTrans_Get(tempkey);
				if (itemExisting == null){
					// 신규건 도착
					LOG.debug("action initialization : " + tempkey);
					String ext1 = "";
					String ext2 = "";
					try {
						ext1 = returnEachObject.getString("ext1");
						ext2 = returnEachObject.getString("ext2");
					} catch (Exception e) {
						// transaction is d or u or r not error
					}
					ItemMap_XmAmassTrans item_init = new ItemMap_XmAmassTrans(agentName, commandPreSubFix[2], transactionID, transDateTime,ext1,ext2);

					if(!server.MapAmassTrans_Create(tempkey, item_init)) {
				 		LOG.error("@@@:" + agentName + ":" + commandPreSubFix[2] + ":" +  transactionID + ":" + transDateTime);
				 	}
				} else {
					// complete 등이 먼저 도착한 경우이므로. 에러처리 하지 않고 insert
					LOG.warn("initialization reached after completion : " + tempkey);

					String successString = DicOrderAdd.AmassSufFix_ActionCompSuc;
					if (!itemExisting.GetSuccessFlag())
						successString = DicOrderAdd.AmassSufFix_ActionCompError;
					
					itemExisting.setActionName(commandPreSubFix[2]);
					
					// ID 중복될 수 있으므로 완료된 것만 insert
					if (itemExisting.GetTransCompleteTime() > 0L) {
						// set init time
						itemExisting.setTransInitTime(transDateTime);

						// 1초 뒤에 insert!!
						itemExisting.setModifiedTimeToInsert(transDateTime + ItemMap_XmAmassTrans.DELAYTIME_TO_INSERT);
						
//						boolean success = insertTrans(agentName, tempkey, itemExisting, successString);
						
						LOG.debug("set removing transaction from buffer (INIT): " + tempkey + " / " + server.MapSize());
//			    		if(!server.MapAmassTransRemove(tempkey,  itemExisting )) {
//			    			LOG.error("MapAmassTransRemove, Key:" + tempkey  + ", Size:" + server.MapSize());
//			    		}
					}
				}

//					ItemMap_XmAmassTrans item_init = new ItemMap_XmAmassTrans(agentName, commandPreSubFix[2], transactionID, transDateTime);
//
//					if(!server.MapAmassTrans_Create(agentName  + xmPropertiesXml.Sep + transactionID, item_init)) {
//				 		LOG.error("@@@:" + agentName + ":" + commandPreSubFix[2] + ":" +  transactionID + ":" + transDateTime);
//				 	}
			}
			else if(commandPreSubFix[1].equals(DicOrderAdd.AmassSufFix_ActionCompletion)) {
				String tempkey =agentName  + xmPropertiesXml.Sep + transactionID;
				ItemMap_XmAmassTrans itemExisting =  server.MapAmassTrans_Get(tempkey);
				if (itemExisting == null) {
					LOG.warn("Completion reached than initialization : " + tempkey);
					
					// 신규건 도착
					ItemMap_XmAmassTrans item_init = new ItemMap_XmAmassTrans(agentName, "NOTYET", transactionID, transDateTime);

					if(commandPreSubFix[2].equals(DicOrderAdd.AmassSufFix_ActionCompSuc)) {
						item_init.SetTransCompleteTime(transDateTime, true);		
						item_init.setResult(DicOrderAdd.AmassSufFix_ActionCompSuc);
					}
					else {
						item_init.SetTransCompleteTime(transDateTime, false);
						item_init.setResult(DicOrderAdd.AmassSufFix_ActionCompError);
						server.tempErrCnt.incrementAndGet();
					}

					if(!server.MapAmassTrans_Create(tempkey, item_init)) {
				 		LOG.error("@@@:" + agentName + ":" + commandPreSubFix[2] + ":" +  transactionID + ":" + transDateTime);
				 	}
				}
				else {
					if(commandPreSubFix[2].equals(DicOrderAdd.AmassSufFix_ActionCompSuc)) {
						itemExisting.SetTransCompleteTime(transDateTime, true);		
						itemExisting.setResult(DicOrderAdd.AmassSufFix_ActionCompSuc);
					}
					else {
						itemExisting.SetTransCompleteTime(transDateTime, false);
						itemExisting.setResult(DicOrderAdd.AmassSufFix_ActionCompError);
						server.tempErrCnt.incrementAndGet();
					}
					
					// 1초 뒤에 insert!!
					itemExisting.setTimeToInsert(transDateTime + ItemMap_XmAmassTrans.DELAYTIME_TO_INSERT);

//					boolean success = insertTrans(agentName, tempkey, itemExisting, commandPreSubFix[2]);
					
					LOG.debug("set removing transaction from buffer (COMPLETE): " + tempkey + " / " + server.MapSize());
//		    		if(!server.MapAmassTransRemove(tempkey,  itemExisting ))
//		    		{
//		    			LOG.error("MapAmassTransRemove couldn't remove trans, Key:" + tempkey  + ", Size:" + server.MapSize());
//		    		}
				}
			}
			else {
				LOG.error("MapAmass_Trans, Key:" + ", Size:" + server.MapSize());
			}
		} // End For				
	}


//	private boolean insertTrans(String agentName, String key, ItemMap_XmAmassTrans itemExisting, String success) {
////		int count = 0;
////		while (true) {
////			if(++count > 3) break;
//			if (itemExisting.GetTransCompleteTime() == 0L)
//				LOG.error("Complete time is zero");
//			if (dbTransction.Insert_XmTransAction(
//					agentName,
//					itemExisting.GetActionName(),
//					itemExisting.GetTransID(),
//					itemExisting.GetTransInitTime(),
//					itemExisting.GetTransCompleteTime(),
//					itemExisting.GetTransCompleteTime() - itemExisting.GetTransInitTime(),
//					success)) {
//				return true;
////			} else {
////				continue;
//			}
////		}
		
//		LOG.error("MapAmass_Trans ::DB, Key:" + key + ", Size:" + server.MapSize());
//		return false;
//	}

	// get logger
	private Logger getLogger(String appenderName) {
		synchronized (logSet) {
			if (!logSet.contains(appenderName)) {
				DailyRollingFileAppender fa = new DailyRollingFileAppender();
				fa.setName(appenderName);
				fa.setDatePattern(".yyyy-MM-dd");
				fa.setFile(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.LogsFolder) + File.separator + appenderName + ".log");
				fa.setLayout(new PatternLayout("%d{[yyyy.MM.dd,HH:mm:ss.SSS]} %M -- %m%n"));
				fa.setThreshold(Level.INFO);
				fa.setAppend(true);
				fa.activateOptions();

				org.apache.log4j.Logger.getLogger(appenderName).addAppender(fa);

				logSet.add(appenderName);
				LOG.info("Log appender(" + appenderName + ") has been added");
				
			}
		}
		
		return LoggerFactory.getLogger(appenderName);
	}
    


	/*
	@Override
	protected void fireReleased() {
		// TODO Auto-generated method stub
		
	}
*/
	


	
}
