package com.speno.xmon.agent;

import java.util.HashMap;

public  class AmassJavaHeap   implements IxMon 
{
	/*
     * 실제 메모리의 전체 크기 : Runtime.getRuntime().totalMemory()
     * 사용 가능한 실제 메모리의 크기 : Runtime.getRuntime().freeMemory()
     * 가상 메모리의 전체 크기 : Runtime.getRuntime().maxMemory()
     */
	
	////System.out.println("free:" + free + ", max:"+ max + ", total" + total);
	protected long getFreeMemory() {
		return Runtime.getRuntime().freeMemory() ;
	}

	protected long getMaxMemory() {
		return Runtime.getRuntime().maxMemory() ;
	}

	protected long getTotalMemory() {
		return Runtime.getRuntime().totalMemory() ;
	}

	protected long getAllocation() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}
	
	public long getValue(String resourceID, String propertyName) 
	{
		/*if(propertyName.equals("USE"))
			return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		else if(propertyName.equals("TOTAL"))
			return Runtime.getRuntime().totalMemory() ;*/
		
		if(propertyName.equals("USE"))
			return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
					)/1048576;
		else if(propertyName.equals("TOTAL"))
			return Runtime.getRuntime().totalMemory() /1048576  ;
		
		return 0;
	}
	 HashMap<String, String> tempExtMap = new HashMap<String, String>();
	 
	public HashMap<String, String> getExtMap() {
		tempExtMap.clear();
		tempExtMap.put("key1", "value1");
		tempExtMap.put("key2", "value3");
		return tempExtMap;
	}
}
