package com.speno.xmon.pool;

import com.speno.xmon.listener.pool.ThreadPoolMngr;

//import org.apache.commons.pool.PoolableObjectFactory;
//import org.apache.commons.pool.impl.GenericObjectPool;

public class Pooltest {

	public static <T> void main(String[] args) {
		
		ThreadPoolMngr mngr = new ThreadPoolMngr("Pool", 10);
		
		Runnable r = new Runnable() {
			public void run() {
				try {
					System.out.println("started");
					Thread.sleep(1000L);
					System.out.println("terminated");
				} catch (Exception e) {
				}
			}
		};
		
		for (int i = 0; i < 100; i++) {
			try {
				int info[] = mngr.threadsInfo();
				System.out.println(i + " Pool info : " + info[0] + "," + info[1] + ",IDLE:" + info[2]);
				mngr.execute(r);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			Thread.sleep(2000L);
		} catch (Exception e) {
		}
		int info[] = mngr.threadsInfo();
		System.out.println(" Pool info : " + info[0] + "," + info[1] + ",IDLE:" + info[2]);
		
		/*
		PoolableObjectFactory<Sch> factory 				= new TestFactory();
		GenericObjectPool<Sch> genericObjectPool	= new GenericObjectPool<Sch>((PoolableObjectFactory<Sch>) factory);
		for(int i = 0; i < 10; i++) {
		
			Sch obj;
			try {
				obj = genericObjectPool.borrowObject();
	
				System.out.println("i : " + i);
				System.out.println(obj.getClass().getSimpleName()+ "count : " + obj.getCount());
				
				genericObjectPool.returnObject(obj);
		
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		/*
		
		PoolableObjectFactory<Object> factory = new TestFactory();
		
		GenericObjectPool<T> pool = new GenericObjectPool<T>((PoolableObjectFactory<T>) factory);
		pool.setMaxActive(3);

		for(int i=0; i<100; i++)
		{
			try {
				Sch s = (Sch) pool.borrowObject();
				System.out.println(i + "GetName:" + s.print());
				Thread.sleep(100);
				pool.returnObject((T) s);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		
		
	}

}
