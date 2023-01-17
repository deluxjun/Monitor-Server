package com.speno.xmon.aggregate;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.aggregate.pool.xMThreadWorker_Expire;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.db.DataDeleter;
import com.speno.xmon.env.xmPropertiesXml;

public class Aggregate_Expire extends xMThreadWorker_Expire
{	
	
	private final static Logger LOG = LoggerFactory.getLogger(Aggregate_Expire.class);
	
	private SimDate sd	= null;
	Calendar addExpireResoSec	= Calendar.getInstance();
	Calendar addExpireResoMin	= Calendar.getInstance();
	Calendar addExpireResoHour	= Calendar.getInstance();
	Calendar addExpireResoDay	= Calendar.getInstance();
	
	
	Calendar addExpireTActSec 	= Calendar.getInstance();
	Calendar addExpireActSec 	= Calendar.getInstance();
	Calendar addExpireActMin 		= Calendar.getInstance();
	Calendar addExpireActHour 	= Calendar.getInstance();
	Calendar addExpireActDay 	= Calendar.getInstance();
	
	String aggreExpireTActSec		= "";
	String aggreExpireActSec 		= "";
	String aggreExpireActMin 		= "";
	String aggreExpireActHour 		= "";
	String aggreExpireActDay 		= "";
	
	String aggreExpireResoSec 	= "";
	String aggreExpireResoMin 	= "";
	String aggreExpireResoHour 	= "";
	
	String aggreExpireResoDay 	= "";

	DataDeleter xmExpireTransaction ;
	DataDeleter xmExpireAction ;
	DataDeleter xmExpireResource ;
	
	boolean bExpireTActSec = false;
	
	public Aggregate_Expire() {} 
	public Aggregate_Expire(Aggregate_Expire e) 
	{
		xmExpireTransaction	= new DataDeleter();
		xmExpireAction			= new DataDeleter();
		xmExpireResource		= new DataDeleter();
		
		sd								= new SimDate();
	}
		
	public synchronized void SetCurrentDate(Date currentDate, boolean bExpireTActSec) {
		if(currentDate == null) return;
		this.bExpireTActSec = bExpireTActSec;
		addExpireTActSec.setTime(currentDate);
		addExpireTActSec.add(Calendar.SECOND, - xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Expire_TActionSec));
		
		addExpireActSec.setTime(currentDate);
		addExpireActSec.add(Calendar.SECOND, - xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Expire_ActionSec));
		addExpireActMin.setTime(currentDate);
		addExpireActMin.add(Calendar.MINUTE, - xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Expire_ActionMin));
		addExpireActHour.setTime(currentDate);
		addExpireActHour.add(Calendar.HOUR, - xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Expire_ActionHour));
		addExpireActDay.setTime(currentDate);
		addExpireActDay.add(Calendar.DAY_OF_MONTH,  - xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Expire_ActionDay));

		addExpireResoSec	.setTime(currentDate);
		addExpireResoSec.add(Calendar.SECOND, - xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Expire_ResourceSec));
		addExpireResoMin.setTime(currentDate);
		addExpireResoMin.add(Calendar.MINUTE, - xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Expire_ResourceMin));
		addExpireResoHour.setTime(currentDate);
		addExpireResoHour.add(Calendar.HOUR, -xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Expire_ResourceHour));
		addExpireResoDay	.setTime(currentDate);
		addExpireResoDay.add(Calendar.DAY_OF_MONTH, - xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Expire_ResourceDay));
		
		
		aggreExpireTActSec 	= this.sd.getDateTimeFormatter_Sec().format(addExpireTActSec.getTime());
		
		aggreExpireActSec 	= this.sd.getDateTimeFormatter_Sec().format(addExpireActSec.getTime());
		aggreExpireActMin		= this.sd.getDateTimeFormatter_Min().format(addExpireActMin.getTime());
		aggreExpireActHour	= this.sd.getDateTimeFormatter_Hour().format(addExpireActHour.getTime());
		aggreExpireActDay	= this.sd.getDateTimeFormatter_Day().format(addExpireActDay.getTime());
		
		aggreExpireResoSec = this.sd.getDateTimeFormatter_Sec().format(addExpireResoSec.getTime());
		aggreExpireResoMin	= this.sd.getDateTimeFormatter_Min().format(addExpireResoMin.getTime());
		aggreExpireResoHour	= this.sd.getDateTimeFormatter_Hour().format(addExpireResoHour.getTime());
		aggreExpireResoDay	= this.sd.getDateTimeFormatter_Day().format(addExpireResoDay.getTime());
		
		synchronized (this) {
			notify();
		}
	}

	@Override
	protected synchronized void work() {
		try
		{
			String tableName = "";
	
			if(this.bExpireTActSec)
			{
				tableName = "xMTransaction";
				this.ExpireTransActionCall(tableName, this.aggreExpireTActSec);
			}
			else
			{
				tableName = "XmAggregatedActionSec";
				this.ExpireActionCall(tableName, this.aggreExpireActSec);
				tableName = "XmAggregatedActionMin";
				this.ExpireActionCall(tableName, this.aggreExpireActMin);
				tableName = "XmAggregatedActionHour";
				this.ExpireActionCall(tableName, this.aggreExpireActHour);
				tableName = "XmAggregatedActionDay";
				this.ExpireActionCall(tableName, this.aggreExpireActDay);
				
				tableName = "XmAggregatedResourceSec";
				this.ExpireResourceCall(tableName, this.aggreExpireResoSec);
				tableName = "XmAggregatedResourceMin";
				this.ExpireResourceCall(tableName, this.aggreExpireResoMin);
				tableName = "XmAggregatedResourceHour";
				this.ExpireResourceCall(tableName, this.aggreExpireResoHour);
				tableName = "XmAggregatedResourceDay";
				this.ExpireResourceCall(tableName, this.aggreExpireResoDay);
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
			//LOG.debug("now waiting..." + this.getId());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 단위 초 마다 리소스를 Agent에 리소스 요청 
	 */
	
	private void ExpireTransActionCall(String tableName,String expireDatetimeSec){
		LOG.debug("AggregatedExpireTransaction" + ", " + expireDatetimeSec);
		synchronized(xmExpireTransaction)
		{
			xmExpireTransaction.Delete_XmExpireTransAction(tableName, expireDatetimeSec);
		}
	}
	private  void ExpireActionCall(String tableName,String expireDatetimeSec){
		LOG.debug("AggregatedExpireAction" + ", " + expireDatetimeSec);
		synchronized(xmExpireAction)
		{
			xmExpireAction.Delete_XmExpireAction(tableName, expireDatetimeSec);
		}
	}
	private void ExpireResourceCall(String tableName,String expireDatetimeSec){
		LOG.debug("AggregatedExpireReso" + ", " + expireDatetimeSec);
		synchronized(xmExpireResource)
		{
			xmExpireResource.Delete_XmExpireResource(tableName, expireDatetimeSec);
		}
	}


	@Override
	protected void fireReleased() {
		// TODO Auto-generated method stub
		
	}

}
