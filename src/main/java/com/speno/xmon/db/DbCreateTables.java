package com.speno.xmon.db;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DbCreateTables {
	private final static Logger LOG = LoggerFactory.getLogger(DbCreateTables.class);
	
	private DBProperties db;
	public  DbCreateTables(){
	 {			 	
			this.db = DBProperties.getInstance();
			
		 List<String> Tables = new ArrayList<String>();
		 Tables.add(this.CreaeTBL_ECM_LISTS());
		 Tables.add(this.CreateTBL_ACC_CONCURRENT());
		 Tables.add(this.CreateTBL_ACC_DBPOOLS());
		 Tables.add(this.CreateTBL_ACC_THREADS());
		 Tables.add(this.CreateTBL_REC_INSERT());
		 Tables.add(this.CreateTBL_REC_SEARCH());
		 Tables.add(this.CreateTBL_CON_COUNT());
		 
		    //Connection c = null;
		    Statement stmt = null;
		    Connection con = null;
		    try {
		    	int result =-1;
		     // Class.forName("org.sqlite.JDBC");
		      //c = DriverManager.getConnection("jdbc:sqlite:" + DbProperties.DATABASE);
		      //LOG.debug("Opened database successfully");
		    	
		    	con = this.db.getConnection(DBProperties.dbType_Action);
		      stmt = con.createStatement();
		      for(int idx =0; idx < Tables.size(); idx ++ )
		      {
		    	  result = stmt.executeUpdate(Tables.get(idx));
		    	  LOG.debug("result:" +result);
		    	  
		      }
		      stmt.close();
		      this.db.close(DBProperties.dbType_Action);
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		    } finally {
				if (con != null)
					this.db.releaseConnection(DBProperties.dbType_Action, con);

		    }
		    LOG.debug("Table created successfully");
		  }
	}
	private String CreaeTBL_ECM_LISTS(){
		
		String sql = "CREATE    TABLE    TBL_AGENT_LISTS"
				+ "( AGE_IDX               INT    PRIMARY KEY  NOT NULL,"
				+ "  AGE_IP                TEXT                           NULL,"
				+ "  AGE_HOSTNAME TEXT                           NULL,"
				+ "  AGE_PORT          TEXT                           NULL,"
				+ "  AGE_DESC          TEXT                           NULL"
				+ ");";
				
		return sql;
	}
	
	/*
	 * accrue
	 */
	private String CreateTBL_ACC_CONCURRENT(){
			String sql = " CREATE    TABLE    TBL_ACC_CONCURRENT"
					+ "(AGE_IDX INT NOT NULL,"
					+ "CON_DATE  TEXT NULL,"
					+ "CON_TIME   TEXT NULL,"
					+ "CON_IP       TEXT  NULL,"
					+ "CON_HOSTNAME TEXT NULL,"
					+ "CON_DESC          TEXT NULL,"
					+ "RCV_DATETIME    TEXT NULL"
					+ ");";
			
			return sql;
	}
	private String CreateTBL_ACC_DBPOOLS(){
	       String sql = " CREATE    TABLE    TBL_ACC_DBPOOLS"
	       		+ "(AGE_IDX    INT  NOT NULL,"
	       		+ "DBP_DATE TEXT NULL,"
	       		+ "DBP_TIME  TEXT NULL,"
	       		+ "DBP_IP      TEXT  NULL,"
	       		+ "DBP_HOSTNAME TEXT NULL,"
	       		+ "DBP_DESC TEXT NULL,"
	       		+ "RCV_DATETIME   TEXT NULL"
	       		+ ");";

	       return sql;
	}
	
	private String CreateTBL_ACC_THREADS(){
		String sql = "CREATE    TABLE    TBL_ACC_THREADS"
				+ "(AGE_IDX    INT NOT NULL,"
				+ "THR_DATE   TEXT NULL,"
				+ "THR_TIME    TEXT NULL,"
				+ "THR_IP         TEXT NULL,"
				+ "THR_HOSTNAME TEXT NULL,"
				+ "THR_DESC    TEXT  NULL,"
				+ "RCV_DATETIME TEXT NULL"
				+ ");";
		
		return sql;			
	}
	
  private String CreateTBL_REC_INSERT(){
	  String sql = " CREATE    TABLE    TBL_REC_INSERT"
	  		+ "(AGE_IDX   INT  NULL,"
	  		+ "INS_DATE   TEXT NULL,"
	  		+ "INS_TIME    TEXT NULL,"
	  		+ "INS_IP         TEXT  NULL,"
	  		+ "INS_HOSTNAME  TEXT NULL,"
	  		+ "INS_SIZE     TEXT NULL"
	  		+ ");";
	  return sql;
  }
  
  private String CreateTBL_REC_SEARCH(){
	  String sql = " CREATE    TABLE    TBL_REC_SEARCH"
	  		+ "(AGE_IDX    INT   NULL,"
	  		+ "SEA_DATE    TEXT    NULL,"
	  		+ "SEA_TIME     TEXT    NULL,"
	  		+ "SEA_IP         TEXT    NULL,"
	  		+ "SEA_HOSTNAME    TEXT    NULL,"
	  		+ "SEA_SIZE    TEXT    NULL"
	  		+ ");";
	  
	  return sql;
  }
  private String CreateTBL_CON_COUNT(){
	  String sql = " CREATE    TABLE    TBL_CON_COUNT"
	  		+ "(ACCRUE_DATETIME    TEXT   NULL,"
	  		+ " CNT                             INT    NULL"
	  		+ ");";
	  
	  return sql;
  }
}