
package com.speno.xmon.util;

// Java stuff
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
// xvarm

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.db.DBProperties;

// Archive agent
public class DbConnectionMngr{
	private final static Logger LOG = LoggerFactory.getLogger(DbConnectionMngr.class);
	
	private Hashtable	m_pools = new Hashtable();
	
	private boolean autoCommit = false;
	
	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	// Do-Nothing Constructor
	public DbConnectionMngr(){
	}

	// Starts
	public boolean start(){
		dbPool	p;
		boolean bOk = true;

		// Start each pool
	    for (Enumeration e = m_pools.elements() ; e.hasMoreElements() ;){
	    	p = (dbPool)e.nextElement();
	    	bOk = p.start();
	    	if (!bOk)
	    		break;
	    }
		return bOk;
	}

	// Stops
	public void stop(){
		dbPool	p;

		// Stop each pool
	    for (Enumeration e = m_pools.elements() ; e.hasMoreElements() ;){
	    	p = (dbPool)e.nextElement();
	    	p.stop();
	    }
	}

	// Make status list for each possible connection
	public List getStatus(){
		List l = new ArrayList();
	    for (Enumeration e = m_pools.elements() ; e.hasMoreElements() ;){
	    	dbPool p = (dbPool)e.nextElement();
	    	p.addConnectStatus(l);
	    }
	    return l;
	}

	// Make config list for each pool
	public List getPoolConfig(){
		List l = new ArrayList();
	    for (Enumeration e = m_pools.elements() ; e.hasMoreElements() ;){
	    	dbPool p = (dbPool)e.nextElement();
	    	List row = new ArrayList();
			p.addPoolConfig(row);
			l.add(row);
		}
		return l;
	}

	// Add a pool
	// 20140612, junsoo, connection property �߰�
	public void addPool(String name, String driver, Vector cons, String user, String pswd,
										String count, String precon, String unicode, String dbType, String maxTrans, String prefix,
										String connectionProperties, String connectionTimeout){
		dbPool p = new dbPool(name, driver, cons, user, pswd, count, precon, unicode, dbType, maxTrans, prefix, connectionProperties, connectionTimeout);
		System.out.println("add pool");
		m_pools.put(name, p);
	}

	// Gets a connection
	public synchronized Connection getConnect(String name, StringBuffer fatalErr){
		dbPool p = (dbPool)m_pools.get(name);
		if (p != null){
			// May return null if none available
			//	Return null and fatalErr set if error with database
			return p.getConnect(fatalErr);
		}
		
		else{
			fatalErr.append("Invalid connect pool");
			return null;
		}
	}

	// Releases a connection
	public void releaseConnect(String name, Connection con, int lastRet){
		dbPool p = (dbPool)m_pools.get(name);
		if (p != null){
			p.releaseConnect(con, lastRet);
		}
	}

	// Get unicode flag for a pool
	public boolean isUnicodeConnect(String name){
		dbPool p = (dbPool)m_pools.get(name);
		if (p != null)
			return p.m_unicode;
		else
			return false;
	}

	// Gets the database type for a pool
	public int getDbType(String name){
		dbPool p = (dbPool)m_pools.get(name);
		if (p != null)
			return p.m_dbType;
		else
			return 1;
	}

	public String getDbTablePrefix(String s){
        dbPool pool = (dbPool)m_pools.get(s);
        if(pool != null)
            return pool.m_prefix;
        else
            return "";
    }
	
	// Gets the database type for a pool
	public boolean hadDeadlock(String pool, String errMsg){
		boolean ret = false;
		dbPool p = (dbPool)m_pools.get(pool);
		if (p != null){
			// SQLServer
			int pos = -1;
			if (p.m_dbType == 1)
				pos = errMsg.indexOf("deadlock victim");
			// Oracle, Informix, Tibero
		    else if (p.m_dbType == 2 || p.m_dbType == 7 || p.m_dbType == 8)
				pos = errMsg.indexOf("deadlock detected");
			// DB2
			else if (p.m_dbType == 3)
				pos = errMsg.indexOf("deadlock or timeout");
			// Sybase, Altibase
			else if (p.m_dbType == 4 || p.m_dbType == 6)
				pos = errMsg.indexOf("deadlock situation");
			// MySQL
			else if (p.m_dbType == 5)
				pos = errMsg.indexOf("deadlock");

			if (pos > 0)
				ret = true;
		}
		return ret;
	}

	// See if any pool has connect problems
	public boolean isPoolConnectProblem(){
	    for (Enumeration e = m_pools.elements() ; e.hasMoreElements() ;){
	    	dbPool p = (dbPool)e.nextElement();
	    	if (p.m_connectError)
	    		return true;
	    }
	    return false;
	}


	/*************************************************************
		A single pool
	**************************************************************/
	private class dbPool{
		private static final int	READY = 0;
		private static final int	INUSE = 1;
		private static final int	NOINIT = 2;

		// Base info on the connect
		private String	m_name="p1";
		private String	m_driver="";
		private Vector	m_cons=null;
		private String	m_user="";
		private String	m_pswd="";
		private int		m_count=1;
		private boolean	m_precon=false;
		public  boolean m_unicode=false;
		public	int	m_dbType=1;
	    public String m_prefix;
		public  int     m_maxTrans=0;
		public	boolean	m_connectError = false;

	    private String connectionProperties;
	    private int connectionTimeout;

		
		// Connection details
		private int			m_state[];
		private Connection	m_con[];
		private int			m_useCount[];
		private int			m_transCount[];


		// Collect details
		public dbPool(String name, String driver, Vector cons, String user, String pswd,
									String count, String precon, String unicode, String dbType, String maxTrans, String prefix,
									String connectionProperties, String connectionTimeout){
			if (name != null)		m_name = name;
			if (driver != null)		m_driver = driver;
			if (cons != null)	    m_cons = cons;
			if (user != null)		m_user = user;
			if (pswd != null)		m_pswd = pswd;
			if (count != null)		m_count = Integer.parseInt(count);
			if (precon != null && precon.equals("TRUE"))
				m_precon = true;
			if (unicode != null && unicode.equals("TRUE"))
				m_unicode = true;
			if (dbType != null && dbType.length() > 0)
				m_dbType = Integer.parseInt(dbType);
			if (maxTrans != null && maxTrans.length() > 0)
				m_maxTrans = Integer.parseInt(maxTrans);
			
			m_prefix = prefix;
			
			this.connectionProperties = connectionProperties;
			try {
				this.connectionTimeout = Integer.parseInt(connectionTimeout);
			} catch (Exception e) {
			}
		}

		// Gets a connection
		public boolean start(){
			boolean bOk = true;

			// Make arrays
			m_state = new int[m_count];
			m_con = new Connection[m_count];
			m_useCount = new int[m_count];
			m_transCount = new int[m_count];
			for (int i = 0; i < m_count; i++){
				m_state[i] = NOINIT;
				m_con[i] = null;
				m_useCount[i] = 0;
				m_transCount[i] = 0;
			}

			// Pre-connect if configured
			if (m_precon){
				for (int i = 0; i < m_count; i++){
					Connection con = makeConnect();
					if (con != null){
						m_state[i] = READY;
						m_con[i] = con;
					}
					else{
						// No need to go on
						bOk = false;
						break;
					}
				}
			}
			
			// TODO:
//			lock = new ReentrantLock();
			return bOk;
		}

		// Stop or unconnect
		public void stop(){
			// Close each connection if open
			for (int i = 0; i < m_count; i++){
				if (m_state[i] == READY || m_state[i] == INUSE){
					Connection con = m_con[i];
					try{
						// junsoo, 20100217, con is null in case for deadlock.
						if (con != null)
							con.close();
					} catch (SQLException se){
						LOG.error("Error closing connection to database." +" Driver=" + m_driver + ",Connect=" + makeConDisplay(),se);
					}
				}
				m_state[i] = NOINIT;
				m_con[i] = null;
			}
		}

		// Gets an available connection
//		private Lock lock;
		private long etime;
		public synchronized Connection getConnect(StringBuffer fatalErr){
			Connection ret = null;
			
//			List list = new ArrayList();
//			addConnectStatus(list);
//			System.out.println("[BEFORE] " + list);
//			LOG.debug("++++++++++++++++++++++++ " + m_name + " +++++++++++++++++++++++");
			
			long start = System.currentTimeMillis();

			if (m_dbType == DBProperties.SQLITE) {
				if (m_con[0] == null)
					m_con[0] = makeConnect();
				ret = m_con[0];
				m_state[0] = INUSE;
				m_useCount[0] = m_useCount[0] + 1;
				m_transCount[0] = m_transCount[0] + 1;
			}
			else {
				// First find one that's ready - already connected
				for (int i = 0; i < m_count; i++){
					if (m_state[i] == READY){
						ret = m_con[i];
						m_state[i] = INUSE;
						m_useCount[i] = m_useCount[i] + 1;
						m_transCount[i] = m_transCount[i] + 1;
						break;
					}
				}
				// If not, try to make one ready
				if (ret == null){
					for (int i = 0; i < m_count; i++){
						if (m_state[i] == NOINIT){
							ret = makeConnect();
							if (ret != null){
								m_con[i] = ret;
								m_state[i] = INUSE;
								m_useCount[i] = m_useCount[i] + 1;
								m_transCount[i] = m_transCount[i] + 1;
							}
							else{
								fatalErr.append("Error generating connection");
							}
							break;
						}
					}
				}
			}
					
			if (ret == null) {			
				tempcount++;
				ret = makeConnect();				
				LOG.info("temp connection maked. "
						+ "remove want this message than increase pool size."
						+"current size" + (m_count + tempcount));
			}		
			etime = System.currentTimeMillis();
			return ret;
		}
		private int tempcount = 0;

		// Releases a connection
		public synchronized void releaseConnect(Connection con, int lastError){
//			LOG.debug("-------------------- " + m_name + " --------------------");
			if (System.currentTimeMillis()- etime > 3000) {
				try {
					throw new Exception(m_name + " slow!!!!!!");
				} catch (Exception e) {
					//yys 디비 작업이 오래 걸리는 현상이므로 장애 아님
					//다만 시간을 출력할 필요가 있음
					long time = System.currentTimeMillis()- etime;
					LOG.debug("DB work is too slow. working time for this "	+ time + "ms."	+m_name);
					//e.printStackTrace();
				}
			}
			etime = System.currentTimeMillis();
			
			if (m_dbType == DBProperties.SQLITE) {
				m_state[0] = READY;
				return;
			}

			
			// Find the slot and change the status
			boolean bFound = false;
			for (int i = 0; i < m_count; i++){
				if (m_con[i] == con){
					// No connect error, just set to ready for reuse
					if (lastError == 0){
						if (m_maxTrans > 0 && m_transCount[i] >= m_maxTrans){
							m_transCount[i] = 0;
							// Undo
							try{
								con.close();
							}
							catch (SQLException se){}
							m_con[i] = null;
							m_state[i] = NOINIT;
						}
						else{
						    m_state[i] = READY;
						}
					}
					else{
						// Undo
						try{
							con.close();
						}
						catch (SQLException se){}
						m_con[i] = null;
						m_state[i] = NOINIT;
					}
					bFound = true;
					break;
				}
			}
			
			if (!bFound) {
				try {
					LOG.info("temp connection relese");
					con.close();
					tempcount--;
				} catch (SQLException e) {					
					e.printStackTrace();
				}
			}
			
//			if (m_dbType == DBProperties.SQLITE) {
//				try {
//					notifyAll();
//				} catch (Exception e) {
//					// TODO: handle exception
//				}
//			}
		}

		// Add status lines for these connections
		public void addConnectStatus(List l){
			for (int i = 0; i < m_count; i++){
				List row = new ArrayList();
				row.add(m_name);
				if (m_state[i] == READY)
					row.add("Ready");
				else if (m_state[i] == INUSE)
					row.add("Processing");
				else
					row.add("No Connect");
				row.add(Integer.toString(m_useCount[i]));
				l.add(row);
			}
		}

		// Add a status line for the pool configuration
		public void addPoolConfig(List row){
			row.add(m_name);
			row.add(m_driver);
			row.add(makeConDisplay());
			row.add(Integer.toString(m_count));
			if (m_unicode)
				row.add("Yes");
			else
				row.add("No");
			if (m_dbType == 1)
				row.add("SQLServer");
			else if (m_dbType == 2)
				row.add("ORACLE");
			else if (m_dbType == 3)
				row.add("DB2");
			else if (m_dbType == 4)
				row.add("Sybase");
			else if (m_dbType == 5)
				row.add("MySQL");
			else if (m_dbType == 6)
				row.add("Altibase");
			else if (m_dbType == 7)
				row.add("Informix");
			else if (m_dbType == 8)
				row.add("Tibero");
			else
				row.add("???");
		}

		// Makes connection String from Vector for display/error messages
		public String makeConDisplay(){
			StringBuffer ret = new StringBuffer();
			for (int i = 0; i < m_cons.size(); i++){
				if (ret.length() > 0)
					ret.append(" ## ");
				ret.append((String)m_cons.elementAt(i));
			}
			return ret.toString();
		}

		// Make a connection
		private synchronized Connection makeConnect(){
		    for (int i = 0; i < m_cons.size(); i++){
				String strCon = (String)m_cons.elementAt(i);
				try{
					// Load driver
					Class.forName(m_driver);
					
					// 20140612, junsoo, connection property �߰�
					java.util.Properties info = null;
					if (connectionProperties != null && connectionProperties.length() > 0) {
						String[] properties = connectionProperties.split(";");
						if (properties != null) {
							for (int j = 0; j < properties.length; j++) {
								if (properties[j] == null || properties[j].length() < 1)
									continue;
								
								int colindex = properties[j].indexOf('=');
								String key = properties[j].substring(0, colindex);
								String value = properties[j].substring(colindex+1);
								if (info == null)
									info = new java.util.Properties();
								info.put(key, value);
								
//								info.put ("oracle.jdbc.ReadTimeout","5000");	
//								info.put ("oracle.net.CONNECT_TIMEOUT","5000");
							}
						}
						
						if (info != null) {
							info.put ("user", m_user);
							info.put ("password",m_pswd);
						}
					}
					
					// Dabby, 5/12/06
					Connection con = null;
					
					if (connectionTimeout != 0)
						DriverManager.setLoginTimeout(connectionTimeout);
					else
						DriverManager.setLoginTimeout(5 * 60);		// default : 5 minitues
					
					if(m_dbType == 7)
						con = DriverManager.getConnection(strCon);
					//yys 추가 connetion info 추가
					else if (m_dbType == DBProperties.SQLITE) {
						Properties config  = new Properties();
						config.setProperty("SynchronousMode", "OFF");
						config.setProperty("SynchronousMode", "MEMORY");
						config.setProperty("TempStore", "MEMORY");
						con = DriverManager.getConnection(strCon,config);
						con.setTransactionIsolation(con.TRANSACTION_READ_UNCOMMITTED);
					}
					else {
						if (info == null)
							con = DriverManager.getConnection(strCon, m_user, m_pswd);
						else 
							con = DriverManager.getConnection(strCon, info);
					}
					
					// init
					if (m_dbType == DBProperties.SQLITE) {
						//무의미함 sqlite에서는 안먹음 yys
						/*SQLiteConfig config = new SQLiteConfig();
						config.set
						String setting = "PRAGMA synchronous=OFF;\n";	
						//동시성 타임아웃 핸들링(느린 CPU를 IO를 위함) yys					
						setting += "PRAGMA busy_timeout=60000;\n";					
						boolean ok = con.prepareStatement(setting).execute();
						if(ok)
						System.out.println("sqlite setting inited..");
						//con.commit();*/
					}
					con.setAutoCommit(autoCommit);

					// Sybase options
					if (m_dbType == 4){
						Statement s = con.createStatement();
						int rowsAffected = s.executeUpdate("SET CHAINED OFF");
						s.close();
					}

					// No error yet
					m_connectError = false;
					return con;
				}
				catch (Exception ex){
					ex.printStackTrace();
					LOG.error("Error connecting to database. "+ "Driver=" + m_driver + ",Connect=" + strCon +", Class " + ex.getClass().getName(), ex);
				}
			}
			m_connectError = true;
			return null;
		}


	}


}