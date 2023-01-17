package com.speno.xmon.ccmap;

import java.util.concurrent.ConcurrentMap;

class CMRemove2 implements Runnable {
	  String name;
	  ConcurrentMap<?, ?> cmp;
	  public CMRemove2(String name, ConcurrentMap<?, ?> cmp) {
	    this.cmp = cmp;
	    this.name = name;
	  }
	  public void run() {
	    try {
	      boolean bol = cmp.remove("2", "B");
	      System.out.println(name + " deleted the given association : " + bol);
	      Thread.sleep(1);
	      System.out.println(name + " deleted the association (2,''B'')then "
	          + "remaining association of map = " + cmp);
	      System.out.println("Remaining Set of key = " + cmp.keySet());
	      System.out.println("Remaining size of Map = " + cmp.size());
	    } catch (InterruptedException e) {
	      e.printStackTrace();
	    }
	  }
	}