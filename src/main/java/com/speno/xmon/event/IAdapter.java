package com.speno.xmon.event;

import java.util.Map;

public interface IAdapter {
	public void startup(Map<String, String> params) throws Exception;
	public void shutdown();
	public void setName(String name);
	public String getName();	
	public void check(Map<String,String> maps);
	public Map<String, String> getPropertMap();
	public void setLimitCount(String count);
}
