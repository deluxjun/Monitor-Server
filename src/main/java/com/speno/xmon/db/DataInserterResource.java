package com.speno.xmon.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.aggregate.builder.AggregatedRecivedResourceItem;
import com.speno.xmon.env.ItemAgent;
import com.speno.xmon.env.ItemAgent.ItemCommandListAndSub;
import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.util.JsonUtil;

public class DataInserterResource {

	private final static Logger LOG = LoggerFactory.getLogger(DataInserterResource.class);
	
	public final static String TBL_ResourceSec		= "XmAggregatedResourceSec";
	public final static String TBL_ResourceMin		= "XmAggregatedResourceMin";
	public final static String TBL_ResourceHour		= "XmAggregatedResourceHour";
	public final static String TBL_ResourceDay 		= "XmAggregatedResourceDay";
	public final static String TBL_ResourceMonth	= "XmAggregatedResourceMonth";

	
	private DBProperties db;

	public DataInserterResource() {
		this.db = DBProperties.getInstance();
	}

	public boolean Insert_XmAggregatedResourceSec(List<AggregatedRecivedResourceItem> listResourceItem)
	{
		
		
		if (this.db.open(DBProperties.dbType_Resource) == false) return false;
		
		Iterator<AggregatedRecivedResourceItem>  iter =  listResourceItem.iterator();
		AggregatedRecivedResourceItem  value = null;
		PreparedStatement prep = null;
		String query = " INSERT INTO  XmAggregatedResourceSec ("
								+ " AgentName"
								+ ",CommandName"
								+ ",ResourceID"
								+ ",PropertyName"
								+ ",PropertyValue"
								+ ",PropertyValueUnit"
								+ ",AggregatedTime"
								+ ",ExtMap)"
								+ " VALUES ("
								+ " ?"
								+ ",?"
								+ ",?"
								+ ",?"
								+ ",?"
								+ ",?"
								+ ",?"
								+ ",?)";
		long start = System.currentTimeMillis();
		while(iter.hasNext()) 
		{
		        value =  iter.next();
		        if(value.GetPropertyName().equals("")) continue;		       
				Connection con = null;
				try {
					con = this.db.getConnection(DBProperties.dbType_Resource);
				
					prep = con.prepareStatement(query);
					prep.setString(1, value.GetAgentName());
					prep.setString(2, value.GetCommandName());
					prep.setString(3, value.GetResourceID());
					prep.setString(4, value.GetPropertyName());
					prep.setLong(5, value.GetValueLong());
					prep.setString(6, value.GetValueUnit());
					prep.setString(7, value.GetAggregatedTime());
					prep.setString(8, value.GetExtMapToString());					
					int ret = prep.executeUpdate();
					prep.close();
					if (ret != 1){
						LOG.error("db ResSec:" +  value.GetCommandName() + "_" + value.GetPropertyName() + "," + value.GetValueLong());
					}					
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error(e.getMessage());
				}
				finally{
					if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
					if (con != null)
						this.db.releaseConnection(DBProperties.dbType_Resource, con);
				}
		}
		long end = System.currentTimeMillis();
		long spend = end -start;
		LOG.debug("Insert_XmAggregatedResourceSec time :" + spend + " Ms");
		return true;
	}
	
	// 리소스 집계
	// 호출하면 최종데이터 이후의 데이터를 집계함
	public final static int TYPE_MIN = 1;
	public final static int TYPE_HOUR = 2;
	public final static int TYPE_DAY = 3;
	public boolean aggregateResource(int type) {
		String sourceTable = "";
		String targetTable = "";
		String propertyValue = "";
		
		int substrIndex = 16;
		if (TYPE_MIN == type) {
			sourceTable = "XmAggregatedResourceSec";
			targetTable = "XmAggregatedResourceMin";
			propertyValue = "PropertyValue";
			substrIndex = 16;
		}
		else if (TYPE_HOUR == type) {
			sourceTable = "XmAggregatedResourceMin";
			targetTable = "XmAggregatedResourceHour";
			propertyValue = "PropertyAvgValue";
			substrIndex = 13;
		}
		else if (TYPE_DAY == type) {
			sourceTable = "XmAggregatedResourceHour";
			targetTable = "XmAggregatedResourceDay";
			propertyValue = "PropertyAvgValue";
			substrIndex = 10;
		}
		else {
			LOG.error("NOT supported type");
			return false;
		}
		
		String query ="";
		query += "insert into   " + targetTable + " (AgentName						\n";                     
		query += "    			,	CommandName							\n";                    
		query += "    			,	ResourceID							\n";
		query += "    			,	PropertyName							\n";
		query += "	    		,	PropertyValueUnit						\n";
		query += "	    		,	AggregatedTime							\n";
		query += "	    		,	PropertyMinValue						\n";
		query += "	    		,	PropertyMaxValue						\n";
		query += "	    		,	PropertyAvgValue						\n";
		query += "	    		,	PropertyValueCnt						\n";
		query += "	    		)											\n";
		query += "Select	AgentName									\n";
		query += "    			,	CommandName							\n";
		query += "    			,	ResourceID							\n";
		query += "    			,	PropertyName							\n";
		query += "	    		,	PropertyValueUnit						\n";
		query += "     			,	substr(AggregatedTime,1,"+substrIndex+")		     	\n";
		query += "	    		,	Min("+propertyValue+")	PropertyMinValue			\n";
		query += "	    		,	Max("+propertyValue+")	PropertyMaxValue			\n";
		query += "	    		,	Avg("+propertyValue+") 	PropertyAvgValue			\n";
		query += "	    		,	count(*)           	PropertyValueCnt			\n";
		query += "From " + sourceTable + " 									\n";
		query += "WHERE	AggregatedTime > (select (CASE WHEN Max(AggregatedTime) IS NULL THEN '0' ELSE Max(AggregatedTime) END) from " + targetTable + " ) || 'z'	\n";
		query += "GROUP BY	AgentName,CommandName, ResourceID, PropertyName, PropertyValueUnit, substr(AggregatedTime,1,"+substrIndex+")						\n";
		
		PreparedStatement prep				= null;
		Connection con = null;
		int ret = 0;

		try {
			con = this.db.getConnection(DBProperties.dbType_Resource);

			prep = con.prepareStatement(query);
//			prep.setInt(1, substrIndex);
//			prep.setInt(2, substrIndex);	
			ret = prep.executeUpdate();		
			return true;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			e.printStackTrace();
		}
		finally{
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
		return false;	
	}

//	public boolean Insert_XmAggregatedResourceMin(String aggreDateTimeStr) {
//		
//		if (this.db.open(DBProperties.dbType_Resource) == false)
//		{
//			return false;
//		}
//		
//		String query ="   SELECT	AgentName"
//				  + "						,	CommandName					"
//				  + "						,	ResourceID					    "           
//	    		  + "						,	PropertyName					"
//	    		  + "         				,	Min(PropertyValue)		min	"
//	    		  + "         				,	Max(PropertyValue)	max	"
//	    		  + "         				,	sum(PropertyValue)	sum	"
//	    		  + "         				,	count(*)           		 	cnt    "
//	    		  + "         				,	Avg(PropertyValue) 	avg    "
//	    		  + "         				,	PropertyValueUnit 			     "
//	    		  + "      		FROM	XmAggregatedResourceSec "
//	    		  + "     		 WHERE	AggregatedTime like ?				"
//	    		  + "     	GROUP BY	AgentName, CommandName, ResourceID, PropertyName, PropertyValueUnit	";
//		
//		String queryInsert = " INSERT INTO  XmAggregatedResourceMin ("
//					+ " AgentName"
//					+ ",CommandName"
//					+ ",ResourceID"
//					+ ",PropertyName"
//					+ ",PropertyMinValue"
//					+ ",PropertyMaxValue"
//					+ ",PropertyAvgValue"
//					+ ",PropertyValueCnt"
//					+ ",PropertyValueUnit"
//					+ ",AggregatedTime)"
//					+ " VALUES ("
//					+ " ?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?)";
///*
//		String query =					
//				   "     INSERT INTO  XmAggregatedResourceMin (                          "
//				+ " 				 AgentName                                          "
//				+ " 				,CommandName                                        "
//				+ " 				,ResourceID                                         "
//				+ " 				,PropertyName                                       "
//				+ " 				,PropertyMinValue                                   "
//				+ " 				,PropertyMaxValue                                   "
//				+ " 				,PropertyAvgValue                                   "
//				+ " 				,PropertyValueCnt                                   "
//				+ " 				,PropertyValueUnit                                  "
//				+ " 				,AggregatedTime)                                    "
//				+ " 	   SELECT	AgentName                                           "
//				+ " 			,	CommandName			                                "
//				+ " 			,	ResourceID			                                "
//				+ " 			,	PropertyName		                                "
//				+ " 			,	Min(PropertyValue)	PropertyMinValue	            "
//				+ " 			,	Max(PropertyValue)	PropertyMaxValue	            "
//				+ " 			,	Avg(PropertyValue) 	PropertyAvgValue                "
//				+ " 			,	count(*)           	PropertyValueCnt           		"		
//				+ " 			,	PropertyValueUnit 			                        "
//				+ " 			,   substr(AggregatedTime,0,17) AggregatedTime          "
//			    + " 		FROM	XmAggregatedResourceSec                             "
//			   	+ " 	   WHERE	substr(AggregatedTime,0,17) =?   "
//			   	+ "     GROUP BY	AgentName                                           "
//				+ " 			, CommandName                                           "
//				+ " 			, ResourceID                                            "
//				+ " 			, PropertyName                                          "
//				+ " 			, PropertyValueUnit                                    ";
//						*/
//				
//		PreparedStatement prep				= null;
//		PreparedStatement prepInsert		= null;
//		ResultSet rs								= null;
//		Connection con = null;
//		int ret = 0;
//		try {
//			con = this.db.getConnection(DBProperties.dbType_Resource);
//			prep = con.prepareStatement(query);
//			prep.setString(1, aggreDateTimeStr + "%");
//			//prep.setString(1, aggreDateTimeStr );
//			rs = prep.executeQuery();
//			
//			//int cnt =0;
//			while(rs.next())
//			{
//				//cnt++;
//				try {
//						prepInsert = con.prepareStatement(queryInsert);
//						prepInsert.setString(1, rs.getString("AgentName"));
//						prepInsert.setString(2, rs.getString("CommandName"));
//						prepInsert.setString(3, rs.getString("ResourceID"));
//						prepInsert.setString(4, rs.getString("PropertyName"));
//						prepInsert.setLong(5, rs.getLong("min"));
//						prepInsert.setLong(6, rs.getLong("max"));
//						prepInsert.setLong(7, rs.getLong("avg"));
//						prepInsert.setInt(8, rs.getInt("cnt"));
//
//						prepInsert.setString(9,rs.getString("PropertyValueUnit"));
//						prepInsert.setString(10, aggreDateTimeStr);
//						
//						ret = prepInsert.executeUpdate();
//						prepInsert.close();
//						
//						if (ret == 1)
//						{
//							LOG.debug("DB ResMin��� ����:" +  rs.getString("CommandName") + "_" + rs.getString("PropertyName") + "," + rs.getLong("avg"));
//						}
//						else
//						{
//							LOG.error("DB ResMin��� ����:" + rs.getString("CommandName") + "_" + rs.getString("PropertyName") + "," + rs.getLong("avg"));
//						}
//					} catch (Exception e) {
//						// java.sql.SQLException: not implemented by SQLite JDBC driver
//						e.printStackTrace();
//					}
//			}
//			if(ret>0) return true;
//			else return false;
//			
//		} catch (Exception e) {
//			// java.sql.SQLException: not implemented by SQLite JDBC driver
//			e.printStackTrace();
//		}
//		finally{
//			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
//			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
//			if(prepInsert != null ) 	try {prepInsert.close();}	catch (SQLException e) {e.printStackTrace();}
//			if (con != null)
//				this.db.releaseConnection(DBProperties.dbType_Resource, con);
//		}
//		return false;	
//	}
//	
//	/*
//	 * �� ���� ���ҽ� ���
//	 */
//	public boolean Insert_XmAggregatedResourceHour(String aggreDateTimeStr) {
//		
//		if (this.db.open(DBProperties.dbType_Resource) == false) return false;
//		
//		String query ="   SELECT	AgentName"
//				  + "						,	CommandName						"
//				  + "						,	ResourceID					    	"
//	    		  + "						,	PropertyName						"
//	    		  + "         				,	Min(PropertyAvgValue)	min	"
//	    		  + "         				,	Max(PropertyAvgValue)	max	"
//	    		  + "         				,	sum(PropertyAvgValue)	sum	"
//	    		  + "         				,	count(*)           		 		cnt   "
//	    		  + "         				,	Avg(PropertyAvgValue) 	avg   "
//	    		  + "         				,	PropertyValueUnit					"
//	    		  + "      		FROM	XmAggregatedResourceMin	"
//	    		  + "     		 WHERE	AggregatedTime like ?			"
//	    		  + "     	GROUP BY	AgentName, CommandName, PropertyName, PropertyValueUnit	";
//		
//		String queryInsert = " INSERT INTO  XmAggregatedResourceHour ("
//					+ " AgentName"
//					+ ",CommandName"
//					+ ",ResourceID"
//					+ ",PropertyName"
//					+ ",PropertyMinValue"
//					+ ",PropertyMaxValue"
//					+ ",PropertyAvgValue"
//					+ ",PropertyValueCnt"
//					+ ",PropertyValueUnit"
//					+ ",AggregatedTime)"
//					+ " VALUES ("
//					+ " ?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?)";
//		
//		Connection con = null;
//		PreparedStatement prep				= null;
//		PreparedStatement prepInsert		= null;
//		ResultSet rs								= null;
//		try {
//			con = this.db.getConnection(DBProperties.dbType_Resource);
//			prep = con.prepareStatement(query);
//			prep.setString(1, aggreDateTimeStr + "%");
//			rs = prep.executeQuery();
//			LOG.debug("AggHour aggreDateTimeStr:"+aggreDateTimeStr);
//			
//			int cnt =0;
//			while(rs.next())
//			{
//				cnt++;
//				//LOG.debug("AggHour RS:"+cnt);
//				try {
//						prepInsert = con.prepareStatement(queryInsert);
//						prepInsert.setString(1, rs.getString("AgentName"));
//						prepInsert.setString(2, rs.getString("CommandName"));
//						prepInsert.setString(3, rs.getString("ResourceID"));
//						prepInsert.setString(4, rs.getString("PropertyName"));
//						prepInsert.setLong(5, rs.getLong("min"));
//						prepInsert.setLong(6, rs.getLong("max"));
//						prepInsert.setLong(7, rs.getLong("avg"));
//						prepInsert.setInt(8, rs.getInt("cnt"));
//
//						prepInsert.setString(9,rs.getString("PropertyValueUnit"));
//						prepInsert.setString(10, aggreDateTimeStr);
//						
//						int ret = prepInsert.executeUpdate();
//						prepInsert.close();
//						
//						if (ret == 1)
//						{
//							//LOG.debug("DB ResHour��� ����:" +  rs.getString("CommandName") + "_" + rs.getString("PropertyName") + "," + rs.getLong("avg"));
//						}
//						else
//						{
//							LOG.debug("DB ResHour��� ����:" + rs.getString("CommandName") + "_" + rs.getString("PropertyName") + "," + rs.getLong("avg"));
//						}
//					} catch (Exception e) {
//						// java.sql.SQLException: not implemented by SQLite JDBC driver
//						e.printStackTrace();
//					}
//			}
//			if(cnt>0) return true;
//			else return false;
//			
//		} catch (Exception e) {
//			// java.sql.SQLException: not implemented by SQLite JDBC driver
//			e.printStackTrace();
//		}
//		finally{
//			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
//			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
//			if(prepInsert != null ) 	try {prepInsert.close();}	catch (SQLException e) {e.printStackTrace();}
//			if (con != null)
//				this.db.releaseConnection(DBProperties.dbType_Resource, con);
//		}
//		return false;
//	}
//	
//	/*
//	 * �� ���� ���ҽ� ���
//	 */
//	public boolean Insert_XmAggregatedResourceDay(String aggreDateTimeStr) {
//		
//		if (this.db.open(DBProperties.dbType_Resource) == false) return false;
//		
//		String query ="   SELECT	AgentName"
//				  + "						,	CommandName						"                    
//	    		  + "						,	PropertyName						"
//	    		  + "						,	ResourceID   							"
//	    		  + "         				,	Min(PropertyAvgValue)	min	"
//	    		  + "         				,	Max(PropertyAvgValue)	max	"
//	    		  + "         				,	sum(PropertyAvgValue)	sum	"
//	    		  + "         				,	count(*)           		 		cnt   "
//	    		  + "         				,	Avg(PropertyAvgValue) 	avg   "
//	    		  + "         				,	PropertyValueUnit					"
//	    		  + "      		FROM	XmAggregatedResourceHour	"
//	    		  + "     		 WHERE	AggregatedTime like ?			"
//	    		  + "     	GROUP BY	AgentName, CommandName, ResourceID, PropertyName, PropertyValueUnit	";
//		
//		String queryInsert = " INSERT INTO  XmAggregatedResourceDay ("
//					+ " AgentName"
//					+ ",CommandName"
//					+ ",ResourceID"
//					+ ",PropertyName"
//					+ ",PropertyMinValue"
//					+ ",PropertyMaxValue"
//					+ ",PropertyAvgValue"
//					+ ",PropertyValueCnt"
//					+ ",PropertyValueUnit"
//					+ ",AggregatedTime)"
//					+ " VALUES ("
//					+ " ?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?"
//					+ ",?)";
//		
//		Connection con = null;
//		PreparedStatement prep				= null;
//		PreparedStatement prepInsert		= null;
//		ResultSet rs 								= null;
//		try {
//			con = this.db.getConnection(DBProperties.dbType_Resource);
//			prep = con.prepareStatement(query);
//			prep.setString(1, aggreDateTimeStr + "%");
//			rs = prep.executeQuery();
//			LOG.debug("AggDay aggreDateTimeStr:"+aggreDateTimeStr);
//			int cnt =0;
//			while(rs.next())
//			{
//				cnt++;
//				//LOG.debug("AggDay RS:"+cnt);
//				try {
//						prepInsert = con.prepareStatement(queryInsert);
//						prepInsert.setString(1, rs.getString("AgentName"));
//						prepInsert.setString(2, rs.getString("CommandName"));
//						prepInsert.setString(3, rs.getString("ResourceID"));
//						prepInsert.setString(4, rs.getString("PropertyName"));
//						prepInsert.setLong(5, rs.getLong("min"));
//						prepInsert.setLong(6, rs.getLong("max"));
//						prepInsert.setLong(7, rs.getLong("avg"));
//						prepInsert.setInt(8, rs.getInt("cnt"));
//
//						prepInsert.setString(9,rs.getString("PropertyValueUnit"));
//						prepInsert.setString(10, aggreDateTimeStr);
//						
//						int ret = prepInsert.executeUpdate();
//						prepInsert.close();
//						
//						if (ret == 1)
//						{
//							//LOG.debug("DB ResDay��� ����:" +  rs.getString("CommandName") + "_" + rs.getString("PropertyName") + "," + rs.getLong("avg"));
//						}
//						else
//						{
//							LOG.error("DB ResDay��� ����:" + rs.getString("CommandName") + "_" + rs.getString("PropertyName") + "," + rs.getLong("avg"));
//						}
//					} catch (Exception e) {
//						// java.sql.SQLException: not implemented by SQLite JDBC driver
//						e.printStackTrace();
//					}
//			}
//			if(cnt>0) return true;
//			else return false;
//			
//		} catch (Exception e) {
//			// java.sql.SQLException: not implemented by SQLite JDBC driver
//			e.printStackTrace();
//		}
//		finally{
//			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
//			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
//			if(prepInsert != null ) 	try {prepInsert.close();}	catch (SQLException e) {e.printStackTrace();}
//			if (con != null)
//				this.db.releaseConnection(DBProperties.dbType_Resource, con);
//		}
//		return false;
//	}
	
	private boolean hasRowData_XmCommandList(String agentName, String commandName) {
		String query = " SELECT count(*) as cnt FROM XmCommandList WHERE"
				+ "         					AgentName			= ?"
				+ " 					AND CommandName		= ?";
		
		Connection con = null;
		PreparedStatement prep = null;
		ResultSet rs					= null;
		try {
			con = this.db.getConnection(DBProperties.dbType_Resource);
			
			prep = con.prepareStatement(query);			
			prep.setString(1, agentName.trim());
			prep.setString(2, commandName.trim());			
			rs = prep.executeQuery();			
			if(rs.next())
				if(rs.getInt("cnt") >= 1)
				return true;				
			return false;			
		} catch (Exception e) {			
			e.printStackTrace();
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
		return false;
	}
	public int Delete_XmCommandSubList(String agentName, String commandName, String resourceID) {
		String query = " DELETE FROM XmCommandSubList WHERE"
				+ "         					AgentName			= ?";
		if (commandName.length() > 0)
			query += "AND CommandName = ?";			
		if (resourceID.length() > 0)
			query += "AND ResourceID = ?";
		
		Connection con = null;
		PreparedStatement prep = null;
		try {
			con = this.db.getConnection(DBProperties.dbType_Resource);
			
			prep = con.prepareStatement(query);
			
			int index = 1;
			prep.setString(index++, agentName.trim());
			if (commandName.length() > 0)
			prep.setString(index++, commandName.trim());
			if (resourceID.length() > 0)
			prep.setString(index++, resourceID.trim());			
			return prep.executeUpdate();			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
		return 0;
		
	}
//	private int hasRowCount_XmCommandSubList(String agentName, String commandName, String resourceID) {
//		String query = " SELECT count(*) as cnt FROM XmCommandSubList WHERE"
//				+ "         					AgentName			= ?"
//				+ " 					AND CommandName		= ?"			
//				+ " 					AND ResourceID			= ?";
//		
//		PreparedStatement prep = null;
//		ResultSet rs					= null;
//		Connection con = null;
//		try {
//			con = this.db.getConnection(DBProperties.dbType_Resource);
//			prep = con.prepareStatement(query);
//			
//			prep.setString(1, agentName.trim());
//			prep.setString(2, commandName.trim());
//			prep.setString(3, resourceID.trim());
//			
//			rs = prep.executeQuery();
//			if(rs.next()) return rs.getInt("cnt");
//			
//			return 0;
//			
//		} catch (Exception e) {
//			// java.sql.SQLException: not implemented by SQLite JDBC driver
//			e.printStackTrace();
//		}
//		finally{
//			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
//			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
//			if (con != null)
//				this.db.releaseConnection(DBProperties.dbType_Resource, con);
//		}
//		return 0;
//	}
	
	private boolean hasRowData_XmCommandSubList(String agentName, String commandName, String resourceID, String propertyName) {
		String query = " SELECT count(*) as cnt FROM XmCommandSubList WHERE"
				+ "         					AgentName			= ?"
				+ " 					AND CommandName		= ?"			
				+ " 					AND ResourceID			= ?"
				+ " 					AND propertyName		= ?";
		
		PreparedStatement prep = null;
		ResultSet rs					= null;
		Connection con = null;
		try {
			con = this.db.getConnection(DBProperties.dbType_Resource);
			
			prep = con.prepareStatement(query);
			
			prep.setString(1, agentName.trim());
			prep.setString(2, commandName.trim());
			prep.setString(3, resourceID.trim());
			prep.setString(4, propertyName.trim());
			
			rs = prep.executeQuery();
			if(rs.next())
				if(rs.getInt("cnt") >= 1)				
					return true;			
			return false;			
		} catch (Exception e) {			
			e.printStackTrace();
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
		return false;
	}
	public void InsertValidation_XmCommandList(String agentName, String commandString)
	{
		String commandName 	="";
		String resourceID 			="";
		String propertyName		="";
		
		String temp					="";
		String valueUnit				="";
		
		String commandTitle		="";
		String commandDesc		="";		
		String aggreUseYN			="";
		String healthUseYN		="";
		
		 JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(commandString);
			String[] commands = JSONObject.getNames(jsonObject);

			for(int i=0; i< commands.length;i++)
			{
				commandName =  commands[i];
	    		if(commands[i].indexOf("(") == 0)
	    		{
	    			int sp 		=  commands[i].indexOf("(");
	    			int lp 		=  commands[i].indexOf(")");
	    			temp 		=  commands[i].trim().substring(sp+1, lp);
	    			commandName = commands[i].substring(lp, commands[i].length()).trim();
	    		}
	    		else if(commands[i].indexOf("(") > 0)
	    		{
	    			int sp 		=  commands[i].indexOf("(");
	    			int lp 		=  commands[i].indexOf(")");
	    			temp		 	=  commands[i].trim().substring(sp+1, lp);
	    			commandName = commands[i].substring(0, sp).trim();
	    		}
	    		
	    		String[] arrayTemp = temp.split(xmPropertiesXml.Sep);
	    		if(arrayTemp.length ==5)
	    		{
		    		valueUnit			= arrayTemp[0];
		    		commandTitle	= arrayTemp[1];
		    		commandDesc	= arrayTemp[2];
		    		aggreUseYN		= arrayTemp[3];
		    		healthUseYN		= arrayTemp[4];
	    		}

				ItemCommandListAndSub itemCommandListAndSub =new ItemAgent().new ItemCommandListAndSub().build()
                        .SetAgentName(agentName)
                        .SetCommandName(commandName)
                        .SetValueUnit(valueUnit)
                        .SetCommandTitle(commandTitle)
                        .SetCommandDesc(commandDesc)
                        .SetAggreUseYN(aggreUseYN)
                        .SetHealthUseYN(healthUseYN);
				this.Insert_XmCommandList( itemCommandListAndSub );
	    		
				JSONObject jsonCommand = jsonObject.getJSONObject(commands[i]);
				
				String[] resourceIDs = JSONObject.getNames(jsonCommand);
				for(int d=0; d< resourceIDs.length; d++)
				{
					resourceID = resourceIDs[d];
					JSONArray jsonCommandIDs = jsonCommand.getJSONArray(resourceID);
					
//					if(jsonCommandIDs.length() != this.hasRowCount_XmCommandSubList( agentName, commandName, resourceID))
//					{
//						this.Delete_XmCommandSubList(agentName, commandName, resourceID);
//					}
					
					for(int a=0;a<jsonCommandIDs.length();a++)
					{
						propertyName = jsonCommandIDs.getString(a);
			
						itemCommandListAndSub
						.SetResourceID(resourceID)
						.SetPropertyName(propertyName);						
						LOG.debug("insert commandSubList: " + resourceID + "," + propertyName);
						this.Insert_XmCommandSubList( itemCommandListAndSub );
					}
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}


	/*private int hasRowCount_XmCommandList(String agentName) {
		String query = " SELECT count(*) as cnt FROM XmCommandList WHERE"
				+ "         					AgentName			= ?";
		
		Connection con = null;
		PreparedStatement prep = null;
		ResultSet rs					= null;
		try {
			con = this.db.getConnection(DBProperties.dbType_Resource);
			prep = con.prepareStatement(query);
			
			prep.setString(1, agentName.trim());
			
			rs = prep.executeQuery();
			if(rs.next()) return rs.getInt("cnt");
			
			return 0;
			
		} catch (Exception e) {
			// java.sql.SQLException: not implemented by SQLite JDBC driver
			e.printStackTrace();
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
		return 0;
	}*/
	
	public boolean Delete_XmCommandList(String agentName) {
			String queryDelete = " DELETE FROM XmCommandList "
									+ " WHERE  AgentName		= ?";
		Connection con = null;

		PreparedStatement prep = null;
		try {
			con = this.db.getConnection(DBProperties.dbType_Resource);
			
				prep = con.prepareStatement(queryDelete);
				prep.setString(1, agentName);				
				int ret = prep.executeUpdate();
				prep.close();		
				return true;
		} catch (Exception e) {			
			e.printStackTrace();
			return false;
		}
		finally{
				if(prep != null ) try { prep.close(); } catch (Exception e) { e.printStackTrace(); }
				if (con != null)
					this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
		
	}
	
	
	public boolean Delete_XmCommandList(ItemCommandListAndSub xmCommand) {
		String queryDelete = " DELETE FROM XmCommandList "
									+ " WHERE  AgentName		= ?"
									+ "       AND CommandName = ?";
		Connection con = null;		
		PreparedStatement prep = null;
		
		try {
			con = this.db.getConnection(DBProperties.dbType_Resource);
			
				prep = con.prepareStatement(queryDelete);
				prep.setString(1, xmCommand.GetAgentName());
				prep.setString(2, xmCommand.GetCommandName());
				
				int ret = prep.executeUpdate();
				prep.close();
				
				if(ret == 0)
				{
					LOG.error("DEL FAIL for CommandList" + xmCommand.GetAgentName() + ":" + xmCommand.GetCommandName());
					return false;
				}
				else
					return true;
		} catch (Exception e) {			
			e.printStackTrace();
			return false;
		}
		finally{
			if(prep != null ) try {
				prep.close();
			}
			catch (Exception e) {				
				e.printStackTrace();
			}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
	}

	private boolean Update_XmCommandList(ItemCommandListAndSub xmCommand) 
	{
		String query = " UPDATE XmCommandList SET "
							+ " CommandDesc					= ?"
							+ ",CommandTitle					= ?"
							+ ",ValueUnit							= ?"
							+ ",AggreUseYN					= ?"
							+ ",HealthUseYN					= ?"
							+ " WHERE  AgentName		= ?"
							+ "       AND CommandName	= ?";
		
		Connection con = null;		
		PreparedStatement prep = null;
		try {
			con = this.db.getConnection(DBProperties.dbType_Resource);
			
				prep = con.prepareStatement(query);
				
				prep.setString(1, xmCommand.GetCommandDesc());
				prep.setString(2, xmCommand.GetCommandTitle());
				prep.setString(3, xmCommand.GetValueUnit());
				prep.setString(4, xmCommand.GetAggreUseYN());
				prep.setString(5, xmCommand.GetHealthUseYN());
				prep.setString(6, xmCommand.GetAgentName());
				prep.setString(7, xmCommand.GetCommandName());
				
				int ret = prep.executeUpdate();
				prep.close();
				
				if (ret == 0)
				{
					LOG.error("update commandList fail");
					return false;
				}
				
				return true;
		} catch (Exception e) {			
			e.printStackTrace();
			return false;
		}
		finally{
			if(prep != null ) try {
				prep.close();
			}
			catch (Exception e) {		
				e.printStackTrace();
			}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
	}
	
	private void Insert_XmCommandList(ItemCommandListAndSub xmCommand) 
	{
		if(hasRowData_XmCommandList(xmCommand.GetAgentName(), xmCommand.GetCommandName()))
		{
			if(this.Update_XmCommandList(xmCommand)) return;
		}
		
 		PreparedStatement prep = null;
		Connection con = null;
		
		try {			
			String query = "";
			
			query = " INSERT INTO  XmCommandList ("
					+ " AgentName"
					+ ",CommandName"
					+ ",CommandDesc"
					+ ",CommandTitle"
					+ ",ValueUnit"
					+ ",AggreUseYN"
					+ ",HealthUseYN"
					+ ")"
					+ " VALUES ("
					+ "?"
					+ ",?"
					+ ",?"
					+ ",?"
					+ ",?"
					+ ",?"
					+ ",?)";
			con = this.db.getConnection(DBProperties.dbType_Resource);
			
			prep = con.prepareStatement(query);
			prep.setString(1, xmCommand.GetAgentName());
			prep.setString(2, xmCommand.GetCommandName());
			prep.setString(3, xmCommand.GetCommandDesc());
			prep.setString(4, xmCommand.GetCommandTitle());
			prep.setString(5, xmCommand.GetValueUnit());
			prep.setString(6, xmCommand.GetAggreUseYN());
			prep.setString(7, xmCommand.GetHealthUseYN());			
			int ret = prep.executeUpdate();
			prep.close();
			
			if (ret != 1)
			{
				LOG.error("insert FAIL");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if(prep != null ) try {
				prep.close();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
		
	}

	/*private boolean Delete_XmCommandSubList(ItemCommandListAndSub xmCommandSub) 
	{
		String queryDelete = " DELETE FROM XmCommandSubList "
									+ " WHERE  AgentName		= ?"
									+ "       AND CommandName = ?";

		Connection con = null;
		
		PreparedStatement prep = null;
		try {
			con = this.db.getConnection(DBProperties.dbType_Resource);
				prep = con.prepareStatement(queryDelete);
				prep.setString(1, xmCommandSub.GetAgentName());
				prep.setString(2, xmCommandSub.GetCommandName());
				
				int ret = prep.executeUpdate();
				prep.close();
				
				if(ret == 0)
				{
				LOG.error("���� ����");
				return false;
				}
				else
				return true;
		} catch (Exception e) {
				// java.sql.SQLException: not implemented by SQLite JDBC driver
				e.printStackTrace();
				return false;
		}
		finally{
				if(prep != null ) try { prep.close();} catch (Exception e) {
				e.printStackTrace();
				}
				if (con != null)
					this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
	}*/

	/*
	private boolean Update_XmCommandSubList(ItemCommandListAndSub xmCommand) 
	{
		String query = " UPDATE XmCommandSubList SET "
							+ " ResourceID						= ?"
							+ ",PropertyName					= ?"
							+ " WHERE  AgentName		= ?"
							+ "       AND CommandName	= ?";
		
		Connection con = null;
		
		PreparedStatement prep = null;
		try {
			con = this.db.getConnection(DBProperties.dbType_Resource);
				
				prep = con.prepareStatement(query);
				prep.setString(1, xmCommand.GetResourceID());
				prep.setString(2, xmCommand.GetPropertyName());
				prep.setString(3, xmCommand.GetAgentName());
				prep.setString(4, xmCommand.GetCommandName());
				
				int ret = prep.executeUpdate();
				prep.close();
				//con.commit();
				
				if (ret == 0)
				{
					LOG.error("�������");
					return false;
				}
				return true;
		} catch (Exception e) {
			// java.sql.SQLException: not implemented by SQLite JDBC driver
			e.printStackTrace();
			return false;
		}
		finally{
			if(prep != null ) try {
				prep.close();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
	}*/
	
	private void Insert_XmCommandSubList(ItemCommandListAndSub xmCommand) 
	{
		if(this.hasRowData_XmCommandSubList(xmCommand.GetAgentName()
								          , xmCommand.GetCommandName()
								          , xmCommand.GetResourceID()
								          , xmCommand.GetPropertyName())) return; 
		
		String query = " INSERT INTO  XmCommandSubList ("
				+ " AgentName"
				+ ",CommandName"
				+ ",ResourceID"
				+ ",PropertyName"
				+ ")"
				+ " VALUES ("
				+ " ?"
				+ ",?"
				+ ",?"
				+ ",?)";

		PreparedStatement prep = null;
		Connection con = null;
		try {
			
			con = this.db.getConnection(DBProperties.dbType_Resource);	
			
			prep = con.prepareStatement(query);
			prep.setString(1, xmCommand.GetAgentName());
			prep.setString(2, xmCommand.GetCommandName());
			prep.setString(3, xmCommand.GetResourceID());
			prep.setString(4, xmCommand.GetPropertyName());			
			int ret = prep.executeUpdate();
			prep.close();
			
			if (ret != 1)
				LOG.error("insert FAIL");
		} catch (Exception e) {
		
			e.printStackTrace();
		}
		finally{
			if(prep != null ) try {
				prep.close();
			}
			catch (Exception e) {			
				e.printStackTrace();
			}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
	}
}
