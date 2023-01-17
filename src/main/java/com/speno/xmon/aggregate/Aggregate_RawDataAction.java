package com.speno.xmon.aggregate;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.aggregate.builder.AggregatedRecivedActionItem;
import com.speno.xmon.aggregate.pool.xMThreadWorker_Action;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.dataset.ItemMap_XmAmassTrans;
import com.speno.xmon.db.DataInserterAction;
import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.listener.AmassServer;

public class Aggregate_RawDataAction extends xMThreadWorker_Action
{
	private final static Logger LOG = LoggerFactory.getLogger(Aggregate_RawDataAction.class);
	
	public Date aggreDateTime;
	private String aggreDateTimeStr	= null;
	
	/*
	 *  Key: item.GetActionName() + "^"+ item.GetAgentName() + "^" + transCompletetimeSec
	 */
	//private ConcurrentMap<String, AggregatedRecivedActionItem>	hmActionAgentName = new ConcurrentHashMap<String, AggregatedRecivedActionItem>();
	//private HashMap<String, AggregatedRecivedActionItem> hmActionAgentName = new HashMap<String, AggregatedRecivedActionItem>();
	
	private  String threadType = null; 
	private   HashMap<String, AggregatedRecivedActionItem> hmActionAgentName = null;
	private DataInserterAction dbAction 		= null;
//	private DataInserterAction dbTransction	= null;
	
	public Aggregate_RawDataAction() {
		hmActionAgentName = new HashMap<String, AggregatedRecivedActionItem>();
		dbAction					= new DataInserterAction();
	}
	
	public Aggregate_RawDataAction(Aggregate_RawDataAction t ){	
		hmActionAgentName = new HashMap<String, AggregatedRecivedActionItem>();
		dbAction					= new DataInserterAction();
	}


	public synchronized void SetCurrentDate(String type, Date currentTime) 
	{
		this.threadType 			= type;
		this.aggreDateTime 	= currentTime;
		
		synchronized (this) {
			notify();
		}
	}

	@Override
	public synchronized void work() {
		try
		{
			if(this.threadType == null) return;
			if(this.threadType.equals(ThreadAggregate_Sch.type_Sec))
			{
				Calendar addTime = Calendar.getInstance();
				addTime.setTime(aggreDateTime);
				addTime.add(Calendar.SECOND,  xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Postponed_TransactionSec));
				
				this.aggreDateTimeStr 	= SimDate.getDateTimeFormatter_Sec().format(addTime.getTime());
				this.AggregatedSec(this.aggreDateTimeStr);
			}
			else if(this.threadType.equals(ThreadAggregate_Sch.type_Min))
			{
				this.AggregatedMin(SimDate.getDateTimeFormatter_Min().format(aggreDateTime));
			}
			else if(this.threadType.equals(ThreadAggregate_Sch.type_Hour))
			{
				this.AggregatedHour(SimDate.getDateTimeFormatter_Hour().format(aggreDateTime));
			}
			else if(this.threadType.equals(ThreadAggregate_Sch.type_Day))
			{
				this.AggregatedDay(SimDate.getDateTimeFormatter_Day().format(aggreDateTime));
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
	
	private synchronized  void AggregatedSec(String aggreDatetimeSec){
		// Delay insert!! : insert a buffer to table
		AmassServer server = AmassServer.getInstance();
		ConcurrentMap<String, ItemMap_XmAmassTrans> allTrans = server.getAmassTrans();
		for (String key : allTrans.keySet()) {
			try {
				ItemMap_XmAmassTrans trans = allTrans.get(key);
				int second = (int)(System.currentTimeMillis() / 1000L);
				long now = second * 1000L;
				if (trans.getTimeToInsert() != 0L && trans.getTimeToInsert() < now) {
//					// NOTYET 이면 complete만 오고 init이 오지 않은 것임
					if (!"NOTYET".equals(trans.GetActionName())) {
						boolean success = server.insertTrans(key, trans);
					}
					LOG.trace("insert trans: " + "size : " + allTrans.size() + "," + trans.toString());
					allTrans.remove(key, trans);
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		
		// 집계 시작!
		int intSize = dbAction.Insert_XmAggregatedActionSecGroup();
		LOG.trace("ActionAggregatedSec-Size:(" + hmActionAgentName.size() + ")  " + ":InsertedTransIDs-Size:(" + intSize + ")  " + aggreDatetimeSec);
	}
	/*
	 * 1. Action �ʴ� ������̺� Insert
	 * 2. �ʴ� Rs ���̺�(XmTransAction)���� �����͸� �ִ´�.
	 * synchronized
	 */
	/*
	@Deprecated
	private synchronized  void AggregatedSec_XXX(String aggreDatetimeSec){
		long transInitTime									= 0;
		
		String keyActionAgentCompleteTime 		= "";
		String transCompletetimeSec					= "";
		
		ItemMap_XmAmassTrans item				= null;
		AggregatedRecivedActionItem TempItem 	= null;
		
		Iterator<ItemMap_XmAmassTrans> iv 		= server.MapAmass_Trans.values().iterator();
		
		System.out.println("ddd:" + AmassServerHandler.MapAmass_Trans.size());
		while(iv.hasNext())
		{
			item  = iv.next();
			synchronized(item)
			{
				if(item.GetTransCompleteTime() == 0) continue; //��û���� ��
				
				transCompletetimeSec 			= this.sd.GetTransformTime( item.GetTransCompleteTime() ,this.sd.DateTimeFormatter_Sec);
				keyActionAgentCompleteTime	= item.GetActionName() + xmPropertiesXml.Sep + item.GetAgentName() + xmPropertiesXml.Sep + transCompletetimeSec;
				
				if(!hmActionAgentName.containsKey(keyActionAgentCompleteTime))
				{
					//map�� �����Ƿ� �ű�
					TempItem =	new AggregatedRecivedActionItem();
					hmActionAgentName.put(keyActionAgentCompleteTime, TempItem);
				}
				else
				{
					//map�� �����Ƿ� ���� ����Ʈ
					TempItem =	hmActionAgentName.get(keyActionAgentCompleteTime);
				}
	
				TempItem.SetAgentName(item.GetAgentName());
				TempItem.SetActionName(item.GetActionName());			
				TempItem.SetAggregatedTime(transCompletetimeSec);
				
				transInitTime = item.GetTransInitTime();
				//�Ϸ� ��
				if(item.GetTransCompleteTime() != 0)
				{
					long rs = TempItem.SuccessCount_Increment( transInitTime, item.GetTransCompleteTime());
					TempItem.SetTransID(item.GetTransID());
					*/
			    	/*
					dbTransction.Insert_XmTransAction(item.GetAgentName()
					                                   				   ,item.GetActionName()
					                                   				   ,item.GetTransID()
					                                   				   ,transInitTime
					                                   				   ,rs
					                                   				   ,DicOrderAdd.TransResult_Success );				
					
					AmassServerThread.MapAmass_Trans.remove( item.GetAgentName() + xmPropertiesXml.Sep +   item.GetTransID());
					*//*
				}
				else
			    if((item.GetErrorCode() != null) || (item.GetErrorMessage() != null))
			    {
			    	TempItem.ErrorCount_incrument();
			    	TempItem.SetTransID(item.GetTransID());
			    	*/
			    	/*
			    	dbTransction.Insert_XmTransAction(item.GetAgentName()
	                                   				   , item.GetActionName()
	                                   				   , item.GetTransID()
	                                   				   , transInitTime
	                                   				   , System.currentTimeMillis() - transInitTime 
	                                   				   , DicOrderAdd.TransResult_Error );
			    	
			    	AmassServerThread.MapAmass_Trans.remove(item.GetAgentName() + xmPropertiesXml.Sep + item.GetTransID());
			    	*//*
			    }
			    else
		    	//TimeOut ��
			    if(TransactionISTimeOut(transInitTime) == true)
			    {
			    	TempItem.TimeoutCount_incrument();
			    	TempItem.SetTransID(item.GetTransID());
			    	
			    	dbTransction.Insert_XmTransAction(item.GetAgentName()
	                                   				   ,item.GetActionName()
	                                   				   ,item.GetTransID()
	                                   				   ,transInitTime
	                                   				   ,item.GetTransCompleteTime()
	                                   				   , Integer.parseInt(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.Transaction_Timeout))
	                                   				   ,DicOrderAdd.TransResult_TimeOut );
			    
			    	AmassServerHandler.MapAmass_Trans.remove(item.GetAgentName() + xmPropertiesXml.Sep + item.GetTransID());
			    }
			}
		}//End While
		
		if(hmActionAgentName.size() >= 1)
		{
			synchronized(dbAction)
			{
				 int intSize = dbAction.Insert_XmAggregatedActionSec(hmActionAgentName);
			
				LOG.info("ActionAggregatedSec-Size:(" + hmActionAgentName.size() + ")  " + ":InsertedTransIDs-Size:(" + intSize + ")  " + aggreDatetimeSec);
				//LOG.info("ActionAggregatedSec-Size:(" + hmActionAgentName.size() + ")  " + ":InsertedTransIDs-Size:(" + insertTransIDs.size() + ")  " + aggreDatetimeSec);
				/*
				Iterator<String> transIDs = insertTransIDs.iterator();
		    	while(transIDs.hasNext())
		    	{
		    		LOG.info(transIDs.next());
		    		//AmassServerThread.MapAmass_Trans.remove(transIDs.next());
		    	}
				Iterator<String>  iter 			=  hmActionAgentName.keySet().iterator();
				String  key 						= "";
				while( iter.hasNext()) 
				{
				        key = (String) iter.next();
				        if(key.equals("")) continue;
				        
				        AggregatedRecivedActionItem value = hmActionAgentName.get(key);
				        Iterator<String>  itr =  value.getTransID().iterator();
				        while(itr.hasNext())
				        {
				        	LOG.info(key + ", ID:" + itr.next());
				        }
				}
				*//*
				hmActionAgentName.clear();
				//insertTransIDs.clear();
			}
			
		}
	}
	*/

	private synchronized void AggregatedMin(String strAggreDateTime){
		dbAction.aggregate(DataInserterAction.TYPE_MIN);
//		Iterator<String> agentlist = xmPropertiesXml.htAgentList.keySet().iterator();
//		List<String> temp = new ArrayList<String>(); 
//		while(agentlist.hasNext())
//		{
//			String agent = agentlist.next();
//			if(temp.contains(agent)) continue;
//			temp.add(agent);
//		}
//		Iterator<String> tempAgent = temp.iterator();
//		while(tempAgent.hasNext())
//		{
//			dbAction.Insert_XmAggregatedAction("XmAggregatedActionSec","XmAggregatedActionMin" , 16, strAggreDateTime,  tempAgent.next() );
//		}
	}
	private synchronized void AggregatedHour(String strAggreDateTime){
		dbAction.aggregate(DataInserterAction.TYPE_HOUR);
//		Iterator<String> agentlist = xmPropertiesXml.htAgentList.keySet().iterator();
//		List<String> temp = new ArrayList<String>(); 
//		while(agentlist.hasNext())
//		{
//			String agent = agentlist.next();
//			if(temp.contains(agent)) continue;
//			temp.add(agent);
//		}
//		Iterator<String> tempAgent = temp.iterator();
//		while(tempAgent.hasNext())
//		{
//			dbAction.Insert_XmAggregatedAction("XmAggregatedActionMin","XmAggregatedActionHour" , 13, strAggreDateTime, tempAgent.next());
//		}
	}
	private synchronized void AggregatedDay(String strAggreDateTime){
		dbAction.aggregate(DataInserterAction.TYPE_DAY);

//		Iterator<String> agentlist = xmPropertiesXml.htAgentList.keySet().iterator();
//		List<String> temp = new ArrayList<String>(); 
//		while(agentlist.hasNext())
//		{
//			String agent = agentlist.next();
//			if(temp.contains(agent)) continue;
//			temp.add(agent);
//		}
//		Iterator<String> tempAgent = temp.iterator();
//		while(tempAgent.hasNext())
//		{
//			new DataInserterAction().Insert_XmAggregatedAction("XmAggregatedActionHour","XmAggregatedActionDay" , 10, strAggreDateTime, tempAgent.next());
//		}
	}
	
//	private synchronized boolean TransactionISTimeOut(long InitTime) {
//		try {
//			return SimDate.GetTimeout(SimDate.DateTimeFormatter_Min, InitTime
//			                          , Integer.parseInt( xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.Transaction_Timeout)));
//		}
//		catch (ParseException e) {
//			e.printStackTrace();
//		}
//		return true;
//	}

	@Override
	protected void fireReleased() {
		// TODO Auto-generated method stub
		
	}
}
