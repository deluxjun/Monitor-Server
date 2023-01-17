package fromagent;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
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

import com.speno.xmon.codedic.DicCommands;
import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.codedic.DicOrderTypes;
import com.speno.xmon.codedic.DicValueUnit;
import com.speno.xmon.codedic.DicXmlProperties;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.compress.ZIPcompress;
import com.speno.xmon.pcf.CodecFactory;
import com.speno.xmon.pcf.CodecFilter;

public class tcp extends IoHandlerAdapter {
    /** The connector */
    private IoConnector connector;

    /** The session */
    private static IoSession session;

    private boolean received = false;

    public tcp() {
    	
        try {
        	connector = new NioSocketConnector();    	
			connector.setHandler(this);       
			DefaultIoFilterChainBuilder chain = connector.getFilterChain();					
			chain.addFirst("protocol", new CodecFilter(new CodecFactory(Charset.forName("UTF-8"),LineDelimiter.AUTO,false,true)));			
//			chain.addLast("logger",  new LoggingFilter());   
			
	        SocketSessionConfig dcfg = (SocketSessionConfig) connector.getSessionConfig();
	        dcfg.setTcpNoDelay(false);

	        dcfg.setReadBufferSize(1024*10);
	        dcfg.setReceiveBufferSize(1024*10);
			ConnectFuture connFuture = connector.connect(new InetSocketAddress("localhost", 18882));             
			if(!connFuture.awaitUninterruptibly(1000)){
			}	
			
			if(connFuture.isConnected()) {
				session = connFuture.getSession();  
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    static AtomicInteger  ai_messageReceived = new AtomicInteger(0) ;
    
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
    	
        if (message instanceof IoBuffer) {
        	ai_messageReceived.getAndIncrement();
            IoBuffer buffer = (IoBuffer) message;
            byte[] byteJsonData  = buffer.array();	            

//            byte[] byteJsonData = new byte[buffer.remaining()];

            String msg = new String(ZIPcompress.inflate(false, byteJsonData), "UTF-8");            
            System.out.println(msg);
        }
        
    	client.received = true;
    }

    private static tcp client = null;
    
    @Test
    public void testSetCommands() throws Exception {
    	JSONObject o = new JSONObject();
		JSONArray jsonArray 			= new JSONArray(); 
    	
		o.put(DicOrderAdd.Command, DicOrderAdd.ActionName);
		o.put(DicOrderAdd.AgentName, "xtorm01");
		o.put(DicOrderAdd.ValueUnit, DicValueUnit.ValueUnit_String);
		o.put(DicOrderAdd.ReturnMsg, "");
		o.put(DicOrderAdd.ReturnCode, 0);
		o.put(DicXmlProperties.ActionName,  	"Inquiry" );
		o.put(DicXmlProperties.Title,  			"dd" );
		o.put(DicXmlProperties.Description,  	"dd" );
		o.put(DicXmlProperties.AggreUseYN,		"Y");
		o.put(DicXmlProperties.HealthUseYN,		"Y");
		
		jsonArray.put(o);

		o.put(DicOrderAdd.Command, DicOrderAdd.CommandList);
		o.put(DicOrderAdd.AgentName, "xtorm01");
		o.put(DicOrderAdd.ValueUnit, DicValueUnit.ValueUnit_String);
		o.put(DicOrderAdd.ReturnMsg, "");
		o.put(DicOrderAdd.ReturnCode, 0);
		o.put("USE",  "{Reso_XvarmArchive(Byte&아카이브&아카이브입니다&Y&Y)={MAIN=[USE, TOTAL], MAIN2=[USE, TOTAL]}, Reso_JavaHeap(KB&자바힙&가나다&Y&Y)={JavaHeap=[USE, TOTAL]}}");

		jsonArray.put(o);

		// logId
		o.put(DicOrderAdd.Command, DicOrderAdd.LogID);
		o.put(DicOrderAdd.AgentName, "xtorm02");
		o.put(DicOrderAdd.ValuesComma, "LogId1, LogId2");
		o.put(DicOrderAdd.ReturnMsg, "");
		o.put(DicOrderAdd.ReturnCode, 0);

		jsonArray.put(o);
		
		JSONObject root 		= new JSONObject();
		root.put(DicOrderTypes.ResponseAgentShort, jsonArray);
		
		root.put(DicCommands.SessionAgent, "xtorm01");
		root.put(DicOrderTypes.OrderType,  DicOrderTypes.ResponseAgentShort);
		root.put(DicOrderAdd.RequestDateTime, SimDate.DateTimeFormatter_MS.format(new Date(System.currentTimeMillis())));

        String CMD = root.toString();
                
        execute(CMD);
    }

    @Before
    public void setUp() throws Exception{
        client = new tcp();

    }
    private void execute(String CMD) {
        byte[] byteJsonDatadd = ZIPcompress.deflate(false, CMD);
        if(byteJsonDatadd == null){
        	return; 
        }
		
        for(int i=0; i<1 ; i++) {
	        session.write(byteJsonDatadd);
        }

        while (client.received == false) {
        	try {
            	Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        
        System.out.println("[END]");
    }
    
	@After
	public void tearDown() throws Exception {
        client.connector.dispose(true);
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
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
    
    public static void main(String[] args) throws Exception {
    }
}
