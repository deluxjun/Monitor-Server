package com.speno.xmon.event;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.comm.SimDate;
import com.speno.xmon.db.DataInserterEventLevel;
import com.speno.xmon.listener.CommandServerMessageThread;

public class EventManager implements Runnable {
	private final static Logger LOG = LoggerFactory.getLogger(EventManager.class);	
	public final static int DEFAULT_CHECKINTERVAL =  5;	
	private boolean shutdown = false;
	private static EventManager instance;
	public static int checkInterval;
	public static boolean smsOn = true;
	
	private Map<String, IAdapter> adapters = new ConcurrentHashMap<String, IAdapter>();
	
	public Map<String, IAdapter> getAdapters() {
		return adapters;
	}

	public EventManager() {
		instance = this;
	}

	public static EventManager getInstance() {
		if(instance == null)
			instance = new EventManager();		
		return instance;
	}
	
	public void setCheckInterval(int waitTime) {
		this.checkInterval = waitTime;
	}
	
	// init
	public void startup(){
		Thread threadHolder = new Thread(this, "EventManager");
		threadHolder.start();
		LOG.info("EventManager has been started");
	}

	// add adapter
	public void addAdapter(String name, String className, Map<String,String> attrs) {
		try{
			Class logClass = Class.forName(className);
			Object[] args = {};
			Constructor[] cons = logClass.getConstructors();
			IAdapter adapter = (IAdapter)cons[0].newInstance(args);
			adapter.startup(attrs);
			addAdapter(name, adapter);
			LOG.info("Event adapter" + className + " started.");
		}
		catch (Exception e){
			LOG.error("Unable to add event adapter " + className + " (" + e.getMessage() + ")");
		}
	}
	
	// add adapter
	public void addAdapter(String name, IAdapter adapter) {
		adapters.put(name, adapter);
	}

	// remove adapter
	public void removeAdapter(String name) {
		adapters.remove(name);
	}

	// Close and exit
	public synchronized void shutdown(){
		shutdown = true;
		notify();
	}

	// Entry for thread
	public void run(){
			// Big loop
		while (!shutdown){
				pause();
			if (!shutdown){
				for (IAdapter adapter : adapters.values()) {
					try {
						Map<String,String> maps = new HashMap<String, String>();
						adapter.check(maps);
						for (String agentName : maps.keySet()) {
							long now = System.currentTimeMillis();
							
							String agentSendDateTime = SimDate.getDateTimeFormatter_MS().format(new Date(now));
							String message = "[" + agentName + "] " + maps.get(agentName);
							
							CommandServerMessageThread.eventMessageProcess(agentName, agentSendDateTime, "ERROR", message, 0);
							DataInserterEventLevel.insertEvent(agentName, "ERROR", message, 0, now, agentSendDateTime);
							
							// print log
							LOG.info("Event(" + adapter.getName() + ") : " + agentName + " : " + maps.get(agentName));
						}
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
						continue;
					}
				}
			}
		}
		
		LOG.info("EventManager has been shutdowned");
	}
	
	// Waits
	private synchronized void pause(){
		try{
			if(!shutdown)
				wait(checkInterval*1000l);
		}
		catch (Exception e){}
	}


}
