package com.speno.xmon.listener;

import java.net.SocketAddress;
import java.util.Iterator;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.db.collectLog.Tail;
import com.speno.xmon.env.xmPropertiesXml;

/**
 * Class the extends IoHandlerAdapter in order to properly handle
 * connections and the data the connections send
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class CommandServerHandler extends IoHandlerAdapter {

	private final static Logger LOG = LoggerFactory.getLogger(CommandServerHandler.class);
	
//	CommandServerMessageThread runner = null;
	CommandServer server;
    //private CommandServer server;
	//xMThreadPool_Received<CommandServerMessageThread> 					pool_Received ;
    public CommandServerHandler(CommandServer commandServer) {
      //  this.server = server;
    	try 
    	{    		
    		server = commandServer;
//            runner = new CommandServerMessageThread(server);
            
		//	pool_Received = new xMThreadPool_Received<CommandServerMessageThread>(5, 10);
	    //	pool_Received.initialize(new CommandServerMessageThread());
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
		
    	String remoteAddress = (String) session.getAttribute("LOGTEXT");
        LOG.debug("[4] Session exceptionCaught..."+ remoteAddress);
        
        String tempLogKey = "";
        String keyStartWith = remoteAddress +   xmPropertiesXml.Sep;
		
		Iterator<String> keysLogTail = xmPropertiesXml.chmConsoleLogTail.keySet().iterator();
		while(keysLogTail.hasNext())
		{
			tempLogKey = keysLogTail.next();
			if(tempLogKey.startsWith(keyStartWith))
			{
				Tail tempTail= xmPropertiesXml.chmConsoleLogTail.get(tempLogKey);
				tempTail.stopTail();
				if(xmPropertiesXml.chmConsoleLogTail.remove(tempLogKey,tempTail))
				 LOG.debug("[4] chmConsoleLogTail Session Remove Success."+ remoteAddress);
				else
				 LOG.debug("[4] chmConsoleLogTail Session Remove  Error!"+ remoteAddress);
				
			}
		}
		
		if( xmPropertiesXml.chmConsoleEvent.containsKey(keyStartWith))
		{
			if(xmPropertiesXml.chmConsoleEvent.remove(keyStartWith, xmPropertiesXml.chmConsoleEvent.get(keyStartWith)))
			{
			    LOG.debug("[4] chmConsoleEvent Session Remove Success."+ remoteAddress);
			}
			else
			{
			    LOG.debug("[4] chmConsoleEvent Session Remove  Error!"+ remoteAddress);
			}
		}
        //cause.printStackTrace();
    }
	
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
    	
    		byte[] byteJsonData;
    		SocketAddress remoteAddress = session.getRemoteAddress();
    		IoBuffer buffer = (IoBuffer) message;
    		
    		if(xmPropertiesXml.useProtocolCodecFilter) {
	            byteJsonData  = buffer.array();	            
    		}
    		else {
	            byteJsonData = new byte[buffer.remaining()];
	            buffer.get(byteJsonData);	        
	            ((IoBuffer) message).flip();
	            
    		}
    	

    		CommandServerMessageThread.process(session, remoteAddress, byteJsonData);
    		LOG.debug("Received Console:"+new String(byteJsonData));    	
//            if (worker == null) {
//            	worker = new WorkerThread();
//            }
//            worker.setByteJsonData(byteJsonData);
//            worker.setRemoteAddress(remoteAddress);
//            worker.setMinaSesseion(session);
//            server.getWorkerPool().execute(worker);
            
        
    }
    @Override
    public void sessionClosed(IoSession session) throws Exception {
    	String remoteAddress = (String) session.getAttribute("LOGTEXT");
        LOG.info("@TCP Session Closed..."+ remoteAddress);
        //server.removeClient(remoteAddress);
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
    	//SocketAddress remoteAddress = session.getRemoteAddress();
    	LOG.debug("@TCP Session Created..." + session.toString());
    	//session.
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
    	//LOG.debug("Session idle..."+ session.toString());
    }
    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
    		LOG.debug("send Console:"+new String((byte[])message) );    		
    }
    @Override
    public void sessionOpened(IoSession session) throws Exception {
    	session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
    	session.setAttribute("LOGTEXT",session.getRemoteAddress().toString());
    	
    	LOG.info("@TCP Session Opened: " 
    			+ session.getRemoteAddress().toString() + " => " + session.getLocalAddress().toString());
    	
    }
    private WorkerThread worker;
    class WorkerThread implements Runnable {
    	IoSession session;
    	SocketAddress remoteAddress;
    	byte[] byteJsonData;
    	
    	public void setRemoteAddress(SocketAddress remoteAddress) {
			this.remoteAddress = remoteAddress;
		}
    	public void setMinaSesseion(IoSession session) {
    		this.session = session;
			
		}
		public void setByteJsonData(byte[] byteJsonData) {
			this.byteJsonData = byteJsonData;
		}
    	
    	public void run() {
    		CommandServerMessageThread.process(session, remoteAddress, byteJsonData);
    	}
    }
    
}
