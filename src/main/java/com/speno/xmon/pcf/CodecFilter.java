package com.speno.xmon.pcf;

import java.net.SocketAddress;
import java.util.Queue;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.NothingWrittenException;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestWrapper;
import org.apache.mina.filter.codec.AbstractProtocolDecoderOutput;
import org.apache.mina.filter.codec.AbstractProtocolEncoderOutput;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderException;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.RecoverableProtocolDecoderException;

public class CodecFilter extends IoFilterAdapter{	
	    private static final Class<?>[] EMPTY_PARAMS = new Class[0];
	    private static final IoBuffer EMPTY_BUFFER = IoBuffer.wrap(new byte[0]);
	    private final AttributeKey ENCODER = new AttributeKey(ProtocolCodecFilter.class, "encoder");
	    private final AttributeKey DECODER = new AttributeKey(ProtocolCodecFilter.class, "decoder");
	    private final AttributeKey DECODER_OUT = new AttributeKey(ProtocolCodecFilter.class, "decoderOut");
	    private final AttributeKey ENCODER_OUT = new AttributeKey(ProtocolCodecFilter.class, "encoderOut");
	   
	    private final ProtocolCodecFactory factory;

	    public CodecFilter(ProtocolCodecFactory factory) {
	        if (factory == null) {
	            throw new IllegalArgumentException("factory");
	        }
	        this.factory = factory;
	    }

	
	    public CodecFilter(final ProtocolEncoder encoder, final ProtocolDecoder decoder) {
	        if (encoder == null) {
	            throw new IllegalArgumentException("encoder");
	        }
	        if (decoder == null) {
	            throw new IllegalArgumentException("decoder");
	        }
	     
	        this.factory = new ProtocolCodecFactory() {
	            public ProtocolEncoder getEncoder(IoSession session) {
	                return encoder;
	            }

	            public ProtocolDecoder getDecoder(IoSession session) {
	                return decoder;
	            }
	        };
	    }
	    
	    public CodecFilter(final Class<? extends ProtocolEncoder> encoderClass,
	            final Class<? extends ProtocolDecoder> decoderClass) {
	        if (encoderClass == null) {
	            throw new IllegalArgumentException("encoderClass");
	        }
	        if (decoderClass == null) {
	            throw new IllegalArgumentException("decoderClass");
	        }
	        if (!ProtocolEncoder.class.isAssignableFrom(encoderClass)) {
	            throw new IllegalArgumentException("encoderClass: " + encoderClass.getName());
	        }
	        if (!ProtocolDecoder.class.isAssignableFrom(decoderClass)) {
	            throw new IllegalArgumentException("decoderClass: " + decoderClass.getName());
	        }
	        try {
	            encoderClass.getConstructor(EMPTY_PARAMS);
	        } catch (NoSuchMethodException e) {
	            throw new IllegalArgumentException("encoderClass doesn't have a public default constructor.");
	        }
	        try {
	            decoderClass.getConstructor(EMPTY_PARAMS);
	        } catch (NoSuchMethodException e) {
	            throw new IllegalArgumentException("decoderClass doesn't have a public default constructor.");
	        }

	        final ProtocolEncoder encoder;

	        try {
	            encoder = encoderClass.newInstance();
	        } catch (Exception e) {
	            throw new IllegalArgumentException("encoderClass cannot be initialized");
	        }

	        final ProtocolDecoder decoder;

	        try {
	            decoder = decoderClass.newInstance();
	        } catch (Exception e) {
	            throw new IllegalArgumentException("decoderClass cannot be initialized");
	        }
	      
	        this.factory = new ProtocolCodecFactory() {
	            public ProtocolEncoder getEncoder(IoSession session) throws Exception {
	                return encoder;
	            }

	            public ProtocolDecoder getDecoder(IoSession session) throws Exception {
	                return decoder;
	            }
	        };
	    }

	    public ProtocolEncoder getEncoder(IoSession session) {
	        return (ProtocolEncoder) session.getAttribute(ENCODER);
	    }

	    @Override
	    public void onPreAdd(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
	        if (parent.contains(this)) {
	            throw new IllegalArgumentException(
	                    "You can't add the same filter instance more than once.  Create another instance and add it.");
	        }
	    }

	    @Override
	    public void onPostRemove(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {	    
	        disposeCodec(parent.getSession());
	    }

	    @Override
	    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {     
	        if (!(message instanceof IoBuffer)) {
	            nextFilter.messageReceived(session, message);
	            return;
	        }

	        IoBuffer in = (IoBuffer) message;
	        ProtocolDecoder decoder = factory.getDecoder(session);
	        ProtocolDecoderOutput decoderOut = getDecoderOut(session, nextFilter);

	        while (in.hasRemaining()) {
	            int oldPos = in.position();

	            try {
	                synchronized (decoderOut) {	                    
	                    decoder.decode(session, in, decoderOut);
	                }
	             
	                decoderOut.flush(nextFilter, session);
	            } catch (Throwable t) {
	                ProtocolDecoderException pde;
	                if (t instanceof ProtocolDecoderException) {
	                    pde = (ProtocolDecoderException) t;
	                } else {
	                    pde = new ProtocolDecoderException(t);
	                }

	                if (pde.getHexdump() == null) {	                    
	                    int curPos = in.position();
	                    in.position(oldPos);
	                    pde.setHexdump(in.getHexDump());
	                    in.position(curPos);
	                }	              
	                decoderOut.flush(nextFilter, session);
	                nextFilter.exceptionCaught(session, pde);
	           
	                if (!(t instanceof RecoverableProtocolDecoderException) || (in.position() == oldPos)) {
	                    break;
	                }
	            }
	        }
	    }

	    @Override
	    public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
	        if (writeRequest instanceof EncodedWriteRequest) {
	            return;
	        }

	        if (writeRequest instanceof MessageWriteRequest) {
	            MessageWriteRequest wrappedRequest = (MessageWriteRequest) writeRequest;
	            nextFilter.messageSent(session, wrappedRequest.getParentRequest());
	        } else {
	            nextFilter.messageSent(session, writeRequest);
	        }
	    }

	    @Override
	    public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
	        Object message = writeRequest.getMessage();

	        if ((message instanceof IoBuffer) || (message instanceof FileRegion)) {
	            nextFilter.filterWrite(session, writeRequest);
	            return;
	        }

	        ProtocolEncoder encoder = factory.getEncoder(session);

	        ProtocolEncoderOutput encoderOut = getEncoderOut(session, nextFilter, writeRequest);

	        if (encoder == null) {
	            throw new ProtocolEncoderException("The encoder is null for the session " + session);
	        }

	        if (encoderOut == null) {
	            throw new ProtocolEncoderException("The encoderOut is null for the session " + session);
	        }

	        try {	          
	            encoder.encode(session, message, encoderOut);
	           
	            Queue<Object> bufferQueue = ((AbstractProtocolEncoderOutput) encoderOut).getMessageQueue();
	          
	            while (!bufferQueue.isEmpty()) {
	                Object encodedMessage = bufferQueue.poll();

	                if (encodedMessage == null) {
	                    break;
	                }
	              
	                if (!(encodedMessage instanceof IoBuffer) || ((IoBuffer) encodedMessage).hasRemaining()) {
	                    SocketAddress destination = writeRequest.getDestination();
	                    WriteRequest encodedWriteRequest = new EncodedWriteRequest(encodedMessage, null, destination);

	                    nextFilter.filterWrite(session, encodedWriteRequest);
	                }
	            }
	       
	            nextFilter.filterWrite(session, new MessageWriteRequest(writeRequest));
	        } catch (Throwable t) {
	            ProtocolEncoderException pee;
	          
	            if (t instanceof ProtocolEncoderException) {
	                pee = (ProtocolEncoderException) t;
	            } else {
	                pee = new ProtocolEncoderException(t);
	            }

	            throw pee;
	        }
	    }

	    @Override
	    public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
	        
	        ProtocolDecoder decoder = factory.getDecoder(session);
	        ProtocolDecoderOutput decoderOut = getDecoderOut(session, nextFilter);

	        try {
	            decoder.finishDecode(session, decoderOut);
	        } catch (Throwable t) {
	            ProtocolDecoderException pde;
	            if (t instanceof ProtocolDecoderException) {
	                pde = (ProtocolDecoderException) t;
	            } else {
	                pde = new ProtocolDecoderException(t);
	            }
	            throw pde;
	        } finally {
	         
	            disposeCodec(session);
	            decoderOut.flush(nextFilter, session);
	        }
	       
	        nextFilter.sessionClosed(session);
	    }

	    private static class EncodedWriteRequest extends DefaultWriteRequest {
	        public EncodedWriteRequest(Object encodedMessage, WriteFuture future, SocketAddress destination) {
	            super(encodedMessage, future, destination);
	        }

	        public boolean isEncoded() {
	            return true;
	        }
	    }

	    private static class MessageWriteRequest extends WriteRequestWrapper {
	        public MessageWriteRequest(WriteRequest writeRequest) {
	            super(writeRequest);
	        }

	        @Override
	        public Object getMessage() {
	            return EMPTY_BUFFER;
	        }

	        @Override
	        public String toString() {
	            return "MessageWriteRequest, parent : " + super.toString();
	        }
	    }

	    private static class ProtocolDecoderOutputImpl extends AbstractProtocolDecoderOutput {
	        public ProtocolDecoderOutputImpl() {
	            // Do nothing
	        }

	        public void flush(NextFilter nextFilter, IoSession session) {
	            Queue<Object> messageQueue = getMessageQueue();
	            while (!messageQueue.isEmpty()) {
	                nextFilter.messageReceived(session, messageQueue.poll());
	            }
	        }
	    }

	    private static class ProtocolEncoderOutputImpl extends AbstractProtocolEncoderOutput {
	        private final IoSession session;
	        private final NextFilter nextFilter;
	        private final SocketAddress destination;

	        public ProtocolEncoderOutputImpl(IoSession session, NextFilter nextFilter, WriteRequest writeRequest) {
	            this.session = session;
	            this.nextFilter = nextFilter;
	            destination = writeRequest.getDestination();
	        }

	        public WriteFuture flush() {
	            Queue<Object> bufferQueue = getMessageQueue();
	            WriteFuture future = null;

	            while (!bufferQueue.isEmpty()) {
	                Object encodedMessage = bufferQueue.poll();

	                if (encodedMessage == null) {
	                    break;
	                }
	              
	                if (!(encodedMessage instanceof IoBuffer) || ((IoBuffer) encodedMessage).hasRemaining()) {
	                    future = new DefaultWriteFuture(session);
	                    nextFilter.filterWrite(session, new EncodedWriteRequest(encodedMessage, future, destination));
	                }
	            }

	            if (future == null) {	               
	                WriteRequest writeRequest = new DefaultWriteRequest(null, null, destination);
	                future = DefaultWriteFuture.newNotWrittenFuture(session, new NothingWrittenException(writeRequest));
	            }
	            return future;
	        }
	    }
	  
	    /**
	     * Dispose the encoder, decoder, and the callback for the decoded
	     * messages.
	     */
	    private void disposeCodec(IoSession session) {	       
	        disposeEncoder(session);
	        disposeDecoder(session);
	        disposeDecoderOut(session);
	    }

	    /**
	     * Dispose the encoder, removing its instance from the
	     * session's attributes, and calling the associated
	     * dispose method.
	     */
	    private void disposeEncoder(IoSession session) {
	        ProtocolEncoder encoder = (ProtocolEncoder) session.removeAttribute(ENCODER);
	        if (encoder == null) {
	            return;
	        }

	        try {
	            encoder.dispose(session);
	        } catch (Throwable t) {
	           
	        }
	    }

	    /**
	     * Dispose the decoder, removing its instance from the
	     * session's attributes, and calling the associated
	     * dispose method.
	     */
	    private void disposeDecoder(IoSession session) {
	        ProtocolDecoder decoder = (ProtocolDecoder) session.removeAttribute(DECODER);
	        if (decoder == null) {
	            return;
	        }
	        try {
	            decoder.dispose(session);
	        } catch (Throwable t) {
	           
	        }
	    }

	    /**
	     * Return a reference to the decoder callback. If it's not already created
	     * and stored into the session, we create a new instance.
	     */
	    private ProtocolDecoderOutput getDecoderOut(IoSession session, NextFilter nextFilter) {
	        ProtocolDecoderOutput out = (ProtocolDecoderOutput) session.getAttribute(DECODER_OUT);

	        if (out == null) {	           
	            out = new ProtocolDecoderOutputImpl();
	            session.setAttribute(DECODER_OUT, out);
	        }

	        return out;
	    }

	    private ProtocolEncoderOutput getEncoderOut(IoSession session, NextFilter nextFilter, WriteRequest writeRequest) {
	        ProtocolEncoderOutput out = (ProtocolEncoderOutput) session.getAttribute(ENCODER_OUT);

	        if (out == null) {	            
	            out = new ProtocolEncoderOutputImpl(session, nextFilter, writeRequest);
	            session.setAttribute(ENCODER_OUT, out);
	        }

	        return out;
	    }

	    /**
	     * Remove the decoder callback from the session's attributes.
	     */
	    private void disposeDecoderOut(IoSession session) {
	        session.removeAttribute(DECODER_OUT);
	    }
}
