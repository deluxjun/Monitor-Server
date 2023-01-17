package com.speno.xmon.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author giftsam
 */
public class MinaServerHandler extends IoHandlerAdapter {
	private final Logger logger = (Logger) LoggerFactory.getLogger(getClass());

	private int index = 0;
	
	@Override
	public void sessionCreated(IoSession session) {
		String threadName = Thread.currentThread().getName();
		logger.info("[" + threadName + "] sessionCreated");
	}

	@Override
	public void sessionOpened(IoSession session) {
		String threadName = Thread.currentThread().getName();
		// set idle time to 10 seconds
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		session.setAttribute("Values: ");
		logger.info("[" + threadName + "] sessionOpened");
	}

	@Override
	public void messageReceived(IoSession session, Object message) {
		String threadName = Thread.currentThread().getName();
		
		logger.info("[" + threadName + "] [IN] " + message.toString());
		int sleeptime = 1;
		if (index++ == 0)
			sleeptime = 20;
		else
			sleeptime = 1;
		try {
			Thread.sleep(sleeptime*1000L);
		} catch (Exception e) {
		}
		session.write("OK");
		logger.info("[" + threadName + "] [OUT] : " + message.toString());
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) {
		logger.info("Disconnecting the idle.");
		// disconnect an idle client
		session.close();
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		// close the connection on exceptional situation
		session.close();
	}

}
