package com.speno.xmon.aggregate;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.aggregate.pool.xMThreadPool_Action;
import com.speno.xmon.aggregate.pool.xMThreadPool_Expire;
import com.speno.xmon.aggregate.pool.xMThreadPool_Resource;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.listener.AmassServerThread;

public class ThreadAggregate_Sch  implements Runnable
{
	private final static Logger LOG = LoggerFactory.getLogger(ThreadAggregate_Sch.class);

	private SimDate sd					= null;
	
	public static String type_Sec		="Sec" ;
	public static String type_Min		="Min";
	public static String type_Hour	="Hour";
	public static String type_Day	="Day";
	
	xMThreadPool_Action<Aggregate_RawDataAction> 					pool_Action		= null;
	xMThreadPool_Resource<Aggregate_RawDataReqResource> 	pool_Resource	= null;
	xMThreadPool_Expire<Aggregate_Expire> 								pool_Expire		= null;
	
	public ThreadAggregate_Sch()
	{
		try {
			sd		= new SimDate();
			
		    if(xmPropertiesXml.htXmPropertiesAmass_String.get( xmPropertiesXml.ActionSchYN).equals("Y"))
		    {
		    	pool_Action 	= new xMThreadPool_Action<Aggregate_RawDataAction>(3, 10);
				pool_Action.initialize(new Aggregate_RawDataAction());
		    }
		
		    if(xmPropertiesXml.htXmPropertiesAmass_String.get( xmPropertiesXml.ResponseSchYN).equals("Y"))
		    {
				pool_Resource = new xMThreadPool_Resource<Aggregate_RawDataReqResource>(1, 10);
				pool_Resource.initialize(new Aggregate_RawDataReqResource());
		    }
		    if(xmPropertiesXml.htXmPropertiesAmass_String.get( xmPropertiesXml.ExpireSchYN).equals("Y"))
		    {
				pool_Expire = new xMThreadPool_Expire<Aggregate_Expire>(1, 30);
				pool_Expire.initialize(new Aggregate_Expire());
		    }
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	public void run() {
		
		String lastSec						= "";
		String baseSec 						= "";
		
		String baseExpireSec				= "";
		String baseExpireMin				= "";
		
		String aggreTargetActSec 		= "";
		String aggreTargetResoSec 	= "";
		

		String baseMin 						= "";
		String aggreTargetActMin 		= "";
		String aggreTargetResoMin 	= "";
	
		String baseHour 					= "";
		String aggreTargetActHour 		= "";
		String aggreTargetResoHour 	= "";

		String baseDay 						= "";
		String aggreTargetActDay 		= "";
		String aggreTargetResoDay 	= "";

		
		Date currentDate		= null;
		
		Calendar addExpireSec	= Calendar.getInstance();
		Calendar addExpireMin	= Calendar.getInstance();
		
		Calendar addActSec 	= Calendar.getInstance();
		Calendar addActMin 	= Calendar.getInstance();
		Calendar addActHour = Calendar.getInstance();
		Calendar addActDay 	= Calendar.getInstance();

		Calendar addResoSec	= Calendar.getInstance();
		Calendar addResoMin		= Calendar.getInstance();
		Calendar addResoHour	= Calendar.getInstance();
		Calendar addResoDay	= Calendar.getInstance();

		while(true)
		{
			 
			try
			{
				if(!xmPropertiesXml.isInitXml) 
				{
					Thread.sleep(1000);
					continue;
				}
				
				Thread.sleep(300);
				currentDate 		= new Date(System.currentTimeMillis());

				addActSec.setTime(currentDate);
				addActSec.add(Calendar.SECOND,  xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ActionSec));
				
				addResoSec.setTime(currentDate);
				addResoSec.add(Calendar.SECOND,  xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ResourceSec));
							
				
				addExpireSec.setTime(currentDate);
				addExpireSec.add(Calendar.SECOND,  xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_ExpireRepeat_Sec));
				
				
				addExpireMin.setTime(currentDate);
				addExpireMin.add(Calendar.MINUTE,  xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_ExpireRepeat_Min));
				
				addActMin.setTime(currentDate);
				addActMin.add(Calendar.MINUTE,  xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ActionMin));
				
				addResoMin.setTime(currentDate);
				addResoMin.add(Calendar.MINUTE,  xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ResourceMin));
		
				addActHour.setTime(currentDate);
				addActHour.add(Calendar.HOUR, xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ActionHour));
				
				addResoHour.setTime(currentDate);
				addResoHour.add(Calendar.HOUR, xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ResourceHour));
				
				addActDay.setTime(currentDate);
				addActDay.add(Calendar.DAY_OF_MONTH, xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ActionDay));
				
				addResoDay.setTime(currentDate);
				addResoDay.add(Calendar.DAY_OF_MONTH, xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ResourceDay));
				
				baseSec		= this.sd.getDateTimeFormatter_OnlySec().format(currentDate);				
				baseMin		= this.sd.getDateTimeFormatter_OnlyMin().format(currentDate);
				baseHour		= this.sd.getDateTimeFormatter_OnlyHour().format(currentDate);
				baseDay		= this.sd.getDateTimeFormatter_OnlyDay().format(currentDate);
				
				if(baseExpireSec 		=="") baseExpireSec 	= this.sd.getDateTimeFormatter_OnlyMin().format(addExpireSec.getTime());
				if(baseExpireMin 		=="") baseExpireMin 	= this.sd.getDateTimeFormatter_OnlyMin().format(addExpireMin.getTime());
				
				if(aggreTargetActSec	=="") aggreTargetActSec		= this.sd.getDateTimeFormatter_OnlySec().format(addActSec.getTime() );
				if(aggreTargetActMin	=="") aggreTargetActMin			= this.sd.getDateTimeFormatter_OnlyMin().format(addActMin.getTime() );
				if(aggreTargetActHour	=="") aggreTargetActHour		= this.sd.getDateTimeFormatter_OnlyHour().format(addActHour.getTime());
				if(aggreTargetActDay	=="") aggreTargetActDay		= this.sd.getDateTimeFormatter_OnlyDay().format(addActDay.getTime());
				
				if(aggreTargetResoSec	=="") aggreTargetResoSec	= this.sd.getDateTimeFormatter_OnlySec().format(addResoSec.getTime() );
				if(aggreTargetResoMin	=="") aggreTargetResoMin	= this.sd.getDateTimeFormatter_OnlyMin().format(addResoMin.getTime() );
				if(aggreTargetResoHour	=="") aggreTargetResoHour	= this.sd.getDateTimeFormatter_OnlyHour().format(addResoHour.getTime());
				if(aggreTargetResoDay	=="") aggreTargetResoDay	= this.sd.getDateTimeFormatter_OnlyDay().format(addResoDay.getTime());
				
				//LOG.info("baseSec:" + baseSec + ", aggreTargetActSec" + aggreTargetActSec + ", aggreTargetResoSec:" + aggreTargetResoSec);
				//LOG.info("baseSec:" + baseSec + ", lastSec" + lastSec + ", aggreTargetResoSec:" + aggreTargetResoSec + ", baseExpireMin:" + baseExpireMin);
				
				if (pool_Action != null && (!baseSec.equals(lastSec)))
				{
					lastSec = baseSec;
					LOG.trace("aggreTargetActSec:" + aggreTargetActSec);
					aggreTargetActSec =  this.sd.getDateTimeFormatter_OnlySec().format(addActSec.getTime() );
					
					Aggregate_RawDataAction w = null;
					try {
						w = (Aggregate_RawDataAction) pool_Action.getThread();
						w.SetCurrentDate(ThreadAggregate_Sch.type_Sec, currentDate);
					}
					catch (Exception e) { LOG.error(e.getMessage()); e.getStackTrace(); }
					finally { if (w != null) pool_Action.releaseThread(w); }
				}
				if(pool_Resource!=null && baseSec.equals(aggreTargetResoSec))
				{
					LOG.trace("aggreTargetResoSec:" + aggreTargetResoSec);
					aggreTargetResoSec =  this.sd.getDateTimeFormatter_OnlySec().format(addResoSec.getTime() );
					
					Aggregate_RawDataReqResource w = null;
					try {
						w = (Aggregate_RawDataReqResource) pool_Resource.getThread();
						w.SetCurrentDate(ThreadAggregate_Sch.type_Sec, currentDate);
					}
					catch (Exception e) { LOG.error(e.getMessage()); e.getStackTrace(); }
					finally { if (w != null) pool_Resource.releaseThread(w); }
					
				}
				
				if( pool_Expire != null && baseSec.equals(baseExpireSec))
				{
					LOG.trace("baseExpireSec:" + baseExpireSec);
					baseExpireSec =  this.sd.getDateTimeFormatter_OnlySec().format(addExpireSec.getTime() );
					
					Aggregate_Expire w = null;
					try {
						w = (Aggregate_Expire) pool_Expire.getThread();
						w.SetCurrentDate(currentDate,true);
					}
					catch (Exception e) { LOG.error(e.getMessage()); e.getStackTrace(); }
					finally { if (w != null) pool_Expire.releaseThread(w); }
					
				}
				
				if(pool_Expire != null && baseMin.equals(baseExpireMin))
				{
					LOG.trace("baseExpireMin:" + baseExpireMin);
					baseExpireMin =  this.sd.getDateTimeFormatter_OnlyMin().format(addExpireMin.getTime() );
					
					Aggregate_Expire w = null;
					try {
						w = (Aggregate_Expire) pool_Expire.getThread();
						w.SetCurrentDate(currentDate,false);
					}
					catch (Exception e) { LOG.error(e.getMessage()); e.getStackTrace(); }
					finally { if (w != null) pool_Expire.releaseThread(w); }
					
				}
				
				if(pool_Action != null && baseMin.equals(aggreTargetActMin))
				{
					LOG.trace("aggreTargetActMin:" + aggreTargetActMin);
					aggreTargetActMin =  this.sd.getDateTimeFormatter_OnlyMin().format(addActMin.getTime() );
					
					Aggregate_RawDataAction w = null;
					try {
						w = (Aggregate_RawDataAction) pool_Action.getThread();
						w.SetCurrentDate(ThreadAggregate_Sch.type_Min, currentDate);
					}
					catch (Exception e) { LOG.error(e.getMessage()); e.getStackTrace(); }
					finally { if (w != null) pool_Action.releaseThread(w); }
					
				}
				if(pool_Resource!=null && baseMin.equals(aggreTargetResoMin))
				{
					LOG.trace("aggreTargetResoMin:" + aggreTargetResoMin);
					aggreTargetResoMin =  this.sd.getDateTimeFormatter_OnlyMin().format(addResoMin.getTime() );
					
					Aggregate_RawDataReqResource w = null;
					try {
						w = (Aggregate_RawDataReqResource) pool_Resource.getThread();
						w.SetCurrentDate(ThreadAggregate_Sch.type_Min, currentDate);
					}
					catch (Exception e) { LOG.error(e.getMessage()); e.getStackTrace(); }
					finally { if (w != null) pool_Resource.releaseThread(w); }
				}
				
				if(pool_Action != null && baseHour.equals(aggreTargetActHour))
				{		
					LOG.trace("aggreTargetActHour:" + aggreTargetActHour);
					aggreTargetActHour = this.sd.getDateTimeFormatter_OnlyHour().format(addActHour.getTime());
					
					Aggregate_RawDataAction w = null;
					try {
						w = (Aggregate_RawDataAction) pool_Action.getThread();
						w.SetCurrentDate(ThreadAggregate_Sch.type_Hour, currentDate);
					}
					catch (Exception e) { LOG.error(e.getMessage()); e.getStackTrace(); }
					finally { if (w != null) pool_Action.releaseThread(w); }

				}
				if(pool_Resource!=null && baseHour.equals(aggreTargetResoHour))
				{
					LOG.trace("aggreTargetResoHour:" + aggreTargetResoHour);
					aggreTargetResoHour = this.sd.getDateTimeFormatter_OnlyHour().format(addResoHour.getTime());
					
					Aggregate_RawDataReqResource w = null;
					try {
						w = (Aggregate_RawDataReqResource) pool_Resource.getThread();
						w.SetCurrentDate(ThreadAggregate_Sch.type_Hour, currentDate);
					}
					catch (Exception e) { LOG.error(e.getMessage()); e.getStackTrace(); }
					finally { if (w != null) pool_Resource.releaseThread(w); }
				}
				
				if(pool_Action != null && baseDay.equals(aggreTargetActDay))
				{
					LOG.trace("aggreTargetActDay:" + aggreTargetActDay);
					aggreTargetActDay = this.sd.getDateTimeFormatter_OnlyDay().format(addActDay.getTime());
					
					Aggregate_RawDataAction w = null;
					try {
						w = (Aggregate_RawDataAction) pool_Action.getThread();
						w.SetCurrentDate(ThreadAggregate_Sch.type_Day, currentDate);
					}
					catch (Exception e) { LOG.error(e.getMessage()); e.getStackTrace(); }
					finally { if (w != null) pool_Action.releaseThread(w); }
					
				}
				if(pool_Resource!=null && baseDay.equals(aggreTargetResoDay))
				{
					LOG.trace("aggreTargetResoDay:" + aggreTargetResoDay);
					aggreTargetResoDay = this.sd.getDateTimeFormatter_OnlyDay().format(addResoDay.getTime());
					
					Aggregate_RawDataReqResource w = null;
					try {
						w = (Aggregate_RawDataReqResource) pool_Resource.getThread();
						w.SetCurrentDate(ThreadAggregate_Sch.type_Day, currentDate);
					}
					catch (Exception e) { LOG.error(e.getMessage()); e.getStackTrace(); }
					finally { if (w != null) pool_Resource.releaseThread(w); }
					
				}
				
			}
			catch(Exception e)
			{
			    e.printStackTrace();
			}
		}
	}


}