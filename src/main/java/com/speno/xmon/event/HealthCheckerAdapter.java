package com.speno.xmon.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.speno.xmon.comm.PortCheck;
import com.speno.xmon.env.ItemAgent;
import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.event.postJob.PostJob;
import com.speno.xmon.event.postJob.PostJobController;

public class HealthCheckerAdapter implements IAdapter {
	private String name = "HealthChecker";
	private Map<String,Long> timer = new HashMap<String, Long>();
	private int waitTime = 5;	//sec
	private PostJob job;
	private Map<String, String> map = new HashMap<String, String>();

	public PostJob getJob() {
		return job;
	}

	public void setJob(PostJob job) {
		this.job = job;
	}

	public void startup(Map<String, String> params) throws Exception {
		String sWaitTime = params.get("WAITTIME");
		if(params.get("POSTJOB") != null){
			this.job = PostJobController.getJobMap().get(params.get("POSTJOB"));
		}
		try {
			waitTime = Integer.parseInt(sWaitTime);
		} catch (Exception e) {}
	}

	public void shutdown() {
		

	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	

	public void check(Map<String, String> maps) {
		Iterator<String> agents = xmPropertiesXml.htAgentList.keySet().iterator();
		while(agents.hasNext()) {
			String agentName = agents.next();
			
			// add timer
			if (!timer.containsKey(agentName)) {
				timer.put(agentName, 0L);
			}

			boolean status = xmPropertiesXml.htAgentList.get(agentName).getMinaSessionCheck();
			ItemAgent agent = xmPropertiesXml.htAgentList.get(agentName);
			boolean portStatus = new PortCheck().PortCheckRun(agent.getAgentIP(), agent.getAgentPort());
			if (!(status && portStatus)) {
				// 일정시간이 지나면 전송
				if (System.currentTimeMillis() - timer.get(agentName) > waitTime*1000L) {
					if(job != null && EventManager.smsOn)
						job.run(agentName,"shutdowned!!");
					maps.put(agentName, "shutdowned");
					timer.put(agentName, System.currentTimeMillis());
				}
			}
		}
	}

	public Map<String, String> getPropertMap() {
		return map;
	}

	public void setLimitCount(String count) {
		// TODO Auto-generated method stub
		
	}
}
