package console;
/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */


import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * An UDP client taht just send thousands of small messages to a UdpServer. 
 * 
 * This class is used for performance test purposes. It does nothing at all, but send a message
 * repetitly to a server.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class TcpClient_RequestStats_Trans extends IoHandlerAdapter {
    /** The connector */
    private IoConnector connector;

    /** The session */
    private static IoSession session;

    private boolean received = false;

    /**
     * Create the UdpClient's instance
     */
    public TcpClient_RequestStats_Trans() {
        connector = new NioSocketConnector();
        //connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
        
        connector.setHandler(this);
        SocketSessionConfig dcfg = (SocketSessionConfig) connector.getSessionConfig();
        dcfg.setTcpNoDelay(true);

        ConnectFuture connFuture = connector.connect(new InetSocketAddress("localhost", 18880));

        connFuture.awaitUninterruptibly();
 
        session = connFuture.getSession();
        session.getConfig().setUseReadOperation(true);
      	//session.getCloseFuture().awaitUninterruptibly();
    }
//(0x00000003: nio socket, server, /127.0.0.1:35969 => /127.0.0.1:18880)
    //(0x00000004: nio socket, server, /127.0.0.1:36001 => /127.0.0.1:18880)
    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
    static AtomicInteger  ai_messageReceived = new AtomicInteger(0) ;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
    	
    	
        received = true;
        
        if (message instanceof IoBuffer) {
        	ai_messageReceived.getAndIncrement();
            IoBuffer buffer = (IoBuffer) message;
            byte[] byteJsonData = new byte[buffer.remaining()];
            buffer.get(byteJsonData);
            ((IoBuffer) message).flip();

            SocketAddress remoteAddress	= session.getRemoteAddress();

            /*
            IoBuffer buffer = (IoBuffer) message;
            //String ip = remoteAddress;//xmProperties.GetAgentIP(remoteAddress);
            
            byte[] byteJsonData 					= new byte[buffer.limit()]; 
            buffer.get(byteJsonData, 0, buffer.limit());
            */
            ZIPcompress zip = new ZIPcompress();
            String msg = zip.ZipDeComp(byteJsonData);            
            //new DataInserterResource().Insert_XmAggregatedResourceSec(new RequestAgentShortHandler( ip , msg).GetItemList());
            System.out.println(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionOpened(IoSession session) throws Exception {
    }

    /**
     * The main method : instanciates a client, and send N messages. We sleep 
     * between each K messages sent, to avoid the server saturation.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        TcpClient_RequestStats_Trans client = new TcpClient_RequestStats_Trans();

        long t0 = System.currentTimeMillis();

        /*
         * {"RequestStats":
         * [{"RangeStart":"2014-07-22 15:22"
         * ,"Command":"Req_JavaHeap"
         * ,"PerUnit":"Min"
         * ,"ValueUnit":"MB"
         * ,"AggreUnit":"Avg"
         * ,"RangeEnd":"2014-07-22 16:22"}]
         * ,"RequestDateTime":"2014-07-24 11:22"
         * ,"OrderType":"RequestStats"
         * ,"TargetAgent":"xtorm01"}
         */
        JSONObject jj = new JSONObject();
        JSONArray  ja = new JSONArray();
        JSONObject joSub = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("TargetAgent", "xtorm01");
        jj.put("RequestDateTime", sDateTime);
        jj.put("OrderType", "RequestStats");
        
        
        joSub.put("Command", 	"Trans_Stats_Inquiry");
        joSub.put("ValueUnit", 	"MS");
        joSub.put("AggreUnit", 	"Suc, Min"); //,Min, Max
        joSub.put("PerUnit", 	"Sec");
        joSub.put("RangeStart", "2014-07-25 13:50:15");
        joSub.put("RangeEnd", 	"2014-07-25 13:50:15");
        ja.put(joSub);
        
        jj.put("RequestStats", ja);
        String CMD = jj.toString();
        ZIPcompress zip = new ZIPcompress();
        byte[] byteJsonDatadd = zip.ZipComp(CMD);
        if(byteJsonDatadd == null) 
        {
        	return;
        }
     
		
        for(int i=0; i< 1 ; i++)
        {
	        IoBuffer buffer = IoBuffer.allocate(byteJsonDatadd.length);
			buffer.put(byteJsonDatadd);
	        buffer.flip();            
	        
	        //WriteFuture future =  session.write(buffer);
	        session.write(buffer);
	        Thread.sleep(3);
	        //session.close(true);
        }
        
        
        //(0x00000002: nio socket, server, /192.168.0.27:35943 => /192.168.0.27:18880)
        Thread.sleep(1);
        System.out.println("retunr cnt:" + ai_messageReceived.intValue());
        
        while (client.received == false) {
            Thread.sleep(1);
        }
        Thread.sleep(1);
        client.received = false;

        while (client.received == false) {
            Thread.sleep(1);
        }
        
        client.received = false;
 

        long t1 = System.currentTimeMillis();

        System.out.println("Sent messages delay : " + (t1 - t0));

        Thread.sleep(100000);

        
        
        client.connector.dispose(true);
    }
}
