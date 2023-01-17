package com.speno.xmon.server;

import java.sql.Connection;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.speno.xmon.db.DBProperties;

//import snaq.db.ConnectionPool;


public class DBConnection {
	public static void main(String[] args)    
	{
		long availableProcessors =  Runtime.getRuntime().availableProcessors();
		System.out.println(availableProcessors);
		
		String query ="SELECT sum(SuccessCount)  as cnt FROM XmAggregatedActionSec ";
		
		try {
			long t0 = System.currentTimeMillis();
			DBProperties db = DBProperties.getInstance();
			
			for(int i =0 ; i <10000;i++)
			{
				Connection con_org = db.getConnection(DBProperties.dbType_Action);//DbProperties.getConnection();
				
				PreparedStatement prep_org = con_org.prepareStatement(query);
				ResultSet rs_org = prep_org.executeQuery();
				
				int cnt =0;
				while(rs_org.next())
				{
					String c = rs_org.getString("cnt");
					System.out.println(i + " : " +c);
				}
				prep_org.close();
				//DbProperties.close();
				
			}
	        long t1 = System.currentTimeMillis();

	        System.out.println("Sent messages delay : " + (t1 - t0)); 
	        //DbProperties.close(); //46631, 46985
	        //                                 13244, 13326, 13434
	        //                                 13467, 13389
			
			/*
			DBConnection dbcondddd = DBConnection.getInstance();
			Connection con = dbcondddd.openConnection();
			con.setAutoCommit(true);
			PreparedStatement prep = con.prepareStatement(query);
	
			ResultSet rs = prep.executeQuery();
	
			prep.close();
	
			int cnt =0;
			while(rs.next())
			{
				String c = rs.getString("cnt");
				System.out.println(c);
			}
			*/
			
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
    private static DBConnection _dbSingleton = null;
    //private static ConnectionPool _pool = null;
    private long _idleTimeout;
    private boolean _flag = true; //true: connection open, false: bad or no connection
 
    /** A private Constructor prevents any other class from instantiating. */
    private DBConnection() {
        Class<?> c = null;
        try {
            c = Class.forName("org.sqlite.JDBC");
        } 
        catch (ClassNotFoundException e) {
            _flag = false;
        }
 
        Driver driver = null;
        try {
            driver = (Driver)c.newInstance();
            DriverManager.registerDriver(driver);
        }
        catch (SQLException e) {
            _flag = false;
        }
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (_flag) {
            String name = "Local"; // Pool name.
            int minPool = 1; // Minimum number of pooled connections, or 0 for none.
            int maxPool = 3; // Maximum number of pooled connections, or 0 for none.
            int maxSize = 10; // Maximum number of possible connections, or 0 for no limit.
            _idleTimeout = 30; // Idle timeout (seconds) for idle pooled connections, or 0 for no timeout.
            String url = "jdbc:sqlite:D:\\DEV_ENV\\workspace3\\xMonitorAction.db"; // JDBC connection URL.
            String username = "";//"senecaBBB"; // Database username.
            String password = "";//"db"; // Password for the database username supplied.
            try {
                //_pool = new ConnectionPool(name, minPool, maxPool, maxSize, _idleTimeout, url, username, password);
            }
            finally {
                //_pool.registerShutdownHook(); 
            }
        }
    }
     
    public Connection openConnection() {
        Connection conn = null;
      //  try {
          //  conn = _pool.getConnection(_idleTimeout);
            _flag = true;
     //   } 
       // catch (SQLException e) {
            _flag = false;
       // }
        return conn;
    }
 
    /** Static 'instance' method */
    public static DBConnection getInstance() {
        if (_dbSingleton == null) {
            _dbSingleton = new DBConnection();
        }
        return _dbSingleton;
    }
 
    public boolean getConnectionStatus() {
        return _flag;
    }
}
