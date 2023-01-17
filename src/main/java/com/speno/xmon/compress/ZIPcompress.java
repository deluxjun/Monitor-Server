package com.speno.xmon.compress;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZIPcompress {
	
	private final static Logger LOG = LoggerFactory.getLogger(ZIPcompress.class);
	 
    public static void main(String[] args) throws IOException{
        long startTime = System.currentTimeMillis();
        
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        BufferedOutputStream out = 
                    new BufferedOutputStream(
                               new GZIPOutputStream(
                                           new FileOutputStream("test.gz")));
        
        LOG.debug("Writing file");
        int c;
        
        while((c=in.read()) != -1)
              out.write(c);
        in.close();
        out.close();

        long endTime = System.currentTimeMillis();
        LOG.debug("압축시간 : " + (endTime - startTime) + " ms");
        LOG.debug("Reading file");
        
        
        
        startTime = System.currentTimeMillis();
        BufferedReader in2 = 
                new BufferedReader(
                          new InputStreamReader(
                                      new GZIPInputStream(
                                                  new FileInputStream("test.gz"))));
        BufferedOutputStream out2 = new BufferedOutputStream(
            new FileOutputStream("endtest.java"));
        String s;
        while((s = in2.readLine()) != null) {
              LOG.debug(s);
              out2.write(s.getBytes());
              out2.write("\n".getBytes());
        }
        in2.close();
        out2.close();
        endTime = System.currentTimeMillis();
        LOG.debug("복원시간 : " + (endTime - startTime) + " ms");
  }
    
    static AtomicLong  ZipComp_idx = new AtomicLong(0) ;
    static AtomicLong  ZipComp_idxComp = new AtomicLong(0) ;
    
    /*
    synchronized public  byte[] ZipComp(String inputString) 
    {
    	if(inputString == null) return null;
    	if(inputString.equals("") ) return null;
    
		 byte[] input 			= inputString.getBytes();
		 
		 byte[] output			= new byte[input.length];
 		 byte[] returnbyte 	= null;
		
		Deflater compresser = new Deflater();
		compresser.setInput(input);
		compresser.finish();
		int compressedDataLength = compresser.deflate(output);
		returnbyte = new byte[compressedDataLength];
		System.arraycopy(output, 0, returnbyte, 0, compressedDataLength);
		
		if(!inputString.equals(ZipDeComp(returnbyte)))
		{
			LOG.debug("ZipComp" + ZipComp_idx.longValue() + ": " +  input.length  + "->" + compressedDataLength);
			String temp = ZipDeComp(returnbyte);			
			LOG.debug("ZipComp compressed: Error " + inputString + "," + temp );
		}
		return returnbyte;
	}
    
    static AtomicLong  ZipDecomp_idx = new AtomicLong(0) ;
    static AtomicLong  ZipDecomp_idxComp = new AtomicLong(0) ;
    Inflater decompresser = null;
    public   String ZipDeComp(byte[] output)  
    {

		decompresser = new Inflater();
		decompresser.setInput(output);
    	synchronized(decompresser)
    	{
			byte[] resultBuffer = new byte[output.length * 32];
			int resultLength;
			try {
				resultLength = decompresser.inflate(resultBuffer,0,resultBuffer.length);			
				decompresser.end();
				
				//LOG.debug("ZipDeComp:" + ZipDecomp_idx.longValue() 
				//                  + "[" + (ZipDecomp_idx.longValue() - ZipDecomp_idxComp.longValue()) + "]: " +  output.length + "->" + resultLength);
				
				return new String(resultBuffer, 0, resultLength);
			}
			catch (DataFormatException e) {
				LOG.error( new String(output));
				e.printStackTrace();
			}
			finally
			{
				decompresser.end();
				resultBuffer = null;
//				System.gc(); 
			}
    	}
		return "";
	} 
	*/
    
    // junsoo
    public static byte[] deflate(boolean zip, String inputString, String encoding) 
    {
    	if(inputString == null || inputString.length() < 1 ) return null;
    
    	try {
    		return deflate(zip, inputString.getBytes(encoding));
		} catch (Exception e) {
			return deflate(zip, inputString.getBytes());
		}
    }

    public static byte[] deflate(boolean zip, String inputString) 
    {
    	return deflate(zip, inputString, "UTF-8");
    }
    
    public static byte[] deflate(boolean zip, byte[] source)
    {
    	if (!zip)
    		return source;

    	try {
    		Deflater df = new Deflater();
    		df.setLevel(Deflater.BEST_SPEED);
    		df.setInput(source);
    		ByteArrayOutputStream baos = new ByteArrayOutputStream(source.length); 
    		df.finish();
    		byte[] buff = new byte[1024];
    		while(!df.finished())
    		{
    			int count = df.deflate(buff);
    			baos.write(buff, 0, count);
    		}
    		baos.close();
			df.end();
    		byte[] output = baos.toByteArray();

    		return output;
    	} catch (IOException e) {
    		LOG.error(e.getMessage());
    		return null;
    	}
    }
    
    //ZipDeComp
    // junsoo
    public static byte[] inflate(boolean zip, byte[] source)
    {
    	if (!zip)
    		return source;
    	
    	if (source.length < 1)
    		return new byte[0];
    	try {
    		Inflater ifl = new Inflater();
//    		df.setLevel(Deflater.BEST_SPEED);
    		ifl.setInput(source);

    		ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] buff = new byte[1024];
    		while((!ifl.finished())){
    			int count = ifl.inflate(buff);
    			baos.write(buff, 0, count);
    		}
    		baos.close();
    		ifl.end();
    		byte[] output = baos.toByteArray();
    		return output;
    	} 
    	catch (IOException e) {
    		LOG.error(e.getMessage());
    		return null;
    	}
    	catch (DataFormatException e) {
    		LOG.error(e.getMessage());
    		return null;
    	}
    }

    
    
    
    
    /**
     * Decompress the byte array passed using a default buffer length of 1024.
     * <p>
     * @param input compressed byte array webservice response
     * @return uncompressed byte array
     */
    public static byte[] decompressByteArray( final byte[] input )
    {
        return decompressByteArray( input, 1024 );
    }

    /**
     * Decompress the byte array passed
     * <p>
     * @param input compressed byte array webservice response
     * @param bufferLength buffer length
     * @return uncompressed byte array
     */
    public static byte[] decompressByteArray( final byte[] input, final int bufferLength )
    {
        if ( null == input )
        {
            throw new IllegalArgumentException( "Input was null" );
        }

        // Create the decompressor and give it the data to compress
        final Inflater decompressor = new Inflater();

        decompressor.setInput( input );

        // Create an expandable byte array to hold the decompressed data
        final ByteArrayOutputStream baos = new ByteArrayOutputStream( input.length );

        // Decompress the data
        final byte[] buf = new byte[bufferLength];

        try
        {
            while ( !decompressor.finished() )
            {
                int count = decompressor.inflate( buf );
                baos.write( buf, 0, count );
            }
        }
        catch ( DataFormatException ex )
        {
            LOG.error( "Problem decompressing.", ex );
        }

        try
        {
            baos.close();
        }
        catch ( IOException ex )
        {
        	LOG.error( "Problem closing stream.", ex );
        }

        return baos.toByteArray();
    }

    /**
     * Compress the byte array passed
     * <p>
     * @param input byte array
     * @return compressed byte array
     * @exception IOException thrown if we can't close the output stream
     */
    public static byte[] compressByteArray( byte[] input )
        throws IOException
    {
        return compressByteArray( input, 1024 );
    }

    /**
     * Compress the byte array passed
     * <p>
     * @param input byte array
     * @param bufferLength buffer length
     * @return compressed byte array
     * @exception IOException thrown if we can't close the output stream
     */
    public static byte[] compressByteArray( byte[] input, int bufferLength )
        throws IOException
    {
        // Compressor with highest level of compression
        Deflater compressor = new Deflater();
        compressor.setLevel( Deflater.BEST_COMPRESSION );

        // Give the compressor the data to compress
        compressor.setInput( input );
        compressor.finish();

        // Create an expandable byte array to hold the compressed data.
        // It is not necessary that the compressed data will be smaller than
        // the uncompressed data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream( input.length );

        // Compress the data
        byte[] buf = new byte[bufferLength];
        while ( !compressor.finished() )
        {
            int count = compressor.deflate( buf );
            bos.write( buf, 0, count );
        }

        bos.close();

        // Get the compressed data
        return bos.toByteArray();

    }

}
