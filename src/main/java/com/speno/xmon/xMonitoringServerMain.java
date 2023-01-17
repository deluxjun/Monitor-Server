package com.speno.xmon;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.aggregate.AggregatorThread;
import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.listener.AmassServer;
import com.speno.xmon.listener.CommandServer;
import com.speno.xmon.sms.SendSMS;

public class xMonitoringServerMain 
{
	private final static Logger LOG = LoggerFactory.getLogger(xMonitoringServerMain.class);
	private static SendSMS sender;
	
	public static SendSMS getSendSMS() {
		return sender;
	}


	public static void setSendSMS(SendSMS sendSMS) {
		sender = sendSMS;
	}


	public static void main(String[] args) throws IOException 
	{
	    /**************************************************************/
	    /******************** 1. 프로퍼티 가져오기 *******************/
	    /**************************************************************/
		//디폴트 경로
	    String pathProperties = "D:/DEV_ENV/workspace3/xMonitoring_Server/conf/xMonServer.xml";
	    if(args.length ==1) pathProperties=args[0].trim();
	    
		if(!new xmPropertiesXml().Init(pathProperties, xmPropertiesXml.propertiesTypeXmAmServer))
		{
			LOG.error("configuration error :" + pathProperties);
			return;
		}
		
	
		//for test
	/*	sender.connectSms();
		sender.sendSms();
		sender.disConnectSms();*/
	    /**************************************************************/
	    /*************** 2.Linstener Amass스레드 *****************/
	    /**************************************************************/
		AmassServer transServer = AmassServer.getInstance();
		if (transServer == null) {
			LOG.error("couldn't start Transation server");
			return;
		}
	
	    CommandServer server = new CommandServer();
	    if(server == null){
	    	LOG.error("couldn't start Command server");
	    	return;
	    }
	    	
	    /**************************************************************/
	    /******************** 3.시간별 집계 스레드 *******************/
	    /**************************************************************/
	    AggregatorThread aggregator = AggregatorThread.getInstance();
	    if(server == null){
	    	LOG.error("can't start aggreagtor");
	    	return;
	    }
	    else
	    aggregator.startup();
	    
//	    /**************************************************************/
//	    /******************** 3.시간별 집계 스레드 *******************/
//	    /**************************************************************/
//	    if(xmPropertiesXml.htXmPropertiesAmass_String.get( xmPropertiesXml.AggregateSchedulerYN).equals("Y"))
//	    {
//		    Thread threadAggregatedScheduler = new Thread(new ThreadAggregate_Sch() );
//			threadAggregatedScheduler.setName("Thread Aggregate Scheduler");
//			threadAggregatedScheduler.start();
//	    }
	    
	}
	
	
}
