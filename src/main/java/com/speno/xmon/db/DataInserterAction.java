package com.speno.xmon.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.aggregate.builder.AggregatedActionItem;
import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.dataset.ItemActionList;

public class DataInserterAction {

	private final static Logger LOG = LoggerFactory.getLogger(DataInserterAction.class);

	private DBProperties db;

	public DataInserterAction() {
		this.db = DBProperties.getInstance();
	}

	public boolean Select_XmTransAction(String agentName, String actionName,
			String transResult, String TransID) {
			String query =    "	    SELECT	TransInitTime			                  				"           
				  + "				   ,   	AgentName										"
				  + "				   ,	ActionName										"                    							  
				  + "				   ,	ResponseTime									"
				  + "				   ,	TransID												"
				  + "				   ,	TransResult										"
				  + "   	   FROM	XmTransAction 									"
				  + "      WHERE	AgentName  		= ?                 			"
				  + "    	      AND 	ActionName		= ?                 			"
				  + "    	      AND 	TransResult		= ?                 			"
				  + "    	      AND 	TransID				= ?                 			";

		PreparedStatement prep = null;
		ResultSet rs = null;
		Connection con = null;

		try {
			con = this.db.getConnection(DBProperties.dbType_Action);
			long start = System.currentTimeMillis();
			prep = con.prepareStatement(query);
			prep.setString(1, agentName);
			prep.setString(2, actionName);
			prep.setString(3, transResult);
			prep.setString(4, TransID);
			rs = prep.executeQuery();
			
			while (rs.next()) {
				if (rs.getString("TransID").equals(TransID))
					return true;
			}
			
			long end = System.currentTimeMillis();
			long spend = end - start;
			LOG.debug("Select_XmTransAction spend time " + spend + "MS");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (prep != null)
				try {
					prep.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);
		}
		return false;
	}

	public boolean Insert_XmTransAction(String agentName, String actionName,
			String transID, long transInitTime, long transCompTime,
			long responseTime, String TransResult, String... strings) {
		if ((transInitTime == 0L) || (transCompTime == 0L)) {
			LOG.error("[" + transID + "] init or complete time is zero:"
					+ transInitTime + ", transCompTime:" + transCompTime);
			return false;
		}
		// if(responseTime == 0) return false;

		String query = " INSERT INTO  XmTransAction ("
						+ " AgentName"
						+ ",ActionName"
						+ ",TransID"
						+ ",TransInitTime"
						+ ",TransCompTime"
						+ ",ResponseTime"
						+ ",TransResult";
		if (strings != null) {
			for (int i = 0; i < strings.length; i++) {
				query += ",ext" + (i + 1);
			}
		}
		query += ")";

		String query2 = " VALUES ( ?,? ,? ,? ,?,?,?";
		if (strings != null) {
			for (int i = 0; i < strings.length; i++) {
				query2 += ",?";
			}
		}
		query2 += ")";

		query = query + query2;
		PreparedStatement prep = null;
		Connection con = null;

		try {
			// if(this.Select_XmTransAction(agentName, actionName, TransResult,
			// transID)) return true;
			con = this.db.getConnection(DBProperties.dbType_Action);
			
			prep = con.prepareStatement(query);
			prep.setString(1, agentName);
			prep.setString(2, actionName);
			prep.setString(3, transID);
			prep.setString(4, SimDate.getDateTimeFormatter_MS()
					.format(new java.util.Date(transInitTime)));
			prep.setString(5, SimDate.getDateTimeFormatter_MS()
					.format(new java.util.Date(transCompTime)));
			prep.setLong(6, responseTime);
			prep.setString(7, TransResult);
			if (strings != null) {
				for (int i = 0; i < strings.length; i++) {
					prep.setString(8 + i, strings[i]); 
				}
			}
			int ret = prep.executeUpdate();
			prep.close();
			if (ret == 1)
				return true;
		} catch (Exception e) {
			LOG.error("XmTransAction DB 등록 오류: transID:" + transID	+ ", TransResult" + TransResult, e);
		} finally {
			if (prep != null)
				try {
					prep.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);
		}
		return false;
	}

	// public boolean _Insert_XmAggregatedAction(String fromTable, String
	// toTable, int substrLen, String aggreDateTime)
	// {
	// if (this.db.open(DBProperties.dbType_Action) == false) { return false; }
	// String query = " Insert into " + toTable
	// + " 		( 	AgentName                              	"
	// + " 		, 	ActionName                              "
	// + " 		, 	ResponseAvgTime                     "
	// + " 		, 	ResponseMaxTime                    "
	// + " 		, 	ResponseMinTime                     "
	// + " 		, 	SuccessCount                          	"
	// + " 		, 	ErrorCount                                "
	// + " 		, 	TimeOutCount                           	"
	// + " 		, 	AggregatedTime)                       "
	// + " 		Select	AgentName                       "
	// + "     			,	ActionName                      "
	// + "     			,	round(avg(ResponseAvgTime))		as ResponseAvgTime		"
	// + "     			,	round(avg(ResponseMaxTime))	as ResponseMaxTime	"
	// + "     			,	round(avg(ResponseMinTime))		as ResponseMinTime		"
	// + "     			,	sum(SuccessCount)    				as SuccessCount          "
	// + "     			,	sum(ErrorCount)      					as ErrorCount               	"
	// + "     			,	sum(TimeOutCount)    				as TimeOutCount			"
	// + "     			,	substr(AggregatedTime,0,?) 		as AggregatedTime     	"
	// + " 	 		From " + fromTable
	// +
	// " 		WHERE	substr(AggregatedTime,0,?) > (select ifnull( Max(AggregatedTime),0) from "
	// + toTable + ") "
	// + "     	 	AND		substr(AggregatedTime,0,?) < ?									"
	// + " GROUP BY		AgentName                                      							"
	// + "     			  ,		ActionName                                        						"
	// + "     			  ,		substr(AggregatedTime,0,?)                       					";
	//
	// PreparedStatement prep = null;
	//
	// try {
	//
	// Connection con = this.db.getConnection(DBProperties.dbType_Action);
	// prep = con.prepareStatement(query);
	// prep.setInt(1, substrLen);
	// prep.setInt(2, substrLen);
	// prep.setInt(3, substrLen);
	// prep.setString(4, aggreDateTime);
	// prep.setInt(5, substrLen);
	//
	// int ret = prep.executeUpdate();
	//
	// LOG.debug("ret:" + ret);
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// finally{
	// if(prep != null ) try {prep.close();} catch (SQLException e)
	// {e.printStackTrace();}
	// }
	// return true;
	// }

	// 리소스 집계
	// 호출하면 최종데이터 이후의 데이터를 집계함
	public final static int TYPE_MIN = 1;
	public final static int TYPE_HOUR = 2;
	public final static int TYPE_DAY = 3;

	public boolean aggregate(int type) {
		String sourceTable = "";
		String targetTable = "";
		int substrIndex = 16;
		if (TYPE_MIN == type) {
			sourceTable = "XmAggregatedActionSec";
			targetTable = "XmAggregatedActionMin";
			substrIndex = 16;
		} else if (TYPE_HOUR == type) {
			sourceTable = "XmAggregatedActionMin";
			targetTable = "XmAggregatedActionHour";
			substrIndex = 13;
		} else if (TYPE_DAY == type) {
			sourceTable = "XmAggregatedActionHour";
			targetTable = "XmAggregatedActionDay";
			substrIndex = 10;
		} else {
			LOG.error("NOT supported type");
			return false;
		}

		String query = "";
		if (DBProperties.SQLITE == DBProperties.getInstance().getDbType(
				DBProperties.dbType_Action)) {
			query += "insert into   " + targetTable + " (AgentName						\n";                     
			query += "    			,	ActionName							\n";                    
			query += "	    		,	ResponseAvgTime						\n";
			query += "	    		,	ResponseMaxTime						\n";
			query += "	    		,	ResponseMinTime						\n";
			query += "	    		,	SuccessCount						\n";
			query += "	    		,	ErrorCount						\n";
			query += "	    		,	TimeOutCount						\n";
			query += "	    		,	AggregatedTime)							\n";
			query += "Select	AgentName									\n";
			query += "    			,	ActionName							\n";
			query += "    			,	round(avg(ResponseAvgTime))	as ResponseAvgTime	\n";
			query += "    			,	round(avg(ResponseMaxTime))	as ResponseMaxTime	\n";
			query += "	    		,	round(avg(ResponseMinTime))	as ResponseMinTime	\n";
			query += "	    		,	sum(SuccessCount) as SuccessCount				\n";
			query += "	    		,	sum(ErrorCount) as ErrorCount					\n";
			query += "	    		,	sum(TimeOutCount) as TimeOutCount				\n";
			query += "     			,	substr(AggregatedTime,1,"+substrIndex+")     	\n";
			query += "From " + sourceTable + " 									\n";
			query += "WHERE	AggregatedTime >= (select (CASE WHEN Max(AggregatedTime) IS NULL THEN '0' ELSE Max(AggregatedTime) END) from " + targetTable + " )	\n";
			query += "GROUP BY	AgentName,ActionName,substr(AggregatedTime,1,"+substrIndex+")						\n";
		}
		else if (DBProperties.ORACLE == DBProperties.getInstance().getDbType(DBProperties.dbType_Action)) {
			query += "MERGE INTO " + targetTable + " TT													\n";
			query += "USING (		\n";
			query += "Select	AgentName		\n";									
			query += "    			,	ActionName									\n";
			query += "    			,	round(avg(ResponseAvgTime))	as ResponseAvgTime			\n";
			query += "    			,	round(avg(ResponseMaxTime))	as ResponseMaxTime			\n";
			query += "	    		,	round(avg(ResponseMinTime))	as ResponseMinTime			\n";
			query += "	    		,	sum(SuccessCount) as SuccessCount						\n";
			query += "	    		,	sum(ErrorCount) as ErrorCount							\n";
			query += "	    		,	sum(TimeOutCount) as TimeOutCount						\n";
			query += "     			,	substr(AggregatedTime,1,"+substrIndex+") as AggregatedTimeMin		\n";
			query += "From " + sourceTable + " 											\n";
			query += "WHERE	AggregatedTime >= (select (CASE WHEN Max(AggregatedTime) IS NULL THEN '0' ELSE Max(AggregatedTime) END) from " + targetTable + " )		\n";
			query += "GROUP BY	AgentName,ActionName,substr(AggregatedTime,1,"+substrIndex+")								\n";
			query += ") ST		\n";
			query += "ON (TT.AggregatedTime = ST.AggregatedTimeMin and TT.AgentName = ST.AgentName and TT.ActionName = ST.ActionName)		\n";
			query += "WHEN MATCHED THEN		\n";
			query += "UPDATE SET		\n";
			query += "TT.ResponseAvgTime	= ST.ResponseAvgTime		\n";
			query += ",TT.ResponseMaxTime	= ST.ResponseMaxTime						\n";
			query += ",TT.ResponseMinTime	= ST.ResponseMinTime		\n";
			query += ",TT.SuccessCount	= ST.SuccessCount		\n";
			query += ",TT.ErrorCount		= ST.ErrorCount		\n";
			query += ",TT.TimeOutCount	= ST.TimeOutCount		\n";
			query += "WHEN NOT MATCHED THEN		\n";
			query += "INSERT (AgentName								\n";
			query += "    			,	ActionName									\n";
			query += "	    		,	ResponseAvgTime								\n";
			query += "	    		,	ResponseMaxTime								\n";
			query += "	    		,	ResponseMinTime								\n";
			query += "	    		,	SuccessCount								\n";
			query += "	    		,	ErrorCount								\n";
			query += "	    		,	TimeOutCount								\n";
			query += "	    		,	AggregatedTime)		\n";
			query += "VALUES (		\n";
			query += "ST.AgentName,ST.ActionName,ST.ResponseAvgTime,		\n";
			query += "ST.ResponseMaxTime,ST.ResponseMinTime,ST.SuccessCount,		\n";
			query += "ST.ErrorCount,ST.TimeOutCount,ST.AggregatedTimeMin		\n";
			query += ")		\n";
		}

		PreparedStatement prep = null;
		Connection con = null;
		int ret = 0;

		try {
			con = this.db.getConnection(DBProperties.dbType_Action);
			
			prep = con.prepareStatement(query);
			// prep.setInt(1, substrIndex);
			// prep.setInt(2, substrIndex);
			//LOG.info(query);
			ret = prep.executeUpdate();		

			return true;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			if (prep != null)
				try {
					prep.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);
		}
		return false;
	}

	public boolean Insert_XmAggregatedAction(String fromTable, String toTable,
			int substrLen, String aggreDateTime, String agentName) {
		if (this.db.open(DBProperties.dbType_Action) == false) {
			return false;
		}
		if (this.db.open(DBProperties.dbType_Action) == false) { return false; }
		String query = 	" 		Select	AgentName                      \n"
						+ 	"     			,	ActionName                     \n"
						+	 "     			,	round(avg(ResponseAvgTime))		as ResponseAvgTime		\n"
						+ 	"     			,	round(avg(ResponseMaxTime))	as ResponseMaxTime	\n"
						+ 	"     			,	round(avg(ResponseMinTime))		as ResponseMinTime		\n"
						+ 	"     			,	sum(SuccessCount)    				as SuccessCount         \n"
						+ 	"     			,	sum(ErrorCount)      					as ErrorCount               	\n"
						+ 	"     			,	sum(TimeOutCount)    				as TimeOutCount			\n"
						+ 	"     			,	substr(TBL.AggregatedTime,1,?) 		as AggregatedTime     	\n"
						+ 	" 	 		From " + fromTable + " TBL		\n"
						+ 	" 		WHERE	TBL.AggregatedTime > (select (CASE WHEN Max(AggregatedTime) IS NULL THEN '0' ELSE Max(AggregatedTime) END) from " + toTable + " WHERE agentname =? ) || 'z' \n"
						+ 	"     	 	AND		agentname=?									\n"		                
						+ 	" GROUP BY		AgentName                                      	\n"
						+ 	"     			  ,		ActionName                              \n"
						+ 	"     			  ,		substr(TBL.AggregatedTime,1,?)              \n";
		
		String queryInsert = " Insert into " + toTable
						+ 	" 		( 	AgentName                              \n"
						+ 	" 		, 	ActionName                             \n"
						+ 	" 		, 	ResponseAvgTime                     \n"
						+ 	" 		, 	ResponseMaxTime                    \n"
						+ 	" 		, 	ResponseMinTime                     \n"
						+ 	" 		, 	SuccessCount                          	\n"
						+ 	" 		, 	ErrorCount                                \n"
						+ 	" 		, 	TimeOutCount                           	\n"
						+ 	" 		, 	AggregatedTime)                       \n"
						+ " VALUES ("
						+ " ?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?"
						+ ",?)		\n"; 

		PreparedStatement prep = null;
		PreparedStatement prepInsert = null;
		ResultSet rs = null;
		int ret = 0;
		long start = System.currentTimeMillis();
		
		Connection con = null;
		try {
			con = this.db.getConnection(DBProperties.dbType_Action);

			prep = con.prepareStatement(query);
			int count = 1;
			prep.setInt(count++, substrLen);
			// prep.setInt(count++, substrLen);
			prep.setString(count++, agentName);
			// prep.setInt(count++, substrLen);
			// prep.setString(count++, aggreDateTime);
			prep.setString(count++, agentName);
			prep.setInt(count++, substrLen);			
			rs = prep.executeQuery();
			// int cnt =0;
			while (rs.next()) {				
				prepInsert = con.prepareStatement(queryInsert);
				
				prepInsert.setString(1, rs.getString("AgentName"));
				prepInsert.setString(2, rs.getString("ActionName"));
				prepInsert.setString(3, rs.getString("ResponseAvgTime"));
				prepInsert.setString(4, rs.getString("ResponseMaxTime"));

				prepInsert.setString(5, rs.getString("ResponseMinTime"));
				prepInsert.setString(6, rs.getString("SuccessCount"));
				prepInsert.setString(7, rs.getString("ErrorCount"));

				prepInsert.setString(8, rs.getString("TimeOutCount"));
				prepInsert.setString(9, rs.getString("AggregatedTime"));

				ret = prepInsert.executeUpdate();
				prepInsert.close();
				if (ret != 1) {
					LOG.error("DB ResMin등록 실패:" + rs.getString("CommandName")
							+ "_" + rs.getString("PropertyName") + ","	+ rs.getLong("avg"));
				}
			}
			long end = System.currentTimeMillis();
			long spend = end - start;
			LOG.debug("Insert_XmAggregatedAction spend time " + spend + "MS");
			if (ret > 0)
				return true;
			else
				return false;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			e.printStackTrace();
			return false;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (prep != null)
				try {
					prep.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (prepInsert != null)
				try {
					prepInsert.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);

		}
	}

	public int Insert_XmAggregatedActionSecGroup() {

		StringBuffer query = new StringBuffer("");		
			query.append(" select AgentName, ActionName, SUBSTR(TransCompTime,1, 19) as AggregatedTime,			");
			query.append(" SUM(CASE WHEN TransResult = '" + DicOrderAdd.AmassSufFix_ActionCompSuc + "' THEN 1 ELSE 0 END) as cnt_success,		");
			query.append(" SUM(CASE WHEN TransResult = '" + DicOrderAdd.AmassSufFix_ActionCompError + "' THEN 1 ELSE 0 END) as cnt_error,		");
			query.append(" SUM(CASE WHEN TransResult = '" + DicOrderAdd.AmassSufFix_ActionCompTimeout + "' THEN 1 ELSE 0 END) as cnt_timeout,		");
			query.append(" round(AVG(ResponseTime),1) 	as ResponseAvgTime,				");
			query.append(" MIN(ResponseTime)          		as ResponseMinTime,			");
			query.append(" MAX(ResponseTime)          		as ResponseMaxTime			");
			query.append(" from    XmTransAction								");
			query.append(" where  TransCompTime > (select (CASE WHEN Max(AggregatedTime) IS NULL THEN '0' ELSE Max(AggregatedTime) END) from XmAggregatedActionSec ) || 'z' ");
			query.append(" group by AgentName, ActionName, SUBSTR(TransCompTime,1, 19)			");
			query.append(" order by SUBSTR(TransCompTime,1, 19) desc					");

//		}
		PreparedStatement prep = null;
		PreparedStatement insertPrep = null;
		ResultSet rs = null;
		Connection con = null;

	
		int sumTransCount = 0;
		try {
			con = this.db.getConnection(DBProperties.dbType_Action);			
			prep = con.prepareStatement(query.toString()); 		
			rs = prep.executeQuery();
			long start = System.currentTimeMillis();
			Map<String, AggregatedActionItem> allItems = new HashMap<String, AggregatedActionItem>();
			int count = 0;
			
			while (rs.next()) {
				AggregatedActionItem actionItem = new AggregatedActionItem();
				actionItem.SetAgentName(rs.getString("AgentName"));
				actionItem.SetActionName(rs.getString("ActionName"));
				actionItem.SetAggregatedTime(rs.getString("AggregatedTime"));

				actionItem.SetSuccessCount(rs.getInt("cnt_success"));
				sumTransCount += actionItem.GetSuccessCount();
				actionItem.SetErrorCount(rs.getInt("cnt_error"));
				sumTransCount += actionItem.GetErrorCount();
				actionItem.setTimeOutCount(rs.getInt("cnt_timeout"));
				sumTransCount += actionItem.GetTimeOutCount();

				actionItem.SetResponseAvgTime(rs.getInt("ResponseAvgTime"));
				actionItem.SetResponseMinTime(rs.getInt("ResponseMinTime"));
				actionItem.SetResponseMaxTime(rs.getInt("ResponseMaxTime"));
				
				// update or insert!!		
				if (!hasRowData_XmAggregatedActionSec(actionItem)) {
					// 중복 정의
					String insertQuery = " INSERT INTO  XmAggregatedActionSec ("
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
					try {	
						// 20150401, junsoo, 데이터가 중간중간 빠지는 원인이므로 null일때만 새로생성은 꼭 필요함
						if(insertPrep == null)
							insertPrep = con.prepareStatement(insertQuery);						
						insertPrep.setString(1, actionItem.GetAgentName());
						insertPrep.setString(2, actionItem.GetActionName());
						insertPrep.setInt(3, actionItem.GetResponseAvgTime());
						insertPrep.setInt(4, actionItem.GetResponseMaxTime());
						insertPrep.setInt(5, actionItem.GetResponseMinTime());
						insertPrep.setInt(6, actionItem.GetSuccessCount());
						insertPrep.setInt(7, actionItem.GetErrorCount());
						insertPrep.setInt(8, actionItem.GetTimeOutCount());
						insertPrep.setString(9, actionItem.GetAggregatedTime());
						insertPrep.addBatch();
						
						count++;
						
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
						e.printStackTrace();
					}
					finally{
					}
		        }
		        
			    if( count % 100 == 0 && insertPrep != null) { 
			    	insertPrep.executeBatch();
			    } 
				 actionItem.Clear();
			}

			// 나머지 execute
			if (insertPrep != null)
				insertPrep.executeBatch();

			long end = System.currentTimeMillis();
			long spend = end - start;
			LOG.debug("Insert_XmAggregatedActionSecGroup spend time " + spend + "MS");
			return sumTransCount;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (prep != null)
				try {
					prep.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (insertPrep != null)
				try {
					insertPrep.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);		
		}
		return 0;
	}

	public int Insert_XmAggregatedAction(AggregatedActionItem value) {
		if (this.db.open(DBProperties.dbType_Action) == false) {
			return 0;
		}	
		{
			if (hasRowData_XmAggregatedActionSec(value))
				return 0;
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

			PreparedStatement prep = null;
			Connection con = null;

			try {
				con = this.db.getConnection(DBProperties.dbType_Action);	
				
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
				if (prep != null)
					try {
						prep.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				if (con != null)
					this.db.releaseConnection(DBProperties.dbType_Action, con);
				return ret;
			} catch (Exception e) {			
				e.printStackTrace();
			} finally {
				if (prep != null)
					try {
						prep.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				if (con != null)
					this.db.releaseConnection(DBProperties.dbType_Action, con);
			}
		}
		return 1;
	}

	// public int Insert_XmAggregatedActionSec(HashMap<String,
	// AggregatedRecivedActionItem> hmActionAgentName)
	// {
	// // public List<String> Insert_XmAggregatedActionSec(ConcurrentMap<String,
	// AggregatedRecivedActionItem> hmActionAgentName) {
	//
	// if (this.db.open(DBProperties.dbType_Action) == false) { return 0; }
	//
	// Iterator<String> iter = hmActionAgentName.keySet().iterator();
	// List<String> resultTransIDs = new ArrayList<String>();
	// String key = "";
	//
	// synchronized(iter)
	// {
	// while( iter.hasNext())
	// {
	// key = (String) iter.next();
	// if(key.equals("")) continue;
	//
	// AggregatedRecivedActionItem value = hmActionAgentName.get(key);
	// if(hasRowData_XmAggregatedActionSec(value, resultTransIDs)) continue;
	//
	// String query = " INSERT INTO  XmAggregatedActionSec ("
	// + " AgentName"
	// + ",ActionName"
	// + ",ResponseAvgTime"
	// + ",ResponseMaxTime"
	// + ",ResponseMinTime"
	// + ",SuccessCount"
	// + ",ErrorCount"
	// + ",TimeOutCount"
	// + ",AggregatedTime)"
	// + " VALUES ("
	// + " ?"
	// + ",?"
	// + ",?"
	// + ",?"
	// + ",?"
	// + ",?"
	// + ",?"
	// + ",?"
	// + ",?)";
	//
	// PreparedStatement prep = null;
	// Connection con = null;
	//
	// try {
	//
	// con = this.db.getConnection(DBProperties.dbType_Action);
	// //con.setAutoCommit(false);
	// prep = con.prepareStatement(query);
	// prep.setString(1, value.GetAgentName());
	// prep.setString(2, value.GetActionName());
	// prep.setInt(3, value.GetResponseAvgTime());
	// prep.setInt(4, value.GetResponseMaxTime());
	// prep.setInt(5, value.GetResponseMinTime());
	// prep.setInt(6, value.GetSuccessCount());
	// prep.setInt(7, value.GetErrorCount());
	// prep.setInt(8, value.GetTimeOutCount());
	// prep.setString(9, value.GetAggregatedTime());
	//
	// int ret = prep.executeUpdate();
	// prep.close();
	// //con.commit();
	// return ret;
	//
	// } catch (SQLException e) {
	// // java.sql.SQLException: not implemented by SQLite JDBC driver
	// e.printStackTrace();
	// }
	// finally{
	// if(prep != null ) try {prep.close();} catch (SQLException e)
	// {e.printStackTrace();}
	// if (con != null)
	// this.db.releaseConnection(DBProperties.dbType_Action, con);
	// }
	// }
	// }
	// return 0;
	// }

	/*
	 * 이미 있다면 업데이트를 한다
	 */
	private boolean hasRowData_XmAggregatedActionSec(AggregatedActionItem value) {
		String query = " SELECT * FROM XmAggregatedActionSec WHERE"
				+ "         					AgentName			= ?"
				+ " 					AND ActionName			= ?"
				+ " 					AND AggregatedTime		= ? ";

		PreparedStatement prep = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.db.getConnection(DBProperties.dbType_Action);
			long start = System.currentTimeMillis();
			prep = con.prepareStatement(query);
			prep.setString(1, value.GetAgentName().trim());
			prep.setString(2, value.GetActionName().trim());
			prep.setString(3, value.GetAggregatedTime().trim());
			rs = prep.executeQuery();
		
			int cnt = 0;
			if (rs.next()) {
				cnt++;
				int ResponseAvgTime = rs.getInt("ResponseAvgTime");
				int ResponseMaxTime = rs.getInt("ResponseMaxTime");
				int ResponseMinTime = rs.getInt("ResponseMinTime");

				int oriSuccessCount = rs.getInt("SuccessCount");
				int oriErrorCount = rs.getInt("ErrorCount");
				int oriTimeOutCount = rs.getInt("TimeOutCount");
				int oriSum = oriSuccessCount + oriErrorCount + oriTimeOutCount;

				int SuccessCount = rs.getInt("SuccessCount") + value.GetSuccessCount();
				int ErrorCount = rs.getInt("ErrorCount") +  value.GetErrorCount();
				int TimeOutCount = rs.getInt("TimeOutCount") + value.GetTimeOutCount();

				int sumRsTime = ResponseAvgTime * oriSum;

				sumRsTime += value.GetResponseAvgTime();
				if ((sumRsTime == 0) || (oriSum == 0)) {
					LOG.warn("sumRsTime:" + sumRsTime + " SuccessCount:"+ SuccessCount + "  Zero Error <-"
							+ value.GetAggregatedTime().trim());
					return false;
				}
				ResponseAvgTime = sumRsTime / oriSum;

				if (ResponseMaxTime < value.GetResponseMaxTime())
					ResponseMaxTime = value.GetResponseMaxTime();

				if (ResponseMinTime > value.GetResponseMinTime())
					ResponseMinTime = value.GetResponseMinTime();

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
					prepUp = con.prepareStatement(query);
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
					if (ret == 1) {
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (prepUp != null)
						try {
							prepUp.close();
						} catch (Exception e2) {
						}
				}
			}// End Select Rs While
			long end = System.currentTimeMillis();
			long spend = end - start;
			LOG.debug("hasRowData_XmAggregatedActionSec spend time" + spend + "MS");
			
			if (cnt > 0)
				return true;
			else
				return false;

		} catch (Exception e) {
			// java.sql.SQLException: not implemented by SQLite JDBC driver
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (prep != null)
				try {
					prep.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);
		}
		return false;
	}

	// private boolean
	// hasRowData_XmAggregatedActionSec(AggregatedRecivedActionItem
	// value,List<String> resultTransIDs) {
	// String query = " SELECT * FROM XmAggregatedActionSec WHERE"
	// + "         					AgentName			= ?"
	// + " 					AND ActionName			= ?"
	// + " 					AND AggregatedTime		= ? ";
	//
	// PreparedStatement prep = null;
	// ResultSet rs = null;
	// Connection con = null;
	// try {
	// con = this.db.getConnection(DBProperties.dbType_Action);
	// prep = con.prepareStatement(query);
	//
	// prep.setString(1, value.GetAgentName().trim());
	// prep.setString(2, value.GetActionName().trim());
	// prep.setString(3, value.GetAggregatedTime().trim());
	//
	// rs = prep.executeQuery();
	//
	// int cnt =0;
	// while(rs.next())
	// {
	// cnt++;
	// int ResponseAvgTime = rs.getInt("ResponseAvgTime");
	// int ResponseMaxTime = rs.getInt("ResponseMaxTime");
	// int ResponseMinTime = rs.getInt("ResponseMinTime");
	//
	// int SuccessCount = rs.getInt("SuccessCount") + value.GetSuccessCount();
	// int ErrorCount = rs.getInt("ErrorCount") + value.GetErrorCount();
	// int TimeOutCount = rs.getInt("TimeOutCount") + value.GetTimeOutCount();
	//
	// int sumRsTime = ResponseAvgTime * rs.getInt("SuccessCount");
	//
	// sumRsTime += value.GetResponseAvgTime();
	// ResponseAvgTime = sumRsTime / SuccessCount;
	//
	// if(ResponseMaxTime < value.GetResponseMaxTime())
	// ResponseMaxTime =value.GetResponseMaxTime();
	//
	// if(ResponseMinTime > value.GetResponseMinTime())
	// ResponseMinTime =value.GetResponseMinTime();
	//
	// query = " UPDATE XmAggregatedActionSec SET "
	// + " 					ResponseAvgTime 		= ?"
	// + "				,	ResponseMaxTime		= ?"
	// + "				,	ResponseMinTime		= ?"
	// + "				,	SuccessCount			= ?"
	// + "				,	ErrorCount					= ?"
	// + "				,	TimeOutCount			= ?"
	// + " 		WHERE	AgentName			= ?"
	// + "				AND	ActionName			= ?"
	// + "				AND	AggregatedTime 	= ? ";
	//
	// PreparedStatement prepUp = null;
	// try {
	// prepUp = con.prepareStatement(query);
	//
	// prepUp.setInt(1, ResponseAvgTime);
	// prepUp.setInt(2, ResponseMaxTime);
	// prepUp.setInt(3, ResponseMinTime);
	// prepUp.setInt(4, SuccessCount);
	// prepUp.setInt(5, ErrorCount);
	// prepUp.setInt(6, TimeOutCount);
	//
	// prepUp.setString(7, value.GetAgentName());
	// prepUp.setString(8, value.GetActionName());
	// prepUp.setString(9, value.GetAggregatedTime());
	//
	// int ret = prepUp.executeUpdate();
	//
	// if (ret == 1)
	// {
	// Iterator<String> transIDs= value.getTransID().iterator();
	// while(transIDs.hasNext())
	// {
	// resultTransIDs.add(transIDs.next());
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }finally {
	// if(prepUp != null ) prepUp.close();
	// }
	// }//End Select Rs While
	//
	// if(cnt>0) return true;
	// else return false;
	//
	// } catch (Exception e) {
	// // java.sql.SQLException: not implemented by SQLite JDBC driver
	// e.printStackTrace();
	// }
	// finally{
	// if(rs != null) try {rs.close();} catch (SQLException e)
	// {e.printStackTrace();}
	// if(prep != null ) try {prep.close();} catch (SQLException e)
	// {e.printStackTrace();}
	// if (con != null)
	// this.db.releaseConnection(DBProperties.dbType_Action, con);
	// }
	// return false;
	// }

	private boolean hasRowData_XmActionList(String agentName, String actionName) {
		String query = " SELECT * FROM XmActionList WHERE"
				+ "         					AgentName			= ?"
				+ " 					AND ActionName			= ?";

		PreparedStatement prep = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.db.getConnection(DBProperties.dbType_Action);
			
			prep = con.prepareStatement(query);			
			prep.setString(1, agentName.trim());
			prep.setString(2, actionName.trim());
			
			rs = prep.executeQuery();
			
			if (rs.next())
				if (rs.getString("ActionName").trim().equals(actionName))
					return true;
			return false;
		} catch (Exception e) {			
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (prep != null)
				try {
					prep.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);
		}
		return false;
	}

	private void Update_XmActionList(ItemActionList item) {
		String query = " UPDATE  XmActionList SET "
				+ " ActionDesc		= ?"
				+ ",ActionTitle		= ?"
				+ ",AggreUseYN	= ?"
				+ ",HealthUseYN	= ?"
				+ " WHERE	AgentName		= ?"
				+ "       AND	 ActionName		= ?";

		PreparedStatement prep = null;
		Connection con = null;

		try {
			con = this.db.getConnection(DBProperties.dbType_Action);
		
			prep = con.prepareStatement(query);
			prep.setString(1, item.getDescription());
			prep.setString(2, item.getTitle());
			prep.setString(3, item.getAggreUseYN());
			prep.setString(4, item.getHealthUseYN());
			prep.setString(5, item.getAgentName());
			prep.setString(6, item.getActionName());
			int ret = prep.executeUpdate();
			prep.close();
			if (ret != 1) {
				
			}
		} catch (Exception e) {			
			e.printStackTrace();
		} finally {
			if (prep != null)
				try {
					prep.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);
		}

	}

	// delete agentName's action list
	public void deleteActionList(String agentName) {
		String query = "delete from XmActionList where AgentName = ?";
		PreparedStatement prep = null;
		Connection con = null;

		try {
			con = this.db.getConnection(DBProperties.dbType_Action);
			
			prep = con.prepareStatement(query);
			prep.setString(1, agentName);
			int ret = prep.executeUpdate();
		} catch (Exception e) {
			LOG.error("deleteActionList: " + e.getMessage(), e);
		} finally {
			if (prep != null) {
				try {
					prep.close();
				} catch (Exception e) {
					LOG.error(e.getMessage());
				}		
				}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);
		}
	}

	public void Insert_XmActionList(ItemActionList item) // String agentName,
															// String
															// actionName)
	{
		if (hasRowData_XmActionList(item.getAgentName(), item.getActionName())) {
			this.Update_XmActionList(item);
			return;
		}

		String query = " INSERT INTO  XmActionList ("
							+ " AgentName		"
							+ ",ActionName		"
							+ ",ActionDesc		"
							+ ",ActionTitle		"
							+ ",AggreUseYN	"
							+ ",HealthUseYN	"
							+ ")"
							+ " VALUES ("
							+ "?"
							+ ",?"
							+ ",?"
							+ ",?"
							+ ",?"
							+ ",?)";
		PreparedStatement prep = null;
		Connection con = null;

		try {

			con = this.db.getConnection(DBProperties.dbType_Action);
				
			prep = con.prepareStatement(query);
			prep.setString(1, item.getAgentName());
			prep.setString(2, item.getActionName());
			prep.setString(3, item.getDescription());
			prep.setString(4, item.getTitle());
			prep.setString(5, item.getAggreUseYN());
			prep.setString(6, item.getHealthUseYN());

			int ret = prep.executeUpdate();
			prep.close();		

		} catch (Exception e) {
			// java.sql.SQLException: not implemented by SQLite JDBC driver
			e.printStackTrace();
		} finally {
			if (prep != null)
				try {
					prep.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);
		}
	}

}
