package console;

import java.net.InetSocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * Sends its memory usage to the MemoryMonitor server.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class AmassSender extends IoHandlerAdapter {

	public static void main(String[] args) throws Exception {
		AmassSender as = new AmassSender();
		
	}

    private final static Logger Logger = LoggerFactory.getLogger(AmassSender.class);
    private IoSession session;

    private IoConnector connector;

    private AmassSenderThread amassDataSender;
    /**
     * Default constructor.
     */
    public AmassSender() {
    	
    	Logger.debug(AmassSender.class.toString() + "시작");
        Logger.debug("Created a NioDatagramConnector");
        this.connector = new NioDatagramConnector();

        Logger.debug("Setting the handler");
        this.connector.setHandler(this);
        this.amassDataSender = new AmassSenderThread();
        
        while(true)
        {
	        //Logger.debug("About to connect to the AmassServer:" +xmProperties.AmassServerName  + " Port:" +xmProperties.AmassServerPort);
	        ConnectFuture connFuture = connector.connect(new InetSocketAddress("192.168.0.27",18881));
	
	        Logger.debug("About to wait.");
 	        connFuture.awaitUninterruptibly();
	        
	        Logger.debug("Adding a future listener.");	        
	        connFuture.addListener(new IoFutureListener<ConnectFuture>() {
	            public void operationComplete(ConnectFuture future) {
	                if (future.isConnected()) {
	                	
	                    Logger.debug("AmassServer...Connected");
	                    session = future.getSession();
	                    try {
	                    	// IoSessionConfig dcfg = session.getConfig();
	                    	// dcfg.setMaxReadBufferSize(10240);
	                    	// dcfg.setReuseAddress(true);
	                    	// dcfg.setMaxReadBufferSize(10240);
	                    	
		                    amassDataSender.setSession(session);
	            	        while (true)
	            	        {     	            	        	
	   			             		Thread.sleep(100);
	            	        		if(!amassDataSender.SendData(amassDataSender.getSendingData())) break;	
	            	        }
	                        
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                } else {
	                    Logger.error("Not connected...exiting");
	                }
	            }
	        });
        }

    }


	@Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        Logger.debug("Session recv...");
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        Logger.debug("Message sent...");
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        Logger.debug("Session closed...");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        Logger.debug("Session created...");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        Logger.debug("Session idle...");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        Logger.debug("Session opened...");
    }


    
	
     /*
 	  com.sun.management.OperatingSystemMXBean mxbean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
   long	 free=     mxbean.getProcessCpuTime();
 
  	  switch (selected) {
  	    case 0:         
  	       return mxbean.getProcessCpuTime();
  	       
  	    case 1:
  	       Runtime runtime = Runtime.getRuntime();         
  	       
  	       return runtime.totalMemory()-runtime.freeMemory();   
  	       
  	    case 2:         
  	       return mxbean.getFreeSwapSpaceSize();
  	    case 3:          
  	       return mxbean.getCommittedVirtualMemorySize();
  	    default:
  	       return -1;      
  	    }
  	    */
  	//operationsystemmxbean을 이용하시면 됩니다
}
