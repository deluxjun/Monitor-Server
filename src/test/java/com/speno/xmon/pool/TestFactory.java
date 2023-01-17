package com.speno.xmon.pool;

//import org.apache.commons.pool.PoolableObjectFactory;

public class TestFactory { //implements PoolableObjectFactory<Sch> {

	public void activateObject(Sch arg0) throws Exception {
		System.out.println("activateObject");
	}

	public void destroyObject(Sch arg0) throws Exception {
		System.out.println("destroyObject");
	}

	public Sch makeObject() throws Exception {
		System.out.println("makeObject");
		return new Sch();
	}

	public void passivateObject(Sch arg0) throws Exception {
		System.out.println("passivateObject");		
	}

	public boolean validateObject(Sch arg0) {
		System.out.println("validateObject");
		return true;
	}

}
