package com.speno.xmon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.sql.Connection;

import com.speno.xmon.aggregate.builder.AggregatedRecivedActionItem;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.db.DBProperties;

public class DataInserterActionTest {

	private DBProperties db;

	public DataInserterActionTest() {
		this.db = DBProperties.getInstance();
	}

	public boolean Insert_Acc_Concurrent(int AGE_IDX
			, String CON_DATE
			, String CON_TIME
			, String CON_IP
			, String CON_HOSTNAME
			, String CON_DESC
			, String RCV_DATETIME) throws SQLException {

		if (this.db.open(DBProperties.dbType_Action) == false) { return false; }
		String query = " INSERT INTO TBL_ACC_CONCURRENT ("
						+ "  AGE_IDX"
						+ ",CON_DATE"
						+ ",CON_TIME"
						+ ",CON_IP"
						+ ",CON_HOSTNAME"
						+ ",CON_DESC "
						+ ",RCV_DATETIME )"
						+ " VALUES ("
						+ " ?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?)";

		PreparedStatement prep = this.db.getConnection(DBProperties.dbType_Action).prepareStatement(query);
		prep.setInt(1, AGE_IDX);
		prep.setString(2, CON_DATE);
		prep.setString(3, CON_TIME);
		prep.setString(4, CON_IP);
		prep.setString(5, CON_HOSTNAME);
		prep.setString(6, CON_DESC);
		prep.setString(7, RCV_DATETIME);

		boolean result = false;
		int ret = prep.executeUpdate();
		prep.close();

		if (ret == 1)
			result = true;

		return result;
	}

	public boolean Insert_Rec_Insert(int AGE_IDX
			, String INS_DATE
			, String INS_TIME
			, String INS_IP
			, String INS_HOSTNAME
			, String INS_SIZE
			) throws SQLException {

		if (this.db.open(DBProperties.dbType_Action) == false) { return false; }
		String query = " INSERT INTO  TBL_REC_INSERT ("
						+ "  AGE_IDX"
						+ ",INS_DATE"
						+ ",INS_TIME"
						+ ",INT_IP"
						+ ",INT_HOSTNAME"
						+ ",INT_SIZE)"
						+ " VALUES ("
						+ " ?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?)";

		PreparedStatement prep = this.db.getConnection(DBProperties.dbType_Action)
				.prepareStatement(query);
		prep.setInt(1, AGE_IDX);
		prep.setString(2, INS_DATE);
		prep.setString(3, INS_TIME);
		prep.setString(4, INS_IP);
		prep.setString(5, INS_HOSTNAME);
		prep.setString(6, INS_SIZE);

		boolean result = false;
		int ret = prep.executeUpdate(query);
		prep.close();

		if (ret == 1)
			result = true;

		return result;
	}
	public boolean Insert_XmTransAction(	String agentName
															  , String actionName
															  , String transID
															  , long transTime
															  , long responseTime) 
	{
		if (this.db.open(DBProperties.dbType_Action) == false) { return false; }

		if(transTime == 0) return false;
		if(responseTime == 0) return false;
        
		String query = " INSERT INTO  XmTransAction ("
						+ " AgentName"
						+ ",ActionName"
						+ ",TransID"
						+ ",TransTime"
						+ ",ResponseTime"
						+ ")"
						+ " VALUES ("
						+ " ?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?)";

		PreparedStatement prep;
		
		try {
			Connection con = this.db.getConnection(DBProperties.dbType_Action);
			
			prep = con.prepareStatement(query);
			prep.setString(1, agentName);
			prep.setString(2, actionName);
			prep.setString(3, transID);
			prep.setString(4, new SimDate().DateTimeFormatter_MS.format(new java.util.Date(transTime)));
			prep.setLong(5, responseTime);
			
			int ret = prep.executeUpdate();
			prep.close();
			
			if (ret == 1) return true;
			
		} catch (SQLException e) {
			// java.sql.SQLException: not implemented by SQLite JDBC driver
			e.printStackTrace();
		}
		return false;
	}
	
	public List<String> Insert_XmAggregatedActionSec(HashMap<String, AggregatedRecivedActionItem> hmActionAgentName) {

		if (this.db.open(DBProperties.dbType_Action) == false) { return null; }
		
		Iterator<String>  iter 			=  hmActionAgentName.keySet().iterator();
		List<String> resultTransIDs = new ArrayList<String>();
		String  key 						= "";
		
		while( iter.hasNext()) 
		{
		        key = (String) iter.next();
		        if(key.equals("")) continue;
		        
		        AggregatedRecivedActionItem value = hmActionAgentName.get(key);

		        if(hasDb(value,resultTransIDs))
		        {
		        	continue;
		        }
		        
				String query = " INSERT INTO  XmAggregatedActionSec ("
								+ " AgentName"
								+ ",ActionName"
								+ ",ResponseAvgTime"
								+ ",ResponseMaxTime"
								+ ",ResponseMinTime"
								+ ",SuccessCount"
								+ ",ErrorCount"
								+ ",TimeOutCount"
								+ ",AggregatedTime)"
								+ " VALUES ("
								+ " ?"
								+ ",?"
								+ ",?"
								+ ",?"
								+ ",?"
								+ ",?"
								+ ",?"
								+ ",?"
								+ ",?)";

				PreparedStatement prep;
				
				try {
					
					Connection con = this.db.getConnection(DBProperties.dbType_Action);
					//con.setAutoCommit(false);
					prep = con.prepareStatement(query);
					prep.setString(1, value.GetAgentName());
					prep.setString(2, value.GetActionName());
					prep.setInt(3, value.GetResponseAvgTime());
					prep.setInt(4, value.GetResponseMaxTime());
					prep.setInt(5, value.GetResponseMinTime());
					prep.setInt(6, value.GetSuccessCount());
					prep.setInt(7, value.GetErrorCount());
					prep.setInt(8, value.GetTimeOutCount());
					prep.setString(9, value.GetAggregatedTime());
					
					int ret = prep.executeUpdate();
					prep.close();
					//con.commit();
					
					if (ret == 1)
					{
						Iterator<String> transIDs=  value.getTransID().iterator();
						while(transIDs.hasNext())
						{
							resultTransIDs.add(transIDs.next());
						}
					}
				} catch (SQLException e) {
					// java.sql.SQLException: not implemented by SQLite JDBC driver

					e.printStackTrace();
				}
		}
		return resultTransIDs;
	}
	
	/*
	 * �̹� �ִٸ� ������Ʈ�� �Ѵ�
	 */
	private boolean hasDb(AggregatedRecivedActionItem value,List<String> resultTransIDs) {
		String query = " SELECT * FROM XmAggregatedActionSec WHERE"
				+ "         					AgentName			= ?"
				+ " 					AND ActionName			= ?"
				+ " 					AND AggregatedTime		= ? ";
		
		PreparedStatement prep = null;
		ResultSet rs					= null;
		try {
			prep = this.db.getConnection(DBProperties.dbType_Action).prepareStatement(query);
			
			prep.setString(1, value.GetAgentName().trim());
			prep.setString(2, value.GetActionName().trim());
			prep.setString(3, value.GetAggregatedTime().trim());
			
			rs = prep.executeQuery();

			int cnt =0;
			while(rs.next())
			{
				cnt++;
				int ResponseAvgTime 	= rs.getInt("ResponseAvgTime");
				int ResponseMaxTime 	= rs.getInt("ResponseMaxTime");
				int ResponseMinTime 	= rs.getInt("ResponseMinTime");
				
				int SuccessCount 			= rs.getInt("SuccessCount") 	+ value.GetSuccessCount();
				int ErrorCount 				= rs.getInt("ErrorCount") 			+ value.GetErrorCount();
				int TimeOutCount 			= rs.getInt("TimeOutCount") 	+ value.GetTimeOutCount();
				
				int sumRsTime = ResponseAvgTime * rs.getInt("SuccessCount");
				
				sumRsTime += value.GetResponseAvgTime();
				ResponseAvgTime = sumRsTime / SuccessCount;
				
				if(ResponseMaxTime < value.GetResponseMaxTime())
					ResponseMaxTime =value.GetResponseMaxTime();
				
				if(ResponseMinTime > value.GetResponseMinTime())
					ResponseMinTime =value.GetResponseMinTime();
				
					query = " UPDATE XmAggregatedActionSec SET "
						+ " 					ResponseAvgTime 		= ?"
						+ "				,	ResponseMaxTime		= ?"
						+ "				,	ResponseMinTime		= ?"
						+ "				,	SuccessCount			= ?"
						+ "				,	ErrorCount					= ?"
						+ "				,	TimeOutCount			= ?"						
						+ " 		WHERE	AgentName			= ?"
						+ "				AND	ActionName			= ?"
						+ "				AND	AggregatedTime 	= ? ";
				
					PreparedStatement prepUp = null;
				try {
					 prepUp = this.db.getConnection(DBProperties.dbType_Action).prepareStatement(query);	
					
					prepUp.setInt(1, ResponseAvgTime);
					prepUp.setInt(2, ResponseMaxTime);
					prepUp.setInt(3, ResponseMinTime);
					prepUp.setInt(4, SuccessCount);
					prepUp.setInt(5, ErrorCount);
					prepUp.setInt(6, TimeOutCount);
					
					prepUp.setString(7, value.GetAgentName());
					prepUp.setString(8, value.GetActionName());
					prepUp.setString(9, value.GetAggregatedTime());
					
					int ret = prepUp.executeUpdate();

					if (ret == 1)
					{
						Iterator<String> transIDs=  value.getTransID().iterator();
						while(transIDs.hasNext())
						{
							resultTransIDs.add(transIDs.next());
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}finally {
					if(prepUp != null )  prepUp.close();
				}
			}//End Select Rs While
			
			if(rs != null) rs.close();
			if(prep != null )  prep.close();
			
			if(cnt>0) return true;
			else return false;
			
		} catch (SQLException e) {
			// java.sql.SQLException: not implemented by SQLite JDBC driver
			e.printStackTrace();
		}
		finally{
			
		}
		return false;
	}

	

}
