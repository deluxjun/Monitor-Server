/**
 * thread worker
 * Created Date : 2013/06/07, junsoo
 * Modified :
 * 
 */

package com.speno.xmon.util;


public abstract class asysThreadWorker extends Thread{
	public static final int STATUS_IDLE = 0;
	public static final int STATUS_PROCESS = 1;

	private int status = STATUS_IDLE;
	private long usedCount = 0L;
	
	private int processorID = -1;
	private boolean interrupted = false;
	
	private String workerName;

	public asysThreadWorker(int processorID) throws Exception 
	{
		super( "asysThreadWorker(" + processorID + ")" );
		
		this.processorID = processorID;
	}
	
	public asysThreadWorker() {
		// TODO Auto-generated constructor stub
	}
	
	public long getUsedCount() {
		return usedCount;
	}

	public void addUsedCount() {
		this.usedCount ++;
	}

	public void setProcessorID(int processorID) {
		this.processorID = processorID;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getProcessorID() {
		return processorID;
	}

	public String getWorkerName() {
		return workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}

	protected void log(int level, String message, String parm1, String parm2) {
		System.out.println(message);
	}

	public void run() 
	{
		while( !interrupted ) 
		{
			work();
		}
	}
	
	public boolean isInterrupted() {
		return interrupted;
	}

	public synchronized void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}

	protected abstract void work();
	protected abstract void fireReleased();
}
