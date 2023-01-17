package console;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
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

import com.speno.xmon.compress.ZIPcompress;
import com.speno.xmon.pcf.CodecFactory;
import com.speno.xmon.pcf.CodecFilter;

public class monitoring extends IoHandlerAdapter {
    /** The connector */
    private IoConnector connector;

    /** The session */
    private static IoSession session;

    private boolean received = false;

    public monitoring() {
    	
        try {
        	connector = new NioSocketConnector();    	
			connector.setHandler(this);       
			DefaultIoFilterChainBuilder chain = connector.getFilterChain();					
			chain.addFirst("protocol", new CodecFilter(new CodecFactory(Charset.forName("UTF-8"),LineDelimiter.AUTO,false,true)));			
//			chain.addLast("logger",  new LoggingFilter());   
			
	        SocketSessionConfig dcfg = (SocketSessionConfig) connector.getSessionConfig();
	        dcfg.setTcpNoDelay(false);

	        dcfg.setReadBufferSize(1024*1024);
	        dcfg.setReceiveBufferSize(1024*1024);
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
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
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
//            JSONObject  jsonObject		= new JSONObject(msg);
//            String returnCode = jsonObject.get("ReturnCode").toString();
//            String command = jsonObject.get("Command").toString();
//            if (command.equals("Trans_Stats_Delete") && !returnCode.equals("-100"))
//            	System.out.println("=================== ERR: " + msg);
        }
        
        notifyReceived();
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
    
    private static monitoring client = null;
    
    
    @Before
    public void setUp() throws Exception{
        client = new monitoring();

    }
    
    private synchronized void notifyReceived() {
    	client.received = true;
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
    
    @Test
    public void testResourceJavaHeap() throws Exception {
        JSONObject jj = new JSONObject();
        JSONArray  ja = new JSONArray();
        JSONObject joSub = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("TargetAgent","xtorm01");
        jj.put("RequestDateTime",sDateTime);
        jj.put("ConsoleId","Resource:xtorm01:JavaHeap");
        jj.put("OrderType","RequestAgent");

        joSub.put("Command","Reso_JavaHeap&JavaHeap");
        joSub.put("PropertyNames","USE,TOTAL");
        joSub.put("ResourceID","JavaHeap");
        joSub.put("ValueUnit","KB");
        
        ja.put(joSub);
        
        jj.put("RequestAgent", ja);
        String CMD = jj.toString();
                
        execute(CMD);
    }
    
    @Test
    public void testResourceArchive() throws Exception {

        JSONObject jj = new JSONObject();
        JSONArray  ja = new JSONArray();
        JSONObject joSub = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("TargetAgent","xtorm01");
        jj.put("RequestDateTime",sDateTime);
        jj.put("ConsoleId","Resource:xtorm01:MAIN");
        jj.put("OrderType","RequestAgent");

        joSub.put("Command","Reso_XvarmArchive&MAIN");
        joSub.put("PropertyNames","USE,TOTAL");
        joSub.put("ValueUnit","Byte");
        joSub.put("ResourceID","MAIN");
        
        ja.put(joSub);
        
        jj.put("RequestAgent", ja);
        String CMD = jj.toString();

        execute(CMD);
        
    }
    
    @Test
    public void testResourceArchiveMulti() throws Exception {

        JSONObject jj = new JSONObject();
        JSONArray  ja = new JSONArray();
        JSONObject joSub = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("TargetAgent","xtorm01");
        jj.put("RequestDateTime",sDateTime);
        jj.put("ConsoleId","Resource:xtorm01:MAIN");
        jj.put("OrderType","RequestAgent");

        joSub.put("Command","Reso_XvarmArchive&MAIN");
        joSub.put("PropertyNames","USE,TOTAL");
        joSub.put("ValueUnit","Byte");
        joSub.put("ResourceID","MAIN");
        
        ja.put(joSub);

        joSub = new JSONObject();
        joSub.put("Command","Reso_XvarmArchive&MAIN2");
        joSub.put("PropertyNames","USE,TOTAL");
        joSub.put("ValueUnit","Byte");
        joSub.put("ResourceID","MAIN");

        ja.put(joSub);

        
        jj.put("RequestAgent", ja);
        String CMD = jj.toString();

        execute(CMD);
        
    }

    @Test
    public void testTPSStats() throws Exception {

        JSONObject jj = new JSONObject();
        JSONArray  array = new JSONArray();
        JSONObject joSub = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));

        jj.put("TargetAgent","xtorm01");
        jj.put("RequestDateTime",sDateTime);
        jj.put("ConsoleId","Resource:xtorm01:MAIN");
        jj.put("OrderType","RequestStats");

        joSub.put("Command","Trans_Stats_Inquiry");
        joSub.put("ValueUnit","TPS");
        joSub.put("ResourceID","Inquiry");
        joSub.put("AggreUnit","Success");
        sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis() - 600*1000l));	// 1����
        joSub.put("RangeStart",sDateTime);
        sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        joSub.put("RangeEnd",sDateTime);
        joSub.put("PropertyNames","USE");
        joSub.put("PerUnit","Min");
        
        array.put(joSub);

        
        jj.put("RequestStats", array);
        String CMD = jj.toString();

        execute(CMD);
    }
    
    @Test
    public void testTPSStatsDay() throws Exception {

        JSONObject jj = new JSONObject();
        JSONArray  array = new JSONArray();
        JSONObject joSub = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));

//        {"ConsoleId":"StatsChart:xtorm01:TPS",
//        "RequestStats":[{"RangeStart":"2014-10-27","Command":"Trans_Stats_Inquiry","PerUnit":"Day","ResourceID":"Inquiry","ValueUnit":"Cnt","PropertyNames":"USE","AggreUnit":"Avg,Max,Min","RangeEnd":"2014-11-03"}],"RequestDateTime":"2014-11-03","OrderType":"RequestStats","TargetAgent":"xtorm01"}
        
        jj.put("TargetAgent","xtorm01");
        jj.put("RequestDateTime",sDateTime);
        jj.put("ConsoleId","Resource:xtorm01:MAIN");
        jj.put("OrderType","RequestStats");

        joSub.put("Command","Trans_Stats_Inquiry");
        joSub.put("ValueUnit","Cnt");
        joSub.put("ResourceID","Inquiry");
        joSub.put("AggreUnit","Avg,Max,Min");
        joSub.put("RangeStart","2014-11-01");
        joSub.put("RangeEnd","2014-11-07");
        joSub.put("PropertyNames","USE");
        joSub.put("PerUnit","Day");
        
        array.put(joSub);
        
        jj.put("RequestStats", array);
        String CMD = jj.toString();

        execute(CMD);
    }
    
    @Test
    public void testResourceStats() throws Exception {

        JSONObject jj = new JSONObject();
        JSONArray  array = new JSONArray();
        JSONObject joSub = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("TargetAgent","xtorm01");
        jj.put("RequestDateTime",sDateTime);
        jj.put("ConsoleId","Resource:xtorm01:MAIN");
        jj.put("OrderType","RequestStats");

        joSub.put("Command","Reso_XvarmArchive");
        joSub.put("ValueUnit","KB");
        joSub.put("ResourceID","MAIN2");
        joSub.put("AggreUnit","Avg");
        joSub.put("RangeStart","2014-11-17 10");
        joSub.put("RangeEnd","2014-11-17 14");
        joSub.put("PropertyNames","USE");
        joSub.put("PerUnit","Hour");
        
        array.put(joSub);
        
        jj.put("RequestStats", array);
        String CMD = jj.toString();

        execute(CMD);
    }
    
    @Test
    public void testNewResourceStats() throws Exception {

        JSONObject jj = new JSONObject();
        JSONArray  array = new JSONArray();
        JSONObject joSub = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("Command","StatisticsResource");
        jj.put("ConsoleId","Resource:xtorm01:MAIN");
        jj.put("AgentName","xtorm01");
        jj.put("AggreUnit","Avg");
        jj.put("RangeStart","2014-11-17 10");
        jj.put("RangeEnd","2014-11-17 14");
        jj.put("PropertyNames","USE,TOTAL");
        jj.put("PerUnit","Min");
        jj.put("ResourceGroupID","Reso_XvarmArchive");
        jj.put("ResourceID","MAIN,MAIN2");

        String CMD = jj.toString();

        execute(CMD);
    }
    
    @Test
    public void testMonitorTPS() throws Exception {

        JSONObject jj = new JSONObject();
        JSONArray  array = new JSONArray();
        JSONObject joSub = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("TargetAgent","xtorm01");
        jj.put("RequestDateTime",sDateTime);
        jj.put("ConsoleId","Resource:xtorm01:MAIN");
        jj.put("OrderType","RequestStats");

        joSub.put("Command","Trans_Stats_Inquiry");
        joSub.put("ValueUnit","TPS");
        joSub.put("ResourceID","Inquiry");
        joSub.put("AggreUnit","Success");
        joSub.put("RangeStart","");
        joSub.put("RangeEnd","");
        joSub.put("PropertyNames","USE");
        joSub.put("PerUnit","Sec");
        
        array.put(joSub);
        
        jj.put("RequestStats", array);

        String CMD = jj.toString();

        execute(CMD);
    }
    
    @Test
    public void testLoopMonitorTPS() throws Exception {
    	while(true) {
    		for (int i = 0; i < 3; i++) {
				monitorTPS("Trans_Stats_Inquiry", "Inquiry");
				monitorTPS("Trans_Stats_Create", "Create");
				monitorTPS("Trans_Stats_Delete", "Delete");
			}
    		Thread.sleep(1000L);
    	}
    }
    
    private void monitorTPS(String command, String resourceID ) {
        JSONObject jj = new JSONObject();
        JSONArray  array = new JSONArray();
        JSONObject joSub = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        try {
            jj.put("TargetAgent","xtorm01");
            jj.put("RequestDateTime",sDateTime);
            jj.put("ConsoleId","Resource:xtorm01:MAIN");
            jj.put("OrderType","RequestStats");

            joSub.put("Command",command);
            joSub.put("ValueUnit","TPS");
            joSub.put("ResourceID",resourceID);
            joSub.put("AggreUnit","Success");
            joSub.put("RangeStart","");
            joSub.put("RangeEnd","");
            joSub.put("PropertyNames","USE");
            joSub.put("PerUnit","Sec");
            
            array.put(joSub);
            
            jj.put("RequestStats", array);
		} catch (Exception e) {
			e.printStackTrace();
		}

        String CMD = jj.toString();
        byte[] byteJsonDatadd = ZIPcompress.deflate(false, CMD);
        if(byteJsonDatadd == null){
        	return; 
        }
		
        for(int i=0; i<1 ; i++) {
	        session.write(byteJsonDatadd);
        }
    }
    
    // action scatter
    @Test
    public void testScatter() throws Exception {
    }
    
    public static void main(String[] args) throws Exception {
    }
}
