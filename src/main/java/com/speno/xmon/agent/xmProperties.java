package com.speno.xmon.agent;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.speno.xmon.codedic.DicCommands;
import com.speno.xmon.codedic.DicXmlProperties;
import com.speno.xmon.env.ConfigIF;
import com.speno.xmon.env.ConfigReader;
import com.speno.xmon.env.dom2Writer;

public class xmProperties implements ConfigIF, ILogListener {
	private static ILogger LOG = new LoggerDummy();
	
	public static ResourceCommandManager resourceManager;
	
	public static Hashtable<String, String> htXmPropertiesAgent_Main = new Hashtable<String, String>();
	
    public static final String AgentIP 								= "AgentIP";
    public static final String AgentPort							= "AgentPort";

	public static boolean useProtocolCodecFilter 	= true;
	public static boolean networkCompress = false;

	public static final String Sep										= "&";

	/**************************************************************************************
     * propertiesType
     **************************************************************************************/
    public static String propertiesTypeXmAgent 				= "XmAgent";
    public static String propertiesTypeXmAmServer 		= "XmAmServer";

    /**************************************************************************************
	 * [EnvAgent]
	 **************************************************************************************/
    public static String AgentMyName							= "AgentMyName";
    public static String ResourceCommand						= "ResourceCommand";
    
	public static String MaxJsonDataLength					= "MaxJsonDataLength";
	public static String MaxJsonDataCount						= "MaxJsonDataCount";
	
	public static String SessionCheckRepeatMs				= "SessionCheckRepeatMs";
	
	public static String TransactionRepeatMs					= "TransactionRepeatMs";  
	public static String TransactionRetryConnectionMs	= "TransactionRetryConnectionMs";
	
	public static String PUTLogUseYN							= "PUTLogUseYN";  
	public static String PUTEventUseYN						= "PUTEventUseYN";
	
	public static String AmassServerIP							= "AmassServerIP";		//<UDP>	Agent         -> Server
	public static String AmassTransPort							= "AmassTransPort";		//<UDP> Agent         -> Server

	public static String AmassCommdPort						= "AmassCommdPort";	//<TCP> Agent         -> Server
																														//[ View   -> Server ] -> Agent

	public static List<String> LogCaptureTextList				= new ArrayList<String>();

	public static String LogIdComma	= "Log";


	 //boolean b = m.matches();

	/**************************************************************************************
	 * [EnvAmassServer]
	 **************************************************************************************/

	private static String pathProperties = "";
	
	public void setLogger(ILogger log) {
		LOG = log;
	}
	
	public boolean setProperties(String key, String value)
	{
		if(xmProperties.getPathProperties().equals("")) return false;
		File fi = new File(pathProperties);
		if(!fi.exists())
		{
			LOG.error("cannot find property file. ->" + pathProperties);
			return false;
		}
		dom2Writer d2xmlWrite = new dom2Writer(fi, xmProperties.propertiesTypeXmAmServer);
		
		if(key.equals("LogText"))
		{
			String[] values = value.split(xmProperties.Sep);
			String parentKey = d2xmlWrite.getParentKey(key);
			if(values.length >0 ) 
			{
				for(int d =0; d < xmProperties.LogCaptureTextList.size(); d++)
				{
					d2xmlWrite.setKeyValue(parentKey  , key, "", dom2Writer.setTypeDel);
				}
				xmProperties.LogCaptureTextList.clear();
			}
			for(int i=0; i<values.length; i++)
			{
				d2xmlWrite.setKeyValue(parentKey , key, values[i] , dom2Writer.setTypeAdd);
				xmProperties.LogCaptureTextList.add(values[i]);
			}
			
			return true;
		}
		else
		{
			return	d2xmlWrite.setKeyValue( d2xmlWrite.getParentKey(key) , key, value , dom2Writer.setTypeMod);
		}
	}

    private boolean inAgent = false;
    private String logClassName = "";
    private String commandAndValue = "";
	// Beginning XML section
	public void startParms(String szName, Hashtable attrs){
		// Main server section
		if (szName.equals("AGENT")){
			inAgent = true;

			String MyName 				= (String)attrs.get("MYNAME");
             
            LOG.info("= MyName: " + MyName);
            
            htXmPropertiesAgent_Main.put(xmProperties.AgentMyName, MyName.trim());
            
			String compress 				= (String)attrs.get("NetworkCompress");
    		if (compress != null && compress.equalsIgnoreCase("true")){
    			networkCompress = true;
    		} else {
    			networkCompress = false;
    		}

		}
		else if (inAgent && szName.equals("LOGGER")){
			logClassName = (String)attrs.get("LOGCLASS");
            LOG.info("= " + "LOGGER" + " : " + logClassName);
		}
		else if (logClassName.length() > 0 && szName.equals("PROPERTY")){
			createLogWriter(logClassName, attrs);
		}
		else if (inAgent && szName.equals("LOGFILTER")){
            LOG.info("= " + "LOGFILTER" + " : " + (String)attrs.get("TEXT"));
        	xmProperties.LogCaptureTextList.add((String)attrs.get("TEXT"));
		}
		else if (inAgent && szName.equals("LOGID")){
            LOG.info("= " + "LOGID" + " : " + (String)attrs.get("TEXT"));
        	xmProperties.LogIdComma = (String)attrs.get("TEXT");
		}
		else if (szName.equals("AMASSSERVER")){
			inAgent = true;
            
			String value = (String)attrs.get("AMASSSERVERIP");
        	if(value != null && value.length() > 0)
        		htXmPropertiesAgent_Main.put(xmProperties.AmassServerIP		, value);
        	value = (String)attrs.get("AMASSTRANSPORT");
        	if(value != null && value.length() > 0)
        		htXmPropertiesAgent_Main.put(xmProperties.AmassTransPort		, value);
        	value = (String)attrs.get("AMASSCOMMDPORT");
        	if(value != null && value.length() > 0)
        		htXmPropertiesAgent_Main.put(xmProperties.AmassCommdPort		, value);
        	value = (String)attrs.get("SESSIONCHECKREPEATMS");
        	if(value != null && value.length() > 0)
        		htXmPropertiesAgent_Main.put(xmProperties.SessionCheckRepeatMs		, value);
        	value = (String)attrs.get("MAXJSONDATALENGTH");
        	if(value != null && value.length() > 0)
        		htXmPropertiesAgent_Main.put(xmProperties.MaxJsonDataLength		, value);
        	value = (String)attrs.get("MAXJSONDATACOUNT");
        	if(value != null && value.length() > 0)
        		htXmPropertiesAgent_Main.put(xmProperties.MaxJsonDataCount		, value);
        	value = (String)attrs.get("TRANSACTIONREPEATMS");
        	if(value != null && value.length() > 0)
        		htXmPropertiesAgent_Main.put(xmProperties.TransactionRepeatMs		, value);
        	value = (String)attrs.get("TRANSACTIONRETRYCONNECTIONMS");
        	if(value != null && value.length() > 0)
        		htXmPropertiesAgent_Main.put(xmProperties.TransactionRetryConnectionMs		, value);
        	value = (String)attrs.get("PUTLOGUSEYN");
        	if(value != null && value.length() > 0)
        		htXmPropertiesAgent_Main.put(xmProperties.PUTLogUseYN		, value);
        	value = (String)attrs.get("PUTEVENTUSEYN");
        	if(value != null && value.length() > 0)
        		htXmPropertiesAgent_Main.put(xmProperties.PUTEventUseYN		, value);
		}
		else if (szName.equals("COMMAND")){
			
			HashMap<String, String> m = new HashMap<String, String>();

			String type = (String) attrs.get("TYPE");
			if ("TRAN".equalsIgnoreCase(type)) {
				String actionName = (String) attrs.get("NAME");
				m.put(DicXmlProperties.TranName, actionName);

				String title = (String) attrs.get(DicXmlProperties.Title.toUpperCase());
				if (title == null)
					title = "";
				m.put(DicXmlProperties.Title, title);

				String description = (String) attrs.get(DicXmlProperties.Description.toUpperCase());
				if (description == null)
					description = "";
				m.put(DicXmlProperties.Description, description);

				String aggreUseYN = (String) attrs.get(DicXmlProperties.AggreUseYN.toUpperCase());
				if (aggreUseYN == null)
					aggreUseYN = "N";
				m.put(DicXmlProperties.AggreUseYN, aggreUseYN);

				String healthUseYN = (String) attrs.get(DicXmlProperties.HealthUseYN.toUpperCase());
				if (healthUseYN == null)
					healthUseYN = "N";
				m.put(DicXmlProperties.HealthUseYN, healthUseYN);

				if (!DicCommands.AgentMyActionsList.containsKey(actionName))
					DicCommands.AgentMyActionsList.put(actionName, m);

				LOG.info("-------------------------------------------------------------------");
				LOG.info("ActionName:\t\t" + actionName);
				LOG.info("Title:\t\t\t" + title);
				LOG.info("Description:\t\t" + description);
				LOG.info("AggreUseYN:\t" + aggreUseYN);
				LOG.info("HealthUseYN:\t" + healthUseYN);
			}
			else if ("RESO".equalsIgnoreCase(type)) {
	         	String commandName = (String) attrs.get("NAME");
	         	
	            String classPath		= (String) attrs.get(DicXmlProperties.ClassPath.toUpperCase());
	            String valueUnit		= (String) attrs.get(DicXmlProperties.ValueUnit.toUpperCase());
	            String title		= (String) attrs.get(DicXmlProperties.Title.toUpperCase());
	            String description		= (String) attrs.get(DicXmlProperties.Description.toUpperCase());
	            String aggreUseYN		= (String) attrs.get(DicXmlProperties.AggreUseYN.toUpperCase());
	            String healthUseYN		= (String) attrs.get(DicXmlProperties.HealthUseYN.toUpperCase());

	            LOG.info("--------------------------------------------------------------------------");
	         	LOG.info("CommandName:" + commandName);
	            LOG.info(DicXmlProperties.ClassPath 		+ ":\t" + classPath);
	            LOG.info(DicXmlProperties.ValueUnit 		+ ":\t" + valueUnit);
	            LOG.info(DicXmlProperties.Title 				+ ":\t\t\t" + title);
	            LOG.info(DicXmlProperties.Description 	+ ":\t" + description);
	            LOG.info(DicXmlProperties.AggreUseYN 	+ ":\t" + aggreUseYN);
	            LOG.info(DicXmlProperties.HealthUseYN 	+ ":\t" + healthUseYN);
	            
	            commandAndValue =  commandName + "(" + valueUnit +Sep + title +Sep + description + Sep + aggreUseYN + Sep + healthUseYN +")";
	            
	            this.setResourceClass(commandName,classPath);
			}
		}
		else if (commandAndValue.length() > 0 && szName.equals("RESOURCES")) {
         	String resourcesID = (String) attrs.get("NAME");
         	String properties = (String) attrs.get("PROPERTIES");
         	
         	HashMap<String,List<String>> commandID = null;
         	String[] props = properties.split(",");
         	if (props != null) {
				List<String> propertyName = new ArrayList<String>();
         		for (String prop : props) {
    					propertyName.add(prop.trim());
				}

         		commandID = DicCommands.ResourceCommand.get(commandAndValue);
	         	
	         	if (commandID == null)
	         		commandID = new HashMap<String, List<String>>();
	         	
   				commandID.put(resourcesID.trim(), propertyName);
         	}
         	
     		DicCommands.ResourceCommand.put(commandAndValue, commandID);
		}
	}

	// Ending XML section
	public void endParms(String szName){
		// Main server section
		if (szName.equals("AGENT") && inAgent){
			inAgent = false;
		}
		else if (szName.equals("LOGGER")){
			logClassName = "";
		}

	}
    
    public boolean Init(String pathPropertiesFile)
    {
    	MainXAgent.addLogListener(this);
    	
        LOG.info("=====================================================");
        LOG.info("= XML Path:" +pathPropertiesFile);
        LOG.info("=====================================================");

        ConfigReader cr = new ConfigReader(pathPropertiesFile, this);
		String cfgError = cr.parse();
		if (cfgError.length() > 0){
			LOG.error("Error parsing config file " + pathPropertiesFile + " (" + cfgError + ")");
			return false;
		}
  
		return true;
	}

	// Creates logger
	private void createLogWriter(String className, Hashtable attrs){
		LOG = MainXAgent.getLogger();
		try{
			Class logClass = Class.forName(className);
			Object[] args = {};
			Constructor[] cons = logClass.getConstructors();
			ILogger log = (ILogger) cons[0].newInstance(args);
			log.init(attrs);
			log.info("Log writer " + className + " started");
			MainXAgent.setLogger(log);
		}
		catch (Exception e){
			LOG.error("Unable to start log Writer " + className + " (" + e.getMessage() + ")");
		}
	}

	private boolean setResourceClass(String commandName, String classPath) {
		try {
			if(classPath.equals("")) return false;
			resourceManager.setResource(commandName, (IxMon)Class.forName(classPath).newInstance());
			return true;
		}
		catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//"com.speno.xmon.amass.real.JavaHeap.AmassJavaHeap"
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	private String getChildNodeValue(NodeList nl, String key) {
		Node nod = this.getChildNode(nl, key);
		
		if(nod == null || nod .equals("")) return null;
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
	private String getAttributeValue(Node node, String attributeName) {
     	if(node.getNodeType() != Node.ELEMENT_NODE) return null;
 	   	NamedNodeMap Propertysname = node.getAttributes();
        for (int i = 0;null!=Propertysname && i< Propertysname.getLength(); i++) 
        {
             Node tempNode = Propertysname.item(i);
             if(tempNode.getNodeType() != Node.ATTRIBUTE_NODE) return null;
             if(Propertysname.item(i).getNodeName().equals(attributeName))
             {
            	 LOG.info(tempNode.getFirstChild().getNodeValue());
            	 return tempNode.getFirstChild().getNodeValue();
             }
        }
		return null;
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
		xmProperties.pathProperties = pathProperties;
	}

}
