package com.speno.xmon.comm.charset;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class MinaCharset {
   private static Charset charset              = Charset.forName("euc-kr"); 
   public static  CharsetEncoder encoder = charset.newEncoder();
   public static  CharsetDecoder decoder = charset.newDecoder(); 
}
