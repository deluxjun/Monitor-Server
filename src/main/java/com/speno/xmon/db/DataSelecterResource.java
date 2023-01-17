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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.codedic.DicCommands;
import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.codedic.DicOrderTypes;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.env.ItemAgent;
import com.speno.xmon.listener.CommandServerRequestBuilder;
import com.speno.xmon.util.CommonUtil;

public class DataSelecterResource 
{
	private final static Logger LOG = LoggerFactory.getLogger(DataSelecterResource.class);

	private DBProperties db;
	/*
	public DataSelecterResource() {
		this.db = DBProperties.getInstance();
	}
	*/
//	private String agentName;
//	private String commandName;
//	private String resourceID;
//	private String perUnit;
//	private String valueUnit;
//	private List<String> aggreUnit;
//	
//	private String aggreUnitMix;
//	
//	private String rangeStart;
//	private String rangeEnd;
//	private String consoleId;
//	
//	private String tableName;
//	Iterator<String> iterAggreUnit;
	
    public DataSelecterResource()
    { 
    	this.db = DBProperties.getInstance();
    }
//    public DataSelecterResource(CommandServerRequestBuilder builder) 
//    {
//    	this.db = DBProperties.getInstance();
//    	this.sd = new SimDate();
//    	
//    	this.agentName 		= builder.getAgentName();
//    	this.commandName	= builder.getCommandName();
//    	this.resourceID			= builder.getResourcdID();
//    	this.perUnit 				= builder.getPerUnit();
//    	this.valueUnit 			= builder.getValueUnit();
//    	this.aggreUnit			= builder.getAggreUnit();
//        this.rangeStart 			= builder.getRangeStart();
//        this.rangeEnd 			= builder.getRangeEnd();
//        this.consoleId			= builder.getConsoleId();
//        
//        this.tableName 			= builder.GetTableNameOfPerUnit(DicOrderAdd.AmassPreFix_Resource, this.perUnit);
//    	this.iterAggreUnit 		= builder.GetColumnNameOfAggreUnit(DicOrderAdd.AmassPreFix_Resource,this.aggreUnit).iterator();
//    	this.aggreUnitMix		= builder.GetMixOfAggreUnit(this.aggreUnit);
//	}
    
//    public void setBuilder(CommandServerRequestBuilder builder) 
//    {
//    	this.db = DBProperties.getInstance();
//    	this.sd = new SimDate();
//    	
//    	this.agentName 		= builder.getAgentName();
//    	this.commandName	= builder.getCommandName();
//    	this.resourceID			= builder.getResourcdID();
//    	this.perUnit 				= builder.getPerUnit();
//    	this.valueUnit 			= builder.getValueUnit();
//    	this.aggreUnit			= builder.getAggreUnit();
//        this.rangeStart 			= builder.getRangeStart();
//        this.rangeEnd 			= builder.getRangeEnd();
//        this.consoleId			= builder.getConsoleId();
//        
//        this.tableName 			= builder.GetTableNameOfPerUnit(DicOrderAdd.AmassPreFix_Resource, this.perUnit);
//    	this.iterAggreUnit 		= builder.GetColumnNameOfAggreUnit(DicOrderAdd.AmassPreFix_Resource,this.aggreUnit).iterator();
//    	this.aggreUnitMix		= builder.GetMixOfAggreUnit(this.aggreUnit);
//	}
    
	public String Select_XmAggregatedResource(CommandServerRequestBuilder builder) {		
		
		String agentName;
		String commandName;
		String resourceID;
		String perUnit;
		String valueUnit;
		List<String> aggreUnit;		
		String aggreUnitMix;		
		String rangeStart;
		String rangeEnd;
		String consoleId;
		
		String tableName;
		Iterator<String> iterAggreUnit;
    	agentName 	= builder.getAgentName();
    	commandName	= builder.getCommandName();
    	resourceID	= builder.getResourcdID();
    	String propertyName	= builder.getPropertyName();
    	perUnit = builder.getPerUnit();
    	valueUnit = builder.getValueUnit();
    	aggreUnit = builder.getAggreUnit();
        rangeStart = builder.getRangeStart();
        rangeEnd = builder.getRangeEnd();
        consoleId = builder.getConsoleId();
        
        tableName = builder.GetTableNameOfPerUnit(DicOrderAdd.AmassPreFix_Resource, perUnit);
    	iterAggreUnit = builder.GetColumnNameOfAggreUnit(DicOrderAdd.AmassPreFix_Resource,aggreUnit).iterator();
    	aggreUnitMix = builder.GetMixOfAggreUnit(aggreUnit);
    	String groupBy	= builder.GetGroupBy(DicOrderAdd.AmassPreFix_Resource,aggreUnit);
		
		String query_AggreUnit	= "";		
		if(tableName.equals("")) return ""; 		
		//Iterator<String> iterValueUnit =  this.GetColumnNameOfValueUnit(this.valueUnit).iterator();
		while(iterAggreUnit.hasNext())
		query_AggreUnit += " , " + iterAggreUnit.next(); 
				
		String query =    "	    SELECT	AggregatedTime                  				\n"           
							  + "				   ,   	AgentName									\n"
							  + "				   ,	CommandName									\n"
							  + "				   ,	ResourceID									\n"          
							  + "				   ,	PropertyName								\n"
							  + 						query_AggreUnit 
							  + " 				   ,	PropertyValueUnit 			    				\n"	
							  + "   	   FROM	" + tableName
							  + "      WHERE	AggregatedTime Between ? AND ?      \n"
							  + "		      AND 	AgentName  		= ?                 			\n"
							  + "    	      AND 	CommandName	= ?                 			\n"
							  + "    	      AND 	ResourceID		= ?                 			\n"
							  + "    	      AND 	PropertyName		in (?)                 			\n"
							  + " GROUP BY	AggregatedTime, AgentName				\n"
							  + "         	       ,    CommandName, ResourceID, PropertyName          \n"
							  + "         	       ,    " + groupBy + "						\n"
							  + "         	       ,    PropertyValueUnit	                		  	\n";
		
		JSONObject jsonObjectResponse = new JSONObject();
	  	JSONObject jsonObjectSubResponse = null;
	  	JSONArray jsonArrayResponse	= new JSONArray();
	  	
		PreparedStatement prep						= null;
		ResultSet rs 										= null;
		Connection con = null;
		try {
		  	jsonObjectResponse.put(DicOrderAdd.Command,	commandName);

			if (db.open(DBProperties.dbType_Resource) == false)
			{
			  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "DB Conn Error");
			  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, DicCommands.GENERAL_ERROR);
			}
			
			con = db.getConnection(DBProperties.dbType_Resource);
			
			prep = con.prepareStatement(query);
			prep.setString(1, rangeStart );
			prep.setString(2, rangeEnd );
			prep.setString(3, agentName );
			prep.setString(4, commandName );
			prep.setString(5, resourceID );
			prep.setString(6, propertyName );				
			rs = prep.executeQuery();
		
			int rsCnt =0;
		  	
		  	jsonObjectResponse.put(DicOrderAdd.AgentName, agentName);
		  	jsonObjectResponse.put(DicOrderAdd.SendTime, SimDate.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis())));	        
		  	
		  	jsonObjectResponse.put(DicOrderTypes.OrderType,	DicOrderTypes.ResponseStats);
		  	jsonObjectResponse.put(DicOrderAdd.ResourceID, 	resourceID);
		  	
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
					
					String PropertyValueUnit = "";
					while(iterAggreUnits.hasNext())
					{
						jsonObjectSubResponse 			= new JSONObject();
						
						aggUnit					= iterAggreUnits.next();
						RsValueUnit			= rs.getLong(aggUnit);
						PropertyValueUnit 	= rs.getString(DicOrderAdd.PropertyValueUnit);
						
						if(!PropertyValueUnit.equals(valueUnit))
						{
							//RsValueUnit = RsValueUnit;
						}
						jsonObjectSubResponse.put(DicOrderAdd.ValueUnit, 	valueUnit);
						jsonObjectSubResponse.put(DicOrderAdd.AggreUnit, aggUnit);
						jsonObjectSubResponse.put(DicOrderAdd.AggregatedTime, rs.getString(DicOrderAdd.AggregatedTime));
						jsonObjectSubResponse.put(rs.getString(DicOrderAdd.PropertyName), RsValueUnit );
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
			  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "no data");
			  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, DicCommands.NODATA);
			}
		} catch (Exception e) {		
			LOG.error( query );
			e.printStackTrace();
	
			try {
			  	jsonObjectResponse.put(DicOrderAdd.ReturnMsg, "query error : " + e.getMessage());
			  	jsonObjectResponse.put(DicOrderAdd.ReturnCode, DicCommands.GENERAL_ERROR);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				db.releaseConnection(DBProperties.dbType_Resource, con);
		}
		return jsonObjectResponse.toString();
	}
	

	
	public JSONArray Select_XmCommandListAndSub(String agentName, String commandName) {
    	String query = " SELECT   c.AgentName                  	"
				           + "      ,   c.CommandName                     "
				           + "      ,   c.ValueUnit                             	"
				           + "      ,   c.CommandDesc                     	"
				           + "      ,   c.CommandTitle                       	"
				           + "      ,   c.AggreUseYN                         	"
				           + "      ,   c.HealthUseYN                        	"
				           + "      ,   cs.ResourceID                         	"
				           + "      ,   cs.PropertyName                     	"
				           + "     FROM    XmCommandList c, XmCommandSubList cs	"
				           + "  WHERE    c.AgentName		= cs.AgentName           		"
				           + "       AND    c.CommandName	= cs.CommandName        	";
		if(!agentName.equals(""))
			query +=     "       AND    c.AgentName		= ?                                  	";
		if(!commandName.equals(""))
			query +=     "       AND    c.CommandName	= ?                                  	";
		
		PreparedStatement prep = null;
		Connection con = null;
		ResultSet rs					= null;
		try {
			con = db.getConnection(DBProperties.dbType_Resource);
			
			prep = con.prepareStatement(query);
			
			if(!agentName.equals(""))
				prep.setString(1, agentName.trim());
			if(!commandName.equals(""))
				prep.setString(2, commandName.trim());			
			rs = prep.executeQuery();
			
			JSONObject jsonRs	= null;
			JSONArray jsonArray	= new JSONArray();
			while(rs.next())
			{
				jsonRs = new JSONObject();
				jsonRs.put(ItemAgent.KeyAgentName, 		rs.getString(ItemAgent.KeyAgentName)); 
				jsonRs.put(ItemAgent.KeyCMDCommandName,	rs.getString(ItemAgent.KeyCMDCommandName));
				jsonRs.put(ItemAgent.KeyCMDValueUnit, 			rs.getString(ItemAgent.KeyCMDValueUnit));
				jsonRs.put(ItemAgent.KeyCMDCommandDesc, 	rs.getString(ItemAgent.KeyCMDCommandDesc));
				jsonRs.put(ItemAgent.KeyCMDCommandTitle, 	rs.getString(ItemAgent.KeyCMDCommandTitle));
				jsonRs.put(ItemAgent.KeyCMDAggreUseYN, 		rs.getString(ItemAgent.KeyCMDAggreUseYN));
				jsonRs.put(ItemAgent.KeyCMDHealthUseYN, 	rs.getString(ItemAgent.KeyCMDHealthUseYN));
				jsonRs.put(ItemAgent.KeyCMDResourceID, 		rs.getString(ItemAgent.KeyCMDResourceID));
				jsonRs.put(ItemAgent.KeyCMDPropertyName, 	rs.getString(ItemAgent.KeyCMDPropertyName));
				
				jsonArray.put(jsonRs);
				
			}//End Select Rs While
			return jsonArray;
			
		} catch (Exception e) {			
			e.printStackTrace();
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				db.releaseConnection(DBProperties.dbType_Resource, con);
		}
		return null;
	}
	
	public int getResourceCount(String agentName) {		
		String query = " Select count(*) as cnt From  XmCommandList L, XmCommandSubList    S\n";
		query += " 	WHERE HEALTHUSEYN   ='Y'AND L.AgentName   = S.AgentName			\n";
		query += " 	AND L.CommandName = S.CommandName AND L.agentname   = ?			\n";
		
		PreparedStatement prep = null;
		ResultSet rs					= null;
		Connection con = null;
		
		try {
			con = db.getConnection(DBProperties.dbType_Resource);
			prep = con.prepareStatement(query);
			
			prep.setString(1, agentName.trim());		
			rs = prep.executeQuery();
			if (rs.next()) {
				return rs.getInt("cnt");      
			}
			
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			e.printStackTrace();
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				db.releaseConnection(DBProperties.dbType_Resource, con);
		}
		return 0;
	}
	
	public JSONArray Select_XmDataSelecterResource_Helth(String agentName) {
//		String commandName;
//		String resourceID;
//		String perUnit;
//		String valueUnit;
//		List<String> aggreUnit;
//		
//		String aggreUnitMix;
//		
//		String rangeStart;
//		String rangeEnd;
//		String consoleId;
//		
//		String tableName;
//		Iterator<String> iterAggreUnit;
//    	commandName	= builder.getCommandName();
//    	resourceID			= builder.getResourcdID();
//    	perUnit 				= builder.getPerUnit();
//    	valueUnit 			= builder.getValueUnit();
//    	aggreUnit			= builder.getAggreUnit();
//        rangeStart 			= builder.getRangeStart();
//        rangeEnd 			= builder.getRangeEnd();
//        consoleId			= builder.getConsoleId();
//        
//        tableName 			= builder.GetTableNameOfPerUnit(DicOrderAdd.AmassPreFix_Resource, perUnit);
//    	iterAggreUnit 		= builder.GetColumnNameOfAggreUnit(DicOrderAdd.AmassPreFix_Resource,aggreUnit).iterator();
//    	aggreUnitMix		= builder.GetMixOfAggreUnit(aggreUnit);

    	
		if(agentName.equals("")) return null;
		
		int resCount = getResourceCount(agentName);
		if (resCount < 1)
			return null;
		
		String query =	"";
		
		if(DBProperties.SQLITE == DBProperties.getInstance().getDbType(DBProperties.dbType_Resource)) {
		query = "  Select R.AgentName                  				AS AgentName         		\n"
					             + "			, R.CommandName                		AS CommandName   		\n"
					             + "			, R.ResourceID                 				AS ResourceID        		\n"
					             + "			, R.PropertyName               			AS PropertyName      	\n"             
					             + "			, R.PropertyValueUnit          			AS PropertyValueUnit 	\n"
					             + "			, ROUND( AVG(R.PropertyValue))	AS PropertyValue     		\n"
					             + "			, R.AggregatedTime AS AggregatedTime     		\n"
					             + "    From XmAggregatedResourceSec R                  							\n"
					             + "       , (                                                 										\n"
					             + "       		Select L.AgentName                                  					\n"
					             + "            			, L.CommandName                                				\n"
					             + "            			, S.ResourceID                                 						\n"	
					             + "            			, S.PropertyName                               					\n"
					             + "         	      From  XmCommandList L                       						\n"
					             + "            			 , XmCommandSubList    S                        				\n"
					             + "        	  WHERE HEALTHUSEYN   ='Y'  "
					             + "					AND R.AggregatedTime > ?                        				\n"
					             + "          			AND L.AgentName   = S.AgentName                  			\n"
					             + "          			AND L.CommandName = S.CommandName                \n"
					             + "          			AND L.agentname   = ?												\n"
					             + "          ) C                                              										\n"					             
					             + "   	WHERE R.AgentName    		= C.AgentName                      	\n"
					             + "     		  AND R.CommandName 	= C.CommandName                   \n"
					             + "     		  AND R.ResourceID   		= C.ResourceID                     	\n"
					             + "     		  AND R.PropertyName 		= C.PropertyName                   	\n"
					             + "  GROUP BY R.AgentName                                     						\n"
					             + "       			, R.CommandName                                     				\n"
					             + "       			, R.ResourceID                                      					\n"
					             + "       			, R.PropertyName                                    					\n"
					             + "       			, R.PropertyValueUnit                               					\n";
		}
		else if(DBProperties.ORACLE == DBProperties.getInstance().getDbType(DBProperties.dbType_Resource)) {
//			query += " select /*+ INDEX_DESC(R XmResourceSec_Time) */ 		\n";
//			query += " R.AgentName                  				AS AgentName		\n";
//			query += " , R.CommandName                		AS CommandName		\n";
//			query += " , R.ResourceID                 				AS ResourceID		\n";
//			query += " , R.PropertyName               			AS PropertyName		\n";
//			query += " , R.PropertyValueUnit          			AS PropertyValueUnit		\n";
//			query += " , R.PropertyValue	AS PropertyValue		\n";
//			query += " , R.AggregatedTime AS AggregatedTime		\n";
//			query += " from XmAggregatedResourceSec R				\n";
//			query += " ,( Select L.AgentName, L.CommandName, S.ResourceID, S.PropertyName From  XmCommandList L		\n";
//			query += "	, XmCommandSubList    S		\n";
//			query += "	WHERE HEALTHUSEYN   ='Y'		\n";
//			query += "	AND L.AgentName   = S.AgentName		\n";
//			query += "	AND L.CommandName = S.CommandName		\n";
//			query += "	AND L.agentname   = ?) C		\n";
//			query += " where R.agentname = C.agentname and R.commandname = c.commandname and R.resourceID = C.ResourceID and R.PropertyName = C.PropertyName		\n";
//			query += " and R.AGGREGATEDTIME is not null		\n";
//			query += " and rownum < "+(resCount+1)+"		\n";
		
			
			query += " select /*+ INDEX_DESC(R XmResourceSec_Time) */ 		\n";
			query += " R.AgentName                  				AS AgentName		\n";
			query += " , R.CommandName                		AS CommandName		\n";
			query += " , R.ResourceID                 				AS ResourceID		\n";
			query += " , R.PropertyName               			AS PropertyName		\n";
			query += " , R.PropertyValueUnit          			AS PropertyValueUnit		\n";
			query += " , R.PropertyValue	AS PropertyValue		\n";
			query += " , R.AggregatedTime AS AggregatedTime		\n";
			query += " from XmAggregatedResourceSec R				\n";
			query += " where R.AggregatedTime > ?		\n";
			query += " and R.agentname = ?		\n";
			query += " and rownum < "+(resCount+1)+"		\n";

			
		}
		long time = System.currentTimeMillis() - 30000;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String limit = dateFormat.format(new Date(time));
		
		PreparedStatement prep = null;
		ResultSet rs					= null;
		Connection con = null;
		try {
			con = db.getConnection(DBProperties.dbType_Resource);

			prep = con.prepareStatement(query);
			prep.setString(1, limit);			
			prep.setString(2, agentName.trim());			
			rs = prep.executeQuery();
			
			JSONObject jsonRs	= null;
			JSONArray jsonArray	= new JSONArray();
			while(rs.next())
			{
				jsonRs = new JSONObject();
	 
				jsonRs.put("CommandName",		rs.getString("CommandName"));      
				jsonRs.put("ResourceID",			rs.getString("ResourceID"));       
				jsonRs.put("PropertyName",		rs.getString("PropertyName"));     
				jsonRs.put("PropertyValueUnit",	rs.getString("PropertyValueUnit"));
				jsonRs.put("PropertyValue",		rs.getString("PropertyValue"));
				jsonRs.put("AggregatedTime",		rs.getString("AggregatedTime"));
				
				jsonArray.put(jsonRs);
				
			}//End Select Rs While
			return jsonArray;
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			e.printStackTrace();
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				db.releaseConnection(DBProperties.dbType_Resource, con);
		}
		return null;
	}
	
	
	
	// statistics
//	List<String> data = dbResourceSelect.selectResource(rangeStart, rangeEnd, perUnit, listAggreUnit, listPropertyNames, listResourceId, listResourceGroupId);
	public List<List<String>> selectResource(String agentName, String rangeStart, String rangeEnd, String perUnit, List<String> listAggreUnit, List<String> listPropertyNames, List<String> listResourceId, List<String> listResourceGroupId, StringBuffer errMsg ) {		
		
		String tableName = CommandServerRequestBuilder.GetTableNameOfPerUnit(DicOrderAdd.AmassPreFix_Resource, perUnit);
		
//    	String groupBy 		= CommandServerRequestBuilder.GetGroupBy(DicOrderAdd.AmassPreFix_Resource,listAggreUnit);
//    	List<String> listAggreUnitQuery	= CommandServerRequestBuilder.GetColumnNameOfAggreUnit(DicOrderAdd.AmassPreFix_Resource,listAggreUnit);

//    	String query_AggreUnit = "";
//		Iterator<String> iterAggreUnitQuery =  listAggreUnitQuery.iterator();
//		while(iterAggreUnitQuery.hasNext()) {
//			query_AggreUnit += " , " + iterAggreUnitQuery.next();
//		}
		
		String inQueryProperty = CommonUtil.getInQueryFromList(listPropertyNames);
		String inQueryResourceId = CommonUtil.getInQueryFromList(listResourceId);
		String inQueryResourceGroupId = CommonUtil.getInQueryFromList(listResourceGroupId);

		String query = "SELECT	AggregatedTime,	AgentName ,	CommandName ,	ResourceID ,	PropertyName\n"
							  + ",	PropertyAvgValue ,	PropertyMinValue ,	PropertyMaxValue ,	PropertyValueUnit \n"	
							  + " FROM	" + tableName + " WHERE	AggregatedTime Between ? AND ?      \n"
							  + " AND 	AgentName = ?              			\n"
							  + " AND 	CommandName	in ("+inQueryResourceGroupId+")     \n"
							  + " AND 	ResourceID	in ("+inQueryResourceId+")          \n"
							  + " AND 	PropertyName in ("+inQueryProperty+")           \n"
							  + " ORDER BY	AggregatedTime, AgentName,    CommandName, ResourceID, PropertyName       \n";

		PreparedStatement prep						= null;
		ResultSet rs 										= null;
		Connection con = null;
	  	List<List<String>> result = new ArrayList<List<String>>();

	  	try {
			if (db.open(DBProperties.dbType_Resource) == false) {
				errMsg.append("DB Conn Error");
				return null;
			}
			
			con = db.getConnection(DBProperties.dbType_Resource);
		
			prep = con.prepareStatement(query);
			prep.setString(1, rangeStart );
			prep.setString(2, rangeEnd );
			prep.setString(3, agentName );
			
			LOG.trace("QUERY: " + query);
			rs = prep.executeQuery();
			
			int rsCnt =0;
		  	
			while(rs.next()) {
				rsCnt++;
				// column
				if (rsCnt == 1) {
				  	List<String> column = new ArrayList<String>();
				  	
				  	column.add(DicOrderAdd.AggregatedTime);
				  	column.add(DicOrderAdd.AgentName);
				  	column.add("CommandName");
				  	column.add("ResourceID");
				  	column.add("PropertyName");
				  	if (listAggreUnit.contains(CommandServerRequestBuilder.aggreUnitAvg))
				  		column.add("PropertyAvgValue");
				  	if (listAggreUnit.contains(CommandServerRequestBuilder.aggreUnitMin))
				  		column.add("PropertyMinValue");
				  	if (listAggreUnit.contains(CommandServerRequestBuilder.aggreUnitMax))
				  		column.add("PropertyMaxValue");
				  	column.add(DicOrderAdd.PropertyValueUnit);
				  	
					result.add(column);
				}

				// data
				List<String> row = new ArrayList<String>();
				
				row.add(rs.getString(DicOrderAdd.AggregatedTime));
				row.add(rs.getString(DicOrderAdd.AgentName));
				row.add(rs.getString("CommandName"));
				row.add(rs.getString("ResourceID"));
				row.add(rs.getString("PropertyName"));
			  	if (listAggreUnit.contains(CommandServerRequestBuilder.aggreUnitAvg))
			  		row.add(rs.getString("PropertyAvgValue"));
			  	if (listAggreUnit.contains(CommandServerRequestBuilder.aggreUnitMin))
			  		row.add(rs.getString("PropertyMinValue"));
			  	if (listAggreUnit.contains(CommandServerRequestBuilder.aggreUnitMax))
			  		row.add(rs.getString("PropertyMaxValue"));
				row.add(rs.getString(DicOrderAdd.PropertyValueUnit));
				
				result.add(row);

			} //End While
			
			rs.close();
			prep.close();
			
		} catch (Exception e) {
		
			LOG.error( query );
			e.printStackTrace();
			errMsg.append(e.getMessage());
			return null;
		}
		finally{
			if(rs != null) 		try {rs.close();}		catch (SQLException e) {e.printStackTrace();}
			if(prep != null ) 	try {prep.close();}	catch (SQLException e) {e.printStackTrace();}
			if (con != null)
				db.releaseConnection(DBProperties.dbType_Resource, con);
		}
	  	
		return result;
	}
	
}
