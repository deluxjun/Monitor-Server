package minatest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.speno.xmon.pcf.CodecFactory;
import com.speno.xmon.pcf.CodecFilter;

import console.ZIPcompress;

/**
 * @author giftsam
 */
public class MinaClient extends Thread{

	private int id = 0;
	public MinaClient(int id) {
		this.id = id;
	}
	@Override
	public void run() {
//		IoConnector connector = new NioSocketConnector();
////		connector.getSessionConfig().setReadBufferSize(2048);
//
////		connector.getFilterChain().addLast("logger", new LoggingFilter());
//		//connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
//
//		connector.setHandler(new MinaClientHandler("Hello Server.." + id, id));
//		
//		System.out.println("connecting : " + id);
//		
//		ConnectFuture future = connector.connect(new InetSocketAddress("localhost", 18880));
//		future.awaitUninterruptibly();
//
//		if (!future.isConnected()) {
//			return;
//		}
//		IoSession session = future.getSession();
//		session.getConfig().setUseReadOperation(true);
//		
//		if(session.isConnected()){
//			try {
//				this.sendData(session);
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		
//		session.getCloseFuture().awaitUninterruptibly();
//
//		System.out.println("After Writing : " + id);
//		connector.dispose();

		
        try {
        	IoConnector connector = new NioSocketConnector();    	
			connector.setHandler(new MinaClientHandler("Hello Server.." + id, id));       
			DefaultIoFilterChainBuilder chain = connector.getFilterChain();					
			chain.addFirst("protocol", new CodecFilter(new CodecFactory(Charset.forName("UTF-8"),LineDelimiter.AUTO,false,true)));			
//			chain.addLast("logger",  NormalUtil.removeMinaLogger(new LoggingFilter()));   
			
			ConnectFuture connFuture = connector.connect(new InetSocketAddress("localhost", 1234));             
			if(!connFuture.awaitUninterruptibly(1000)){
			}	
			
			IoSession session = null;

			if(connFuture.isConnected()) {
				session = connFuture.getSession();
				
				try {
					this.sendData(session);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
				session.getCloseFuture().awaitUninterruptibly();
			}
			

			System.out.println("After Writing : " + id);
			connector.dispose();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendData(IoSession session) throws JSONException, InterruptedException {

		try {
		    JSONObject jj = new JSONObject();
	        JSONArray  ja = new JSONArray();
	        JSONObject joSub = new JSONObject();
	        
	        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
	        
	        jj.put("TargetAgent", "xtorm01");
	        jj.put("RequestDateTime", sDateTime);
	        jj.put("OrderType", "RequestAgent");
	        
	        
	        joSub.put("Command", "Req_JavaHeap");
	        joSub.put("ValueUnit", "MB");
	        ja.put(joSub);
	        
	        jj.put("RequestAgent", ja);
	        String CMD = jj.toString();
	        
	        
	        ZIPcompress zip = new ZIPcompress();
	        byte[] byteJsonDatadd = zip.ZipComp(CMD);
	        if(byteJsonDatadd == null) 
	        {
	        	return;
	        }
	        
	        for(int i=0; i< 10 ; i++)
	        {
		        IoBuffer buffer = IoBuffer.allocate(byteJsonDatadd.length);
				buffer.put(byteJsonDatadd);
		        buffer.flip();            
		        
		        if(session.isConnected())
		        {
		        //WriteFuture future =  session.write(buffer);
		        	session.write(buffer);
		        }
		        Thread.sleep(3);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	public static void main(String[] args) throws IOException,
			InterruptedException {
		MinaClient[] cons = new MinaClient[10];
		for (int i = 0; i < 10; i++) {
			cons[i]  = new MinaClient(i+1);
			cons[i].start();
			Thread.sleep(1000);
		}
		while(true)
		{
			Thread.sleep(1000);
		}
			
	}

}
