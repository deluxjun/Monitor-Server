package console;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * An TCP server used for performance tests.
 * 
 * It does nothing fancy, except receiving the messages, and counting the number of
 * received messages.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class RequestListener extends IoHandlerAdapter {
	public static void main(String[] args) throws Exception {
		RequestListener as = new RequestListener();
		
	}
	
    /** The listening port (check that it's not already in use) */
   
    /** The number of message to receive */
    public static final int MAX_RECEIVED = 100000;

    /** The starting point, set when we receive the first message */
    private static long t0;

    /** A counter incremented for every recieved message */
    private AtomicInteger nbReceived = new AtomicInteger(0);

    /**
     * Create the TCP server
     */
    public RequestListener() throws IOException {
        NioSocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setHandler(this);

        // The logger, if needed. Commented atm
        //DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
        //chain.addLast("logger", new LoggingFilter());

        SocketSessionConfig scfg = acceptor.getSessionConfig();

        acceptor.bind(new InetSocketAddress("192.168.0.27",18882));

        System.out.println("xMDeliverAgent Server started...");
    }


	public void SendRequest(String requestCommand) {

		//eCodeDic.RequestData:requestCommand
		
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
        session.close(true);
    }
   
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
    	 
        int nb = nbReceived.incrementAndGet();
        

        if (nb == 1) {
            t0 = System.currentTimeMillis();
        }

        if (message instanceof IoBuffer) {
            IoBuffer buffer = (IoBuffer) message;
            SocketAddress remoteAddress = session.getRemoteAddress();
            byte[] byteJsonData = new byte[buffer.limit()]; 
            buffer.get(byteJsonData, 0, buffer.limit());
            
            ZIPcompress zip = new ZIPcompress();
            String msg = zip.ZipDeComp(byteJsonData);
 

          // server.recvUpdate(remoteAddress, msg);
            /*
             * {"AgentIP":"192.168.0.27","CommandArray":
             * [{"Command":"Req_Memory_USE","ValueUnit":"MB"}
             * ,{"Command":"Req_Memory_TOTAL","ValueUnit":"MB"}
             * ,{"Command":"Req_JavaThread_USE","ValueUnit":"Count"}
             * ,{"Command":"Req_JavaThread_TOTAL","ValueUnit":"Count"}
             * ,{"Command":"Req_JavaHeap_USE","ValueUnit":"MB"}
             * ,{"Command":"Req_JavaHeap_TOTAL","ValueUnit":"MB"}
             * ,{"Command":"Req_XvarmArchive_USE","ValueUnit":"MB"}
             * ,{"Command":"Req_XvarmArchive_TOTAL","ValueUnit":"MB"}
             * ,{"Command":"Req_XvarmHealth_[xtorm01","ValueUnit":""}
             * ,{"Command":"Req_XvarmHealth_xtorm02] USE","ValueUnit":""}]
             * ,"OrderType":"RequestOrderAgent","SendTime":"2014-07-10 15:04:53.284"}
             */
        ((IoBuffer) message).flip();

        // If we want to test the write operation, uncomment this line
      //  session.write("2101:32");
        
        /*
         * 실제 메모리의 전체 크기 : Runtime.getRuntime().totalMemory()
         * 사용 가능한 실제 메모리의 크기 : Runtime.getRuntime().freeMemory()
         * 가상 메모리의 전체 크기 : Runtime.getRuntime().maxMemory()
         */
        
        long free = Runtime.getRuntime().freeMemory() ;
        long max = Runtime.getRuntime().maxMemory() ;
        long total = Runtime.getRuntime().totalMemory() ;
        long allocation = (total- free)  / (1024 * 1024);
        long availableProcessors =  Runtime.getRuntime().availableProcessors();
        
        System.out.println("free:" + free + ", max:"+ max + ", total" + total);
        
        File file = new File("c:\\");
        long disk_total = file.getTotalSpace();
        long disk_Usable = file.getUsableSpace();
        long disk_Free = file.getFreeSpace();
        
        System.out.println("disk_total:" + disk_total + ", disk_Usable:"+ disk_Usable + ", disk_Free" + disk_Free);
        
        Iterable<Object> ddd = session.getAttributeKeys();
        Iterator bb = ddd.iterator();
        while(bb.hasNext())
        {
        	System.out.println("key:" + bb.next());
        }
        
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonSubObject = new JSONObject();
        JSONArray jsonArray = new JSONArray(); 
        jsonObject.put("AgentIP", session.toString());
        jsonObject.put("SendTime", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis())));
        
        jsonSubObject.put("Command", "Req_JavaHeap_TOTAL");
        jsonSubObject.put("ValueUnit", "MB");
        jsonSubObject.put("Value", total );
        jsonArray.put(jsonSubObject);
        
        jsonSubObject = new JSONObject();
        jsonSubObject.put("Command", "Req_JavaHeap_USE");
        jsonSubObject.put("ValueUnit", "MB");
        jsonSubObject.put("Value", total - free );
        jsonArray.put(jsonSubObject);
        
        jsonSubObject = new JSONObject();
        jsonSubObject.put("Command", "Req_Storage_TOTAL");
        jsonSubObject.put("ValueUnit", "MB");
        jsonSubObject.put("Value", disk_total  );
        jsonArray.put(jsonSubObject);
        
        jsonSubObject = new JSONObject();
        jsonSubObject.put("Command", "Req_Storage_USE");
        jsonSubObject.put("ValueUnit", "MB");
        jsonSubObject.put("Value", disk_total - disk_Free );
        jsonArray.put(jsonSubObject);
        
        
        jsonObject.put("ResponseOrderAgent", jsonArray);
        
        String temp = jsonObject.toString();
        
       	byte[] byteJsonDatadd = zip.ZipComp(temp);
       	
        IoBuffer rbuffer = IoBuffer.allocate(byteJsonDatadd.length);
        rbuffer.put(byteJsonDatadd);
        rbuffer.flip();            

        WriteFuture future = session.write(rbuffer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("Session closed...");

        // Reinitialize the counter and expose the number of received messages
        System.out.println("Nb message received : " + nbReceived.get());
        nbReceived.set(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("Session created...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("Session idle...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        System.out.println("Session Opened...");
    }



}
