package com.speno.xmon.ccmap;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

class CMRemove1 implements Runnable {
	public  static   ConcurrentMap<String, String> cmp;
	  String name;
	  public CMRemove1(String name, ConcurrentMap<String, String> cmpp) {
	    cmp = cmpp;
	    this.name = name;
	  }
	  public void run() {
	    System.out.println(name + " maps the element ");
	    cmp.putIfAbsent("1", "A");
	    cmp.putIfAbsent("2", "B");
	    cmp.putIfAbsent("3", "C");
	    cmp.putIfAbsent("4", "D");
	    System.out.println(cmp);
	    System.out.println("Size of Map = " + cmp.size());
	    Set<String> s = cmp.keySet();
	    System.out.println("Key set of map = " + s);
	    
	    Runnable b = new CMRemove2("B", cmp);
	    b.run();
	    try {
	      Thread.sleep(400);
	    } catch (InterruptedException e) {
	      e.printStackTrace();
	    }
	  }
	}

	