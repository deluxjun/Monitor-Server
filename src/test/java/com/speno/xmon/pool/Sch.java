package com.speno.xmon.pool;

public class Sch {
	private static int count = 0;

	public Sch() {
		count++;
	}
	
	public String print() {
		return Thread.currentThread().getName();
	}


	
	public int getCount() {
		return count;
	}


}
