package com.speno.xmon.agent;

import java.util.Hashtable;

public class LoggerDummy implements ILogger {
	private int level = 0;
	
	public void init(Hashtable attrs) throws Exception{
		// Only one should be labeled "file"
		String level = (String)attrs.get("LEVEL");
		if (level != null){
			try {
				int ilevel = Integer.parseInt(level);
				SetLevel(ilevel);
			} catch (Exception e) {
				SetLevel(1);
				System.out.println("LoggerDummy init failure. set level 1");
			}
		}
		else{
			throw new Exception("LEVEL parameter not specified");
		}

	}

	public void warn(String message) {
		if (level >= 3)
		System.out.println("[WARN] " + message);
	}
	public void debug(String message){
		if (level >= 4)
		System.out.println("[DEBUG] " + message);
	}
	public void error(String message){
		if (level >= 1)
		System.out.println("[ERROR] " + message);
	}
	public void info(String message){
		if (level >= 2)
		System.out.println("[INFO] " + message);
	}
	
	public void SetLevel(int level) {
		this.level = level;
	}
}
