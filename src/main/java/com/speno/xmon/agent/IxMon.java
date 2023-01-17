package com.speno.xmon.agent;

import java.util.HashMap;

public interface IxMon  {
	long getValue(String resourceID, String propertyName);
	HashMap<String, String> getExtMap();
}
