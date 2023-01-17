package com.speno.xmon;

import java.sql.SQLException;
import java.util.Date;

import com.speno.xmon.comm.SimDate;
import com.speno.xmon.db.DataCounter;
import com.speno.xmon.db.DbCreateTables;

public class MMain {

	//static DataReader dr;
	static DbCreateTables ct;
	static DataInserterActionTest di;
	static DataCounter dc;
	public static void main(String[] args){
		create();
		//test();
		//count();
		//Insert();
	}
	public static void count(){
		dc = new DataCounter();
		int cnt;
		try {
			cnt = dc.Count_Acc_Concurrent("20140311");
			System.out.println("cnt:" + cnt);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void Insert(){
		try {						
			long time = System.currentTimeMillis(); 
			String str = new SimDate().DateTimeFormatter_Sec.format(new Date(time));
			
			di = new DataInserterActionTest();
			
			long start = System.currentTimeMillis() ; 
			for(int i =0 ; i<10000;i++){
				di.Insert_Acc_Concurrent(i, "20140311", "133251111", "192.168.63.1", "HOSTNAME", "client",  str );
			}
			
			long end = System.currentTimeMillis(); 
			System.out.println((end-start)/1000 +" �� �ɸ�");		 //74 �� �ɸ�.	�ʴ�135��
			//di.Insert_Acc_Concurrent(1, "20140311", "133252111", "192.168.63.1", "HOSTNAME", "client",  str );
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
	public static void create(){
		ct = new DbCreateTables();
	}
	
	public static void test(){
		System.out.println("test");
	}

}
