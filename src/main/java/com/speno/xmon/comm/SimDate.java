package com.speno.xmon.comm;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimDate {
	
	private final static Logger LOG = LoggerFactory.getLogger(SimDate.class);
	// "yyyyMMddHHmmss"
	//
        
    public static DateFormat getDateTimeFormatter_MS() {
    	DateFormat DateTimeFormatter_MS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return DateTimeFormatter_MS;
	}
	public static DateFormat getDateTimeFormatter_Sec() {
		DateFormat DateTimeFormatter_Sec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return DateTimeFormatter_Sec;
	}
	public static DateFormat getDateTimeFormatter_Min() {
		DateFormat DateTimeFormatter_Min = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return DateTimeFormatter_Min;
	}
	public static DateFormat getDateTimeFormatter_Hour() {
		DateFormat DateTimeFormatter_Hour 		= new SimpleDateFormat("yyyy-MM-dd HH");
		return DateTimeFormatter_Hour;
	}
	public static DateFormat getDateTimeFormatter_Day() {
		DateFormat DateTimeFormatter_Day 			= new SimpleDateFormat("yyyy-MM-dd");
		return DateTimeFormatter_Day;
	}
	public static DateFormat getDateTimeFormatter_OnlySec() {
		DateFormat DateTimeFormatter_OnlySec 	= new SimpleDateFormat("ss");
		return DateTimeFormatter_OnlySec;
	}
	public static DateFormat getDateTimeFormatter_OnlyMin() {
		DateFormat DateTimeFormatter_OnlyMin = new SimpleDateFormat("mm");
		return DateTimeFormatter_OnlyMin;
	}
	public static DateFormat getDateTimeFormatter_OnlyHour() {
		DateFormat DateTimeFormatter_OnlyHour 	= new SimpleDateFormat("HH");
		return DateTimeFormatter_OnlyHour;
	}
	public static DateFormat getDateTimeFormatter_OnlyDay() {
		DateFormat DateTimeFormatter_OnlyDay = new SimpleDateFormat("yyyy-MM-dd");
		return DateTimeFormatter_OnlyDay;
	}

	
    
    public static final String gap_ms		= "gap_ms";
    public static final String gap_sec		= "gap_sec";
    public static final String gap_min 		= "gap_min";
    public static final String gap_hour		= "gap_hour";
    public static final String gap_day 	= "gap_day";
    
    static public   String GetTransformTime(DateFormat dateTimeFormatterOrg, String strTime
    																			  , DateFormat dateTimeFormatterRevise)
    {
    	Date dateTime = null;
		try {
			dateTime = dateTimeFormatterOrg.parse(strTime);
			
			// 가끔 터무니없이 큰 값이 들어와서 체크.
			if (dateTime.after(new Date(System.currentTimeMillis() + 86400*1000L)))
				return "";
			
			return dateTimeFormatterRevise.format(dateTime);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
    	return "";
    }
    static public   String GetTransformTime(long strTime, DateFormat dateTimeFormatterRevise)
	{
		String   result = dateTimeFormatterRevise.format(new java.util.Date(strTime));
		return result;
	}
    static public   int  GetTimegap(DateFormat dateTimeFormatter_Init, String initTime
    																   ,DateFormat dateTimeFormatter_Comp, String completeTime, String returnType)
	{
		  int returnValue = 0;

		  int returnTimeType =1;
		  if(returnType.equals(SimDate.gap_ms))
			  returnTimeType = 1;
		  else if(returnType.equals(SimDate.gap_sec))
			  returnTimeType = 1000;
		  else if(returnType.equals(SimDate.gap_min))
			  returnTimeType = 1000 * 60 ;
		  else if(returnType.equals(SimDate.gap_hour))
			  returnTimeType = 1000 * 60 * 60;
		  else if(returnType.equals(SimDate.gap_day))
			  returnTimeType = 1000 * 60 * 60 * 24;   
				  
		  try{
			  returnValue = (int)(dateTimeFormatter_Comp.parse(completeTime).getTime() - dateTimeFormatter_Init.parse(initTime).getTime())  ; 
			  if(returnValue <0)
			  {
				  LOG.error("GetTimegap initTime:"+ initTime + "[" + dateTimeFormatter_Init.toString() + "], completeTime:" + completeTime + "[" + dateTimeFormatter_Comp.toString() + "]");
			  }
		}catch(Exception e)
		{
		   e.printStackTrace();
		   returnValue = -1;
		}
		  return returnValue / returnTimeType;
	}
    

    static public boolean GetTimeout(DateFormat dateTimeFormatter_Init, String initTime, int transaction_Timeout) throws ParseException 
    {
        Calendar addSec 	= Calendar.getInstance();
        Date currentDate	= null;
        Date date_initTime	= null;
        
    	currentDate 		= new Date(System.currentTimeMillis());
    	date_initTime 	= dateTimeFormatter_Init.parse(initTime); //14 04716 412 750 + 600 000
    	addSec.setTime(date_initTime);
		addSec.add(Calendar.SECOND,  transaction_Timeout);  //14 04717 012 750
		if(currentDate.getTime() - addSec.getTimeInMillis() < 0 )  return false;
		return true;
	} 
    static public boolean GetTimeout(DateFormat dateTimeFormatter_Init, long initTime, int transaction_Timeout) throws ParseException 
    {
    	Calendar addSec 	= Calendar.getInstance();
    	addSec.setTime(new Date(initTime));
		addSec.add(Calendar.SECOND,  transaction_Timeout);  //14 04717 012 750
		if(System.currentTimeMillis() - addSec.getTimeInMillis() < 0 )  return false;
		return true;
	}

	public String GetDateTime(DateFormat dateTimeFormatter) {
		return dateTimeFormatter.format(new Date(System.currentTimeMillis()));
	} 
}
