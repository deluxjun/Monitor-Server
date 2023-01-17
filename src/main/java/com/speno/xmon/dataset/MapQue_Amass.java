package com.speno.xmon.dataset;

import java.util.Queue;
import java.util.LinkedList;

/*
 * Agent 로 부터 들어오는  Action Raw Data
 */
public class MapQue_Amass {

	public static TransQueue 	putTransData	= new TransQueue();
	public static LogQueue 		putLogText 		= new LogQueue();
	public static EventQueue 	putEvent 			= new EventQueue();
	
	private static final int MAXBUFF = 10000;
	
	/*
	 * Agent에서 Put 로 들어오는 Trans Raw Queue
	 */
	//private  static final Queue<ItemQueue_Amass_Trans> QueAmass_Trans = new LinkedList<ItemQueue_Amass_Trans>();
	public static class TransQueue 
	{
		 //LinkedList<Object> queue = new LinkedList<Object>();
		   Queue<ItemAmassTrans> QueAmass_Trans = new LinkedList<ItemAmassTrans>();
		  
		public synchronized boolean addTrans(ItemAmassTrans o) {
			  // FIXME
//			  System.out.println("Trans Size: " + QueAmass_Trans.size());
			if (QueAmass_Trans.size() > MAXBUFF) {
//				System.out.println("FULL!! Trans Size: " + QueAmass_Trans.size());
				return false;
			}
		    QueAmass_Trans.offer(o);
		    notify();
		    return true;
		  }

		  public synchronized ItemAmassTrans getTrans() throws InterruptedException {
//		    while (QueAmass_Trans.isEmpty()) { wait(); }
		    return QueAmass_Trans.poll();
		  }
		public int size() { return QueAmass_Trans.size(); }
		public boolean isEmpty() { return QueAmass_Trans.isEmpty(); }

	
	}
	/*
	 * Agent에서 Put 로 들어오는 LOG Raw Queue
	 */
	public static class LogQueue 
	{
		   Queue<ItemAmassLogs> QueAmass_Logs = new LinkedList<ItemAmassLogs>();
		  
		  public synchronized boolean addLogText(ItemAmassLogs o){
//			  // FIXME
//			  System.out.println("Log Size: " + QueAmass_Logs.size());
			  if (QueAmass_Logs.size() > MAXBUFF)
				  return false;

		    QueAmass_Logs.offer(o);
		    notify();
		    return true;
		  }

		  public synchronized ItemAmassLogs getLogText() throws InterruptedException {
//		    while (QueAmass_Logs.isEmpty()) { wait(); }
		    return QueAmass_Logs.poll();
		  }
		public int size() { return QueAmass_Logs.size(); }
		public boolean isEmpty() { return QueAmass_Logs.isEmpty(); }

	}
	
	/*
	 * Agent에서 Put 로 들어오는 Event Raw Queue
	 */
	public static class EventQueue 
	{
		   Queue<ItemAmassEvent> QueAmass_Event = new LinkedList<ItemAmassEvent>();
		  
		  public synchronized boolean addEvent(ItemAmassEvent o) {
			  if (QueAmass_Event.size() > MAXBUFF)
				  return false;

			  QueAmass_Event.offer(o);
			  notify();
			  return true;
		  }

		  public synchronized ItemAmassEvent getEvent() throws InterruptedException {
//		    while (QueAmass_Event.isEmpty()) { wait(); }
		    return QueAmass_Event.poll();
		  }
		public int size() { return QueAmass_Event.size(); }
		public boolean isEmpty() { return QueAmass_Event.isEmpty(); }

	}
}
