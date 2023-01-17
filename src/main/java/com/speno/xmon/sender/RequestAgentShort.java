package com.speno.xmon.sender;

import java.nio.charset.CharacterCodingException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.compress.ZIPcompress;
import com.speno.xmon.env.ItemAgent;
import com.speno.xmon.env.xmPropertiesXml;

public class RequestAgentShort extends IoHandlerAdapter {

	private final static Logger LOG = LoggerFactory.getLogger(RequestAgentShort.class);
	private ZIPcompress z = new ZIPcompress();
    
    /*
     * {"TargetAgent":"xtorm01","OrderType":"RequestAgentShort"
     * ,"RequestAgentShort":[
     * {"Command":"Req_JavaHeap","ValueUnit":"KB"}]
     * ,"RequestDateTime":"2014-07-22 15:20:06.219"}
     */
	public int SendRequestCmd(String tempAgentName, String requestMessage) throws CharacterCodingException, InterruptedException {
		if(requestMessage == null || requestMessage.equals("")) return -1;
		
		ItemAgent tempAgengObj = xmPropertiesXml.htAgentList.get(tempAgentName);
		if( tempAgengObj == null || tempAgengObj.getMinaSession(false) == null)
		{
			LOG.error("Agent Session is null:" + tempAgentName);
			return -1;
		}
		if(! tempAgengObj.getMinaSession(false).isConnected())
		{
			LOG.error("Agent Session is Not Connected");
			return -9;
		}
		if(this.sendingMessageResoRequest(tempAgengObj.getMinaSession(true), requestMessage ))
			return 1;
		else
			return -5;
	}
	
    protected boolean sendingMessageResoRequest(IoSession session, String jsonSendData) {
        byte[] byteJsonDatadd = ZIPcompress.deflate(xmPropertiesXml.networkCompress, jsonSendData);
        if(byteJsonDatadd == null) 
        {
        	LOG.error("sendingMessageResoRequest -> sendingResRequestCommand is null" );
        	return false;
        }
        if(!session.isConnected())
        {
        	LOG.error("session.isConnected is null" + session.toString());
        	return false;
        }
       	if(xmPropertiesXml.useProtocolCodecFilter)
       	{
       		session.write(byteJsonDatadd);
       	}
       	else
       	{
       		IoBuffer rbuffer = IoBuffer.allocate(byteJsonDatadd.length);
            rbuffer.put(byteJsonDatadd);
            rbuffer.flip();
            WriteFuture future = session.write(rbuffer);
       	}
       	return true;
	}
}

