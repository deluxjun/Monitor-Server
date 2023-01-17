package com.speno.xmon.listener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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

import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.listener.pool.ThreadPoolMngr;
import com.speno.xmon.pcf.CodecFactory;
import com.speno.xmon.pcf.CodecFilter;

/**
 * The class that will accept and process clients in order to properly
 * track the memory usage.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class CommandServer {

	private final static Logger LOG = LoggerFactory.getLogger(CommandServer.class);
    
    public CommandServer() throws IOException {

    	try{
    		//final int processorCount = Runtime.getRuntime().availableProcessors() + 1;
    		final int processorCount =4;
    		
//	        NioSocketAcceptor acceptor = new NioSocketAcceptor(processorCount);
	        NioSocketAcceptor acceptor = new NioSocketAcceptor();
	        acceptor.setHandler(new CommandServerHandler(this));

	        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();

	        if(xmPropertiesXml.useProtocolCodecFilter)
	        {
		        chain.addFirst("protocol", new CodecFilter(new CodecFactory(Charset.forName("UTF-8"),LineDelimiter.AUTO,false,true)));			
	        }


	        ExecutorService executor = new OrderedThreadPoolExecutor(processorCount, 10, 180, TimeUnit.SECONDS);
	        chain.addLast("threadPool", new ExecutorFilter(executor));

		    //acceptor.getFilterChain().addLast("authentication", createAuthenticationIoFilter());
	        
	        acceptor.setReuseAddress(true);
	        //acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
	        
	        SocketSessionConfig acceptorConfig = acceptor.getSessionConfig();
	        acceptorConfig.setReadBufferSize( 1024*1024 );
	        acceptorConfig.setReceiveBufferSize(1024*1024);
	        //네이글 알고리즘에 따른 ACK 리턴 생성
	        acceptorConfig.setTcpNoDelay(true);


	        LoggingFilter loggingFilter = new LoggingFilter("logger");
	        loggingFilter.setMessageReceivedLogLevel	(LogLevel.NONE);
	        loggingFilter.setMessageSentLogLevel			(LogLevel.NONE);
			loggingFilter.setSessionIdleLogLevel			(LogLevel.NONE);
			loggingFilter.setSessionCreatedLogLevel		(LogLevel.NONE);
			loggingFilter.setSessionOpenedLogLevel		(LogLevel.NONE);
			loggingFilter.setSessionClosedLogLevel		(LogLevel.NONE);
			loggingFilter.setExceptionCaughtLogLevel	(LogLevel.NONE);
			
	        chain.addLast("logger", loggingFilter);
	        
	        LOG.info("=	@Command TCP Server listening on port " + xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.Listener_AmassServerReqPort));
	        acceptor.bind(new InetSocketAddress(Integer.parseInt(xmPropertiesXml.htXmPropertiesAmass_String.get(xmPropertiesXml.Listener_AmassServerReqPort))));
	           
	        // init pool
//	        if (workerThreadPool == null)
//	        {
//	        	workerThreadPool = new ThreadPoolMngr("TCP",xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.Listener_AmassServerThreadInitReqPoolSize));
//	        }
    	}
    	catch(Exception e)
    	{
    		LOG.error(e.getMessage());
    		e.printStackTrace();
    	}
    }

    protected void recvUpdate(SocketAddress clientAddr, long update) {
    	/*
    	XConsoleItem xAgent = xConsoleList.get(clientAddr);
        if (xAgent != null) {
        	xAgent.updateTextField(update);
        } else {
            System.err.println("Received update from unknown client");
        }
        */
    }
    protected void removeClient(SocketAddress clientAddr) {
       // xConsoleList.remove(clientAddr);
    }
    
	private ThreadPoolMngr workerThreadPool = null; 
	public ThreadPoolMngr getWorkerPool() {
		return workerThreadPool;
	}
}
