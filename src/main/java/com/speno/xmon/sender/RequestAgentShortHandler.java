package com.speno.xmon.sender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.aggregate.builder.AggregatedRecivedResourceItem;
import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.env.xmPropertiesXml;

public class RequestAgentShortHandler 
{
	
	private final static Logger LOG = LoggerFactory.getLogger(RequestAgentShortHandler.class);
	private SimDate sd = null;
    private List<AggregatedRecivedResourceItem> listResourceItem = new ArrayList<AggregatedRecivedResourceItem>();
    
    public  RequestAgentShortHandler(String agentName, String aggregatedTime, JSONObject jsonSubObject) {
    	AggregatedRecivedResourceItem resoItem = null;
    	this.sd = new SimDate();
    	try {
    			Iterator<?> itrExtKeys  =  jsonSubObject.keys();
    			HashMap<String, String> extMap = new HashMap<String, String>();
    			String extKeys = "";
    			while(itrExtKeys.hasNext())
    			{
    				extKeys= String.valueOf( itrExtKeys.next()).trim();
    				if(!extKeys.startsWith( DicOrderAdd.Exten )) continue;
    				extMap.put(extKeys, jsonSubObject.getString(extKeys));
    			}
				String Command 	=  jsonSubObject.getString(DicOrderAdd.Command);
				String ValueUnit		=  jsonSubObject.getString(DicOrderAdd.AgentValueUnit);
				@SuppressWarnings("unused")
				String ReturnCode	=  jsonSubObject.getString(DicOrderAdd.ReturnCode);
				
				ConcurrentMap<String, ArrayList<String>>  temp_CmdList = xmPropertiesXml.htAgentList.get(agentName).getHtResourceCMDList();
				if(temp_CmdList == null)
				{
					LOG.error("Agent:" + agentName + ", ResourceCMDList  is null");
					return;
				}
				String[] StrCommand = Command.split(xmPropertiesXml.Sep);
				ArrayList<String> al ;
				if(StrCommand.length ==3 )
				{
					al= temp_CmdList.get(StrCommand[0] + xmPropertiesXml.Sep + StrCommand[1] );
				}
				else
				{
					al= temp_CmdList.get(Command);
				}
				if(al == null)
				{
					//Req_Connections&Connections&USE
					al= temp_CmdList.get(StrCommand[0] + xmPropertiesXml.Sep + StrCommand[1] );
				}
					
				Iterator<String> ItemCommand  = al.iterator();
				while(ItemCommand.hasNext())
				{
					String propertyName 	=  ItemCommand.next().split(xmPropertiesXml.Sep)[2];
					if(propertyName.equals("")) continue;
					String Value 				=  jsonSubObject.getString(propertyName); //IxMon.USE
					
					resoItem = new AggregatedRecivedResourceItem( agentName);
					String time = SimDate.GetTransformTime(SimDate.getDateTimeFormatter_MS(), aggregatedTime, SimDate.getDateTimeFormatter_Sec());
					if (time.length() < 1)
						continue;
					resoItem.SetAggregatedTime(time);
					resoItem.SetValueLong(Long.parseLong(Value));
					resoItem.SetCommandName(Command.split(xmPropertiesXml.Sep)[0]);
					resoItem.SetResourceID(Command.split(xmPropertiesXml.Sep)[1]);
					resoItem.SetPropertyName(propertyName);
					resoItem.SetValueUnit(ValueUnit);
 					resoItem.SetExtMap(extMap.toString());
					listResourceItem.add(resoItem);
				}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public List<AggregatedRecivedResourceItem> GetItemList() {
		return this.listResourceItem;
	}

}
