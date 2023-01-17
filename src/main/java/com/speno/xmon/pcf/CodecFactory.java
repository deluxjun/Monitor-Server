package com.speno.xmon.pcf;


import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineDecoder;
import org.apache.mina.filter.codec.textline.TextLineEncoder;

/**
 * 
 * @author YYS
 * @see null 모든 문자열을 한번에 받기 위한 필터 제작
 */
public class CodecFactory implements ProtocolCodecFactory{
	private Map<IoSession,ProtocolDecoder> decoderHolder = new HashMap<IoSession, ProtocolDecoder>();
	private ProtocolEncoder encoder;
	private ProtocolDecoder textDecoder = null;
	
	public CodecFactory(Charset charset,LineDelimiter delimiter,boolean text,boolean client){
		if(text){
		 if (delimiter.equals(LineDelimiter.AUTO)) {	           
	            encoder = new TextLineEncoder(charset);
	        } else {
	            encoder = new TextLineEncoder(charset, delimiter);
	        }
		 textDecoder = new TextLineDecoder(charset, delimiter);		
		}
		else{
			if(client){
				encoder = new ClientEncoder();
//				decoder = new ServerNClientDecoder();
			}
			else{
				encoder = new ServerEncoder();
//				decoder = new ServerNClientDecoder();
			}
			
		}
	}
	
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		if (textDecoder != null)
			return textDecoder;
		
		synchronized (arg0) {
			ProtocolDecoder decoder = decoderHolder.get(arg0);
			if (decoder == null) {
				decoder = new ServerNClientDecoder();
				decoderHolder.put(arg0, decoder);
			}
			return decoder;
		}
	}

	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		return encoder;
	}

}
