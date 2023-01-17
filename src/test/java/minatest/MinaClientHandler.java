package minatest;


import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import console.ZIPcompress;

/**
 * @author giftsam
 */
public class MinaClientHandler extends IoHandlerAdapter {
	private final String values;
	private boolean finished;
	private int id;

	public MinaClientHandler(String values, int id) {
		this.values = values;
		this.id = id;
	}

	public boolean isFinished() {
		return finished;
	}

	@Override
	public void sessionOpened(IoSession session) {
		//session.write(values);
	}
	static AtomicInteger  ai_messageReceived = new AtomicInteger(0) ;
	@Override
	public void messageReceived(IoSession session, Object message) {
		System.out.println("Message received in the client..");
//		try {
//			Thread.sleep(10*1000L);
//		} catch (Exception e) {
//		}

        if (message instanceof IoBuffer) {
        	ai_messageReceived.getAndIncrement();
            IoBuffer buffer = (IoBuffer) message;
            byte[] byteJsonData = new byte[buffer.remaining()];
            buffer.get(byteJsonData);
            ((IoBuffer) message).flip();

            SocketAddress remoteAddress	= session.getRemoteAddress();
            ZIPcompress zip = new ZIPcompress();
            String msg = zip.ZipDeComp(byteJsonData);            
            //new DataInserterResource().Insert_XmAggregatedResourceSec(new RequestAgentShortHandler( ip , msg).GetItemList());
            System.out.println(msg);
            
            //System.out.println("Message is: " + message.toString());
        }
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		session.close();
	}

}
