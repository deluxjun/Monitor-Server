package com.speno.xmon.agent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.speno.xmon.codedic.DicCommands;
import com.speno.xmon.codedic.DicOrderAdd;
import com.speno.xmon.codedic.DicOrderTypes;
import com.speno.xmon.codedic.DicValueUnit;
import com.speno.xmon.codedic.DicXmlProperties;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.compress.ZIPcompress;
import com.speno.xmon.pcf.CodecFactory;

/**
 * An TCP server used for performance tests.
 * 
 * It does nothing fancy, except receiving the messages, and counting the number of
 * received messages.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class CommandNioSession extends IoHandlerAdapter implements Runnable, ILogListener {

	private IoConnector connector;
//	private IoSession session;
	//CommandSessionHandler  commandSessionHnd;
    private static ILogger LOG = new LoggerDummy();
    private boolean connected = false;
    
    private boolean shutdown = false;
    
    /** A counter incremented for every recieved message */
    private AtomicInteger nbReceived = new AtomicInteger(0);
    
    public void setLogger(ILogger log) {
    	LOG = log;
    }

    public CommandNioSession() throws IOException {
    	// junsoo, 20141021, set main logger
    	MainXAgent.addLogListener(this);
    	
        LOG.info("Created a NioSocketConnector");
        this.connector = new NioSocketConnector();

        DefaultIoFilterChainBuilder chain	= this.connector.getFilterChain();
        if(xmProperties.useProtocolCodecFilter)
        {
	        CodecFactory fac 				= new CodecFactory(Charset.forName("euc-kr"), LineDelimiter.AUTO,false, false);
	        ProtocolCodecFilter protocol 	= new ProtocolCodecFilter(fac);
	        chain.addFirst("protocol", protocol);
        }
        
        LoggingFilter loggingFilter = new LoggingFilter("logger");
		loggingFilter.setMessageReceivedLogLevel(LogLevel.NONE);
		loggingFilter.setMessageSentLogLevel(LogLevel.NONE);
		loggingFilter.setSessionIdleLogLevel(LogLevel.NONE);
        chain.addLast("logger", loggingFilter);
        
        LOG.info("Setting the handler");
        this.connector.setHandler(this);
        //this.commandSessionHnd = new CommandSessionHandler();
    }
    
    public synchronized void shutdown() {
    	shutdown = true;
    	try {
        	if (connector != null) {
        		connector.dispose();
        	}

        	notifyAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	public void run() 
	{
	    while(!shutdown)
	    {
	    	 try {
				Thread.sleep( Integer.parseInt( xmProperties.htXmPropertiesAgent_Main.get( xmProperties.SessionCheckRepeatMs)));
//				if( this.session != null && this.session.isConnected() ) continue;
				if( connected ) {
					connected = true;
				}
				else{
		        LOG.info("======== connecting to the CommandServer:" + xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AmassServerIP)  + " Port:" + xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AmassCommdPort));
		        ConnectFuture connFuture = connector.connect(new InetSocketAddress(xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AmassServerIP) , Integer.parseInt( xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AmassCommdPort))));
		        ((SocketSessionConfig) connector.getSessionConfig()).setTcpNoDelay(true);
	 	        connFuture.awaitUninterruptibly();
		        
		        connFuture.addListener(new IoFutureListener<ConnectFuture>() {
		            public void operationComplete(ConnectFuture future) 
		            {
		                try {
			                if (future.isConnected()) {
			                	LOG.info("Connected to CommandServer");
			                    IoSession session = future.getSession();
			                    session.setAttribute("sessionType", "Agent");
			                    LOG.info("Session MaxReadBufferSize:" + session.getConfig().getMaxReadBufferSize());
			                    agentSessionInit(session);
			                    connected = true;
			                    
			                } else {
			                	LOG.error("Not connected...Wait ms:" + xmProperties.htXmPropertiesAgent_Main.get(xmProperties.SessionCheckRepeatMs));
			                    connected = false;
			                }
	                    } catch (Exception e) {
	                    	connected = false;
	                        e.printStackTrace();
	                        LOG.error(e.getMessage());
	                    }
		            } //End operationComplete
		        });
				}
			}
			catch (InterruptedException e1) {
			
				e1.printStackTrace();
			}
			catch (Exception e) {
				LOG.error(e.getMessage());
				LOG.error("Reconnect Has error.but do not concern");
			}
	    }
	}


    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        byte[] byteJsonData = null;
        
       	if(message instanceof String) {
    		//LOG.info("Received string:"+message);
       		byteJsonData = message.toString().getBytes();
    	} else if(message instanceof byte[]) {
    		//LOG.info("Received bytes:"+new String((byte[])message) );
    		byteJsonData = (byte[])message;
    	}
    	else if (message instanceof IoBuffer)
    	{
            IoBuffer buffer = (IoBuffer) message;
            if(xmProperties.useProtocolCodecFilter)
            {
            	byteJsonData = buffer.array();	
            }
            else
            {
                byteJsonData = new byte[buffer.remaining()];
                buffer.get(byteJsonData);
            }
            
            ((IoBuffer) message).flip();
        }
       	//LOG.info("Received:" + session.getLocalAddress().toString() + " Length:" + byteJsonData.length);

//       	Thread sendMsg = new Thread(new CommandNioSessionThread(session, byteJsonData));
//       	sendMsg.setName("ThreadMsgReceived");
//       	sendMsg.start();
       	
       	processReceiving(session, byteJsonData);
    }    
    
    
    
	private void agentSessionInit(IoSession session)
	{
		try {
			JSONObject jsonAgentInit 		= new JSONObject();
			JSONArray jsonArray 			= new JSONArray(); 
			JSONObject jsonAgentInitSub = null;
			
			jsonAgentInit.put(DicCommands.SessionAgent, xmProperties.htXmPropertiesAgent_Main.get( xmProperties.AgentMyName));
			
			jsonAgentInit.put(DicOrderTypes.OrderType,  DicOrderTypes.INIT_AGENT);
			jsonAgentInit.put(DicOrderAdd.RequestDateTime, SimDate.getDateTimeFormatter_MS().format(new Date(System.currentTimeMillis())));
			
			
			// add dummy
			jsonAgentInitSub = new JSONObject();
			jsonAgentInitSub.put(DicOrderAdd.DUMMY, DicOrderAdd.DUMMY);
			jsonAgentInitSub.put(DicOrderAdd.Command, DicOrderAdd.DUMMY);
			jsonAgentInitSub.put(DicOrderAdd.AgentName, xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AgentMyName));
			jsonAgentInitSub.put(DicOrderAdd.ValueUnit, DicValueUnit.ValueUnit_String);

			jsonArray.put(jsonAgentInitSub);
			
			Iterator<String> tempKeys =  DicCommands.AgentMyActionsList.keySet().iterator();
			HashMap<String, String> m  = null;
			while(tempKeys.hasNext())
			{
				m = DicCommands.AgentMyActionsList.get(tempKeys.next());
				if(m == null) continue;
				jsonAgentInitSub = new JSONObject();
				jsonAgentInitSub.put(DicOrderAdd.Command, DicOrderAdd.ActionName);
				jsonAgentInitSub.put(DicOrderAdd.AgentName, xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AgentMyName));
				
				jsonAgentInitSub.put(DicOrderAdd.ValueUnit, DicValueUnit.ValueUnit_String);
				jsonAgentInitSub.put(DicOrderAdd.ReturnMsg, "");
				jsonAgentInitSub.put(DicOrderAdd.ReturnCode, 0);
				jsonAgentInitSub.put(DicXmlProperties.ActionName,  	m.get(DicXmlProperties.TranName) );
				jsonAgentInitSub.put(DicXmlProperties.Title,  				m.get(DicXmlProperties.Title) );
				jsonAgentInitSub.put(DicXmlProperties.Description,  	m.get(DicXmlProperties.Description) );
				jsonAgentInitSub.put(DicXmlProperties.AggreUseYN,  m.get(DicXmlProperties.AggreUseYN) );
				jsonAgentInitSub.put(DicXmlProperties.HealthUseYN,  m.get(DicXmlProperties.HealthUseYN) );
				
				jsonArray.put(jsonAgentInitSub);
			}
			
			if(DicCommands.ResourceCommand.size() > 0)
			{
				jsonAgentInitSub = new JSONObject();
				jsonAgentInitSub.put(DicOrderAdd.Command, DicOrderAdd.CommandList);
				jsonAgentInitSub.put(DicOrderAdd.AgentName, xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AgentMyName));
				
				jsonAgentInitSub.put(DicOrderAdd.ValueUnit, DicValueUnit.ValueUnit_String);
				jsonAgentInitSub.put(DicOrderAdd.ReturnMsg, "");
				jsonAgentInitSub.put(DicOrderAdd.ReturnCode, 0);
				jsonAgentInitSub.put("USE",  DicCommands.ResourceCommand);
				jsonArray.put(jsonAgentInitSub);
			}
			
			if(xmProperties.LogIdComma.length() > 0)
			{
				jsonAgentInitSub = new JSONObject();
				jsonAgentInitSub.put(DicOrderAdd.Command, DicOrderAdd.LogID);
				jsonAgentInitSub.put(DicOrderAdd.AgentName, xmProperties.htXmPropertiesAgent_Main.get(xmProperties.AgentMyName));
				jsonAgentInitSub.put(DicOrderAdd.ValuesComma, xmProperties.LogIdComma);
				jsonAgentInitSub.put(DicOrderAdd.ValueUnit, DicValueUnit.ValueUnit_String);
				jsonAgentInitSub.put(DicOrderAdd.ReturnMsg, "");
				jsonAgentInitSub.put(DicOrderAdd.ReturnCode, 0);
				jsonArray.put(jsonAgentInitSub);
			}

			jsonAgentInit.put(DicOrderTypes.INIT_AGENT, jsonArray);
			this.agentSessionInitSendData(session, jsonAgentInit.toString());
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean agentSessionInitSendData(IoSession session, String jsonSendData ) {
		if(jsonSendData.trim().equals("")) return true;
		byte[] byteJsonData =  ZIPcompress.deflate(xmProperties.networkCompress, jsonSendData);

    	if(byteJsonData.length >  Integer.parseInt( xmProperties.htXmPropertiesAgent_Main.get(xmProperties.MaxJsonDataLength)))
    	{
    		LOG.error("send fail: byteJsonData.length > Properties.Transmit_MaxJsonDataLength: " 
    	                         + byteJsonData.length +">"+ xmProperties.htXmPropertiesAgent_Main.get(xmProperties.MaxJsonDataLength));
    	}
		
		try {
				if(session.isConnected())
				{
					 LOG.info("AmassServer is Connected successful:" + session.getLocalAddress() + "=>" + session.getRemoteAddress() + " Session init...:");
			            if(xmProperties.useProtocolCodecFilter)
			            {
							 session.write(byteJsonData);
			            }
			            else
			            {
			        		IoBuffer buffer = IoBuffer.allocate(byteJsonData.length);
			        		buffer.put(byteJsonData);
			        		buffer.flip();
			        		session.write(buffer);
			            }
				}
				else
				{					
					LOG.error("TransactionRetryConnectionMs ...:" + xmProperties.htXmPropertiesAgent_Main.get( xmProperties.TransactionRetryConnectionMs) + "ms");
					Thread.sleep( Integer.parseInt(xmProperties.htXmPropertiesAgent_Main.get( xmProperties.TransactionRetryConnectionMs)));
			        connected = false;
			        return false;
				}
			}  catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
    }
	public void processReceiving(IoSession session, byte[] byteJsonData) {
		if(byteJsonData == null) return;
   		String msgToAmassServer = this.receivedMsgProcessing(byteJsonData); 
   		
   		if(msgToAmassServer.equals("")) return;
   		this.SendMessageToAmassServer(session, msgToAmassServer);
	}
    private boolean SendMessageToAmassServer(IoSession session, String jsonSendData) {
    	if(jsonSendData.trim().equals("")) return true;
       	byte[] byteJsonDatadd = ZIPcompress.deflate(xmProperties.networkCompress, jsonSendData);
       	
       	if(xmProperties.useProtocolCodecFilter)
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
        //LOG.info("write:" + session.getLocalAddress().toString() + " Length:" + byteJsonDatadd.length);
        LOG.debug("To Server:" + jsonSendData);

        return true;
	}
    ZIPcompress zipcomp = new ZIPcompress();
	public String receivedMsgProcessing(byte[] byteJsonData) 
	{
		String msgToAmassServer		= "";
       	try {
       		String msg = "";
       		try {
           		msg = new String( ZIPcompress.inflate(xmProperties.networkCompress, byteJsonData), "UTF-8");
			} catch (Exception e) {
           		msg = new String( ZIPcompress.inflate(xmProperties.networkCompress, byteJsonData));
			}

            if(msg.equals(""))
            {
            	msgToAmassServer = "ZipDeComp Error";
            }
      
            JSONObject jsonObject_GetMsg			= null;
            if(msg.length() > 0)
            {
					jsonObject_GetMsg			= new JSONObject(msg);
            }
            else
            {
            	LOG.error("messageReceived:" +  new String(msg));
            	return "";
            }
	        if(msgToAmassServer.equals("ZipDeComp Error"))
	        {
	        	LOG.error("ZipDeComp Error");
	        	return "";
	        }
	        
	        // 20141201, new command
	        if (jsonObject_GetMsg.has(DicOrderAdd.Command)) {
	        	String command = jsonObject_GetMsg.get(DicOrderAdd.Command).toString();
				
	        	JSONObject root						= new JSONObject();
				JSONArray data						= new JSONArray();
				
				root.put(DicOrderAdd.ConsoleId, jsonObject_GetMsg.getString(DicOrderAdd.ConsoleId));
				root.put(DicOrderAdd.AgentName, jsonObject_GetMsg.getString(DicOrderAdd.AgentName));

				if ("getResource".equals(command)) {
	        		root.put(DicOrderAdd.ReturnMsg, "");
	        		root.put(DicOrderAdd.ReturnCode, 0);
	        	}

				root.put("Data", jsonObject_GetMsg.getString(DicOrderAdd.AgentName));

        		return root.toString();
	        }
	        
	        String agentName								= jsonObject_GetMsg.get(DicOrderAdd.TargetAgent).toString();
	        String orderType 								= jsonObject_GetMsg.get(DicOrderTypes.OrderType).toString();
			String RequestDateTime						= jsonObject_GetMsg.get(DicOrderAdd.RequestDateTime).toString();
			
			JSONArray jsonOrderArray_GetMsg	= jsonObject_GetMsg.getJSONArray(orderType);
			JSONObject jsonSubObject_GetMsg	= null;
			
			JSONObject jsonObject						= new JSONObject();
			JSONArray jsonArray							= new JSONArray();
			
			jsonObject.put(DicOrderAdd.AgentName, agentName);
			jsonObject.put(DicOrderAdd.SendTime, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new Date(System.currentTimeMillis())));
	
			String returnOrderType = "";
			if(orderType.trim().equals(DicOrderTypes.RequestAgent) )
		    {
				jsonObject.put(DicOrderTypes.OrderType, DicOrderTypes.ResponseAgent);
				jsonObject.put(DicOrderAdd.ConsoleCmdID, jsonObject_GetMsg.getString(DicOrderAdd.ConsoleCmdID));
				
				jsonObject.put(DicOrderAdd.ConsoleId, jsonObject_GetMsg.getString(DicOrderAdd.ConsoleId));
				
				jsonObject.put(DicOrderAdd.ReturnMsg, "");
				jsonObject.put(DicOrderAdd.ReturnCode, 0);
				
				returnOrderType = DicOrderTypes.ResponseAgent;
		    }
			else if (orderType.trim().equals(DicOrderTypes.RequestAgentSet))
			{
				jsonObject.put(DicOrderTypes.OrderType, DicOrderTypes.ResponseAgentSet);
				jsonObject.put(DicOrderAdd.ConsoleCmdID, jsonObject_GetMsg.getString(DicOrderAdd.ConsoleCmdID));
				
				jsonObject.put(DicOrderAdd.ConsoleId, jsonObject_GetMsg.getString(DicOrderAdd.ConsoleId));
				
				jsonObject.put(DicOrderAdd.ReturnMsg, "");
				jsonObject.put(DicOrderAdd.ReturnCode, 0);
				
				returnOrderType = DicOrderTypes.ResponseAgentSet;
			}
			else if(orderType.trim().equals(DicOrderTypes.RequestAgentShort)) 
		    {
		    	jsonObject.put(DicOrderTypes.OrderType, DicOrderTypes.ResponseAgentShort);
		    	returnOrderType = DicOrderTypes.ResponseAgentShort;
		    }
			
			String commandSub 	= "";
			String valueUnit			= "";		
			JsonGeneratorAgent jsonGen = new JsonGeneratorAgent();
			for(int i =0 ; i<jsonOrderArray_GetMsg.length();i ++)
			{
				jsonSubObject_GetMsg =jsonOrderArray_GetMsg.getJSONObject(i);
				
				commandSub	= jsonSubObject_GetMsg.getString(DicOrderAdd.Command);	//Req_Memory_USE
				valueUnit			= jsonSubObject_GetMsg.getString(DicOrderAdd.ValueUnit);	//MB, GB, KB, Byte
				  
				if(commandSub.equals(DicCommands.Set_EnvAgent))
				{
					String[] PropertyNames		= jsonObject_GetMsg.getString(DicOrderAdd.PropertyNames).split(",");
					
					xmProperties xmProp = new xmProperties(); 
					for(int pn=0; pn<PropertyNames.length;pn++)
					{
						String[] propName		= JSONObject.getNames(jsonSubObject_GetMsg);
						for(int p=0;p< propName.length; p++)
						{
							boolean bMemorySet = false;
							if(xmProperties.htXmPropertiesAgent_Main.containsKey(propName[p]))
							{
								xmProperties.htXmPropertiesAgent_Main.put(propName[p], jsonSubObject_GetMsg.getString(propName[p]) );
								bMemorySet = true;
							}
							else
							{
								continue;
							}
							
							boolean bRet = xmProp.setProperties(propName[p], jsonSubObject_GetMsg.getString(propName[p]));
							if(!bRet && !bMemorySet) 
							{
								jsonObject.put(DicOrderAdd.ReturnMsg, "SetProperty Fail:" + propName[p]);
								jsonObject.put(DicOrderAdd.ReturnCode, -2);
								jsonObject.put(DicOrderTypes.ResponseSet,jsonArray);
								return  jsonObject.toString();
							}
							jsonSubObject_GetMsg.put(propName[p], jsonSubObject_GetMsg.getString(propName[p]));
				  			jsonArray.put(jsonSubObject_GetMsg);
						}
					} //End For
				}else if(commandSub.equals(DicCommands.Set_EnvProperties_Main))
				{
					String properties = "";
		  			Iterator<String> propertiesMain = xmProperties.htXmPropertiesAgent_Main.keySet().iterator();
		  			while(propertiesMain.hasNext())
		  			{
		  				properties = propertiesMain.next();
		  				//"Command":"Set_EnvProperties_Main","USE":"ActionsList","VALUE":"Inquiry, DbConn","ValueUnit":""
		  				JSONObject jsonSubObject = new JSONObject();  
		  				jsonSubObject.put("USE", properties );
		  				jsonSubObject.put("VALUE", xmProperties.htXmPropertiesAgent_Main.get(properties) );
		  				jsonSubObject.put( DicOrderAdd.Command , jsonSubObject_GetMsg.getString(DicOrderAdd.Command));
		  				jsonSubObject.put( DicOrderAdd.ValueUnit, jsonSubObject_GetMsg.getString(DicOrderAdd.ValueUnit));
		  				
			  			jsonArray.put(jsonSubObject);
		  			}
				}
			    else {
			    	String base = jsonSubObject_GetMsg.getString(DicOrderAdd.PropertyNames);
			    	String[] propertyNames = null;
			    	if (base.indexOf(xmProperties.Sep) > 0) {
			    		propertyNames = jsonSubObject_GetMsg.getString(DicOrderAdd.PropertyNames).split(xmProperties.Sep);
			    	} else {
			    		propertyNames = jsonSubObject_GetMsg.getString(DicOrderAdd.PropertyNames).split(",");
			    	}
					jsonArray = jsonGen.resourceValue(jsonArray, commandSub, valueUnit, propertyNames);
				}
			} // End jsonOrderArray_GetMsg
			
			if(jsonArray == null) return "";
			
	    	jsonObject.put(returnOrderType, jsonArray);
	        msgToAmassServer = jsonObject.toString();
	        return msgToAmassServer;
		}
		catch (JSONException e) {
			LOG.error(new String(byteJsonData));
			e.printStackTrace();
		}

       	return "";
	}
    
    
    

	/**
     * {@inheritDoc}
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        LOG.info("Session closed...");

        // Reinitialize the counter and expose the number of received messages
        LOG.info("Nb message received : " + nbReceived.get());
        connected = false;

        nbReceived.set(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        //LOG.info("Session created...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        //LOG.info("Session idle...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        //LOG.info("Session Opened...");
    }
}
