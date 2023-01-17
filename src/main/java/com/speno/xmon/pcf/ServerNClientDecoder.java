package com.speno.xmon.pcf;

import java.io.IOException;
import java.io.PushbackInputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServerNClientDecoder  extends CumulativeProtocolDecoder{
	private final static Logger LOG = LoggerFactory.getLogger(ServerNClientDecoder.class);

	private int totalSize = 0;
	private int currentSize = 0;
	private  IoBuffer savedBuffer = IoBuffer.allocate(4096, true);
	

	
	 private ThreadLocal<Integer> totalSizeTh = new ThreadLocal<Integer>();
	 private ThreadLocal<Integer> remianSizeTh = new ThreadLocal<Integer>();
	 private ThreadLocal<IoBuffer> savedBufferTh = new ThreadLocal<IoBuffer>();
	 private ThreadLocal<Boolean> isFirstTh = new ThreadLocal<Boolean>();

	
	 //private Map<IoSession, IoBuffer> savedbufferMap = new HashMap<IoSession,IoBuffer>();
	 private static final int minSize = 4;
	 public static final long maxSize = Long.MAX_VALUE;
	 
	 public  ServerNClientDecoder(){
		 if (savedBufferTh.get() == null)
			 savedBufferTh.set(IoBuffer.allocate(4096, true));
		savedBufferTh.get().setAutoExpand(true);
		savedBufferTh.get().setAutoShrink(true);
	}
	
	 private int getTotalSize() {
		 if (totalSizeTh.get() == null)
			 totalSizeTh.set(0);
		 return totalSizeTh.get();
	 }
	 private int getCurrentSize() {
		 if (remianSizeTh.get() == null)
			 remianSizeTh.set(0);
		 return remianSizeTh.get();
	 }
	 private boolean isFirst() {
		 if (isFirstTh.get() == null)
			 isFirstTh.set(false);
		 return isFirstTh.get();
	 }
	 
	@Override
	protected boolean doDecode(IoSession session, IoBuffer message, ProtocolDecoderOutput decoderOut) throws Exception {
		
//            // worker
//        	IoBuffer buffer = (IoBuffer) message;
//    		byte[] src  = buffer.array();	
//    		byte[] byteJsonData = new byte[src.length];
//    		System.arraycopy(src, 0, byteJsonData, 0, src.length);
//
//    		String reciveJsonData;
//			reciveJsonData	= new String(byteJsonData); 
//    		// print debug
//    		LOG.debug("=========== [" + Thread.currentThread().getName() +"] received: " + reciveJsonData);

	
		//FIXME: YYS 여기서 버그남 데이터를 버리는 부분이 존재함 
		/*if (isDecodable(session, message) == MessageDecoderResult.NEED_DATA) {			
			return false;
		}		*/
	
//		Boolean isFirst;
//		if(isFirstMap.get(session) != null)
//		isFirst = isFirstMap.get(session);
//		else{
//		isFirst = false;	
//		isFirstMap.put(session, isFirst);
//		}		
			
		if (!isFirst()) {
//			totalSize = message.getInt();
			totalSizeTh.set(message.getInt());
			if (getTotalSize() <= 0) {
			    throw new IOException("Total length of message is negative : " + getTotalSize());
			}
			if (getTotalSize() > maxSize)
			    throw new IOException("Total length of message is big : " + getTotalSize());
			
			if (message.remaining() == getTotalSize()) {				
				savedBufferTh.get().put(message);				
				savedBufferTh.get().flip();			
				decodeBody(session, decoderOut);
				reset();
//				replaceMap(isFirst()Map, session, isFirst,true);
			} else { // not yet
				isFirstTh.set(true);
				remianSizeTh.set(getTotalSize() - message.remaining());
				savedBufferTh.get().put(message);				
//				replaceMap(isFirstMap, session, isFirst,false);
			}
			return true;
		}		
		
		int readableSize = getCurrentSize() - message.remaining();		
		if (readableSize <= 0) { // all received
			savedBufferTh.get().put(message);		
			savedBufferTh.get().flip();
			decodeBody(session, decoderOut);
			reset();
		} else {
			isFirstTh.set(true);
			remianSizeTh.set(getCurrentSize() - message.remaining());
			savedBufferTh.get().put(message);			

		}
		return true;
	}
	
	private void decodeBody(IoSession session, ProtocolDecoderOutput out) throws IOException{
		PushbackInputStream stream = new PushbackInputStream(savedBufferTh.get().asInputStream(), savedBufferTh.get().remaining());	
		if(getTotalSize() > 0){
			byte[] data = new byte[getTotalSize()];
			stream.read(data,0,getTotalSize());
			IoBuffer buffer= IoBuffer.allocate(getTotalSize());		
			buffer.put(data);	
			out.write(buffer);
		}
	}
	
	private MessageDecoderResult isDecodable(IoSession session, IoBuffer in) {
		if (in.remaining() < minSize) {
			return MessageDecoderResult.NEED_DATA;
		}
		return MessageDecoderResult.OK;
	}

	private void reset() {
		savedBufferTh.get().clear();
//		totalSizeTh.set(0);
		remianSizeTh.set(0);
		isFirstTh.set(false);		
	}
		

}
