package com.speno.xmon.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.comm.SimDate;

public class DataInserterEventLevel {

	private final static Logger LOG = LoggerFactory.getLogger(DataInserterEventLevel.class);
	
	public static boolean insertEvent(String agentName , String eventName , String eventText , int eventLevel
		    , long eventTime , long lAgentSendDateTime){
		String agentSendDateTime = SimDate.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis()));
		return insertEvent(agentName, eventName, eventText, eventLevel, eventTime, agentSendDateTime);
	}
	
	public static boolean insertEvent(String agentName , String eventName, String eventText
									  , int eventLevel , long eventTime  ,String agentSendDateTime) 
	{
		if (DBProperties.getInstance().open(DBProperties.dbType_EventLog) == false) { 
			return false;
		}
		
		if(eventTime == 0) return false;        
		String query = " INSERT INTO  XmEventLevel ("
						+ " AgentName"
						+ ",EventName"
						+ ",EventText"
						+ ",EventLevel"
						+ ",EventDateTime"
						+ ",ReceviedTime"
						+ ")"
						+ " VALUES ("
						+ " ?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?)";

		PreparedStatement prep = null;		
		Connection con = null;
		try {
			con = DBProperties.getInstance().getConnection(DBProperties.dbType_EventLog);
			
			prep = con.prepareStatement(query);
			prep.setString(1, agentName);
			prep.setString(2, eventName);
			prep.setString(3, eventText);
			prep.setInt(4, eventLevel);
			prep.setString(5, SimDate.getDateTimeFormatter_MS().format(new java.util.Date(eventTime)));
			prep.setString(6, agentSendDateTime);
			
			int ret = prep.executeUpdate();
			prep.close();
			
			if (ret == 1) return true;
			
		} catch (Exception e) {
			LOG.error("eventName:" + eventName + ", eventText" + eventText);			
		}
		finally{
			if (prep != null)
				try {
					prep.close();
					prep = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (con != null)
				DBProperties.getInstance().releaseConnection(DBProperties.dbType_EventLog, con);
		}
		return false;
	}

	
	


}
