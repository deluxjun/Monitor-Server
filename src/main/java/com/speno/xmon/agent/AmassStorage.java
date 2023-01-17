package com.speno.xmon.agent;

import java.io.File;
import java.util.HashMap;


public class AmassStorage  implements IxMon  {
	private File file = null;
	//System.out.println("disk_total:" + disk_total + ", disk_Usable:"+ disk_Usable + ", disk_Free" + disk_Free);
	/*
	public long getTotalSpace() {
		return  file.getTotalSpace();
		//return 0;
	}
	public long getUsableSpace() {
		return  file.getUsableSpace();
		//return 0;
	}
	public long getFreeSpace() {
		return  file.getFreeSpace();
		//return 0;
	}
	public long getUseSpace() {
		return 0;
	}
	*/
	public long getValue(String resourceID, String propertyName) 
	{
		String rootName = "";

		if(resourceID.equals("C"))
			rootName = "C:/";
		if(resourceID.equals("D"))
			rootName = "D:/";
		if(resourceID.equals("E"))
			rootName = "E:/";
		
		if(propertyName.equals("USE"))
		{
			this.file = new File(rootName);
			//return (long) (file.getUsableSpace() / Math.pow(1024, 3));
			return 0;
		}
		else if(propertyName.equals("TOTAL"))
		{
			this.file = new File(rootName);
			//return (long) (file.getTotalSpace() / Math.pow(1024, 3));
			return 0;
		}
		else if(propertyName.equals("MAX"))
		{
			this.file = new File(rootName);
			//return (long) (file.getTotalSpace() / Math.pow(1024, 3));
			return 0;
		}
		
		
		return 0;
	}
	public HashMap<String, String> getExtMap() {
		// TODO Auto-generated method stub
		return null;
	}
}
