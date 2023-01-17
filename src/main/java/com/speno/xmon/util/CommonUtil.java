package com.speno.xmon.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CommonUtil {

	public static List<String> getListFromString(String separator, String comma) {
		String[] array = comma.split(separator);
		List<String> list = new ArrayList<String>();
		for (String string : array) {
			list.add(string.trim());
		}
		return list;
	}
	
	public static String getInQueryFromList(List<String> values) {
		StringBuffer buf = new StringBuffer();
		for (String string : values) {
			buf.append("'" + string + "',");
		}
		
		if (buf.length() > 0)
			buf.setLength(buf.length()-1);
		
		return buf.toString();
	}
	
	
	
	public static byte[] getBytes(InputStream is) throws IOException {

		int len;
		int size = 1024;
		byte[] buf;

		if (is instanceof ByteArrayInputStream) {
			size = is.available();
			buf = new byte[size];
			len = is.read(buf, 0, size);
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			buf = new byte[size];
			while ((len = is.read(buf, 0, size)) != -1)
				bos.write(buf, 0, len);
			buf = bos.toByteArray();
		}
		return buf;
	}
}
