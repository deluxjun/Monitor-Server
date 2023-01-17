package console;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.speno.xmon.compress.ZIPcompress;
import com.speno.xmon.pcf.CodecFactory;
import com.speno.xmon.pcf.CodecFilter;

public class Log extends IoHandlerAdapter {
	private Log client = null;
	
	 /** The connector */
    private IoConnector connector;

    /** The session */
    private static IoSession session;

    private boolean received = false;
    static AtomicInteger  ai_messageReceived = new AtomicInteger(0) ;
    
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
    	
    	received = true;
        
        if (message instanceof IoBuffer) {
        	ai_messageReceived.getAndIncrement();
            IoBuffer buffer = (IoBuffer) message;
            byte[] byteJsonData = buffer.array();

            String msg = new String(ZIPcompress.inflate(byteJsonData));            
            System.out.println(msg);
        }
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
    }
    
    public Log() {
    	
        try {
        	connector = new NioSocketConnector();    	
			connector.setHandler(this);       
			DefaultIoFilterChainBuilder chain = connector.getFilterChain();					
			chain.addFirst("protocol", new CodecFilter(new CodecFactory(Charset.forName("UTF-8"),LineDelimiter.AUTO,false,true)));			
			
	        SocketSessionConfig dcfg = (SocketSessionConfig) connector.getSessionConfig();
	        dcfg.setTcpNoDelay(false);

	        dcfg.setReadBufferSize(1024*10);
	        dcfg.setReceiveBufferSize(1024*10);
			ConnectFuture connFuture = connector.connect(new InetSocketAddress("localhost", 18880));             
			if(!connFuture.awaitUninterruptibly(1000)){
			}	
			
			if(connFuture.isConnected()) {
				session = connFuture.getSession();  
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	@Before
    public void setUp() throws Exception{
        client = new Log();
		
    }
    
	@After
	public void tearDown() throws Exception {
        client.connector.dispose(true);

	}
	
	/**
	 * Log Test
	 **/
	@Test
	public void testLogtext() throws Exception {
		
		for (int i = 0; i < 200; i++) {
			JSONObject jj = new JSONObject();
	        JSONArray  ja = new JSONArray();
	        JSONObject joSub = new JSONObject();
	        
	        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
	        
	        jj.put("TargetAgent", "xtorm01");
	        jj.put("RequestDateTime", sDateTime);
	        jj.put("OrderType", "RequestStats");
	        jj.put("ConsoleId", "전체 에이전트");
	        
	        
	        joSub.put("Command", 	"Log_LogText");
	        joSub.put("ValueUnit", 	"500");
	        joSub.put("AggreUnit", 	"XtormAgent01"); //,Min, Max
	        joSub.put("PerUnit", 	"Day");
	        joSub.put("RangeStart", "2014-10-29");
	        joSub.put("RangeEnd", 	"2014-10-29");
	        ja.put(joSub);
	        
	        jj.put("RequestStats", ja);
	        
	        String CMD = jj.toString();
	        
        	byte[] byteJsonDatadd = ZIPcompress.deflate(CMD);
            if(byteJsonDatadd == null){
            	return;
            }
            
           	session.write(byteJsonDatadd);
           	
	        Thread.sleep(3000);
		}
        
        while (client.received == false) {
            Thread.sleep(1);
        }
        
        System.out.println("=================== The End ===================");
        
	}
}
