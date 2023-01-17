package com.speno.xmon.pcf;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class ClientEncoder implements ProtocolEncoder {

	public void dispose(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void encode(IoSession arg0, Object arg1, ProtocolEncoderOutput arg2)
			throws Exception {
		// TODO Auto-generated method stub
		byte[] data = (byte[])arg1;
		IoBuffer buffer = IoBuffer.allocate(4 + data.length);	
		buffer.putInt(data.length);
		buffer.put(data);
		buffer.flip();
		
		arg2.write(buffer);
	}

}