package com.speno.xmon.event.postJob;

import java.util.HashMap;
import java.util.Map;

public class PostJobController {
	private static Map<String, PostJob> jobMap = new HashMap<String, PostJob>();

	public static Map<String, PostJob> getJobMap() {
		return jobMap;
	}
	
	public static Map<String, Double> CCmap = new HashMap<String, Double>();

	public static Map<String, Double> getCCmap() {
		return CCmap;
	}
	
}
