package com.speno.xmon.agent;

import java.util.HashMap;
import java.util.Map;

public class ResourceCommandManager {
	private Map<String, IxMon> resources = new HashMap<String, IxMon>();
	
	public void setResource(String command, IxMon resource){
		resources.put(command, resource);
	};
	
	public IxMon getResource(String command) {
		return resources.get(command);
	}
}
