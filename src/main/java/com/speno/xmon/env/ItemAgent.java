package com.speno.xmon.env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.core.session.IoSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemAgent
{
	private final static Logger LOG = LoggerFactory.getLogger(ItemAgent.class);
	
	public static String KeyAgentName				= "AgentName"; 
	public static String KeyCMDCommandName	= "CommandName";
	public static String KeyCMDValueUnit 			= "ValueUnit";
	public static String KeyCMDCommandDesc 	= "CommandDesc";
	public static String KeyCMDCommandTitle 	= "CommandTitle";
	public static String KeyCMDAggreUseYN 	= "AggreUseYN";
	public static String KeyCMDResourceID 		= "ResourceID";
	public static String KeyCMDPropertyName 	= "PropertyName";
	public static String KeyCMDHealthUseYN		= "HealthUseYN";
	
	public static String KeyACTActionName		= "ActionName";
	public static String KeyACTActionDesc		= "ActionDesc";
	public static String KeyACTActionTitle		= "ActionTitle";

	public static String KeyACTAggreUseYN		= "AggreUseYN";
	public static String KeyACTHealthUseYN		= "HealthUseYN";
	
	public ItemAgent() {	}
	
	public ItemAgent(String agentName, String agentIP, String agentPort, String agentHealthUseYN,String desc) {
		this.setAgentName(agentName);
		this.setAgentIP(agentIP);
		if(agentPort.equals("")) agentPort = "0"; 
		this.setAgentPort(Integer.parseInt(agentPort));
		this.agentDesc = desc;
		if(agentHealthUseYN.toUpperCase().trim().equals("Y"))
			this.setHealthUse(true);
		else
			this.setHealthUse(false);
	}
	/*
	public ItemAgent(String agentName, String agentIP, String agentPort, JSONArray cmdAll) {
		ItemAgent(agentName, agentIP, agentPort);
		this.setCommandList(cmdAll);
	}
 */
	private String agentName				= "";
	private int agentPort 					= 0;
	private String agentIP 					= "";
	private boolean agentHealthUse	= false;;
	private String agentDesc = "";
	/*
	public<T> void paralleRecursive(final Executor exec, List<Node<T>> nodes, final Collection<T> results){
        for(final Node<T> n : nodes){
                  exec.execute(new Runnable(){
                             public void run(){
                                       results.add(n.compute());
                             }
                  });
                  parallelRecursive(exec, n.getChildren(), results);
        }
	}
*/

	public String getAgentDesc() {
		return agentDesc;
	}

	public void setAgentDesc(String agentDesc) {
		this.agentDesc = agentDesc;
	}
	private ConcurrentMap<IoSession, Boolean>	ioSessionMap	= new ConcurrentHashMap<IoSession, Boolean>();
	private List<ItemCommandListAndSub> cmds 					= new ArrayList<ItemCommandListAndSub>();  
	private JSONArray cmdsJsonArrayList 								= new JSONArray();


	
	public String getAgentName() {
		return agentName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public boolean getMinaSessionCheck() {
		IoSession temp = this.getMinaSession(true);
		if(temp == null) return false;
		if(temp.isConnected()) return true;
		return false;
	}
	public JSONArray getCommandListAndSubJsonArray() {
		return cmdsJsonArrayList;
	}
	public List<ItemCommandListAndSub> getCommandList() {
		return this.cmds;
	}
	
	public IoSession getMinaSession(boolean realUse)
	{
		IoSession tempIoSession ;
		Iterator<IoSession> iter =   ioSessionMap.keySet().iterator();
		while(iter.hasNext())
		{
			tempIoSession = iter.next();
			if(tempIoSession != null ) 
			if(tempIoSession.isConnected())
			{
				/*
				boolean bUsing = ((Boolean) tempIoSession.getAttribute("using"));
				if(bUsing) 
			    {
					//continue;
			    }
			    */
				if(realUse)
				{
					//ioSessionMap.get(tempIoSession).getAndIncrement();
					tempIoSession.setAttribute("using", true);
					LOG.debug(tempIoSession.getRemoteAddress() +  " session useCnt:" + ioSessionMap.get(tempIoSession));
				}
				return tempIoSession;
			}
			else
			{
				ioSessionMap.remove(tempIoSession);
			}
		}
		return null;
	}
	public void setMinaSession( IoSession session)
	{
		session.setAttribute("using", false);
		ioSessionMap.put(session, true );
	}
	public int getAgentPort() {
		return agentPort;
	}
	public void setAgentPort(String agentPort) {
		this.setAgentPort(Integer.parseInt(agentPort));
	}
	public void setAgentPort(int agentPort) {
		this.agentPort = agentPort;
	}
	public String getAgentIP() {
		return agentIP;
	}
	public void setAgentIP(String agentIP) {
		this.agentIP = agentIP;
	}
	
	public void setHealthUseYN(String b) {
		if(b.toUpperCase().trim().equals("Y"))
			this.agentHealthUse = true;
		else
			this.agentHealthUse = false;
	}
	public void setHealthUse(boolean b) {
		this.agentHealthUse = b;
	}
	public boolean getHealthUse() {
		return this.agentHealthUse;
	}
	private ConcurrentMap<String, ArrayList<String>> htResourceCMDList = new ConcurrentHashMap<String, ArrayList<String>>(); 
	/*
	 * key: Req_JavaHeap_USE
	 * value: MB
	 */
	public HashMap<String, String> Command_Req_ValueUnit= new HashMap<String,String>();
	
	public void setCommandListAndSubJsonArray(JSONArray cmdAll) {
		this.cmdsJsonArrayList = cmdAll;
		try {
				for(int i=0;i< cmdAll.length();i++)
				{
					ItemCommandListAndSub tempCommand = new ItemCommandListAndSub();	
					JSONObject jsonObject;
		
					jsonObject = cmdAll.getJSONObject(i);
					
					tempCommand.SetAgentName(			jsonObject.getString(ItemAgent.KeyAgentName)); 
					tempCommand.SetCommandName(	jsonObject.getString(ItemAgent.KeyCMDCommandName));
					tempCommand.SetValueUnit(				jsonObject.getString(ItemAgent.KeyCMDValueUnit));
					tempCommand.SetCommandDesc(		jsonObject.getString(ItemAgent.KeyCMDCommandDesc));
					tempCommand.SetCommandTitle(		jsonObject.getString(ItemAgent.KeyCMDCommandTitle));
					tempCommand.SetAggreUseYN(			jsonObject.getString(ItemAgent.KeyCMDAggreUseYN));
					tempCommand.SetResourceID(			jsonObject.getString(ItemAgent.KeyCMDResourceID));
					tempCommand.SetPropertyName(		jsonObject.getString(ItemAgent.KeyCMDPropertyName));
					tempCommand.SetHealthUseYN (		jsonObject.getString(ItemAgent.KeyCMDHealthUseYN));
					
					this.cmds.add(tempCommand);
				
					if(!tempCommand.GetAggreUse()) continue;
					
					String tempHtCmdListKey	= tempCommand.GetCommandName() + xmPropertiesXml.Sep + tempCommand.GetResourceID();
					String tempCmdJsonKey	= tempCommand.GetCommandName() + xmPropertiesXml.Sep + tempCommand.GetResourceID() + xmPropertiesXml.Sep + tempCommand.GetPropertyName();
					
					if(htResourceCMDList.containsKey(tempHtCmdListKey))
					{
						if(!htResourceCMDList.get(tempHtCmdListKey).contains(tempCmdJsonKey))
							htResourceCMDList.get(tempHtCmdListKey).add(tempCmdJsonKey);
					}
					else
					{
						htResourceCMDList.put(tempHtCmdListKey, new  ArrayList<String>());
						htResourceCMDList.get(tempHtCmdListKey).add(tempCmdJsonKey);
					}
					
					Command_Req_ValueUnit.put(tempHtCmdListKey, tempCommand.GetValueUnit());
				}
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public ConcurrentMap<String, ArrayList<String>> getHtResourceCMDList() {
		return  this.htResourceCMDList;
	}
	public void printAll() {
		String cmdString = "";
		for(int i=0; i< this.cmdsJsonArrayList.length(); i++)
		{
			//JSONObject i += ++i + ":" + cmdsJsonArrayList.getJSONObject(i) + " \n";			
		}
 
		LOG.info("\n# Target AgentName: " + this.agentName 
		         //+ "\n# IP:" + this.agentIP 
		         //+ "\n# Port:" + this.agentPort 
		         + "\n# CMD:\n" + cmdString );
	}
	
	public class ItemCommandListAndSub
	{
		private String agentName			="";
		private String commandName	="";
		private String commandDesc		="";
		private String commandTitle		="";
		private String valueUnit				="";
		private boolean aggreUseYN	=false;
		private boolean healthUseYN	=false;
		private String resourceID			="";
		private String propertyName		="";
		
		public ItemCommandListAndSub build() {
			return this;
		}
		
		public ItemCommandListAndSub SetHealthUseYN(String strHealthUseYN) {
			if(strHealthUseYN.trim().toUpperCase().equals("Y") || strHealthUseYN.trim().toUpperCase().equals("TRUE"))
				this.healthUseYN = true;
			else
				this.healthUseYN = false;
			
			return this;
		}

		public ItemCommandListAndSub SetAggreUseYN(String strAggreUseYN) {
			if(strAggreUseYN.trim().toUpperCase().equals("Y") || strAggreUseYN.trim().toUpperCase().equals("TRUE"))
				this.aggreUseYN = true;
			else
				this.aggreUseYN = false;
			
			return this;
		}
		public ItemCommandListAndSub SetCommandTitle(String commandTitle) {
			this.commandTitle = commandTitle;
			return this;
		}
		public ItemCommandListAndSub SetCommandDesc(String commandDesc) {
			this.commandDesc =commandDesc;
			return this;
		}
		public ItemCommandListAndSub SetAgentName(String agentName) {
			this.agentName = agentName;
			return this;
		}
		public ItemCommandListAndSub SetPropertyName(String propertyName) {
			this.propertyName =  propertyName;
			return this;
		}
		public ItemCommandListAndSub SetResourceID(String resourceID) {
			this.resourceID = resourceID;
			return this;
		}
		public ItemCommandListAndSub SetValueUnit(String valueUnit) {
			this.valueUnit = valueUnit;
			return this;
		}
		
		public ItemCommandListAndSub SetCommandName(String commandName) {
			this.commandName =commandName;
			return this;
		}
		public String GetAgentName() {
			return agentName;
		}
		public String GetCommandName() {
			return commandName;
		}
		public String GetCommandDesc() {
			if(this.commandDesc.equals("null")) this.commandDesc = "";
			return commandDesc;
		}
		public String GetCommandTitle() {
			if(this.commandTitle.equals("null")) this.commandTitle = "";
			return commandTitle;
		}
		public String GetValueUnit() {
			if(this.valueUnit.equals("null")) this.valueUnit = "";
			return valueUnit;
		}
		public boolean GetAggreUse() {
			return aggreUseYN;
		}
		public String GetAggreUseYN() {
			String strAggreUseYN = "Y";
			if(this.GetAggreUse()) strAggreUseYN = "Y";
			else strAggreUseYN = "N";
			
			return strAggreUseYN;
		}
		public boolean GetHealthUse() {
			return healthUseYN;
		}
		public String GetHealthUseYN() {
			String strHealthUseYN = "Y";
			if(this.GetHealthUse()) strHealthUseYN = "Y";
			else strHealthUseYN = "N";
			
			return strHealthUseYN;
		}
		public String GetResourceID() {
			return resourceID;
		}
		public String GetPropertyName() {
			return propertyName;
		}
	}




}