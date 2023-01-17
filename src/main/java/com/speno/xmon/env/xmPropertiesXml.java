package com.speno.xmon.env;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.PropertyConfigurator;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import xmon.encr.ABC;

import com.speno.xmon.xMonitoringServerMain;
import com.speno.xmon.agent.ResourceCommandManager;
import com.speno.xmon.db.DBProperties;
import com.speno.xmon.db.collectLog.Tail;
import com.speno.xmon.event.EventManager;
import com.speno.xmon.event.IAdapter;
import com.speno.xmon.event.postJob.PostJob;
import com.speno.xmon.event.postJob.PostJobController;
import com.speno.xmon.sms.SendSMS;

public class xmPropertiesXml implements ConfigIF {
	private final static Logger LOG = LoggerFactory.getLogger(xmPropertiesXml.class);
	
	public static ResourceCommandManager resourceManager;
	
	public static ConcurrentHashMap<String, ItemAgent> htAgentList  				= new ConcurrentHashMap<String, ItemAgent>();

	public static ConcurrentHashMap<String, String[]> mapLogId	= new ConcurrentHashMap<String, String[]>();
	
	public static ConcurrentHashMap<String, IoSession> chmConsoleEvent		= new ConcurrentHashMap<String, IoSession>();
	public static ConcurrentHashMap<String, Tail>	chmConsoleLogTail 			= new ConcurrentHashMap<String, Tail>();
	
	
	public static Hashtable<String, Integer> htXmPropertiesAmass_Int = new Hashtable<String, Integer>();
	public static Hashtable<String, String> htXmPropertiesAmass_String = new Hashtable<String, String>();
	
    public static final String AgentIP 								= "AgentIP";
    public static final String AgentPort							= "AgentPort";

	public static  boolean useProtocolCodecFilter 	= true;
	
	public static boolean networkCompress = false;
	
	public static final String Sep										= "&";

	/**************************************************************************************
     * propertiesType
     **************************************************************************************/
    public static String propertiesTypeXmAmServer 		= "XmAmServer";
	/**************************************************************************************
	 * [EnvAgent]
	 **************************************************************************************/
    public static String AServerMyName								= "AServerMyName";
    public static String ActionCommand								= "ActionCommand";
    public static String ResourceCommand							= "Commands";
    
    public static String[] AgentMyActionsList						= null;
    
	public static String Transmit_MaxJsonDataLength			= "Transmit_MaxJsonDataLength";
	public static String Transmit_MaxJsonDataCnt				= "Transmit_MaxJsonDataCnt";
	
	public static String Session_CheckRepeatMs				= "Session_CheckRepeatMs";
	
	public static String Transmit_Repeat_ActionMs				= "Transmit_Repeat_ActionMs";  
	public static String Transmit_RetryConnectionMs			= "Transmit_RetryConnectionMs";
	
	public static String AmassServerName							= "AmassServerName";   		
	public static String AmassServerPort							= "AmassServerPort";   	//<TCP> Agent         -> Server => log

	public static String CommandSessionPort						= "CommandSessionPort";     //<TCP> Agent         -> Server
																						   //[ View   -> Server ] -> Agent
	
	//public static String LogCaptureText								= "LogCaptureText";
	public static List<String> LogCaptureTextList					= new ArrayList<String>();

	
	/**************************************************************************************
	 * [EnvAmassServer]  value = "XML Node Name"
	 **************************************************************************************/
	
	public static String AggregateSchedulerYN								= "AggregateSchedulerYN";
	public static String ActionSchYN 												= "ActionSchYN";
	public static String ResponseSchYN 										= "ResponseSchYN";
	public static String ExpireSchYN 												= "ExpireSchYN";
	
	public static String AggregateDb_Repeat_ActionSec 				= "Repeat_ActionSec";
	public static String AggregateDb_Repeat_ActionMin					= "Repeat_ActionMin";
	public static String AggregateDb_Repeat_ActionHour 				= "Repeat_ActionHour";
	public static String AggregateDb_Repeat_ActionDay 				= "Repeat_ActionDay";
	                                                          
	public static String AggregateDb_Repeat_ResourceSec 			= "Repeat_ResourceSec";
	public static String AggregateDb_Repeat_ResourceMin			= "Repeat_ResourceMin";
	public static String AggregateDb_Repeat_ResourceHour 			= "Repeat_ResourceHour";
	public static String AggregateDb_Repeat_ResourceDay 			= "Repeat_ResourceDay";
	                                                          
	public static String AggregateDb_Postponed_TransactionSec	= "Postponed_TransactionSec";
	
	public static String AggregateDb_Postponed_ResourceSec		= "Postponed_ResourceSec";
	public static String AggregateDb_Postponed_ResourceMin		= "Postponed_ResourceMin";
	public static String AggregateDb_Postponed_ResourceHour 	= "Postponed_ResourceHour";
	public static String AggregateDb_Postponed_ResourceDay 	= "Postponed_ResourceDay";
	                                                          
	
	public static String AggregateDb_ExpireRepeat_Sec				= "ExpireRepeat_Sec";
	public static String AggregateDb_ExpireRepeat_Min					= "ExpireRepeat_Min";
	
	public static String AggregateDb_Expire_TActionSec 				= "Expire_TransActionSec";
	
	public static String AggregateDb_Expire_ActionSec 					= "Expire_ActionSec";
	public static String AggregateDb_Expire_ActionMin					= "Expire_ActionMin";
	public static String AggregateDb_Expire_ActionHour 				= "Expire_ActionHour";
	public static String AggregateDb_Expire_ActionDay 					= "Expire_ActionDay";
	                                                          
	public static String AggregateDb_Expire_ResourceSec 			= "Expire_ResourceSec";
	public static String AggregateDb_Expire_ResourceMin				= "Expire_ResourceMin";
	public static String AggregateDb_Expire_ResourceHour 			= "Expire_ResourceHour";
	public static String AggregateDb_Expire_ResourceDay 			= "Expire_ResourceDay";

	
	// new aggregator
	public static String TimerToExpireTrans_Sec=         "TimerToExpireTrans_Sec"       ;

	public static String TimerToExpireAction_Sec=        "TimerToExpireAction_Sec"      ;
	public static String TimerToExpireAction_Min=        "TimerToExpireAction_Min"      ;
	public static String TimerToExpireAction_Hour=       "TimerToExpireAction_Hour"     ;
	public static String TimerToExpireAction_Day=        "TimerToExpireAction_Day"      ;
	public static String TimerToExpireResource_Sec=      "TimerToExpireResource_Sec"    ;
	public static String TimerToExpireResource_Min=      "TimerToExpireResource_Min"    ;
	public static String TimerToExpireResource_Hour=     "TimerToExpireResource_Hour"   ;
	public static String TimerToExpireResource_Day=      "TimerToExpireResource_Day"    ;
	
	public static String IntervalToExpireTrans_Sec=      "IntervalToExpireTrans_Sec"    ;
	
	public static String IntervalToExpireAction_Sec=     "IntervalToExpireAction_Sec"   ;
	public static String IntervalToExpireAction_Min=     "IntervalToExpireAction_Min"   ;
	public static String IntervalToExpireAction_Hour=    "IntervalToExpireAction_Hour"  ;
	public static String IntervalToExpireAction_Day=     "IntervalToExpireAction_Day"   ;
	public static String IntervalToExpireResource_Sec=   "IntervalToExpireResource_Sec" ;
	public static String IntervalToExpireResource_Min=   "IntervalToExpireResource_Min" ;
	public static String IntervalToExpireResource_Hour=  "IntervalToExpireResource_Hour";
	public static String IntervalToExpireResource_Day=   "IntervalToExpireResource_Day" ;

	
	public static String Listener_AmassServerThreadInitPoolSize	= "Listener_AmassServerThreadInitPoolSize";
	public static String Listener_AmassServerThreadInitReqPoolSize	= "Listener_AmassServerThreadInitReqPoolSize";
	
	public static String 	Listener_AmassServerName						= "Listener_AmassServerName"; 	//  Agent -> Server
	public static String 	Listener_AmassServerPort						= "AmassServerPort";   					//  Agent -> Server 
	public static String 	Listener_AmassServerReqPort					= "AmassServerReqPort";				// [ View   -> Server ] -> Agent
	
	public static String	ransmit_ConsolName								= "ransmit_ConsolName";				//Server(Data) -> View
	public static String 	Transaction_Timeout									= "Transaction_Timeout";				//Transaction TimeOut ���� �ð� 
	
    public static String DbDriverURL 												= "Driver";
    public static String DbUser		 												= "DbUser";
    public static String DbUserPw  	 												= "DbUserPw";
    
    public static String DbUrl_Action												= "DbUrl_Action";
    public static String DbUrl_Resource											= "DbUrl_Resource";
    public static String DbUrl_TransAction										= "DbUrl_TransAction";
    public static String DbUrl_EventLog											= "DbUrl_EventLog";
    
	public static String LogsFolder													= "LogsFolder";
    
	private static String pathProperties = "";

	public static boolean isInitXml;
	public static boolean startAtLog = false;

	public final static String TransactionTimeoutWaitTime = "TransactionTimeoutWaitTime";
	public final static String TransactionTimeout = "TransactionTimeout";
	
	
	public boolean setProperties(String key, String value)
	{
		if(xmPropertiesXml.getPathProperties().equals("")) return false;
		File fi = new File(pathProperties);
		if(!fi.exists())
		{
			LOG.error("cannot find file ->" + pathProperties);
			return false;
		}
		dom2Writer d2xmlWrite = new dom2Writer(fi, xmPropertiesXml.propertiesTypeXmAmServer);
		
		if(key.equals("LogText"))
		{
			String[] values = value.split(xmPropertiesXml.Sep);
			String parentKey = d2xmlWrite.getParentKey(key);
			if(values.length >0 ) 
			{
				for(int d =0; d < xmPropertiesXml.LogCaptureTextList.size(); d++)
				{
					d2xmlWrite.setKeyValue(parentKey  , key, "", dom2Writer.setTypeDel);
				}
				xmPropertiesXml.LogCaptureTextList.clear();
			}
			for(int i=0; i<values.length; i++)
			{
				d2xmlWrite.setKeyValue(parentKey , key, values[i] , dom2Writer.setTypeAdd);
				xmPropertiesXml.LogCaptureTextList.add(values[i]);
			}
			
			return true;
		}
		else
		{
			return	d2xmlWrite.setKeyValue( d2xmlWrite.getParentKey(key) , key, value , dom2Writer.setTypeMod);
		}
	}
	private void setIntProperty(String key, String value, int defaultValue) {

		try {
			defaultValue = Integer.parseInt(value);
		} catch (Exception e) {
			LOG.warn("setting Property(" + key+") : " + e.getMessage());
		}
		htXmPropertiesAmass_Int.put(key, defaultValue);
	}
	
	public int getIntProperty(String key, int defaultValue) {
		Integer value = htXmPropertiesAmass_Int.get(key);
		if (value == null)
			return defaultValue;
		return value;
	}

	public boolean Init(String pathPropertiesFile) {
		return Init(pathPropertiesFile, "");
	}

	// parse config file
	public boolean Init(String pathPropertiesFile, String serverName) {
               
        ConfigReader cr = new ConfigReader(pathPropertiesFile, this);
		String cfgError = cr.parse();
		if (cfgError.length() > 0){
			LOG.error("Error parsing config file :" +  cfgError + ")");
			return false;
		}  
		return true;
	}

    private boolean inServer = false;
    private boolean inDB = false;
    private boolean inEventAdapter = false;
    private boolean inSms = false;
    private boolean inPostJob = false;
    private boolean inadapter = false;
    private String now = "";
    private String logClassName = "";
    private String commandAndValue = "";
	// Beginning XML section
	public void startParms(String szName, Hashtable attrs){
		// Main server section
		if (szName.equals("AMASSSERVER")){
			inServer = true;

			String MyName 				= (String)attrs.get("MYNAME");
			String MyIP 				= (String)attrs.get("IP");
             
            LOG.info("= MyName: " + MyName + ", IP:" + MyIP); 
            
            htXmPropertiesAmass_String.put(xmPropertiesXml.AServerMyName	, MyName);
    		htXmPropertiesAmass_String.put(Listener_AmassServerName			, MyIP);

    		// log folder
			String valueLogFolder 				= (String)attrs.get("LOGFOLDER");
			if (valueLogFolder == null)
				valueLogFolder = "log";
            LOG.info("= LogFolder: " + valueLogFolder); 
            htXmPropertiesAmass_String.put(LogsFolder , valueLogFolder);
    		
    		String compress= (String)attrs.get("NETWORKCOMPRESS");
    		if (compress != null && compress.equalsIgnoreCase("TRUE")){
    			xmPropertiesXml.networkCompress = true;
    		} else {
    			xmPropertiesXml.networkCompress = false;
    		}
    		
            setIntProperty(TransactionTimeoutWaitTime, (String)attrs.get("TRANSACTIONTIMEOUTWAITTIME"), 1);
            setIntProperty(TransactionTimeout, (String)attrs.get("TRANSACTIONTIMEOUT"), 60);
    		htXmPropertiesAmass_String.put(Transaction_Timeout, (String)attrs.get("TRANSACTIONTIMEOUT"));
		}
		else if (inServer && szName.equals("LOGFILTER")){
            LOG.info("= " + "LOGFILTER" + " : " + (String)attrs.get("TEXT"));
        	xmPropertiesXml.LogCaptureTextList.add((String)attrs.get("TEXT"));        	
        	try {
				xmPropertiesXml.startAtLog = Boolean.parseBoolean((String)attrs.get("START"));
			} catch (Exception e) {				
			}
		}
		//yys log4j 경로 설정 추가 UNIX,AIX서 제대로 경로 추가 안되는 점 디버그
		else if(inServer && szName.equals("LOGPATH")){			
			String path = (String)attrs.get("PATH");
			PropertyConfigurator.configure(path);
		}
		// agents
		else if (inServer && szName.equals("AGENT")){
			String agentName = (String)attrs.get("NAME");
			String agentIP = (String)attrs.get("IP");
			String agentPort = (String)attrs.get("PORT");
			String agentDesc = (String)attrs.get("DESC");
			String agentHealthUse = (String)attrs.get("HEALTHUSEYN");
            LOG.info("= " + "AGENT" + " : " + agentName);
            LOG.info("= " + "IP" + " : " + agentIP);
            LOG.info("= " + "PORT" + " : " + agentPort);
            LOG.info("= " + "HEALTHUSEYN" + " : " + agentHealthUse);
            
			ItemAgent agentListObject = xmPropertiesXml.htAgentList.get(agentName);
			if(agentListObject == null){
				xmPropertiesXml.htAgentList.putIfAbsent(agentName, new ItemAgent(agentName, agentIP, agentPort, agentHealthUse,agentDesc));
			}
			else
			{
				agentListObject.setAgentIP(agentIP);
				agentListObject.setAgentPort(agentPort);
				agentListObject.setHealthUseYN(agentHealthUse);
				agentListObject.setAgentDesc(agentDesc);
				xmPropertiesXml.htAgentList.put(agentName, agentListObject);
			}
		}
		else if (inServer && szName.equals("AMASSSERVERPORT")){
            LOG.info("= " + "AmassServerPort PoolInitSize" + " : " + (String)attrs.get("POOLINITSIZE"));
            LOG.info("= " + "AmassServerPort PORT" + " : " + (String)attrs.get("PORT"));
            
            htXmPropertiesAmass_String.put(Listener_AmassServerPort						, (String)attrs.get("PORT"));

            setIntProperty(Listener_AmassServerThreadInitPoolSize, (String)attrs.get("POOLINITSIZE"), 30);
		}
		else if (inServer && szName.equals("AMASSSERVERREQPORT")){
            LOG.info("= " + "AmassServerReqPort PoolInitSize" + " : " + (String)attrs.get("POOLINITSIZE"));
            LOG.info("= " + "AmassServerReqPort PORT" + " : " + (String)attrs.get("PORT"));
            
            htXmPropertiesAmass_String.put(Listener_AmassServerReqPort						, (String)attrs.get("PORT"));

            setIntProperty(Listener_AmassServerThreadInitReqPoolSize, (String)attrs.get("POOLINITSIZE"), 30);
		}
		else if (szName.equals("AGGREGATEDB")){
			inDB = true;

            htXmPropertiesAmass_String.put(AggregateSchedulerYN, (String)attrs.get("AGGREGATESCHEDULERYN"));
            
            htXmPropertiesAmass_String.put(ActionSchYN, (String)attrs.get("ACTIONSCHYN"));
            htXmPropertiesAmass_String.put(ResponseSchYN, (String)attrs.get("RESPONSESCHYN"));
            htXmPropertiesAmass_String.put(ExpireSchYN, (String)attrs.get("EXPIRESCHYN"));
            
            setIntProperty(AggregateDb_Repeat_ActionSec, (String)attrs.get("REPEAT_ACTIONSEC"), 1);
            setIntProperty(AggregateDb_Repeat_ActionMin, (String)attrs.get("REPEAT_ACTIONMIN"), 1);
            setIntProperty(AggregateDb_Repeat_ActionHour, (String)attrs.get("REPEAT_ACTIONHOUR"), 1);
            setIntProperty(AggregateDb_Repeat_ActionDay, (String)attrs.get("REPEAT_ACTIONDAY"), 1);
            
            setIntProperty(AggregateDb_Repeat_ResourceSec, (String)attrs.get("REPEAT_RESOURCESEC"), 1);
            setIntProperty(AggregateDb_Repeat_ResourceMin, (String)attrs.get("REPEAT_RESOURCEMIN"), 1);
            setIntProperty(AggregateDb_Repeat_ResourceHour, (String)attrs.get("REPEAT_RESOURCEHOUR"), 1);
            setIntProperty(AggregateDb_Repeat_ResourceDay, (String)attrs.get("REPEAT_RESOURCEDAY"), 1);
            
//            setIntProperty(AggregateDb_Postponed_TransactionSec, (String)attrs.get("POSTPONED_TRANSACTIONSEC"), 1);
//            
//            setIntProperty(AggregateDb_Postponed_ResourceSec, (String)attrs.get("POSTPONED_RESOURCESEC"), 1);
//            setIntProperty(AggregateDb_Postponed_ResourceMin, (String)attrs.get("POSTPONED_RESOURCEMIN"), 1);
//            setIntProperty(AggregateDb_Postponed_ResourceHour, (String)attrs.get("POSTPONED_RESOURCEHOUR"), 1);
//            setIntProperty(AggregateDb_Postponed_ResourceDay, (String)attrs.get("POSTPONED_RESOURCEDAY"), 1);
//            
//            setIntProperty(AggregateDb_ExpireRepeat_Sec, (String)attrs.get("EXPIREREPEAT_SEC"), 1);
//            setIntProperty(AggregateDb_ExpireRepeat_Min, (String)attrs.get("EXPIREREPEAT_MIN"), 1);
//            
//            setIntProperty(AggregateDb_Expire_TActionSec, (String)attrs.get("EXPIRE_TRANSACTIONSEC"), 1);
//            
//            setIntProperty(AggregateDb_Expire_ActionSec, (String)attrs.get("EXPIRE_ACTIONSEC"), 1);
//            setIntProperty(AggregateDb_Expire_ActionMin, (String)attrs.get("EXPIRE_ACTIONMIN"), 1);
//            setIntProperty(AggregateDb_Expire_ActionHour, (String)attrs.get("EXPIRE_ACTIONHOUR"), 1);
//            setIntProperty(AggregateDb_Expire_ActionDay, (String)attrs.get("EXPIRE_ACTIONDAY"), 1);
//
//            setIntProperty(AggregateDb_Expire_ResourceSec, (String)attrs.get("EXPIRE_RESOURCESEC"), 1);
//            setIntProperty(AggregateDb_Expire_ResourceMin, (String)attrs.get("EXPIRE_RESOURCEMIN"), 1);
//            setIntProperty(AggregateDb_Expire_ResourceHour, (String)attrs.get("EXPIRE_RESOURCEHOUR"), 1);
//            setIntProperty(AggregateDb_Expire_ResourceDay, (String)attrs.get("EXPIRE_RESOURCEDAY"), 1);
            
            // new aggregator
            setIntProperty(TimerToExpireTrans_Sec, (String)attrs.get("TIMERTOEXPIRETRANS_SEC"), 60);
            setIntProperty(TimerToExpireAction_Sec, (String)attrs.get("TIMERTOEXPIREACTION_SEC"), 60);
            setIntProperty(TimerToExpireAction_Min, (String)attrs.get("TIMERTOEXPIREACTION_MIN"), 60);
            setIntProperty(TimerToExpireAction_Hour, (String)attrs.get("TIMERTOEXPIREACTION_HOUR"), 60);
            setIntProperty(TimerToExpireAction_Day, (String)attrs.get("TIMERTOEXPIREACTION_DAY"), 60);
            setIntProperty(TimerToExpireResource_Sec, (String)attrs.get("TIMERTOEXPIRERESOURCE_SEC"), 60);
            setIntProperty(TimerToExpireResource_Min, (String)attrs.get("TIMERTOEXPIRERESOURCE_MIN"), 60);
            setIntProperty(TimerToExpireResource_Hour, (String)attrs.get("TIMERTOEXPIRERESOURCE_HOUR"), 60);
            setIntProperty(TimerToExpireResource_Day, (String)attrs.get("TIMERTOEXPIRERESOURCE_DAY"), 60);
            setIntProperty(IntervalToExpireTrans_Sec, (String)attrs.get("INTERVALTOEXPIRETRANS_SEC"), 60);
            setIntProperty(IntervalToExpireAction_Sec, (String)attrs.get("INTERVALTOEXPIREACTION_SEC"), 60);
            setIntProperty(IntervalToExpireAction_Min, (String)attrs.get("INTERVALTOEXPIREACTION_MIN"), 60);
            setIntProperty(IntervalToExpireAction_Hour, (String)attrs.get("INTERVALTOEXPIREACTION_HOUR"), 60);
            setIntProperty(IntervalToExpireAction_Day, (String)attrs.get("INTERVALTOEXPIREACTION_DAY"), 60);
            setIntProperty(IntervalToExpireResource_Sec, (String)attrs.get("INTERVALTOEXPIRERESOURCE_SEC"), 60);
            setIntProperty(IntervalToExpireResource_Min, (String)attrs.get("INTERVALTOEXPIRERESOURCE_MIN"), 60);
            setIntProperty(IntervalToExpireResource_Hour, (String)attrs.get("INTERVALTOEXPIRERESOURCE_HOUR"), 60);
            setIntProperty(IntervalToExpireResource_Day, (String)attrs.get("INTERVALTOEXPIRERESOURCE_DAY"), 60);


		}
		else if (inDB && szName.equals("POOL")){
			DBProperties db = DBProperties.getInstance();
			
			String prefix = (String)attrs.get("TABLEPREFIX");
            if(prefix == null)
            	prefix = "";
            //암호화 작업 넣을 폴더
            String PSWD = (String)attrs.get("PSWD");           
            try {
				String et = (String)attrs.get("ENCRYPT");
				//FIXME:
//				if(et.equalsIgnoreCase("true"))
//					PSWD = ABC.de();
			} catch (Exception e) {
				System.out.println("Encrypt not active");
			}
			
			db.addPool((String)attrs.get("NAME"), (String)attrs.get("DRIVER"), (String)attrs.get("CONNECT"),
					(String)attrs.get("USER"),
					PSWD,
					(String)attrs.get("COUNT"),
					(String)attrs.get("PRECONNECT"), (String)attrs.get("UNICODE"),
					(String)attrs.get("DBTYPE"), prefix,
					(String)attrs.get("CONNECTIONPROPERTIES"),
					(String)attrs.get("CONNECTIONTIMEOUT"));
		}
		else if (szName.equals("EVENTADAPTER")){
			inEventAdapter = true;
			String sInterval = (String)attrs.get("CHECKINTERVAL");
			int interval = EventManager.DEFAULT_CHECKINTERVAL;
			try {
				interval = Integer.parseInt(sInterval);
			} catch (Exception e) {
			}
			EventManager.getInstance().setCheckInterval(interval);
		}
		else if (inEventAdapter && szName.equals("ADAPTER")){
			EventManager.getInstance().addAdapter((String)attrs.get("NAME"), (String)attrs.get("CLASS"), attrs);
			now = (String)attrs.get("NAME");
		}
		else if(inEventAdapter && szName.equals("PROPERTY")){
			String name = (String) attrs.get("NAME");
			String value = (String) attrs.get("VALUE");	
			String max = (String) attrs.get("LIMITCOUNT");	
			IAdapter adapter = EventManager.getInstance().getAdapters().get(now);
			adapter.getPropertMap().put(name, value);
			adapter.setLimitCount(max);
		}
		else if(szName.equals("SMS")){
			inSms = true;
			String className = (String) attrs.get("CLASS");
			String interval = (String) attrs.get("WAITTIME");			 
			try {
				Class logClass = Class.forName(className);
				Object[] args = {};
				Constructor[] cons = logClass.getConstructors();
				SendSMS sendSMS = (SendSMS)cons[0].newInstance(args);
				xMonitoringServerMain.setSendSMS(sendSMS);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 			
		}
		else if(inSms && szName.equals("USER")){
			String name = (String) attrs.get("NAME");
			String num = (String) attrs.get("NUM");	
			SendSMS sendSMS = xMonitoringServerMain.getSendSMS();
			sendSMS.setUser("speno", "speno1234");
			//for test
			sendSMS.addSmsMgs(num, "01050047887", "for" + name + "\n teemo put mushroom on ground. please help us to make our world mushroom ground");			
		}
		else if(szName.equals("POSTJOB")){
			inPostJob = true;
		}
		else if(inPostJob && szName.equals("JOB")){
			try {
				String name = (String) attrs.get("NAME");
				String className = (String) attrs.get("CLASS");
				String user = (String) attrs.get("USER");
				String pswd = (String) attrs.get("PSWD");
				String connect = (String) attrs.get("CONNECT");
				String driver = (String) attrs.get("DRIVER");
				Class logClass = Class.forName(className);
				Object[] args = {};
				Constructor[] cons = logClass.getConstructors();
				PostJob job = (PostJob)cons[0].newInstance(args);
				job.init(user,pswd,connect,driver);
				PostJobController.getJobMap().put(name, job);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	// Ending XML section
	public void endParms(String szName){
		// Main server section
		if (szName.equals("AMASSSERVER") && inServer){
			inServer = false;
		}
		else if (szName.equals("AGGREGATEDB") && inDB){
			inDB = false;
			DBProperties.getInstance().start();			
			isInitXml = true;
		}
		else if (szName.equals("EVENTADAPTER") && inEventAdapter){
			inEventAdapter = false;
			EventManager.getInstance().startup();
		}
		else if(szName.equals("SMS") && inSms){
			inSms = false;
		}
		else if(szName.equals("ADAPTER") && inadapter){
			inadapter = false;
		}
		
	}
    
	private String getNodeValue(Node node, String key) {
     	return this.getChildNodeValue(node.getChildNodes(), key);
	}
	private String getChildNodeValue(NodeList nl, String key) {
		Node nod = this.getChildNode(nl, key);
		
		if(nod == null) return "";
		return nod.getFirstChild().getNodeValue().trim();
	}
	private Node getChildNode(NodeList nl, String key) {
		   for (int i = 0;null!=nl && i < nl.getLength(); i++)
           {
               Node nod = nl.item(i);
               if(nod == null) continue;
               if(nod.getNodeType() != Node.ELEMENT_NODE) continue;
               if(nod.getFirstChild() == null) continue;
               
               if(nod.getNodeName().equals(key.trim()))
               	  return nod;
               else continue;
           }
		return null;
	}
	private String getNodeTypeValue(Node node, String attributeValue, String key) {

		NodeList nl =node.getChildNodes();
		for(int n=0; null!=nl && n<nl.getLength(); n++)
		{
			Node subNode = nl.item(n);
			if(subNode.getNodeType() != Node.ELEMENT_NODE) continue;;
	 	   	NamedNodeMap Propertysname = subNode.getAttributes();
	        for (int i = 0;null!=Propertysname && i< Propertysname.getLength(); i++) 
	        {
	             Node tempNode = Propertysname.item(i);
	             if(tempNode.getNodeType() != Node.ATTRIBUTE_NODE) return null;
	             if(Propertysname.item(i).getNodeName().equals("Type") &&  tempNode.getNodeValue().equals(attributeValue))
	             {
	            	//무의미한 주석코드 제거	            	 
	                 return subNode.getFirstChild().getNodeValue().trim();
	             }
	        }
		}
     	
		return null;
	}
	private String getAttributeValue(Node node, String attributeName) {
     	if(node.getNodeType() != Node.ELEMENT_NODE) return null;
 	   	NamedNodeMap Propertysname = node.getAttributes();
        for (int i = 0;null!=Propertysname && i< Propertysname.getLength(); i++) 
        {
             Node tempNode = Propertysname.item(i);
             if(tempNode.getNodeType() != Node.ATTRIBUTE_NODE) return null;
             if(Propertysname.item(i).getNodeName().equals(attributeName))
             {
            	 return tempNode.getFirstChild().getNodeValue();
             }
        }
		return null;
	}
	/*
	 * XmAgentList
	 */
	public static String GetAgentName(String searchAgentIP) 
	{
		String agentName = "";
		Enumeration<String> agentNames = xmPropertiesXml.htAgentList.keys();
		while(agentNames.hasMoreElements())
		{
			agentName = agentNames.nextElement();
			if(searchAgentIP.equals(xmPropertiesXml.htAgentList.get(agentName).getAgentIP()))  return agentName; 
		}
		return "";
	}
    
	public static String GetAgentIP(String searchAgentName) 
	{
		return xmPropertiesXml.htAgentList.get(searchAgentName).getAgentIP();
	}

	/*
	 * /192.168.0.27:50938
	 */
	public static String GetAgentIP(SocketAddress remoteAddress) {
		String[] temp =  remoteAddress.toString().split(":");
		if(temp.length != 2) return "";
		if(temp[0].startsWith("/")) temp[0] = temp[0].substring(1); 
		return temp[0];
		
		/*
		 *             String tempRemoteAddress 		= remoteAddress.toString();
            tempRemoteAddress					= tempRemoteAddress.replace("/", "");
            String[] agentIpPort 					= tempRemoteAddress.split(":");
		 */
	}

	/**
	 * @return the pathProperties
	 */
	public static String getPathProperties() {
		return pathProperties;
	}

	/**
	 * @param pathProperties the pathProperties to set
	 */
	public static void setPathProperties(String pathProperties) {
		xmPropertiesXml.pathProperties = pathProperties;
	}

}
