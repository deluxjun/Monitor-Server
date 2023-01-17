package com.speno.xmon.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.speno.xmon.pcf.CodecFactory;
import com.speno.xmon.pcf.CodecFilter;

public class MinaServer {
	private static final int PORT = 18880;

	public static void main(String[] args) throws IOException {
		SocketAcceptor acceptor = new NioSocketAcceptor(3);
		
		ExecutorService executor = new OrderedThreadPoolExecutor(3, 10, 20, TimeUnit.SECONDS);
		acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(executor)); 
		
		acceptor.setReuseAddress(true);

//		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
        chain.addLast("protocol", new CodecFilter(new CodecFactory(Charset.forName("UTF-8"),LineDelimiter.AUTO,false,true)));			

		acceptor.setHandler(new MinaServerHandler());
//		acceptor.getSessionConfig().setReadBufferSize(2048);
//		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		acceptor.bind(new InetSocketAddress(PORT));
	}
}	