package com.speno.xmon.listener.pool;

/**
 * worker thread pool
 * 
 * Created Date : 2013/06/07, junsoo
 * Modified :
 * 
 */
import java.util.ArrayList;


public class xMThreadPool_Received<T extends xMThreadWorker_Received> {
	
	private static int poolSize = 20;
	private static int maxPoolSize = 200;

	private final ArrayList<T> queue = new ArrayList<T>();

	private boolean wait = false;
	private int total = 0;
	private int index = 0;
	
	private Class<T> handlerClass;
	private T handler;

	public xMThreadPool_Received() throws Exception {
		this(poolSize, maxPoolSize);
	}
	
	public xMThreadPool_Received(int size, int maxSize) throws Exception{
		poolSize = size;
		maxPoolSize = maxSize;
	}
	
	public int getTotal() {
		return total;
	}

	public static int getPoolSize() {
		return poolSize;
	}

	public static int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void initialize(T handler) throws Exception{
		this.handler = handler;
		this.handlerClass = (Class<T>) handler.getClass();

		for (index = 0; index < poolSize; index++) {
			T thread = getInstance();
			thread.setName(thread.getWorkerName() + (index + 1));
			thread.setProcessorID(index);
			thread.start();
			queue.add(thread);
			total++;
		}
	}

	
	public void terminate(){
		synchronized (queue) {
			for (T worker : queue) {
				worker.setInterrupted(true);
				worker.interrupt();
			}
			queue.clear();
		}
		
	}
	
	public ArrayList<T> getQueue() {
		return queue;
	}

	public T getThread() {
		T worker = null;

		if (queue.size() > 0) {
			synchronized (queue) {
				worker = (T) queue.remove(0);
			}
		} else {
			if (wait) {
				return waitQueue();
			} else {
				try {
					if (index < maxPoolSize) {
						index ++;
						worker = getInstance();
						worker.setName(worker.getWorkerName() + index);
						worker.setProcessorID(index);
						worker.start();
//						queue.add(worker);
						total++;
						return worker;
					} else {
						return waitQueue();
					}
				} catch (Exception e) {
					e.printStackTrace();
					return waitQueue();
				}
			}
		}
		worker.addUsedCount();
		return worker;
	}
	
	private T getInstance() throws Exception{
		return handlerClass.getConstructor(handlerClass).newInstance(handler);
	}

	private synchronized T waitQueue() {
		while (queue.isEmpty()) {
			log(1, "Thread Pool is empty. now waiting.. %1", getMaxPoolSize() + "/" + getTotal(), "");
			try {
				queue.wait();
			} catch (InterruptedException ignored) {
			}
		}
		return (T) queue.remove(0);
	}

	public void releaseThread(T thread) {
		if (queue.size() >= maxPoolSize) {
			thread = null;
			--index;
		} else {
			synchronized (queue) {
				queue.add(thread);
				queue.notify();
			}
		}
		
		thread.fireReleased();
	}

	public boolean isWait() {
		return wait;
	}

	public void setWait(boolean wait) {
		this.wait = wait;
	}
	
	public int[] getStatus(){
		ArrayList<T> list = (ArrayList<T>) queue.clone();
		int[] status = new int[list.size()];
		int index = 0;
		synchronized (list) {
			for (T worker : list) {
				status[index ++] = worker.getStatus();
			}
		}
		return status;
	}
	
	protected void log(int level, String message, String parm1, String parm2){
		System.out.println(message);
	}
}
