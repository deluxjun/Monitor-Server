package com.speno.xmon.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.speno.xmon.db.DBProperties;
import com.speno.xmon.event.postJob.PostJob;
import com.speno.xmon.event.postJob.PostJobController;

public class AlertTrackerAdater implements IAdapter{
	private String name = "AlertTracker";
	private Map<String,Long> timer = new HashMap<String, Long>();
	private int waitTime = 5;	//sec
	private boolean shutdown = false;
	private Map<String, String> propertyMap = new HashMap<String, String>();
	private PostJob job;
	private int now = 0;
	private String count = "";
		
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
		Connection transConnet = null;
		DBProperties properties = null;
		ResultSet rs = null;
		PreparedStatement statement = null;
		//작업내용을 여기에 
		if(now >= waitTime){
		try {
			properties = DBProperties.getInstance();		
			transConnet = properties.getConnection(DBProperties.dbType_Action);	
			//작업 파트
			for(String name : propertyMap.keySet()){
				String value = propertyMap.get(name);
				String selectQuery = "select count(ext1),ext2 from XmTransAction where ext2 = ? and TO_NUMBER(ext1) > TO_NUMBER(?) group by ext2";
				statement = transConnet.prepareStatement(selectQuery);
				statement.setString(1, name);
				statement.setString(2, value);
				rs = statement.executeQuery();
				List<String[]> list = new ArrayList<String[]>();
				int Columncount = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					String[] row = new String[Columncount];
					for (int i = 0; i < Columncount; i++)
						row[i] = rs.getString(i + 1);
					list.add(row);
				}			
				if(list.size() > 0){
					long counted = Long.valueOf(list.get(0)[0]);
					long max = Long.valueOf(count);
					if (counted > max){					
						if (job != null && EventManager.smsOn)
						job.run(name);
					}
				}
			}
			now = 0;
		}
		catch(Exception e){					
			e.printStackTrace();
		}
		finally{
			try {
				if(rs != null){
					rs.close();
					rs = null;
				}
				if(statement != null){
					statement.close();
					statement = null;
				}
			} catch (SQLException e) {			
				e.printStackTrace();
			}
			if(transConnet != null)
			properties.releaseConnection(DBProperties.dbType_Action, transConnet);
		}
		}
		else{
			now += EventManager.checkInterval;
		}
		
	}
	
	public Map<String, String> getPropertMap() {
		return propertyMap;
	}

	public void setLimitCount(String count) {
		this.count = count;
	}

}
