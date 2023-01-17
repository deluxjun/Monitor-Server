package com.speno.xmon.agent;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.speno.xmon.pcf.CodecFactory;

public class AmassSender extends IoHandlerAdapter implements ILogListener {

	private static ILogger LOG = new LoggerDummy();
    private IoSession session;
    private IoConnector connector;
    private IoConnector logConnector;
    private AmassSenderGen amassSenderGen;
    private AmassSenderGen logSenderGen;
    private boolean connected = false;
    private boolean connectedLog = false;
    private boolean shutdown = false;
    
    public synchronized void shutdown() {
    	shutdown = true;
    	try {
        	if (connector != null)
        		connector.dispose();
        	if (logConnector != null)
        		logConnector.dispose();
        	notifyAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    public void setLogger(ILogger log) {
    	LOG = log;
    }
    
    /**
     * Default constructor.
     */
    public AmassSender() {
    	
    	MainXAgent.addLogListener(this);
    	
        LOG.info("Created a NioConnector");
        
    	connector = new NioSocketConnector();    
    	logConnector = new NioSocketConnector();    
    	
		connector.setHandler(this);       
		logConnector.setHandler(this);       
		DefaultIoFilterChainBuilder chain = connector.getFilterChain();					
        CodecFactory fac 						= new CodecFactory(Charset.forName("UTF-8"), LineDelimiter.AUTO,false, false);
        ProtocolCodecFilter protocol 		= new ProtocolCodecFilter(fac);
        chain.addFirst("protocol", protocol);
        
		DefaultIoFilterChainBuilder chain2 = logConnector.getFilterChain();					
        CodecFactory fac2 						= new CodecFactory(Charset.forName("UTF-8"), LineDelimiter.AUTO,false, false);
        ProtocolCodecFilter protocol2 		= new ProtocolCodecFilter(fac2);
        chain2.addFirst("protocol", protocol2);

        
//		chain.addLast("logger",  NormalUtil.removeMinaLogger(new LoggingFilter()));   
		
//		connFuture = connector.connect(new InetSocketAddress(EnvSetting.getIP(), EnvSetting.getPort()));             
//		connFuture.awaitUninterruptibly(1000);
//		
//		if(connFuture.isConnected()) {
//			client = this;
//			job = new BackGroundProgressJob("Reconnect..", client);
//			session = connFuture.getSession();  
//		}
//		else{
//			NormalUtil.errorCodeMessage("", LanguageIniter.getMessage("error.notconnect"));				
//		}

        //////////////
//        this.connector = new NioDatagramConnector();
//        /* tcp */
//        //this.connector = new NioSocketConnector();
//
//        DefaultIoFilterChainBuilder chain = this.connector.getFilterChain();
//        CodecFactory fac 						= new CodecFactory(Charset.forName("UTF-8"), LineDelimiter.AUTO,false, false);
//        ProtocolCodecFilter protocol 		= new ProtocolCodecFilter(fac);
//        chain.addFirst("protocol", protocol);
        
//        LOG.info("Setting the handler");
//        this.connector.setHandler(this);
        this.amassSenderGen = new AmassSenderGen();
        this.logSenderGen = new AmassSenderGen();
        
        Runnable r = new Runnable() {
			public void run() {

		        while(!shutdown) {
		        	
		        	try {
						// connect 되어 있지 않으면, connect
						connectIfNotConnected();
						connectLogIfNotConnected();
						
						// connect 되어 있으면, send!!
						sendIfConnected();
						sendLogIfConnected();
					} catch (Exception e) {
						
						e.printStackTrace();
					}

		        }
        
			}
		};
		
		Thread mainThread = new Thread(r);
		mainThread.start();

    }
    
    // connect
    public void connectIfNotConnected() {
    	if (connected)
    		return;
    	
        LOG.info("======== connecting to the AmassServer:" + xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AmassServerIP) 
        		+ " Port:" + xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AmassTransPort));
        ConnectFuture connFuture = connector.connect(new InetSocketAddress(xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AmassServerIP) , Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.AmassTransPort))));
        //네이글 알고리즘 OFF
        ((SocketSessionConfig) connector.getSessionConfig()).setTcpNoDelay(true);
        //재접속 시도 시간이 너무 길어 스레드 멈춤 현상 발생 제거 
        connFuture.awaitUninterruptibly(1000);
        try {
			if (connFuture.isConnected()) {	      
			    session = connFuture.getSession();
			    amassSenderGen.setSession(session);
			    // connect 직후 전송한 패킷이 유실되어 sleep함.
			    Thread.sleep(1000L);
			    connected = true;
			} else {
			    LOG.error("Not connected...exiting");
			    connected = false;
			    Thread.sleep(Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.SessionCheckRepeatMs)));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
      

    }
    
    // connect
    public void connectLogIfNotConnected() {
    	if (connectedLog)
    		return;
    	
        LOG.info("======== connecting to the AmassServer:" + xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AmassServerIP)  + " Port:" + xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AmassTransPort));
        ConnectFuture connFuture = logConnector.connect(new InetSocketAddress(xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AmassServerIP) , Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.AmassTransPort))));
        ((SocketSessionConfig) logConnector.getSessionConfig()).setTcpNoDelay(true);

        connFuture.awaitUninterruptibly(5000);
        connFuture.addListener(new IoFutureListener<ConnectFuture>() {				        	
            public void operationComplete(ConnectFuture future) {
                try {				                    
	                if (future.isConnected()) {
	                    session = future.getSession();
	                    logSenderGen.setSession(session);
	                    // connect 직후 전송한 패킷이 유실되어 sleep함.
	                    Thread.sleep(1000L);
	                    connectedLog = true;

	                } else {
	                    LOG.error("Not connected...exiting LOG server");
	                    connectedLog = false;
	                    Thread.sleep(Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.SessionCheckRepeatMs)));
	                }
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error(e.getMessage());
                }
            } //End operationComplete
        });

    }
    
    // send
    public void sendIfConnected() {
    	if (!connected)
    		return;
    	
    	try {
    		Thread.sleep( Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.TransactionRepeatMs)));
    		if(amassSenderGen.sendData(amassSenderGen.getSendingData()) == 100) {
    			connected = false;
    			return;
    		}
//    		if(amassSenderGen.sendData(amassSenderGen.getSendingLog()) == 100) {
//    			connected = false;
//    			return;
//    		}
    		if(amassSenderGen.sendData(amassSenderGen.getSendingEvent()) == 100) {
    			connected = false;
    			return;
    		}
		} catch (Exception e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
		}
    }
    
    // send
    public void sendLogIfConnected() {
    	if (!connectedLog)
    		return;    	
    	try {
    		Thread.sleep( Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.TransactionRepeatMs)));
    		if(logSenderGen.sendData(logSenderGen.getSendingLog()) == 100) {
    			connectedLog = false;
    			return;
    		}
		} catch (Exception e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
		}
    }

	@Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		LOG.error(String.valueOf(session.getId()));
		LOG.error(cause.getMessage());
        cause.printStackTrace();
        connected = false;
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
    	
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
       
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {        
        connected = false;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
      
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
       
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
       
    }
}
