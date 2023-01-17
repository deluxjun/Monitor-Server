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
import org.junit.*;

import com.speno.xmon.compress.ZIPcompress;
import com.speno.xmon.pcf.CodecFactory;
import com.speno.xmon.pcf.CodecFilter;

public class Settings extends IoHandlerAdapter {
	private Settings client = null;
	
	/* The connector */
    private IoConnector connector;

    /* The session */
    private static IoSession session;

    private boolean received = false;
    public final boolean useProtocolCodecFilter 	= true;
    static AtomicInteger  ai_messageReceived = new AtomicInteger(0) ;
    
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
    
    public void messageReceived(IoSession session, Object message) throws Exception {
    
    	received = true;
        
        if (message instanceof IoBuffer) {
        	ai_messageReceived.getAndIncrement();
            IoBuffer buffer = (IoBuffer) message;
            byte[] byteJsonData = buffer.array();

            String msg = new String(ZIPcompress.inflate(false, byteJsonData), "UTF-8");            
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

    public Settings() {
    	
        try {
        	connector = new NioSocketConnector();    	
			connector.setHandler(this);       
			DefaultIoFilterChainBuilder chain = connector.getFilterChain();					
			chain.addFirst("protocol", new CodecFilter(new CodecFactory(Charset.forName("UTF-8"),LineDelimiter.AUTO,false,true)));			
			
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
    
	@Before
    public void setUp() throws Exception{
        client = new Settings();
		
    }
    
	@After
	public void tearDown() throws Exception {
        client.connector.dispose(true);

	}
	

	/**
	 * Agent�̸� �ޱ�
	 **/
	@Test
	public void testSetAgentNames() throws Exception {
		JSONObject jj = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("RequestDateTime", sDateTime);
        jj.put("OrderType", "RequestSet");
        jj.put("Command", 	"Set_AgentNames"); //Trans_Unit_Inquiry
        
        testExecution(jj);
	}
	
	
	/**
	 * Command(���ҽ�) �ޱ�
	 **/
	@Test
	public void testSetCommands() throws Exception {
		JSONObject jj = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("RequestDateTime", sDateTime);
        jj.put("OrderType", "RequestSet");
        jj.put("Command", 	"Set_Commands"); //Trans_Unit_Inquiry
        
        testExecution(jj);
	}
	
	
	/**
	 * Action(�ŷ�) �ޱ�
	 **/
	@Test
	public void testSetActionNames() throws Exception {
        JSONObject jj = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("RequestDateTime", sDateTime);
        jj.put("OrderType", "RequestSet");
        jj.put("Command", 	"Set_ActionNames"); //Trans_Unit_Inquiry
        
        testExecution(jj);
	}
	

	/**
	 * LOG ����Ʈ �ޱ�
	 **/
	@Test
	public void testSetLogIds() throws Exception {
		JSONObject jj = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("RequestDateTime", sDateTime);
        jj.put("OrderType", "RequestSet");
        jj.put("Command", 	"Set_LogIds"); //Trans_Unit_Inquiry
        
        testExecution(jj);
	}

	
	/**
	 * ���� ���� �ޱ�
	 **/
	@Test
	public void testSetEnvPropertiesSch() throws Exception {
		JSONObject jj = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("RequestDateTime", sDateTime);
        jj.put("OrderType", "RequestSet");
        jj.put("Command", 	"Set_EnvProperties_Sch������"); //Trans_Unit_Inquiry
        
        testExecution(jj);
	}
	

	/**
	 * ������Ʈ ���� �ޱ�
	 **/
	@Test
	public void testSetEnvPropertiesMain() throws Exception {
		JSONObject jj = new JSONObject();
        JSONArray  ja = new JSONArray();
        JSONObject joSub = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("TargetAgent", "xtorm01111");
        jj.put("OrderType", "RequestAgentSet");
        jj.put("RequestDateTime", sDateTime);
        jj.put("ConsoleId", "1234");
        
        joSub.put("ValueUnit", "");
        joSub.put("Command", 	"Set_EnvProperties_Mainsss"); //Trans_Unit_Inquiry
        
        ja.put(joSub);
        
        jj.put("RequestAgentSet", ja);
        
        testExecution(jj);
	}
	

	/**
	 * ��ó�� ���� ���� -> �̺�Ʈ�� �ޱ� ����
	 **/
	@Test
	public void testSetInitSession() throws Exception {
		JSONObject jj = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("RequestDateTime", sDateTime);
        jj.put("OrderType", "RequestSet");
        jj.put("Command", 	"Set_InitSession");
        
        testExecution(jj);
        
	}


	/**
	 * �ܼ� ���� üũ
	 **/
//	@Test
//	public void testSetAgentHealth() throws Exception {
//		JSONObject jj = new JSONObject();
//        
//        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
//        
//        jj.put("Command", 	"Set_AgentHealth");
//        jj.put("RequestDateTime", sDateTime);
//        jj.put("OrderType", "RequestSet");
//        jj.put("TargetAgent", "xtorm01");
//        
//        testExecution(jj);
//	}
	
	
	/**
	 * �� ���� üũ
	 **/
	@Test 
	public void testSetAgentHealthPort() throws Exception { //command�� Set_AgentHealthPort�� ����
		JSONObject jj = new JSONObject();
        
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
        
        jj.put("Command", 	"Set_AgentHealthPort");
        jj.put("RequestDateTime", sDateTime);
        jj.put("OrderType", "RequestSet");
        jj.put("TargetAgent", "xtorm01");
        
        testExecution(jj);
	}
	
	
	/**
	 * ��� ���� ����
	 **/
//	@Test
//	public void testChangeCommandSetting() throws Exception {
//		JSONObject jj = new JSONObject();
//		JSONArray ja = new JSONArray();
//		JSONObject joSub = new JSONObject();
//        
//        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
//        
//        /*������ �ִ� ���ÿ� ����*/
//        joSub.put("Name", "Register");
//        joSub.put("desc", "����");
//        joSub.put("ResourceId", "Register");
//        joSub.put("ValueUnit", ",");
//        joSub.put("Aggregation", "Y");
//        joSub.put("Property", "USE");
//        joSub.put("Title", "��ȸ");
//        joSub.put("Command", ""); 
//        ja.put(joSub);
//        
//        jj.put("RequestAgentSet", ja);
//        jj.put("ConsoleId", 	"1");
//        jj.put("RequestDateTime", sDateTime);
//        jj.put("OrderType", "RequestAgentSet");
//        jj.put("TargetAgent", "xtorm111111111");
//        
//        testExecution(jj);
//	}
	
	
	/**
	 * ����/������Ʈ ���� ����
	 **/
//	@Test
//	public void testSetEnvAmass() throws Exception { 
//
//		JSONObject jj = new JSONObject();
//		JSONArray ja = new JSONArray();
//		JSONObject joSub = new JSONObject();
//        
//        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
//        
//        /*������ �ִ� ���ÿ� ����*/
//        joSub.put("Transaction_Timeout", "30");
//        ja.put(joSub);
//        
//        jj.put("RequestSet", ja);
//        jj.put("Command", 	"Set_EnvAmass");
//        jj.put("PropertyNames", "Transaction_Timeout");
//        jj.put("RequestDateTime", sDateTime);
//        jj.put("OrderType", "RequestSet");
//        jj.put("TargetAgent", "Server");
//        
//        testExecution(jj);
//		
//	}

	public void testExecution(JSONObject jj) throws Exception {
		String CMD = jj.toString();
        
        for(int i=0; i< 1 ; i++)
        {
        	byte[] byteJsonDatadd = ZIPcompress.deflate(false, CMD);
            if(byteJsonDatadd == null) 
            {
            	return;
            }
            
           	session.write(byteJsonDatadd);
        }
        
        while (client.received == false) {
            Thread.sleep(1);
        }
        
        System.out.println("=================== The End ===================");
	}
	
	
}
