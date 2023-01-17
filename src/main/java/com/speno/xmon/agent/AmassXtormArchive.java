package com.speno.xmon.agent;

import java.util.HashMap;
import java.util.Random;

public class AmassXtormArchive  implements IxMon 
{
	Random random = new Random();
	public long getTotalSpace() {
		return 300000000000L;
	}
	public long getUsableSpace() {
		return 0;
	}
	public long getFreeSpace() {
		return 0;
	}
	public long getUseSpace() {
		return 200000000000L + random.nextInt(1000000000);
	}
	
	public long getValue(String resourceID, String propertyName) 
	{
		if(propertyName.equals("USE"))
			return 200000000000L + random.nextInt(1000000000);
		else if(propertyName.equals("TOTAL"))
			return 300000000000L;
		
		return 0;
	}
	public HashMap<String, String> getExtMap() {
		// TODO Auto-generated method stub
		return null;
	}
}
