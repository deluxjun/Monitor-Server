package com.speno.xmon.ccmap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CMRemove {
	  public static void main(String args[]) {
		  
	    ConcurrentMap<String, String> cmp = new ConcurrentHashMap<String, String>();
	    Runnable a = new CMRemove1("A", cmp);
	    new Thread(a).start();
	    try {
	      Thread.sleep(400);
	    } catch (InterruptedException e) {
	      e.printStackTrace();
	    }
	//    Runnable b = new CMRemove2("B", cmp);
	 //   b.run();
	    //new Thread(b).start();
	  }
	}