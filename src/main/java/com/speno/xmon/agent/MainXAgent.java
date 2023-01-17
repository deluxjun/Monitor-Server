package com.speno.xmon.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.speno.xmon.codedic.DicCommands;
import com.speno.xmon.dataset.ItemAmassEvent;
import com.speno.xmon.dataset.ItemAmassLogs;
import com.speno.xmon.dataset.ItemAmassTrans;
import com.speno.xmon.dataset.MapQue_Amass;

public  class MainXAgent {
	
	private boolean initialized = false;
	private final static String transTypeInit		= "Init";
	private final static String transTypeComp	= "Comp";
	private final static String transName 		= "Trans";
	private final static String CompSuccess	= "Success";
	private final static String CompError			= "Error";
	
	private static ILogger LOG = new LoggerDummy();
	
	private CommandNioSession commandSession;
	private AmassSender transSession;
	
	// junsoo
	public boolean isInitialized() {
		return initialized;
	}
	
	public static ILogger getLogger() {
		return LOG;
	}
	public static void setLogger(ILogger logger) {
		LOG = logger;
		
		for (ILogListener log : loggers) {
			log.setLogger(LOG);
		}
	}
	
	private static List<ILogListener> loggers = new ArrayList<ILogListener>();
	public static void addLogListener(ILogListener logger) {
		logger.setLogger(LOG);
		loggers.add(logger);
	}
	
	
	public void init(String pathProperties) {
		
		// specify -DConfig=filepath/to/config.ini
		if (pathProperties == null || pathProperties.length() < 1) {
			pathProperties = System.getProperty("XmonConfig");
		}

		if (pathProperties == null || pathProperties.length() < 1) {
			System.err.println("Monitoring config file is required");
			initialized = false;
			return;
		}

		if (xmProperties.resourceManager == null)
			xmProperties.resourceManager = new ResourceCommandManager();

		try {
			boolean bInit = new  xmProperties().Init(pathProperties);
			LOG.info("Initialized :"+ bInit); 
			if (!bInit) {
				return;
			}
	
//			 Thread[] commandNioSession = new Thread[5];
//			 for(int i =0;  i<commandNioSession.length ; i++ )
//			 {
//				 commandNioSession[i] = new Thread(new CommandNioSession() );
//				 commandNioSession[i].setName("CommandNioSession" + i);
//				 commandNioSession[i].start();
//				 Thread.sleep(300);
//			 }

			commandSession = new CommandNioSession();
			Thread commandSessionTh = new Thread(commandSession);
			commandSessionTh.setName("CommandNioSession");
			commandSessionTh.start();

			transSession = new AmassSender(); 
			
			// junsoo, init success
			initialized = true;
		}
		catch (Exception e) { //IO
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		commandSession.shutdown();
		transSession.shutdown();
	}

	/*
	 * Trans_Init_ActionName
	 */
	public boolean putInit(String ActionName, String TranjectionID, long TransDateTime, String ErrorCode, String ErrorMessage, HashMap<String, String> extMap) 
	{
		if (!isInitialized())
			return false;

		if(ActionName.equals(""))
		{
			LOG.error("Error ActionName :" + ActionName);
			return false;
		}

		if(ActionName.startsWith("Trans_"))
		{
			ActionName = ActionName.substring(6);
		}
		
		if( TranjectionID.trim().equals(""))
		{
			LOG.error("Error TranjectionID Empty:" + TranjectionID );
			return false;
		}
		
		if(!DicCommands.AgentMyActionsList.containsKey(ActionName)) {
			return false;
		}
		
		if(MapQue_Amass.putTransData.addTrans(new ItemAmassTrans(
		                                                                       TranjectionID
		                                                                     , transName + "_" + transTypeInit + "_" + ActionName
		                                                                     , ActionName
		                                                                     , TransDateTime
		                                                                     , ErrorCode
		                                                                     , ErrorMessage,extMap)))
		{
//			LOG.debug("putInit Trans Success. Size:" + MapQue_Amass.putTransData.size() + " " + ActionName + " :"+ TranjectionID);
			return true;
		}else{
			//LOG.info("QueAmass_Trans Size:" + MapQue_Amass.putTransData.size());
			return false;
		}
	}
	/*
	 * Trans_Comp_Success
	 * completion
	 */
	public boolean putCompSuccess(String ActionName, String TranjectionID, long TransDateTime, String ErrorCode, String ErrorMessage, HashMap<String, String> extMap) 
	{
		if (!isInitialized())
			return false;
		
		
		if(ActionName.equals(""))
		{
			ActionName = "CompOK";
			//LOG.error("Error ActionName :" + ActionName);
			//return false;
		}
		
		if( TranjectionID.trim().equals("")){
			LOG.error("Error TranjectionID Empty:" + TranjectionID );
			return false;
		}
		
		if(DicCommands.AgentMyActionsList.size() < 1) {
			return false;
		}

		
		if(MapQue_Amass.putTransData.addTrans(new ItemAmassTrans(
		                                                                       TranjectionID
		                                                                     ,  transName + "_" + transTypeComp + "_" + CompSuccess
		                                                                     , ActionName
		                                                                     , TransDateTime
		                                                                     , ErrorCode
		                                                                     , ErrorMessage, extMap)))
		{
			//LOG.info("QueAmass_Trans : Size:" + MapQue_Amass.putTransData.size() + " " + ActionName + " :"+ TranjectionID);
			return true;
		}else{
			LOG.warn("QueAmass_Trans :" + MapQue_Amass.putTransData.size());
			return false;
		}
	}
	/*
	 * Trans_Comp_Error
	 * completion
	 */
	public boolean putCompError(String ActionName, String TranjectionID, long TransDateTime, String ErrorCode, String ErrorMessage, HashMap<String, String> extMap) 
	{
		if (!isInitialized())
			return false;
		if(ActionName.equals(""))
		{
			ActionName = "CompOK";
			//LOG.error("Error ActionName :" + ActionName);
			//return false;
		}
		if( TranjectionID.trim().equals("")){
			LOG.error("Error TranjectionID Empty:" + TranjectionID );
			return false;
		}
		
		if(DicCommands.AgentMyActionsList.size() < 1) {
			return false;
		}
		
		if(MapQue_Amass.putTransData.addTrans(new ItemAmassTrans(
		                                                                       TranjectionID
		                                                                     ,  transName + "_" + transTypeComp + "_" + CompError
		                                                                     , ActionName
		                                                                     , TransDateTime
		                                                                     , ErrorCode
		                                                                     , ErrorMessage, extMap)))
		{
			//LOG.info("QueAmass_Trans : Size:" + MapQue_AmassAction.queue.size() + " " + ActionName + " :"+ TranjectionID);
			return true;
		}else{
			LOG.warn("QueAmass_Trans :" + MapQue_Amass.putTransData.size());
			return false;
		}
	}
	public boolean putLog(String logID, String logText, long currentTimeMillis, String errorMessage, String errorCode, HashMap<String, String> extMap) 
	{
		if (!isInitialized())
			return false;

		
		if(xmProperties.htXmPropertiesAgent_Main.get(xmProperties.PUTLogUseYN	).equals("N"))
			return false;
		
		if(logID.equals("")) 
		{
			LOG.error("Error logID :" + logID);
				return false;
		}
		if( logText.trim().equals(""))
		{
			LOG.error("Error logText Empty:" + logText );
				return false;
		}
		Iterator<String> itr = xmProperties.LogCaptureTextList.iterator();
		String ftText = "";
		while(itr.hasNext())
		{
			ftText = itr.next();
			
			Pattern p = Pattern.compile(ftText);
			Matcher m = p.matcher(logText);

			if (!m.find()) continue;
//			if(logText.toUpperCase().indexOf(ftText) < 0) continue;
			
			if(MapQue_Amass.putLogText.addLogText(new ItemAmassLogs(
                                                                       logID
                                                                     , DicCommands.Log_Persist
                                                                     , logText
                                                                     , currentTimeMillis
                                                                     , errorCode
                                                                     , errorMessage)))
			{
				//LOG.info("QueAmass_Trans : Size:" + MapQue_Amass.putLogText.size() + " " + logID + " :"+ logText);
				return true;
			}else{
				LOG.warn("QueAmass_Trans :" + MapQue_Amass.putLogText.size());
				return false;
			}
		}
		return false;
	}
	public boolean putEvent(String eventName, String eventText, int eventLevel, long eventDateTime, String ErrorCode, String ErrorMessage, HashMap<String, String> extMap) 
	{
		if (!isInitialized())
			return false;

		if(xmProperties.htXmPropertiesAgent_Main.get(xmProperties.PUTEventUseYN	).equals("N"))
			return false;
		
		if(eventName.equals(""))
		{
			LOG.error("Error EventName :" + eventName);
			return false;
		}

		if(MapQue_Amass.putEvent.addEvent(new ItemAmassEvent(
		                                                                       eventName
		                                                                     , eventText
		                                                                     , eventLevel
		                                                                     , eventDateTime
		                                                                     , ErrorCode
		                                                                     , ErrorMessage,extMap)))
		{
			return true;
		}else{
			LOG.warn("QueAmass_Trans :" + MapQue_Amass.putTransData.size());
			return false;
		}
	}
	
	/*
	 * set resource
	 */
	public static void setResource(String command, IxMon resource) {
		if (xmProperties.resourceManager == null)
			xmProperties.resourceManager = new ResourceCommandManager();
		xmProperties.resourceManager.setResource(command, resource);
	}
	
	public static IxMon getResource(String command) {
		return xmProperties.resourceManager.getResource(command);
	}
}
