package com.speno.xmon;

import java.util.Random;
import java.util.UUID;

import com.speno.xmon.agent.MainXAgent;

public class AgentTest {
	
	public static void main(String[] args) {
		final MainXAgent agent = new MainXAgent();
		agent.init("./conf/xMonTestAgent.xml");
		
		Runnable r = new Runnable() {
			
			public void run() {
				try {
					while(true) {
//						agent.putLog("Log1", "now " + System.currentTimeMillis(), System.currentTimeMillis(), "0", "", null);
						String tid = UUID.randomUUID().toString();
						agent.putInit("Inquiry", tid, System.currentTimeMillis(), "0", "", null);
						Random r = new Random();
						Thread.sleep(30 + r.nextInt(20));
						agent.putCompSuccess("Inquiry", tid, System.currentTimeMillis(), "0", "", null);
//						Thread.sleep(100L);
//						MainXAgent.putCompSuccess("Inquiry", tansId, System.currentTimeMillis(), "0", "", null);
						
//						Thread.sleep(20L);
					}
					} catch (Exception e) {
					}
			}
		};
		
		Thread th[] = new Thread[10];
		for (int i = 0; i < 10; i++) {
			th[i] = new Thread(r);
			th[i].start();
		}
		
		try {
			for (int i = 0; i < 10; i++) {
				th[i].join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
