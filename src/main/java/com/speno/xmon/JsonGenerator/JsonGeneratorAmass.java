package com.speno.xmon.JsonGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.codedic.DicCommands;
import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.codedic.DicOrderTypes;
import com.speno.xmon.comm.PortCheck;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.db.DataSelecterAction;
import com.speno.xmon.db.DataSelecterResource;
import com.speno.xmon.env.ItemAgent;
import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.event.EventManager;

public class JsonGeneratorAmass {
	
    private final static Logger LOG = LoggerFactory.getLogger(JsonGeneratorAmass.class);

    	private SimDate sd							= null;
    	private JSONObject rcvJsonObject 	= null;
    	private DataSelecterAction dbAction 	= null;
    	
    	public JsonGeneratorAmass()
    	{
    		this.sd = new SimDate();
    	}
    	public JsonGeneratorAmass(JSONObject jsonObject) 
    	{
    		this.sd = new SimDate();
    		this.rcvJsonObject = jsonObject;
    		this.dbAction			= new DataSelecterAction();
    	}

	/*
	 * RequestAgentShort
     */
	public String RequestAgentShort(String agengName) {
	    JSONObject	jsonObject		= null;
	    JSONArray	jsonArray		= null;
	    JSONObject	jsonOb_Que	= null;
	    
		try {
			
			String sDateTime_Send = sd.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis()));
			jsonObject = new JSONObject();

			jsonObject.put(DicOrderAdd.TargetAgent,			agengName);
			jsonObject.put(DicOrderAdd.RequestDateTime,	sDateTime_Send);
			jsonObject.put(DicOrderTypes.OrderType,				DicOrderTypes.RequestAgentShort);
			//RequestAgentShort agent의 모든 명령을 담고 있음
			ItemAgent temp =xmPropertiesXml.htAgentList.get(agengName);
			
			Iterator<String> propertiesCommands = temp.getHtResourceCMDList().keySet().iterator();
			jsonArray   = new JSONArray();
			while(propertiesCommands.hasNext())
			{
				String tempCmdAlKey 		= propertiesCommands.next();

				//int ssss = temp.getHtCMDList().get(tempCmdAlKey).size();
				//String ddd = temp.getHtCMDList().get(tempCmdAlKey).get(0);
				
				ArrayList<String> fullCmds = temp.getHtResourceCMDList().get(tempCmdAlKey); //propertyNames
				
				if(temp.getHtResourceCMDList().get(tempCmdAlKey) == null) continue;
				if(temp.getHtResourceCMDList().get(tempCmdAlKey).size() == 1)
				{
					jsonOb_Que = new JSONObject();
					jsonOb_Que.put(DicOrderAdd.Command,	temp.getHtResourceCMDList().get(tempCmdAlKey).get(0));
					jsonOb_Que.put(DicOrderAdd.AgentValueUnit,	 temp.Command_Req_ValueUnit.get(tempCmdAlKey));
					jsonOb_Que.put(DicOrderAdd.PropertyNames,  fullCmds.get(0).split(xmPropertiesXml.Sep)[2]);
					
					jsonArray.put(jsonOb_Que);
				}
				else
				{
					String PropertyNames = "";
					for(int p=0;p<fullCmds.size();p++)
					{
						PropertyNames += fullCmds.get(p).split(xmPropertiesXml.Sep)[2] + xmPropertiesXml.Sep;
					}
					
					jsonOb_Que = new JSONObject();
					jsonOb_Que.put(DicOrderAdd.Command,	tempCmdAlKey);
					jsonOb_Que.put(DicOrderAdd.AgentValueUnit,	 temp.Command_Req_ValueUnit.get(tempCmdAlKey));
					jsonOb_Que.put(DicOrderAdd.PropertyNames,  PropertyNames);
					jsonArray.put(jsonOb_Que);
				}
			}
			if(jsonArray.length() ==0) return "";
			jsonObject.put(DicOrderTypes.RequestAgentShort, jsonArray);
			return jsonObject.toString();
		}
		catch (Exception ee)
		{
			ee.printStackTrace();
			LOG.error(ee.getMessage());
			return null;
		}
	}

	public String ResponseSet(String orderType, String command, String agentNames, String sms) {
		JSONObject jsonObjectResponse 		= new JSONObject();
	  	JSONObject jsonObjectSubResponse = null;
	  	JSONArray jsonArrayResponse 			= new JSONArray();
		  
		try {
			jsonObjectResponse.put(DicOrderAdd.SendTime, sd.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis())));
			
		  	jsonObjectResponse.put(DicOrderTypes.OrderType, DicOrderTypes.ResponseSet);
		  	jsonObjectResponse.put(DicOrderAdd.Command, 	command);
		  	
		  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "");
		  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, 1);
		  	
		  	if(command.equals(DicCommands.Set_AgentNames))
		  	{
		  		Iterator<ItemAgent> tempAgentNames =  xmPropertiesXml.htAgentList.values().iterator();
		  		
		  		while(tempAgentNames.hasNext())
		  		{
		  			ItemAgent agentInfo =  tempAgentNames.next();
		  			jsonObjectSubResponse = new JSONObject();
		  			jsonObjectSubResponse.put("USE", agentInfo.getAgentName());
		  			jsonObjectSubResponse.put("DESC", agentInfo.getAgentDesc());
		  			jsonArrayResponse.put(jsonObjectSubResponse);
		  		}
		  		jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
		  	}
		  	else if(command.equals(DicCommands.Set_ActionNames))
		  	{
			  		Iterator<String> tempAgentNames =  xmPropertiesXml.htAgentList.keySet().iterator();
			  		while(tempAgentNames.hasNext())
			  		{
			  			JSONArray cmds = this.dbAction.Select_XmActionList(tempAgentNames.next());
			  			JSONObject cmd ;
			  			for(int i=0; i<cmds.length();i++)
			  			{
			  				cmd = cmds.getJSONObject(i);
				  			jsonArrayResponse.put(cmd);
			  			}
			  		}
			  		
			  		jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
				  
		  	}else if(command.equals(DicCommands.Set_Commands))
		  	{
		  		Iterator<String> tempAgentNames =  xmPropertiesXml.htAgentList.keySet().iterator();
		  		String agentName = "";
		  		while(tempAgentNames.hasNext())
		  		{
		  			agentName =  tempAgentNames.next();
		  			JSONArray cmds = xmPropertiesXml.htAgentList.get(agentName).getCommandListAndSubJsonArray(); 
		  			JSONObject cmd ;
		  			for(int i=0; i<cmds.length();i++)
		  			{
		  				cmd = cmds.getJSONObject(i);
			  			jsonArrayResponse.put(cmd);
		  			}
		  		}
		  		jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
		  		
		  	}else if(command.equals(DicCommands.Set_LogIds))
		  	{
//		  		File dirLogs = new File(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.LogsFolder));
//		  		if(!dirLogs.isDirectory()) return "";
//		  		File[] logFiles = dirLogs.listFiles();
//		  		
//		  		Hashtable<String,String> listLogId = new Hashtable<String,String>();
//		  		for(File logFile : logFiles)
//		  		{
//		  			String[] logID = logFile.getName().split("_");
//		  			if(logID.length !=3) continue;
//		  			String tempLogId = (logID[0] + "_" + logID[1]).trim();
//		  			
//		  			//contains
//		  			if( !listLogId.containsKey(tempLogId)) 
//		  				listLogId.put(tempLogId,"");
//		  		}
//		  		
//		  		Iterator<String> itrLogId =  listLogId.keySet().iterator();
//		  		while(itrLogId.hasNext())
//		  		{
//		  			jsonObjectSubResponse = new JSONObject();
//		  			jsonObjectSubResponse.put("USE", itrLogId.next());
//		  			jsonArrayResponse.put(jsonObjectSubResponse);
//		  		}
//		  		jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
		  		
		  		Map<String, String[]> map = xmPropertiesXml.mapLogId;
		  		for (String key : map.keySet()) {
					String[] logIds = map.get(key);
		  			for (int i = 0; i < logIds.length; i++) {
			  			jsonObjectSubResponse = new JSONObject();
			  			jsonObjectSubResponse.put("USE", key + "_" + logIds[i]);
			  			jsonArrayResponse.put(jsonObjectSubResponse);
					}
				}
		  		jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
		  		
		  	/*
		  	 * Agent Health
		  	 */
		  	}
		  	else if(command.equals(DicCommands.Set_AgentHealth))
		  	{
				String agentKey = "";
	  			Iterator<String> agents = xmPropertiesXml.htAgentList.keySet().iterator();
	  			while(agents.hasNext())
	  			{
	  				agentKey = agents.next();
	  				jsonObjectSubResponse = new JSONObject();
		  			jsonObjectSubResponse.put("USE", agentKey );
		  			jsonObjectSubResponse.put("VALUE", xmPropertiesXml.htAgentList.get(agentKey).getMinaSessionCheck() );
		  			jsonArrayResponse.put(jsonObjectSubResponse);
	  			}
		  		jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
		  	/*
		  	 * Agent Health Portüũ
		  	 */
		  	}else if(command.equals(DicCommands.Set_AgentHealthPort))
		  	{
		  		DataSelecterResource dsr = new DataSelecterResource();
		  		
		  		// xmPropertiesXml.htAgentList.iterator() --> String���ڿ� ,�������� ������ ������ ���� 2014.10.28 soeun
//		  		Iterator<String> agents = agentNames.split(",");//xmPropertiesXml.htAgentList.keySet().iterator();
		  		Iterator<String> agents = Arrays.asList(agentNames.split(",")).iterator(); 
		  		
	  			ItemAgent item = null;
	  			while(agents.hasNext())
	  			{
	  				item = xmPropertiesXml.htAgentList.get(agents.next());
	  				if(!item.getHealthUse()) continue;
	  				
	  				jsonObjectSubResponse = new JSONObject();
	  				
		  			jsonObjectSubResponse.put("AgentName",			item.getAgentName());
		  			
		  			if(xmPropertiesXml.htAgentList.get(item.getAgentName()) == null)
		  				jsonObjectSubResponse.put("AgentSession", 		false );
		  			else
		  				jsonObjectSubResponse.put("AgentSession", 		xmPropertiesXml.htAgentList.get(item.getAgentName()).getMinaSessionCheck() );
		  			
		  			jsonObjectSubResponse.put("PortSession",        new PortCheck().PortCheckRun(item.getAgentIP(), item.getAgentPort()) );
		  			jsonObjectSubResponse.put("Port", item.getAgentPort());
		  			
		  			JSONArray jsonArray = dsr.Select_XmDataSelecterResource_Helth(item.getAgentName());
		  			jsonArray = dbAction.getActionsTPSForHealth(item.getAgentName(), jsonArray);
		  			jsonObjectSubResponse.put("VALUES", jsonArray);
		  			
		  			jsonArrayResponse.put(jsonObjectSubResponse);
	  			}
		  		jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
		  	}
			else if(command.equals(DicCommands.Set_EnvProperties_Sch ))
			{
				String properties = "";
	  			Iterator<String> propertiesSch = xmPropertiesXml.htXmPropertiesAmass_Int.keySet().iterator();
	  			while(propertiesSch.hasNext())
	  			{
	  				properties = propertiesSch.next();
	  				jsonObjectSubResponse = new JSONObject();
		  			jsonObjectSubResponse.put("USE", properties );
		  			jsonObjectSubResponse.put("VALUE", xmPropertiesXml.htXmPropertiesAmass_Int.get(properties) );
		  			jsonArrayResponse.put(jsonObjectSubResponse);
	  			}
		  		jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
			}
			else if(command.equals(DicCommands.Set_EnvProperties_Main ))
			{
				String properties = "";
	  			Iterator<String> propertiesMain = xmPropertiesXml.htXmPropertiesAmass_String.keySet().iterator();
	  			while(propertiesMain.hasNext())
	  			{
	  				properties = propertiesMain.next();
	  				jsonObjectSubResponse = new JSONObject();
		  			jsonObjectSubResponse.put("USE", properties );
		  			jsonObjectSubResponse.put("VALUE", xmPropertiesXml.htXmPropertiesAmass_String.get(properties) );
		  			jsonArrayResponse.put(jsonObjectSubResponse);
	  			}
		  		jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
			}
			else if(command.equals(DicCommands.Set_EnvAmass))
			{
				String[] PropertyNames		= this.rcvJsonObject.get(DicOrderAdd.PropertyNames).toString().split(",");
				JSONArray jsonOrderArray	= this.rcvJsonObject.getJSONArray(orderType);
				
				if(jsonOrderArray.length() != PropertyNames.length )
				{
					jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "jsonOrderArray and  PropertyNames Not match");
					jsonObjectResponse.put(DicOrderAdd.ReturnCode, -1);
					jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
					return  jsonObjectResponse.toString();
				}
				xmPropertiesXml xmProp = new xmPropertiesXml(); 
				for(int i=0; i<PropertyNames.length;i++)
				{
					JSONObject jsonProp	= jsonOrderArray.getJSONObject(i);
					String[] propName		= JSONObject.getNames(jsonProp);
					for(int p=0;p< propName.length; p++)
					{
						boolean bMemorySet = false;
						if(xmPropertiesXml.htXmPropertiesAmass_Int.containsKey(propName[p]))
						{
							xmPropertiesXml.htXmPropertiesAmass_Int.put(propName[p], Integer.parseInt(jsonProp.getString(propName[p])));
							bMemorySet = true;
						}
						else if(xmPropertiesXml.htXmPropertiesAmass_String.containsKey(propName[p]))
						{
							xmPropertiesXml.htXmPropertiesAmass_String.put(propName[p], jsonProp.getString(propName[p]));
							bMemorySet = true;
						}
						else
						{
							continue;
							/*
							jsonObjectResponse.put(DicKeysOrderAde.ReturnMsg, "SetProperty Memory Fail:" + propName[p]);
							jsonObjectResponse.put(DicKeysOrderAde.ReturnCode, -2);
							jsonObjectResponse.put(DicKeysOrderTypes.ResponseSet,jsonArrayResponse);
							return  jsonObjectResponse.toString();
							*/
						}
						
						boolean bRet = xmProp.setProperties(propName[p], jsonProp.getString(propName[p]));
						if(!bRet && !bMemorySet) 
						{
							jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "SetProperty Fail:" + propName[p]);
							jsonObjectResponse.put(DicOrderAdd.ReturnCode, -2);
							jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
							return  jsonObjectResponse.toString();
						}
						jsonObjectSubResponse = new JSONObject();
			  			jsonObjectSubResponse.put(propName[p], jsonProp.getString(propName[p]));
			  			jsonArrayResponse.put(jsonObjectSubResponse);
					}
				} //End For
				
			  	jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
			}
			else if(command.equals("Set_Sms")){
				if(sms.equals("ON"))
					EventManager.smsOn = true;
				else
					EventManager.smsOn = false;
			}
			else
			{
				jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "command  Error:" + command);
				jsonObjectResponse.put(DicOrderAdd.ReturnCode, -2);
				jsonObjectResponse.put(DicOrderTypes.ResponseSet,jsonArrayResponse);
				return  jsonObjectResponse.toString();
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}	    
		return jsonObjectResponse.toString();
	}
}
