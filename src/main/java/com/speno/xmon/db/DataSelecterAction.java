package com.speno.xmon.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.codedic.DicCommands;
import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.codedic.DicOrderTypes;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.env.ItemAgent;
import com.speno.xmon.listener.CommandServerRequestBuilder;

public class DataSelecterAction {
	
	private final static Logger LOG = LoggerFactory.getLogger(DataSelecterAction.class);

	private DBProperties db;
//	private SimDate	sd = null;
//	private String agentName;
//	private String commandName;
//	private String actionName;
//	
//	private String perUnit;
//	private String valueUnit;
//	private List<String> aggreUnit;
//	
//	private String aggreUnitMix;
//	private String aggreUnitWhere;
//	
//	private String rangeStart;
//	private String rangeEnd;
//	private String consoleId;
//	
//	private String tableName;
//	Iterator<String> iterAggreUnit;
	
	public DataSelecterAction()
	{
		db = DBProperties.getInstance();
//		this.sd = new SimDate();
//		
//		this.agentName 		= "";
//    	this.commandName	= "";
//    	this.actionName			= "";
//        
//    	this.perUnit 				= "";
//    	this.valueUnit 			= "";
//    	this.aggreUnit			= null;
//        this.rangeStart 			= "";
//        this.rangeEnd 			= "";
//        this.consoleId			= "";
//        
//        this.tableName 			= "";
//    	this.iterAggreUnit 		= null;
//    	this.aggreUnitMix		= "";
//    	this.aggreUnitWhere	= "";
	}
	
//    public DataSelecterAction(CommandServerRequestBuilder builder) 
//    {
//    	this.db = DBProperties.getInstance();
//		this.sd = new SimDate();
//    
//    	this.agentName 		= builder.getAgentName();
//    	this.commandName	= builder.getCommandName();
//    	this.actionName			= builder.getActionName();
//        
//    	this.perUnit 				= builder.getPerUnit();
//    	this.valueUnit 			= builder.getValueUnit();
//    	this.aggreUnit			= builder.getAggreUnit();
//        this.rangeStart 			= builder.getRangeStart();
//        this.rangeEnd 			= builder.getRangeEnd();
//        this.consoleId			= builder.getConsoleId();
//        
//        this.tableName 			= builder.GetTableNameOfPerUnit(DicOrderAdd.AmassPreFix_Trans, this.perUnit);
//    	this.iterAggreUnit 		= builder.GetColumnNameOfAggreUnit(DicOrderAdd.AmassPreFix_Trans, this.aggreUnit).iterator();
//    	this.aggreUnitMix		= builder.GetMixOfAggreUnit(this.aggreUnit);
//    	this.aggreUnitWhere	= builder.GetConditionOfAggreUnit(this.aggreUnit);
//	}
    
//    public void setBuilder(CommandServerRequestBuilder builder) 
//    {
//    	this.db = DBProperties.getInstance();
//		this.sd = new SimDate();
//    
//    	this.agentName 		= builder.getAgentName();
//    	this.commandName	= builder.getCommandName();
//    	this.actionName			= builder.getActionName();
//        
//    	this.perUnit 				= builder.getPerUnit();
//    	this.valueUnit 			= builder.getValueUnit();
//    	this.aggreUnit			= builder.getAggreUnit();
//        this.rangeStart 			= builder.getRangeStart();
//        this.rangeEnd 			= builder.getRangeEnd();
//        this.consoleId			= builder.getConsoleId();
//        
//        this.tableName 			= builder.GetTableNameOfPerUnit(DicOrderAdd.AmassPreFix_Trans, this.perUnit);
//    	this.iterAggreUnit 		= builder.GetColumnNameOfAggreUnit(DicOrderAdd.AmassPreFix_Trans, this.aggreUnit).iterator();
//    	this.aggreUnitMix		= builder.GetMixOfAggreUnit(this.aggreUnit);
//    	this.aggreUnitWhere	= builder.GetConditionOfAggreUnit(this.aggreUnit);
//	}

	public String Select_XmAggregatedAction(CommandServerRequestBuilder builder) {		
		String agentName;
		String commandName;
		String actionName;
		
		String perUnit;
		String valueUnit;
		List<String> aggreUnit;
		
		String aggreUnitMix;
		String aggreUnitWhere;
		
		String rangeStart;
		String rangeEnd;
		String consoleId;
		
		String tableName;
		Iterator<String> iterAggreUnit;
		
    	agentName 		= builder.getAgentName();
    	commandName	= builder.getCommandName();
    	actionName			= builder.getActionName();
        
    	perUnit 				= builder.getPerUnit();
    	valueUnit 			= builder.getValueUnit();
    	aggreUnit			= builder.getAggreUnit();
        rangeStart 			= builder.getRangeStart();
        rangeEnd 			= builder.getRangeEnd();
        consoleId			= builder.getConsoleId();
        
        tableName 			= builder.GetTableNameOfPerUnit(DicOrderAdd.AmassPreFix_Trans, perUnit);
    	iterAggreUnit 		= builder.GetColumnNameOfAggreUnit(DicOrderAdd.AmassPreFix_Trans, aggreUnit).iterator();
    	aggreUnitMix		= builder.GetMixOfAggreUnit(aggreUnit);
    	aggreUnitWhere	= builder.GetConditionOfAggreUnit(aggreUnit);
    	// =======
		
		String query_AggreUnit 		= "";
		
		if(tableName.equals("")) return ""; 
		
		while(iterAggreUnit.hasNext()) {
			query_AggreUnit += " , " + iterAggreUnit.next(); 
		}

		String query = "";
		boolean bMonitoring = false;
		
		// 20141028, junsoo, 모니터링용은 최근 값 한개만 리턴
		if (rangeStart.length() < 1 && rangeEnd.length() < 1){
			bMonitoring = true;
			if(DBProperties.SQLITE == DBProperties.getInstance().getDbType(DBProperties.dbType_Action)) {
				query =   "SELECT AggregatedTime,AgentName,ActionName" + query_AggreUnit 
						  + "FROM " + tableName + " WHERE AggregatedTime > ? and AgentName	= ? AND	ActionName = ? \n"
						  + " order by AggregatedTime desc limit 1 \n";
			}
			else if(DBProperties.ORACLE == DBProperties.getInstance().getDbType(DBProperties.dbType_Action)) {
				query =   "SELECT /*+ INDEX_DESC(TBL XmActionSec_Time) */ AggregatedTime,AgentName\n"
						+ " ,ActionName " + query_AggreUnit	+ " FROM " + tableName + " TBL "
						+ " WHERE AggregatedTime > ? and AgentName = ? and ActionName = ?\n"
						+ " AND	rownum < 2 \n";
			}
		}
		else {
			query = "SELECT	AggregatedTime,	AgentName ,	ActionName						\n"                    							  
								  +	query_AggreUnit + " FROM " + tableName
								  + "WHERE	AggregatedTime Between ? AND ?		\n"
								  + "AND 	AgentName  	= ? AND ActionName= ?        \n";
		}
		long time = System.currentTimeMillis() - 30000;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String limit = dateFormat.format(new Date(time));
		
		
		JSONObject jsonObjectResponse 		= new JSONObject();
	  	JSONObject jsonObjectSubResponse = null;
	  	JSONArray jsonArrayResponse 			= new JSONArray();
		
	  	Connection con = null;		
		PreparedStatement prep						= null;
		ResultSet rs 										= null;
		try {
			if (this.db.open(DBProperties.dbType_Action) == false)
			{
			  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "DB Conn Error");
			  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, "-1");
			}
			con = this.db.getConnection(DBProperties.dbType_Action);
			
			prep = con.prepareStatement(query);
		
			int index = 1;
			if (!bMonitoring){
				prep.setString(index++, rangeStart );
				prep.setString(index++, rangeEnd );
			}
			else
			prep.setString(index++, limit );
			prep.setString(index++, agentName );
			prep.setString(index++, actionName );

			LOG.trace("QUERY: " + query);
			
			rs = prep.executeQuery();
			int rsCnt =0;
		  	
		  	jsonObjectResponse.put(DicOrderAdd.AgentName, agentName);
		  	String sendTime =SimDate.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis()));
		  	jsonObjectResponse.put(DicOrderAdd.SendTime,sendTime );	        
		  	
		  	jsonObjectResponse.put(DicOrderTypes.OrderType,	DicOrderTypes.ResponseStats);
		  	jsonObjectResponse.put(DicOrderAdd.Command, 	commandName);
		  	jsonObjectResponse.put(DicOrderAdd.PerUnit, 		perUnit);
		  	
		  	jsonObjectResponse.put(DicOrderAdd.AggreUnit, 	aggreUnitMix);
		  	
		  	jsonObjectResponse.put(DicOrderAdd.RangeStart,	rangeStart);
		  	jsonObjectResponse.put(DicOrderAdd.RangeEnd, 	rangeEnd);
		  	jsonObjectResponse.put(DicOrderAdd.ConsoleId, 	consoleId);
		  	
		  	
		  	long RsValueUnit		= 0;
		  	String aggUnit				= "";
			while(rs.next())
			{
				rsCnt++;				
				try {					
					Iterator<String> iterAggreUnits	= aggreUnit.iterator();					
					while(iterAggreUnits.hasNext())
					{
						jsonObjectSubResponse 			= new JSONObject();						
						aggUnit			= iterAggreUnits.next();
						RsValueUnit	= rs.getLong(aggUnit);
						
						jsonObjectSubResponse.put(DicOrderAdd.ValueUnit, 	valueUnit);
						jsonObjectSubResponse.put(DicOrderAdd.AggreUnit, aggUnit); 
						
						//yys 시간이 너무 지난값은 0을 리턴함. 2015-05-13
						String aggreTime = rs.getString(DicOrderAdd.AggregatedTime);
						if(rangeStart.length() <1)
						{
							 Date end = SimDate.getDateTimeFormatter_MS().parse(sendTime);
							 Date start = SimDate.getDateTimeFormatter_Sec().parse(aggreTime);
							 if(end.getTime() - start.getTime() < 5000){
								 jsonObjectSubResponse.put(DicOrderAdd.AggregatedTime, aggreTime);
								 jsonObjectSubResponse.put("USE", RsValueUnit );
							 }
							 else{				 
								 jsonObjectSubResponse.put(DicOrderAdd.AggregatedTime, sendTime.substring(0, sendTime.length()-3));
								 jsonObjectSubResponse.put("USE", 0);
							 }								 
						}
						else{					
						jsonObjectSubResponse.put(DicOrderAdd.AggregatedTime, aggreTime);
						jsonObjectSubResponse.put("USE", RsValueUnit );
						}
						
						
						
						jsonArrayResponse.put(jsonObjectSubResponse);
					}
					
				} catch (Exception e) {				
					e.printStackTrace();					
				  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, e.getMessage());
				  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, DicCommands.GENERAL_ERROR);
				}
			} //End While

			jsonObjectResponse.put(DicOrderTypes.ResponseStats, jsonArrayResponse);
			
			rs.close();
			prep.close();
			
			if(rsCnt>0) {
			  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "");
			  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, DicCommands.OK);
			}
			else {
				jsonObjectSubResponse 			= new JSONObject();
				jsonObjectSubResponse.put("USE", RsValueUnit );
				jsonArrayResponse.put(jsonObjectSubResponse);
				jsonObjectResponse.put(DicOrderTypes.ResponseStats, jsonArrayResponse);
			  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "No data");
			  	// 20141027, junsoo, �����Ͱ� ������ -100���� ���� 
			  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, DicCommands.NODATA);
			}
		} catch (Exception e) {			
			e.printStackTrace();			
			return "";
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				db.releaseConnection(DBProperties.dbType_Action, con);
		}
		return jsonObjectResponse.toString();
	}
	
	public String Select_XmTransAction(CommandServerRequestBuilder builder) {
		// set builder
		String agentName;
		String commandName;
		String actionName;
		
		String perUnit;
		String valueUnit;
		List<String> aggreUnit;
		
		String aggreUnitMix;
		String aggreUnitWhere;
		
		String rangeStart;
		String rangeEnd;
		String consoleId;
		
		String tableName;
		Iterator<String> iterAggreUnit;
		
    	agentName 		= builder.getAgentName();
    	commandName	= builder.getCommandName();
    	actionName			= builder.getActionName();
        
    	perUnit 				= builder.getPerUnit();
    	valueUnit 			= builder.getValueUnit();
    	aggreUnit			= builder.getAggreUnit();
        rangeStart 			= builder.getRangeStart();
        rangeEnd 			= builder.getRangeEnd();
        consoleId			= builder.getConsoleId();
        
        tableName 			= builder.GetTableNameOfPerUnit(DicOrderAdd.AmassPreFix_Trans, perUnit);
    	iterAggreUnit 		= builder.GetColumnNameOfAggreUnit(DicOrderAdd.AmassPreFix_Trans, aggreUnit).iterator();
    	aggreUnitMix		= builder.GetMixOfAggreUnit(aggreUnit);
    	aggreUnitWhere	= builder.GetConditionOfAggreUnit(aggreUnit);
    	
		String query = " SELECT TransInitTime, AgentName ,	ActionName , ResponseTime									"
							  + " ,	TransID ,TransResult,ext1,ext2 FROM	XmTransAction 									"
							  + " WHERE	TransInitTime Between ? AND ? "
							  + "AND AgentName = ? AND 	ActionName = ? AND 	TransResult		in (" 
							  + aggreUnitWhere + ") ORDER BY TransInitTime";
		
		JSONObject jsonObjectResponse 		= new JSONObject();
	  	JSONObject jsonObjectSubResponse = null;
	  	JSONArray jsonArrayResponse 			= new JSONArray();
	  	
		Connection con = null;

		PreparedStatement prep						= null;
		ResultSet rs										= null;
		try {
			con = db.getConnection(DBProperties.dbType_Action);
			
			prep = con.prepareStatement(query);
			prep.setString(1, rangeStart );
			prep.setString(2, rangeEnd );
			prep.setString(3, agentName );
			prep.setString(4, actionName );
	
			rs = prep.executeQuery();			
			int rsCnt =0;
		  	
		  	jsonObjectResponse.put(DicOrderAdd.AgentName, agentName);
		  	jsonObjectResponse.put(DicOrderAdd.SendTime, SimDate.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis())));	        
		  	
		  	jsonObjectResponse.put(DicOrderTypes.OrderType,	DicOrderTypes.ResponseStats);
		  	jsonObjectResponse.put(DicOrderAdd.Command, 	commandName);
		  	jsonObjectResponse.put(DicOrderAdd.AggreUnit, 	aggreUnitMix);
		  	
		  	jsonObjectResponse.put(DicOrderAdd.PerUnit, 		perUnit);  	
		  	
		  	jsonObjectResponse.put(DicOrderAdd.RangeStart,	rangeStart);
		  	jsonObjectResponse.put(DicOrderAdd.RangeEnd, 	rangeEnd);
		  	jsonObjectResponse.put(DicOrderAdd.ConsoleId, 	consoleId);

		  	while(rs.next())
			{
				rsCnt++;
				try {					
					jsonObjectSubResponse 	= new JSONObject();
					
					jsonObjectSubResponse.put(DicOrderAdd.AggreUnit, rs.getString("TransResult"));
					jsonObjectSubResponse.put(DicOrderAdd.TransTime, rs.getString(DicOrderAdd.TransTime));
					jsonObjectSubResponse.put(DicOrderAdd.TransID, rs.getString(DicOrderAdd.TransID));
					jsonObjectSubResponse.put(DicOrderAdd.ValueUnit, 	valueUnit);
					String ext1 = rs.getString("ext1");
					String ext2 = rs.getString("ext2");
					if(ext1 != null && !ext1.equals(""))
						jsonObjectSubResponse.put("ext1", ext1);
					if(ext2 != null && !ext2.equals(""))
						jsonObjectSubResponse.put("ext2", ext2);
					
					//System.out.println( rs.getString("ResponseTime"));
					jsonObjectSubResponse.put("USE", rs.getString("ResponseTime"));
					jsonArrayResponse.put(jsonObjectSubResponse);
					
				} catch (Exception e) {
					// java.sql.SQLException: not implemented by SQLite JDBC driver
					e.printStackTrace();					
				  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, e.getMessage());
				  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, DicCommands.GENERAL_ERROR);
				}
			} //End While

			jsonObjectResponse.put(DicOrderTypes.ResponseStats, jsonArrayResponse);
			
			rs.close();
			prep.close();
			
			if(rsCnt>0)
			{
			  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "");
			  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, "0");
			}
			else 
			{
			  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "");
			  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, "-100");
			}
		} catch (Exception e) {			
			e.printStackTrace();			
			return "";
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				db.releaseConnection(DBProperties.dbType_Action, con);
		}
		return jsonObjectResponse.toString();
	}
	
	public JSONArray Select_XmActionList(String agentName) {
		String query = " SELECT * FROM XmActionList WHERE AgentName = ?";
		
		PreparedStatement prep = null;
		ResultSet rs					= null;
		Connection con = null;
		try {
			con = db.getConnection(DBProperties.dbType_Action);
			
			prep = con.prepareStatement(query);			
			prep.setString(1, agentName.trim());		
			rs = prep.executeQuery();
			
			
			JSONObject jsonRs	= null;
			JSONArray jsonArray	= new JSONArray();
			while(rs.next())
			{
				jsonRs = new JSONObject();
				jsonRs.put(ItemAgent.KeyAgentName, 			rs.getString(ItemAgent.KeyAgentName)); 
				jsonRs.put(ItemAgent.KeyACTActionName,	rs.getString(ItemAgent.KeyACTActionName));
				jsonRs.put(ItemAgent.KeyACTActionDesc, 	rs.getString(ItemAgent.KeyACTActionDesc));
				jsonRs.put(ItemAgent.KeyACTActionTitle, 		rs.getString(ItemAgent.KeyACTActionTitle));
				
				jsonRs.put(ItemAgent.KeyACTAggreUseYN, 	rs.getString(ItemAgent.KeyACTAggreUseYN));
				jsonRs.put(ItemAgent.KeyACTHealthUseYN, 	rs.getString(ItemAgent.KeyACTHealthUseYN));
				
				jsonArray.put(jsonRs);
				
			}//End Select Rs While
			return jsonArray;
			
		} catch (Exception e) {
			// java.sql.SQLException: not implemented by SQLite JDBC driver
			e.printStackTrace();
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				db.releaseConnection(DBProperties.dbType_Action, con);
		}
		return null;
	}
	
	// health check용 tps 리턴
	public JSONArray getActionsTPSForHealth(String agentName, JSONArray jsonArray) {
		if(agentName.equals("")) return null;
		
		PreparedStatement prep = null;
		ResultSet rs					= null;
		Connection con = null;
		try {
			con = db.getConnection(DBProperties.dbType_Action);
			
			// get actions
			List<String> actions = new ArrayList<String>();			
			String sub = "Select ActionName From xmactionlist where agentname = ? and healthuseyn = ? \n";
			
			prep = con.prepareStatement(sub);
			prep.setString(1, agentName.trim());
			prep.setString(2, "Y");
			rs = prep.executeQuery();
			while(rs.next()){
				actions.add(rs.getString(1));
			}
			rs.close();
			prep.close();
			prep = null;

			// get tps
			if (jsonArray == null)
				jsonArray	= new JSONArray();
			
			for (String actionName : actions) {
				String query = "";
				if(DBProperties.SQLITE == DBProperties.getInstance().getDbType(DBProperties.dbType_Action)) {
					query += "Select AggregatedTime, agentname,actionname as ResourceID, actionname as CommandName, successcount as PropertyValue	\n";
					query += "From XmAggregatedActionSec		\n";
					query += "where  AggregatedTime > ?		\n";
					query += "and agentname = ?		\n";
					query += "and actionname = ?		\n";
					query += "order by aggregatedtime desc limit 1		\n";
				}
				else if(DBProperties.ORACLE == DBProperties.getInstance().getDbType(DBProperties.dbType_Action)) {
					query += "Select /*+ INDEX_DESC(TBL XmActionSec_Time) */ AggregatedTime, agentname,actionname as ResourceID, actionname as CommandName, successcount as PropertyValue	\n";
					query += "From XmAggregatedActionSec TBL		\n";
					query += "where AggregatedTime > ?		\n";
					query += "and agentname = ?		\n";
					query += "and actionname = ?		\n";
					query += "and rownum < 2		\n";
				}
				long time = System.currentTimeMillis() - 30000;
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				String limit = dateFormat.format(new Date(time));
				
				prep = con.prepareStatement(query);
				prep.setString(1,limit);
				prep.setString(2, agentName.trim());
				prep.setString(3, actionName.trim());				
			
				rs = prep.executeQuery();
				JSONObject jsonRs	= null;
				while(rs.next()){
					jsonRs = new JSONObject();
		 
					jsonRs.put("CommandName",		rs.getString("CommandName"));      
					jsonRs.put("ResourceID",			rs.getString("ResourceID"));       
//					jsonRs.put("PropertyName",		rs.getString("PropertyName"));     
//					jsonRs.put("PropertyValueUnit",	rs.getString("PropertyValueUnit"));
					jsonRs.put("PropertyValue",		rs.getString("PropertyValue"));
					jsonRs.put("AggregatedTime",		rs.getString("AggregatedTime"));
					
					jsonArray.put(jsonRs);
					
				}
				rs.close();
				prep.close();
				prep = null;

			}

			return jsonArray;
			
		} catch (Exception e) {
			// java.sql.SQLException: not implemented by SQLite JDBC driver
			LOG.error(e.getMessage(), e);
			e.printStackTrace();
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				db.releaseConnection(DBProperties.dbType_Action, con);
		}
		return null;
	}
}
