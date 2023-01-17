package com.speno.xmon.listener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.dataset.ItemMap_XmAmassTrans;
import com.speno.xmon.db.DataInserterAction;
import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.listener.pool.ThreadPoolMngr;
import com.speno.xmon.pcf.CodecFactory;
import com.speno.xmon.pcf.CodecFilter;

/**
 * The class that will accept and process clients in order to properly
 * track the memory usage.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 * ResponseOrderAgent
 */
public class AmassServer {

	private final static Logger LOG = LoggerFactory.getLogger(AmassServer.class);
	//public static  xMThreadPool_AmassReceived<AmassServerThread>  pool_AmassReceived ;
	
	private ConcurrentMap<String, ItemMap_XmAmassTrans> amassTrans = new ConcurrentHashMap<String, ItemMap_XmAmassTrans>();
	//private ConcurrentMap<String, ItemMap_XmAmassTrans> MapAmass_Trans = new ConcurrentHashTable<String, ItemMap_XmAmassTrans>();
	//private ConcurrentMap<String, ItemMap_XmAmassTrans> MapAmass_Trans2 = new ConcurrentHashMap<String, ItemMap_XmAmassTrans>();

	public AtomicInteger tempInitCnt 		= new AtomicInteger(0);
	public AtomicInteger tempErrCnt 		= new AtomicInteger(0);
	
	private ThreadPoolMngr workerThreadPool = null; 
	
	private TimeoutThread timeoutThread = null;
	
	private DataInserterAction dataAction = null;
	
	private static AmassServer single;
	
	public static AmassServer getInstance() {
		if (single == null) {
			try {
				single = new AmassServer();
			} catch (Exception e) {
				return null;
			}
		}
		return single;
	}
	
	public ThreadPoolMngr getWorkerPool() {
		return workerThreadPool;
	}
	
    public AmassServer() throws Exception {
    	try{
	        NioSocketAcceptor acceptor = new NioSocketAcceptor(4);
	        acceptor.setHandler(new AmassServerHandler(this));

	        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();

	        if(xmPropertiesXml.useProtocolCodecFilter) {
		        chain.addFirst("protocol", new CodecFilter(new CodecFactory(Charset.forName("UTF-8"),LineDelimiter.AUTO,false,true)));			
	        }
	        
	        int poolSize = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.Listener_AmassServerThreadInitPoolSize);

	        ExecutorService executor = new OrderedThreadPoolExecutor(3, poolSize, 180, TimeUnit.SECONDS);
	        chain.addLast("threadPool", new ExecutorFilter(executor));
	        
//	        chain.addLast("executor1", new ExecutorFilter(3, 10, 60, TimeUnit.SECONDS));
	        
	        acceptor.setReuseAddress(true);
	        
	        SocketSessionConfig acceptorConfig = acceptor.getSessionConfig();
	        acceptorConfig.setReadBufferSize( 1024*1024*8 );
	        acceptorConfig.setReceiveBufferSize(1024*1024*8);
	        acceptorConfig.setTcpNoDelay(true);

	        LoggingFilter loggingFilter = new LoggingFilter("logger");
	        loggingFilter.setMessageReceivedLogLevel	(LogLevel.NONE);
	        loggingFilter.setMessageSentLogLevel		(LogLevel.NONE);
			loggingFilter.setSessionIdleLogLevel		(LogLevel.NONE);
			loggingFilter.setSessionCreatedLogLevel		(LogLevel.NONE);
			loggingFilter.setSessionOpenedLogLevel		(LogLevel.NONE);
			loggingFilter.setSessionClosedLogLevel		(LogLevel.NONE);
			loggingFilter.setExceptionCaughtLogLevel	(LogLevel.NONE);
			
	        chain.addLast("logger", loggingFilter);
	        
	        String ip = xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.Listener_AmassServerName);
	        int port = Integer.parseInt( xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.Listener_AmassServerPort));

	        LOG.info("=	@Command TCP Server listening on port " + xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.Listener_AmassServerPort));
	        InetSocketAddress sockAddress = null;
	        if (ip.length() > 0) {
	        	sockAddress = new InetSocketAddress(ip, port);
	        } else {
	        	sockAddress = new InetSocketAddress(port);
	        }
	        acceptor.bind(sockAddress);	      
	        LOG.info("	=	@Amass TCP Server2 listening on port " + xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.Listener_AmassServerPort));

	        ///////////////
//			UDP acceptor do not use now
//	        NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
//	        DatagramSessionConfig datagramSessionConfig = acceptor.getSessionConfig();
//	        datagramSessionConfig.setReadBufferSize( 32768  );
//
////			ExecutorService executor = new OrderedThreadPoolExecutor(3, 10, 30, TimeUnit.SECONDS);
////			acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(executor)); 
//
//	        
//	        acceptor.setHandler(new AmassServerHandler(this));
//	        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
//		    
//	        if(xmPropertiesXml.useProtocolCodecFilter)
//	        {
//		       	CodecFactory fac = new CodecFactory(Charset.forName("UTF-8"), LineDelimiter.AUTO,false, true);
//		        ProtocolCodecFilter protocol = new ProtocolCodecFilter(fac);
//		        
//		        chain.addFirst("protocol", protocol);
//	        }
//	        
//	        LoggingFilter loggingFilter = new LoggingFilter();
//			loggingFilter.setMessageReceivedLogLevel	(LogLevel.NONE);
//			loggingFilter.setMessageSentLogLevel			(LogLevel.NONE);
//			loggingFilter.setSessionIdleLogLevel			(LogLevel.NONE);
//			loggingFilter.setSessionCreatedLogLevel		(LogLevel.NONE);
//			loggingFilter.setSessionOpenedLogLevel		(LogLevel.NONE);
//			loggingFilter.setSessionClosedLogLevel		(LogLevel.NONE);
//	        chain.addLast("logger", loggingFilter);
//	        
//	        InetSocketAddress sockAddress = null;
//
//	        String ip = xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.Listener_AmassServerName);
//	        int port = Integer.parseInt( xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.Listener_AmassServerPort));
//	        if (ip.length() > 0) {
//	        	sockAddress = new InetSocketAddress(ip, port);
//	        } else {
//	        	sockAddress = new InetSocketAddress(port);
//	        }
//	        
//	        acceptor.bind(sockAddress);
//	        LOG.info("------------------------------------------------------------------------------------------------");
//	        LOG.info("	=	@Amass UDP Server listening on port " + xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.Listener_AmassServerPort));
 	        
	        // init pool
	        if (workerThreadPool == null) {
	        	workerThreadPool = new ThreadPoolMngr("TCP-2",poolSize);
	        }
	        
	        // 20141022, junsoo, init timeout thread
		    timeoutThread = new TimeoutThread();
		    timeoutThread.setTimeout(xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.TransactionTimeout));
		    timeoutThread.setWaitTime(xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.TransactionTimeoutWaitTime));
    		Thread t = new Thread(timeoutThread, "TransTimeoutThread");
			t.start();
			
			if(dataAction == null)
				dataAction		= new DataInserterAction();

    	} catch(Exception e) {
    		LOG.error(e.getMessage(), e);    		
    		throw e;
    	}
    }
    public  ItemMap_XmAmassTrans getOrCreate(String agentNameNtransactionID, ItemMap_XmAmassTrans queue_xmTrans) 
	{
		ItemMap_XmAmassTrans rec = this.amassTrans.get(agentNameNtransactionID);
	    if (rec == null) 
	    {
	        rec = this.amassTrans.putIfAbsent(agentNameNtransactionID, queue_xmTrans);
	        if (rec == null) rec = queue_xmTrans;
	    }
	    return rec;
	} 
	public   boolean MapAmassTrans_Create(String agentNameNtransactionID, ItemMap_XmAmassTrans queue_xmTrans) 
	{
	    if (this.amassTrans.get(agentNameNtransactionID) != null)
	    {
	    	LOG.error("AmassData already exists :" + agentNameNtransactionID);
	    	return true;
	    }
    	int a =  this.amassTrans.size();
    	this.amassTrans.putIfAbsent(agentNameNtransactionID, queue_xmTrans);
    	if(this.amassTrans.get(agentNameNtransactionID) != null) 
    	{
	    	LOG.debug("MapAmassTrans_Create registered size : "  +   this.amassTrans.size() + "ID: " + agentNameNtransactionID);
    		return true;
    	}
    	else {
    		return false;
    	}
	}
	
	public ConcurrentMap<String, ItemMap_XmAmassTrans> getAmassTrans() {
		return amassTrans;
	}
	
	//private AtomicInteger tempGetCnt 		= new AtomicInteger(0);
	//private AtomicInteger tempPutCnt 		= new AtomicInteger(0);
	public  ItemMap_XmAmassTrans MapAmassTrans_Get(String tempkey)  
	{
 	//	long dd = Long.parseLong( tempkey.split("&")[1].substring(1));
 	//	LOG.info( tempGetCnt.incrementAndGet() + "SSSGet:"+ this.MapAmass_Trans.size() + " : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(dd)) + "->" + tempkey );
		//xtorm04&T1410943583034
		ItemMap_XmAmassTrans item = this.amassTrans.get(tempkey);
	
//		if(item == null)
//		{
//			try {
//				int cnt =0;
//				while(item == null)
//				{
//					cnt++;
//					Thread.sleep(15);
//					item = this.amassTrans.get(tempkey);
//					if(cnt > 5) break; 
//				}
//				
//			}
//			catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		return item;
	}
	public int MapSize() {
		return amassTrans.size();
	}
	
	public boolean MapAmassTransRemove(String tempkey) {
		ItemMap_XmAmassTrans trans = this.amassTrans.remove(tempkey);
		if(trans == null){
			LOG.warn("No specified trans exists : " + tempkey);
			return false;
		} else {
			return true;
		}
	}
	
	public boolean MapAmassTransRemove(String tempkey, ItemMap_XmAmassTrans itemExisting) {
		//System.out.println( "S:"  +itemExisting.GetAggreServerTime() + " Com:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(itemExisting.GetTransCompleteTime())));
		//int a = this.MapAmass_Trans.size();
		if(!this.amassTrans.remove(tempkey,  itemExisting )){
			return false;
		} else {
			return true;
		}
	}
	
	public boolean insertTrans(String key, ItemMap_XmAmassTrans itemExisting) {
//		  dbInsertReCnt.set(0);
		int count = 0;
		while (true) {
			// if(dbInsertReCnt.incrementAndGet() >3) break;
			if(++count > 3) break;
			if (dataAction.Insert_XmTransAction(
					itemExisting.GetAgentName(),
					itemExisting.GetActionName(),
					itemExisting.GetTransID(),
					itemExisting.GetTransInitTime(),
					itemExisting.GetTransCompleteTime(),
					itemExisting.GetTransCompleteTime() - itemExisting.GetTransInitTime(),
					itemExisting.getResult(),
					itemExisting.getExt1(),itemExisting.getExt2())
					) 
			
			{
				return true;
			} else {
				continue;
			}
		}
		//무의미한 에러 로그 제거 YYS 
		//LOG.error("MapAmass_Trans ::DB��Ͽ���, Key:" + key + ", Size:" + MapSize());
		return false;
	}

	
	// 20141022, junsoo, timeout thread
    class TimeoutThread implements Runnable {
		private boolean m_stop = false;
		private int waitTime = 1;	// 1 sec
		private int timeout = 60;	// 60 sec
		
		public void setWaitTime(int waitTime) {
			this.waitTime = waitTime;
		}

		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}

		// Close and exit
		public synchronized void terminate(){
			m_stop = true;
			notify();
		}

    	// Entry for thread
    	public void run(){
   			// Big loop
			while (!m_stop){pause();
				if (!m_stop){
					try {
						for (String key : amassTrans.keySet()) {
							ItemMap_XmAmassTrans trans = amassTrans.get(key);
							long now = System.currentTimeMillis();
							if (trans != null && (now - trans.GetTransInitTime()) > timeout*1000l) {
								int pos = key.indexOf(xmPropertiesXml.Sep);
//								String agentName = key.substring(0, pos);
//								if (trans.getTimeToInsert() < now) {
//									// NOTYET 이면 complete만 오고 init이 오지 않은 것임
//									if (!"NOTYET".equals(trans.GetActionName())) {
//										boolean success = insertTrans(key, trans, DicOrderAdd.AmassSufFix_ActionCompTimeout);
//									}
//									tempErrCnt.incrementAndGet();
//									MapAmassTransRemove(key, trans);
//									LOG.debug("set removing transaction from buffer (TIMEOUT) : " + key + " / " + MapSize() + ", " + SimDate.DateTimeFormatter_MS.format(new Date(trans.GetTransInitTime())));
//								} else {
									// set complete(timeout) time
								long completeTime = trans.GetTransInitTime() + timeout*1000l;
									trans.SetTransCompleteTime(completeTime , false);
									trans.setModifiedTimeToInsert(completeTime + ItemMap_XmAmassTrans.DELAYTIME_TO_INSERT);
									trans.setResult(DicOrderAdd.AmassSufFix_ActionCompTimeout);
//								}
							}							
							// 버퍼 쓰레기가 남을 수 있으니 다시 클리어.
							if (trans != null && trans.getTimeToInsert() != 0L && trans.getTimeToInsert() < (now - 30 * 1000L)) {
								//YYS 레벨 변경 모든 가비지 트랜잭션에 찍을 필요 없음.
								LOG.debug("BUFFER GARBAGE : " + key + " / " + MapSize() + ", "
										+ SimDate.getDateTimeFormatter_MS().format(new Date(trans.getTimeToInsert())));
								MapAmassTransRemove(key, trans);
							}

						}
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
						continue;
					}
				}
   			}
    	}
    	
		// Waits
		private synchronized void pause(){
			try{
				if(!m_stop)
					wait(waitTime*1000l);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		
    }	// class

}
