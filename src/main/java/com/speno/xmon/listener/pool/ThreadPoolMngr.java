package com.speno.xmon.listener.pool;

/**
 * Title:        asysThreadPoolMngr
 * Description:  This class provides thread pooling capablities to XTorm
 * Copyright:    Copyright (c) 2001
 * Company:      windfireCom
 * @author Patrick M. Hayes
 * @version 1.0
 */

public class ThreadPoolMngr {

  private Queue idleWorkers = null;
  private ThreadPoolWorker[] workerlist;

  
  public Queue getIdleWorkers() {
	return idleWorkers;
}

public ThreadPoolMngr(String name, int numberOfThreads) {
    //make sure that it's a least one
    numberOfThreads = Math.max(1,numberOfThreads);
    idleWorkers = new Queue(numberOfThreads);
    workerlist = new ThreadPoolWorker[numberOfThreads];

    for (int i=0; i<workerlist.length; i++) {
      workerlist[i] = new ThreadPoolWorker(name, idleWorkers, i);
    }
  }

  public void execute(Runnable target)  throws InterruptedException {
    ThreadPoolWorker worker = (ThreadPoolWorker) this.idleWorkers.remove();
    worker.process(target);
  }

  public void stopRequestIdleWorkers() {
    try {
      Object[] idle = this.idleWorkers.removeAll();
      for(int i = 0; i<idle.length; i++) {
        ((ThreadPoolWorker) idle[i]).stopRequest();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void stopRequestAllWorkers() {
    stopRequestIdleWorkers();

    try { Thread.sleep(250); } catch (InterruptedException e) {}

    for (int i = 0; i < this.workerlist.length; i++) {
      if (this.workerlist[i].isAlive() ) {
        this.workerlist[i].stopRequest();
      }
    }
  }

  /**
   * This method returns information about the thread manager
   * @return an array of statistics about the threads being managed (int [])
   */
  public int [] threadsInfo() {
    int stat[] = new int[3];

    stat[0] = 0; stat[1] = 0; stat[2] = 0;

    for (int i = 0; i < this.workerlist.length; i++) {
      if (this.workerlist[i].isUsing() )
    	  stat[0]++;
      if (this.workerlist[i].isAlive() )
    	  stat[1]++;
    }

    stat[2] = this.idleWorkers.getSize();

    return stat;

}

  // Ŭ������ �ν��Ͻ� 1���� 1���� �����尡 ��ȴ�.
  protected class ThreadPoolWorker {
    // ���� �۾�ť�� ������
    private Queue idleWorkers = null;
    private int workerID;
    private Queue handoffBox;

    private boolean using = false;
    private Thread internalThread;
    private volatile boolean noStopRequested;

    public ThreadPoolWorker(String name,Queue idleWorkers, int id) {
      // ���� �۾�ť �Ҵ�
      this.idleWorkers = idleWorkers;
      // �۾� ó�� ť ��
      this.handoffBox = new Queue(1);
      
      this.workerID = id;

      noStopRequested = true;
      // 
      Runnable r = new Runnable() {
          public void run() {
            try {
              runWork();
            } catch( Exception x) {
	    		System.err.println(System.currentTimeMillis());
              x.printStackTrace();
            }
          }
        };

      internalThread = new Thread(r, name + "PoolWorker"+workerID);
      internalThread.start();
    }

	public int getWorkerID() {
		return workerID;
	}
	
    // �����带 �۾� ó��ť�� �ű��.
    public void process(Runnable target) throws InterruptedException {
      handoffBox.add(target);
    }

    private void runWork() {
      while ( noStopRequested ) {
        try {
          idleWorkers.add(this);
          using = false;
          Runnable r = (Runnable) handoffBox.remove();
          using = true;
          runIt(r);
        } catch (InterruptedException x) {
          Thread.currentThread().interrupt();
        }
      }
    }

    private void runIt(Runnable r) {
      try {
        r.run();
      } catch(Exception runx) {
  		System.err.println(System.currentTimeMillis());
        runx.printStackTrace();
      } finally {
        Thread.interrupted();
      }

    }

    public void stopRequest() {
      noStopRequested = false;
      internalThread.interrupt();
    }

    public boolean isAlive() {
    	return (internalThread.isAlive());
    }

    public boolean isUsing() {
    	return using;
    }

  }

}