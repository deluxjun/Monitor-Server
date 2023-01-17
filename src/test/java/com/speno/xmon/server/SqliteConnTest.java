package com.speno.xmon.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/*
import snaq.db.ConnectionPool;
import snaq.db.ConnectionPoolManager;
import snaq.db.DBPoolDataSource;
*/

public class SqliteConnTest {

	void conn()
	{
		/*
		//DBPoolDataSource ds = new DBPoolDataSource();
		ds.setName("pool-ds");
		ds.setDescription("Pooling DataSource");
		ds.setDriverClassName("org.sqlite.JDBC");
		//ds.setUrl("jdbc:mysql://192.168.1.101:3306/ReplicantDB");
		ds.setUrl("jdbc:sqlite:D:\\DEV_ENV\\workspace3\\xMonitorAction.db");
		//ds.setUser("Deckard");
		//ds.setPassword("TyrellCorp1982");
		ds.setMinPool(5);
		ds.setMaxPool(10);
		ds.setMaxSize(30);
		ds.setIdleTimeout(3600);  // Specified in seconds.
		ds.setValidationQuery("SELECT COUNT(*) FROM XmAggregatedActionSec ");

	//	ConnectionPoolManager cpm = null;
		try
		{
		//    cpm = ConnectionPoolManager.getInstance();
		}
		catch (IOException ex)
		{
		    // handle exception
		}

		
		
		String poolname;
		int minpool;
		int maxpool;
		int maxsize;
		long idleTimeout;
		String url;
		String username;
		String password;
		*/
		
		/*
		ConnectionPool pool = new ConnectionPool(poolname,
		                                         minpool,
		                                         maxpool,
		                                         maxsize,
		                                         idleTimeout,
		                                         url,
		                                         username,
		                                         password);

		
		
		Connection con = pool.getConnection();
		Statement st = con.createStatement();
		ResultSet res = st.executeQuery("¡¦");
		// whatever
		res.close();
		st.close();
		con.close();
	*/
		
	}
}
