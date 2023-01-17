package com.speno.xmon.db;

import java.sql.Connection;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.sqlite.SQLiteConfig;

import com.speno.xmon.util.DbConnectionMngr;

public class DBProperties {
	
	private final static Logger LOG = LoggerFactory.getLogger(DBProperties.class);

	public final static int SQLITE = 9;
	public final static int ORACLE = 2;
	
	
		public final static String dbValueOracle				= "Oracle";
		public final static String dbValueSqlite					= "Sqlite";
		
		public final static String dbType_Action 				= "Action";
		public final static String dbType_Resource 			= "Resource";
		public final static String dbType_EventLog	 		= "EventLog";
		
		private DbConnectionMngr conMngr = null;
		
		private DBProperties(){
			conMngr = new DbConnectionMngr();
			conMngr.setAutoCommit(true);
		}
		
		public void setAutoCommit(boolean autoCommit) {
			conMngr.setAutoCommit(true);
		}
		
		// start db
		public void start() {
			conMngr.start();
		}
		
		public void shutdown() {
			conMngr.stop();
		}
		
		// add pool
		public void addPool(String name, String driver, String url, String user, String pswd, String count, String preconnect, String unicode, String dbType, String prefix, String props, String timeout) {
			Vector cons = new Vector();
			cons.add(url);
			conMngr.addPool(name, driver, cons, user, pswd, count, preconnect, unicode, dbType, "", prefix, props, timeout);
		}
		
		public int getDbType(String name) {
			return conMngr.getDbType(name);
		}
		
		private static DBProperties _dbConnectionSingleton = null;
		
//		private static Connection connection_TransAction;
//	    private static Connection connection_Action;
//	    private static Connection connection_Resource;
//	    private static Connection connection_EventLog;
//	    
//	    private boolean isOpened_TransAction = false;
//	    private boolean isOpened_Action 			= false;
//	    private boolean isOpened_Resource 	= false;
//	    private boolean isOpened_EventLog	 	= false;

		public static String dbValue = "";
		
		public Connection getConnection(String dbName) {
			StringBuffer fatalErr = new StringBuffer();
			Connection conn = conMngr.getConnect(dbName, fatalErr);
			if (conn == null)
				LOG.error("Getting db connection fail : " + fatalErr.toString());
			return conn;
		}
		
		public void releaseConnection(String dbName, Connection con) {
			conMngr.releaseConnect(dbName, con, 0);
		}
		
		// 20141103, junsoo, 과거 버전 인터페이스를 위해 작성. 아무것도 하지않음.
		public boolean open(String dbType) {
			// do nothing
			return true;
		}
		
		// 20141103, junsoo, 과거 버전 인터페이스를 위해 작성. 아무것도 하지않음.
		public boolean close(String dbType) {
			// do nothing
			return true;
		}
	    
//	    public Connection getConnection(String dbType) {
//	    	if(dbType.equals(dbType_Action))
//	    	{
//		    	this.open_Action();
//				return connection_Action;
//	    	}else if(dbType.equals(dbType_Resource))
//	    	{
//		    	this.open_Resource();
//				return connection_Resource;
//	    	}else if(dbType.equals(dbType_TransAction))
//	    	{
//		    	this.open_TransAction();
//				return connection_TransAction;
//	    	}else if(dbType.equals(dbType_EventLog))
//	    	{
//		    	this.open_EventLog();
//				return connection_EventLog;
//	    	}
//	    	else
//	    		return null;
//		}
	    
//		static {
//	        try {
//	        	LOG.debug("SQLiteJDBC Class Load:" + xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbDriverURL));
//	        	
//	        	if(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbDriverURL).equals("oracle.jdbc.driver.OracleDriver"))
//	        	{
//	        		dbValue = DBProperties.dbValueOracle;
//	        	}
//	        	else if(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbDriverURL).equals("org.sqlite.JDBC"))
//	        	{
//	        		dbValue = DBProperties.dbValueSqlite;
//	        	}
//	        	
//	        	Class.forName(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbDriverURL)).newInstance();
//	        	Thread.sleep(50);
//	        } catch(Exception e) 
//	        { 
//	        	LOG.error("Error! Class.forName:" + xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbDriverURL));
//	        	e.printStackTrace(); 
//	        }
//	    }
//	    public boolean open(String dbType)
//	    {
//	    	if(dbType.equals(dbType_Action)) return this.open_Action();
//	    	else if(dbType.equals(dbType_TransAction)) return this.open_TransAction();
//	    	else if(dbType.equals(dbType_Resource)) return this.open_Resource();
//	    	else if(dbType.equals(dbType_EventLog)) return this.open_EventLog();
//	    	
//	    	else return false;
//	    }
//	    private  boolean open_Action() {
//		    try {		    	
//		    	
//		        if(isOpened_Action)
//		        {
//		        	if(connection_Action != null)
//		        	if(!connection_Action.isClosed()) return true;
//		        }
//		        
//		       String DbUser 		= xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUser);
//		       String DbUserPw 	= xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUserPw);
//		       
//		       if(DbUser == null || DbUser.equals(""))
//		    	   connection_Action = DriverManager.getConnection(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUrl_Action));
//		       else
//		    	   connection_Action = DriverManager.getConnection(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUrl_Action), DbUser, DbUserPw );
//		 
//		    } 
//		    catch(SQLException e) 
//		    {
//		    	isOpened_Action = false;
//		    	e.printStackTrace(); 
//		    	return false; 
//		    }
//		 
//		    isOpened_Action = true;
//		    return true;
//		}
//	    private boolean open_Resource() {
//		    try {		    	
//		        if(isOpened_Resource)
//		        {
//		        	if(connection_Resource != null)
//		        	if(!connection_Resource.isClosed()) return true;
//		        }
//		        
//			       String DbUser 		= xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUser);
//			       String DbUserPw 	= xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUserPw);
//			       
//			       if(DbUser == null || DbUser.equals(""))
//			    	   connection_Resource = DriverManager.getConnection(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUrl_Resource));
//			       else
//			    	   connection_Resource = DriverManager.getConnection(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUrl_Resource), DbUser, DbUserPw );
//			       
//			       
//		    } catch(SQLException e) 
//		    {
//		    	isOpened_Resource = false;
//		    	e.printStackTrace(); 
//		    	return false; 
//		    }
//		    
//		    isOpened_Resource = true;
//		    return true;
//		}
//	    private boolean open_EventLog() {
//		    try {		    	
//		        if(isOpened_EventLog)
//		        {
//		        	if(connection_EventLog != null)
//		        	if(!connection_EventLog.isClosed()) return true;
//		        }
//		        
//			       String DbUser 		= xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUser);
//			       String DbUserPw 	= xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUserPw);
//			       
//			       if(DbUser == null || DbUser.equals(""))
//			    	   connection_EventLog = DriverManager.getConnection(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUrl_EventLog));
//			       else
//			    	   connection_EventLog = DriverManager.getConnection(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUrl_EventLog), DbUser, DbUserPw );
//			       
//			       
//		    } catch(SQLException e) 
//		    {
//		    	isOpened_EventLog = false;
//		    	e.printStackTrace(); 
//		    	return false; 
//		    }
//		    
//		    isOpened_EventLog = true;
//		    return true;
//		}
//	    private boolean open_TransAction() {
//		    try {		    	
//		        if(isOpened_TransAction)
//		        {
//		        	if(connection_TransAction != null)
//		        	if(!connection_TransAction.isClosed()) return true;
//		        }
//		        
//			       String DbUser 		= xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUser);
//			       String DbUserPw 	= xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUserPw);
//			       
//			       if(DbUser == null || DbUser.equals(""))
//			    	   connection_TransAction = DriverManager.getConnection(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUrl_TransAction));
//			       else
//			    	   connection_TransAction = DriverManager.getConnection(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUrl_TransAction), DbUser, DbUserPw );
//			       
//			       
//		    } catch(SQLException e) 
//		    {
//		    	isOpened_TransAction = false;
//		    	e.printStackTrace(); 
//		    	return false; 
//		    }
//		    
//		    isOpened_TransAction = true;
//		    return true;
//		}
		//private static SQLiteConfig config = null;
		/*
		public  boolean open_ReadOnly() {
		    try {
		        config = new SQLiteConfig();
		        config.setReadOnly(true);
		        //config.setSynchronous(SynchronousMode.FULL);
		        connection_Resource = DriverManager.getConnection(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.DbUrl_Action), config.toProperties());
		    } catch(SQLException e) { e.printStackTrace(); return false; }
		 
		    isOpened_Resource = true;
		    return true;
		} 
		*/		 
//		public  boolean close(String dbType) {
//			if(dbType.equals(dbType_Action))
//			{
//				return this.close_Action();
//			}
//			else if(dbType.equals(dbType_Resource))
//			{
//				return this.close_Resource();
//			}
//			else if(dbType.equals(dbType_TransAction))
//			{
//				return this.close_TransAction();
//			}
//			else if(dbType.equals(dbType_EventLog))
//			{
//				return this.close_EventLog();
//			}
//			else return false;
//		}
//		private boolean close_Action(){
//		    if(isOpened_Action == false) { return true; }
//			 
//		    try {
//		    	if(!connection_Action.isClosed()) 
//		    		connection_Action.close();
//		    	
//		        isOpened_Action = false;
//		    } catch(SQLException e) { 
//		    	e.printStackTrace(); return false; 
//		    }
//		    return true;
//		}
//		private boolean close_TransAction(){
//		    if(isOpened_TransAction == false) { return true; }
//			 
//		    try {
//		    	if(!connection_TransAction.isClosed()) 
//		    		connection_TransAction.close();
//		    	
//		        isOpened_TransAction = false;
//		    } catch(SQLException e) { 
//		    	e.printStackTrace(); return false; 
//		    }
//		    return true;
//		}
//		private  boolean close_Resource() {
//		    if(isOpened_Resource == false) { return true; }
//		 
//		    try {
//		    	if(!connection_Resource.isClosed()) 
//		    		connection_Resource.close();
//		    	
//		    	isOpened_Resource = false;
//		    } catch(SQLException e) { 
//		    	e.printStackTrace(); return false; 
//		    }
//		    return true;
//		}
//		private  boolean close_EventLog() {
//		    if(isOpened_EventLog == false) { return true; }
//		 
//		    try {
//		    	if(!connection_EventLog.isClosed()) 
//		    		connection_EventLog.close();
//		    	
//		    	isOpened_EventLog = false;
//		    } catch(SQLException e) { 
//		    	e.printStackTrace(); return false; 
//		    }
//		    return true;
//		}
		
		
	    public static DBProperties getInstance() {
	        if (_dbConnectionSingleton == null) {
	        	_dbConnectionSingleton = new DBProperties();
	        }
	        return _dbConnectionSingleton;
	    }
}
