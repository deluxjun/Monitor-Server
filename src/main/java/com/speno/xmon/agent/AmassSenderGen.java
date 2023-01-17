package com.speno.xmon.agent;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.speno.xmon.codedic.DicCommands;
import com.speno.xmon.compress.ZIPcompress;

public class AmassSenderGen implements ILogListener{

	private ILogger LOG = new LoggerDummy();
    
    private JsonGeneratorAgent jsonGenerator ;
	private IoSession session;
	
	public void setLogger(ILogger log) {
		LOG = log;
	}

		public AmassSenderGen() 
		{
	    	MainXAgent.addLogListener(this);
			this.jsonGenerator = new JsonGeneratorAgent();
	    }
		public void setSession(IoSession session)
		{
			this.session = session;
		}
		public String getSendingData()
		{
			return this.jsonGenerator.ResponseAgentShort(DicCommands.Trans_Unit);
		}
		public String getSendingLog()
		{
			return this.jsonGenerator.ResponseAgentShort(DicCommands.Log_Persist);
		}
		public String getSendingEvent() {
			return this.jsonGenerator.ResponseAgentShort(DicCommands.Event_Level);
		}
		
		// return : 0 = success, 1: general error, 100: connection error
		public int sendData(String jsonSendData ) {
			if(jsonSendData == null || jsonSendData.trim().equals("")) return 1;
			byte[] byteJsonData = ZIPcompress.deflate(xmProperties.networkCompress, jsonSendData);

//	    	if(byteJsonData.length > Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.MaxJsonDataLength)))
//        	{
//	    		LOG.error("send fail: byteJsonData.length > Properties.MaxJsonDataLength: " 
//        	                         + byteJsonData.length +">"+ xmProperties.htXmPropertiesAgent_Main.get(xmProperties.MaxJsonDataLength));
//	    		return 1;
//        	}
	    	
	    	
			try {
					if(!session.isConnected())
					{
						LOG.info("SessionCheckRepeatMs ...:" + xmProperties.htXmPropertiesAgent_Main.get( xmProperties.SessionCheckRepeatMs) + "ms");
						Thread.sleep( Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.SessionCheckRepeatMs)));
			    		return 100;
					}
					else
					{
						 if(xmProperties.useProtocolCodecFilter) {
							 session.write(byteJsonData);	 
							 LOG.debug("sent : " + jsonSendData);
						 }
						 else {
							 IoBuffer buffer = IoBuffer.allocate(byteJsonData.length);
							 buffer.put(byteJsonData);
							 buffer.flip();
							 session.write(buffer);
						 }
					}
			}  catch (InterruptedException e) {
                e.printStackTrace();
                LOG.error(e.getMessage());
                if (!session.isConnected())
                	return 100;
                
                return 1;
            }
            return 0;
	    }

}


