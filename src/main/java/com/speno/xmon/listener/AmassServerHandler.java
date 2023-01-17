package com.speno.xmon.listener;

import java.net.SocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.env.xmPropertiesXml;

public class AmassServerHandler extends IoHandlerAdapter {

	private final static Logger LOG = LoggerFactory.getLogger(AmassServerHandler.class);

	private AmassServer server;
//    private WorkerThread worker;

    AmassServerThread runner = null;

	public AmassServerHandler(AmassServer amassServer) {
		server = amassServer;
	}

	@Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
    	byte[] b = (byte[])message; 
    	String data= new String(b);    	
    	LOG.debug("send agent : " + data);
    	 	
    }
	
    @Override
    public void messageReceived(final IoSession session, final Object message) throws Exception {
        
            // worker
        	byte[] byteJsonData;
        	IoBuffer buffer = (IoBuffer) message;
        	if(xmPropertiesXml.useProtocolCodecFilter){
        		byteJsonData  = buffer.array();	
        	}
        	else{
	            byteJsonData = new byte[buffer.remaining()];
	            buffer.get(byteJsonData);	   
        	}
            ((IoBuffer) message).flip();
            SocketAddress remoteAddress = session.getRemoteAddress();
            if (remoteAddress == null)
            	return;
            
//    		String reciveJsonData;
//    		try {
//    			reciveJsonData	= new String(ZIPcompress.inflate(xmPropertiesXml.networkCompress, byteJsonData), "UTF-8"); 
//    		} catch (Exception e) {
//    			reciveJsonData	= new String(ZIPcompress.inflate(xmPropertiesXml.networkCompress, byteJsonData)); 
//    		}
//    		// print debug
//    		LOG.debug("[" + Thread.currentThread().getName() +"] received: " + reciveJsonData);
            
        	WorkerThread	worker = new WorkerThread();
        	worker.setByteJsonData(byteJsonData);
            worker.setRemoteAddress(remoteAddress);
            int info[] = server.getWorkerPool().threadsInfo();
//            System.out.println("pooling info : " + info[0] + "," + info[1] + "," + info[2]);        
            server.getWorkerPool().execute(worker);
            LOG.debug("received agent:" + new String(byteJsonData));
        
    }

    class WorkerThread implements Runnable {
    	
    	SocketAddress remoteAddress;
    	byte[] byteJsonData;
    	
    	public WorkerThread() {
    		runner = new AmassServerThread(server);
		}
    	public void setRemoteAddress(SocketAddress remoteAddress) {
			this.remoteAddress = remoteAddress;
		}
    	public void setByteJsonData(byte[] byteJsonData) {
			this.byteJsonData = byteJsonData;
		}
    	
    	public void run() {
    		runner.process(remoteAddress, byteJsonData);
    	}
    }
    
    @Override
    public void sessionClosed(IoSession session) throws Exception {
    	try {
        	if (session != null && session.getRemoteAddress() != null && session.getLocalAddress() != null)
            	LOG.info("@Trans Session closed: "
            			+ session.getRemoteAddress().toString() + " => " + session.getLocalAddress().toString());
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}
    }
    @Override
    public void sessionCreated(IoSession session) throws Exception {
    	try {
        	if (session != null && session.getRemoteAddress() != null && session.getLocalAddress() != null)
            	LOG.debug("@Trans Session created: "
            			+ session.getRemoteAddress().toString() + " => " + session.getLocalAddress().toString());
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}
    }
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
    	//LOG.info("Session idle...");
    }
    @Override
    public void sessionOpened(IoSession session) throws Exception {
    	try {
        	if (session != null && session.getRemoteAddress() != null && session.getLocalAddress() != null)
            	LOG.info("@Trans Session opened: " 
            			+ session.getRemoteAddress().toString() + " => " + session.getLocalAddress().toString());
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}
    }

}
