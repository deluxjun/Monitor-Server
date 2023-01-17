package com.speno.xmon.aggregate;

import java.nio.charset.CharacterCodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.JsonGenerator.JsonGeneratorAmass;
import com.speno.xmon.aggregate.pool.xMThreadWorker_Resource;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.db.DataInserterResource;
import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.sender.RequestAgentShort;

public class Aggregate_RawDataReqResource extends xMThreadWorker_Resource
{
	private final static Logger LOG = LoggerFactory.getLogger(Aggregate_RawDataReqResource.class);
	
	private String threadType 			= null; 
	private Date aggreDateTime		= null;
	
	DataInserterResource dbResource;
	
//	DataInserterResource min;
//	DataInserterResource hour;
//	DataInserterResource day;
	
	public Aggregate_RawDataReqResource() {
		dbResource = new DataInserterResource();
	}
	public Aggregate_RawDataReqResource(Aggregate_RawDataReqResource r) 
	{
		dbResource = new DataInserterResource();

//		min = new DataInserterResource();
//		hour= new DataInserterResource();
//		day = new DataInserterResource();
	}

	public void SetCurrentDate(String type, Date aggreDateTime)
	{
		this.threadType 				= type;
		this.aggreDateTime 		= aggreDateTime;
		
		synchronized (this) {
			notify();
		}
	}
	
	@Override
	protected void work() {
		try
		{
			if(this.threadType == null) return;
			if(this.threadType.equals(ThreadAggregate_Sch.type_Sec))
			{
				Calendar addTime = Calendar.getInstance();
				addTime.setTime(aggreDateTime);
				addTime.add(Calendar.MINUTE,  xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Postponed_ResourceSec));
				
				this.AggregatedSec(SimDate.getDateTimeFormatter_Sec().format(addTime.getTime()));
			}
			else if(this.threadType.equals(ThreadAggregate_Sch.type_Min))
			{
				Calendar addTime = Calendar.getInstance();
				addTime.setTime(aggreDateTime);
				addTime.add(Calendar.MINUTE,  xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Postponed_ResourceMin));
				this.AggregatedMin(SimDate.getDateTimeFormatter_Min().format(addTime.getTime() ));
			}
			else if(this.threadType.equals(ThreadAggregate_Sch.type_Hour))
			{
				Calendar addTime = Calendar.getInstance();
				addTime.setTime(aggreDateTime);
				addTime.add(Calendar.MINUTE,  xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Postponed_ResourceHour));
				
				this.AggregatedHour(SimDate.getDateTimeFormatter_Hour().format(addTime.getTime() ));
			}
			else if(this.threadType.equals(ThreadAggregate_Sch.type_Day))
			{
				Calendar addTime = Calendar.getInstance();
				addTime.setTime(aggreDateTime);
				addTime.add(Calendar.MINUTE,  xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Postponed_ResourceDay));
				
				this.AggregatedDay(SimDate.getDateTimeFormatter_Day().format(addTime.getTime() ));
			}
		}
		finally
		{	
			waitNow();
		}
	}
	
	private synchronized void waitNow(){
		try {
			wait();
			LOG.trace("now waiting..." + this.getId());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	JsonGeneratorAmass jsongen = new JsonGeneratorAmass(); 
	
	/*
	 * 단위 초 마다 리소스를 Agent에 리소스 요청 
	 */
	private void AggregatedSec(String aggreDatetimeSec){
		
		RequestAgentShort Sender_ReqOrderAgent = new RequestAgentShort();
		
		Iterator<String> agentNames = xmPropertiesXml.htAgentList.keySet().iterator();
		try {
			String tempAgentName 				= "";
			while(agentNames.hasNext())
			{
				tempAgentName 	= agentNames.next();
				if(tempAgentName.equals("")) continue;
				
				int ret = Sender_ReqOrderAgent.SendRequestCmd(tempAgentName, jsongen.RequestAgentShort(tempAgentName));
				LOG.info("RequestAgentShort:" + aggreDatetimeSec + " [" + tempAgentName + "] => ReturnCD: " + ret);
			}
		}
		catch (CharacterCodingException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
	private synchronized void AggregatedMin(String aggreDatetimeMin){
		dbResource.aggregateResource(DataInserterResource.TYPE_MIN);
//		min.Insert_XmAggregatedResourceMin(aggreDatetimeMin);
	}
	private synchronized void AggregatedHour(String aggreDatetimeHour){
		dbResource.aggregateResource(DataInserterResource.TYPE_HOUR);
//		hour.Insert_XmAggregatedResourceHour(aggreDatetimeHour);
	}
	private synchronized void AggregatedDay(String aggreDatetimeDay){
		dbResource.aggregateResource(DataInserterResource.TYPE_DAY);
//		day.Insert_XmAggregatedResourceDay(aggreDatetimeDay);
	}


	@Override
	protected void fireReleased() {
		// TODO Auto-generated method stub
		
	}

}
