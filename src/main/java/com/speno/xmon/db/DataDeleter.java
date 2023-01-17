package com.speno.xmon.db;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataDeleter {

	private final static Logger LOG = LoggerFactory.getLogger(DataDeleter.class);
	
	private DBProperties db;
    public DataDeleter() 
    {
    	this.db = DBProperties.getInstance();
	}
    
    
public void Delete_XmExpireTransAction(String tableName, String expireDatetimeSec) {
		
		if(tableName.equals("")) return ;
		if(expireDatetimeSec.equals("")) return ; 
		
		String query =    "	    DELETE"	
							  + "   	   FROM	XmTransAction"
							  + "      WHERE	TransCompTime < ?       ";
		
		PreparedStatement prep						= null;
		Connection con = null;

		try {
			con = this.db.getConnection(DBProperties.dbType_Action);			
			prep = con.prepareStatement(query);			
			prep.setString(1, expireDatetimeSec );			
			int rsCnt  = prep.executeUpdate();
			prep.close();
			
			if(rsCnt>0)
			{
				LOG.info("Delete Count:" + rsCnt + " TableName:" + tableName + " Condition: AggregatedTime < " + expireDatetimeSec );
			}
		} catch (Exception e) {
			// java.sql.SQLException: not implemented by SQLite JDBC driver
			e.printStackTrace();
		} finally {
			if (prep != null) try {
				prep.close();
			} catch (Exception e2) {
			}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);
		}
	}
	/*
	 * 요청 리소스 집계 디비 삭제
	 */
	public void Delete_XmExpireAction(String tableName, String expireDatetimeSec) {
		
		if(tableName.equals("")) return ; 
		
		String query =    "	    DELETE"	
							  + "   	   FROM	" + tableName
							  + "      WHERE	AggregatedTime < ?       ";
		
		PreparedStatement prep						= null;
		Connection con = null;
		long start = System.currentTimeMillis();
		try {
			if (this.db.open(DBProperties.dbType_Action) == false)
			{
			}
			con = this.db.getConnection(DBProperties.dbType_Action);			
			prep = con.prepareStatement(query);
			prep.setString(1, expireDatetimeSec );
			
			int rsCnt  = prep.executeUpdate();
			
			
			if(rsCnt>0)
			{
				LOG.debug("::Deleted Count:(" + rsCnt + "), TableName:[" + tableName + "] Condition: AggregatedTime < " + expireDatetimeSec );
			}
		} catch (Exception e) {			
			e.printStackTrace();
			LOG.error("::TableName:[" + tableName + "] Condition: AggregatedTime < " + expireDatetimeSec );
		} finally {
			if (prep != null)
				try {
					prep.close();
					prep = null;
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);			
			LOG.debug("Delete_XmExpireAction time : " + (System.currentTimeMillis()-start)+ "ms");
		}
	}

	public void Delete_XmExpireResource(String tableName, String expireDatetimeSec) {
		
		if(tableName.equals("")) return ; 
		
		String query =    "	    DELETE"	
							  + "   	   FROM	" + tableName
							  + "      WHERE	AggregatedTime < ?       ";
		
		PreparedStatement prep						= null;
		Connection con = null;

		try {
			if (this.db.open(DBProperties.dbType_Resource) == false)
			{
			}

			con = this.db.getConnection(DBProperties.dbType_Resource);		
			prep = con.prepareStatement(query);

			prep.setString(1, expireDatetimeSec );
			
			int rsCnt  = prep.executeUpdate();
			
			
			if(rsCnt>0)
			{
			}
			else 
			{
			}
		} catch (Exception e) {			
			e.printStackTrace();
		} finally {
			if (prep != null) try {
				prep.close();
				prep = null;
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Resource, con);
		}
	}
}
