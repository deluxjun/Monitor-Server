package com.speno.xmon.aggregate;

import java.nio.charset.CharacterCodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.speno.xmon.JsonGenerator.JsonGeneratorAmass;
import com.speno.xmon.comm.SimDate;
import com.speno.xmon.dataset.ItemMap_XmAmassTrans;
import com.speno.xmon.db.DataDeleter;
import com.speno.xmon.db.DataInserterAction;
import com.speno.xmon.db.DataInserterResource;
import com.speno.xmon.env.xmPropertiesXml;
import com.speno.xmon.listener.AmassServer;
import com.speno.xmon.listener.pool.ThreadPoolMngr;
import com.speno.xmon.sender.RequestAgentShort;

public class AggregatorThread implements Runnable {
	private final static Logger LOG = LoggerFactory.getLogger(AggregatorThread.class);
	private final static int DEFAULT_THREADPOOL_SIZE = 10;
	
	private boolean shutdown = false;
	private static AggregatorThread instance;

	private DataDeleter deleterDao ;
	private DataInserterAction actionInserterDao = null;
	private DataInserterResource resourceInserterDao = null;

	private ActionItem actionSec;
	private ActionItem actionMin;
	private ActionItem actionHour;
	private ActionItem actionDay;

	private ActionItem resourceSec;
	private ActionItem resourceMin;
	private ActionItem resourceHour;
	private ActionItem resourceDay;
	
	private ActionItem expireTran;
	private ActionItem expireActionSec;
	private ActionItem expireActionMin;
	private ActionItem expireActionHour;
	private ActionItem expireActionDay;
	private ActionItem expireResourceSec;
	private ActionItem expireResourceMin;
	private ActionItem expireResourceHour;
	private ActionItem expireResourceDay;
	
	// all action items
	private Set<ActionItem> actionItems = new HashSet<ActionItem>();
	private ThreadPoolMngr workerThreadPool = null;

	public AggregatorThread() {
		deleterDao	= new DataDeleter();
		actionInserterDao = new DataInserterAction();
		resourceInserterDao = new DataInserterResource();		
		workerThreadPool = new ThreadPoolMngr("Aggregator", DEFAULT_THREADPOOL_SIZE);

		initAction();
		initResource();
		initExpire();
	}

	private void initAction() {
		int repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ActionSec);
		actionSec = new ActionItem(repeatTime, 0) {
			@Override
			public void work() {
				aggregateActionSec();
			}
		};
		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ActionMin);
		actionMin = new ActionItem(repeatTime, 0) {
			@Override
			public void work() {
				actionInserterDao.aggregate(DataInserterAction.TYPE_MIN);
			}
		};
		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ActionHour);
		actionHour = new ActionItem(repeatTime, 0) {
			@Override
			public void work() {
				actionInserterDao.aggregate(DataInserterAction.TYPE_HOUR);
			}
		};
		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ActionDay);
		actionDay = new ActionItem(repeatTime, 0) {
			@Override
			public void work() {
				actionInserterDao.aggregate(DataInserterAction.TYPE_DAY);
			}
		};
		
		actionSec.setActionName("Action Seconds");
		actionMin.setActionName("Action Minutes");
		actionHour.setActionName("Action Hours");
		actionDay.setActionName("Action Days");
	}
	
	private void initResource() {
		int repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ResourceSec);
		resourceSec = new ActionItem(repeatTime, 0) {
			@Override
			public void work() {
				requestResouce();
			}
		};
		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ResourceMin);
		resourceMin = new ActionItem(repeatTime, 0) {
			@Override
			public void work() {
				resourceInserterDao.aggregateResource(DataInserterResource.TYPE_MIN);
			}
		};
		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ResourceHour);
		resourceHour = new ActionItem(repeatTime, 0) {
			@Override
			public void work() {
				resourceInserterDao.aggregateResource(DataInserterResource.TYPE_HOUR);
			}
		};
		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.AggregateDb_Repeat_ResourceDay);
		resourceDay = new ActionItem(repeatTime, 0) {
			@Override
			public void work() {
				resourceInserterDao.aggregateResource(DataInserterResource.TYPE_DAY);
			}
		};
		
		resourceSec.setActionName("Resource Seconds");
		resourceMin.setActionName("Resource Minutes");
		resourceHour.setActionName("Resource Hours");
		resourceDay.setActionName("Resource Days");

	}

	private void initExpire() {
		
		// transaction
		int repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.TimerToExpireTrans_Sec);
		int interval = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.IntervalToExpireTrans_Sec);
		expireTran = new ActionItem(repeatTime, interval) {
			@Override
			public void work() {
				long now = System.currentTimeMillis();
				String expireTimeString = SimDate.getDateTimeFormatter_Sec().format(now - this.interval * 1000l);
				deleterDao.Delete_XmExpireTransAction("xmTransaction", expireTimeString);
			}
		};

		// action
		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.TimerToExpireAction_Sec);
		interval = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.IntervalToExpireTrans_Sec);
		expireActionSec = new ActionItem(repeatTime, interval) {
			@Override
			public void work() {
				long now = System.currentTimeMillis();
				String expireTimeString;
				expireTimeString = SimDate.getDateTimeFormatter_Sec().format(now - this.interval * 1000l);
				deleterDao.Delete_XmExpireAction("XmAggregatedActionSec", expireTimeString);
			}
		};

		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.TimerToExpireAction_Min);
		interval = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.IntervalToExpireAction_Min);
		expireActionMin = new ActionItem(repeatTime, interval) {
			@Override
			public void work() {
				long now = System.currentTimeMillis();
				String expireTimeString;
				expireTimeString = SimDate.getDateTimeFormatter_Min().format(now - this.interval * 1000l);
				deleterDao.Delete_XmExpireAction("XmAggregatedActionMin", expireTimeString);
			}
		};
		
		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.TimerToExpireAction_Hour);
		interval = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.IntervalToExpireAction_Hour);
		expireActionHour = new ActionItem(repeatTime, interval) {
			@Override
			public void work() {
				long now = System.currentTimeMillis();
				String expireTimeString;
				expireTimeString = SimDate.getDateTimeFormatter_Hour().format(now - this.interval * 1000l);
				deleterDao.Delete_XmExpireAction("XmAggregatedActionHour", expireTimeString);
			}
		};

		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.TimerToExpireAction_Day);
		interval = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.IntervalToExpireAction_Day);
		expireActionDay = new ActionItem(repeatTime, interval) {
			@Override
			public void work() {
				long now = System.currentTimeMillis();
				String expireTimeString;
				expireTimeString = SimDate.getDateTimeFormatter_Day().format(now - this.interval * 1000l);
				deleterDao.Delete_XmExpireAction("XmAggregatedActionDay", expireTimeString);
			}
		};
		
		// resource
		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.TimerToExpireResource_Sec);
		interval = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.IntervalToExpireResource_Sec);
		expireResourceSec = new ActionItem(repeatTime, interval) {
			@Override
			public void work() {
				long now = System.currentTimeMillis();
				String expireTimeString;
				expireTimeString = SimDate.getDateTimeFormatter_Sec().format(now - this.interval * 1000l);
				deleterDao.Delete_XmExpireResource("XmAggregatedResourceSec", expireTimeString);
			}
		};

		interval = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.TimerToExpireResource_Min);
		interval = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.IntervalToExpireResource_Min);
		expireResourceMin = new ActionItem(repeatTime, interval) {
			@Override
			public void work() {
				long now = System.currentTimeMillis();
				String expireTimeString;
				expireTimeString = SimDate.getDateTimeFormatter_Min().format(now - this.interval * 1000l);
				deleterDao.Delete_XmExpireResource("XmAggregatedResourceMin", expireTimeString);
			}
		};
		
		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.TimerToExpireResource_Hour);
		interval = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.IntervalToExpireResource_Hour);
		expireResourceHour = new ActionItem(repeatTime, interval) {
			@Override
			public void work() {
				long now = System.currentTimeMillis();
				String expireTimeString;
				expireTimeString = SimDate.getDateTimeFormatter_Hour().format(now - this.interval * 1000l);
				deleterDao.Delete_XmExpireResource("XmAggregatedResourceHour", expireTimeString);
			}
		};

		repeatTime = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.TimerToExpireResource_Day);
		interval = xmPropertiesXml.htXmPropertiesAmass_Int.get(xmPropertiesXml.IntervalToExpireResource_Day);
		expireResourceDay = new ActionItem(repeatTime, interval) {
			@Override
			public void work() {
				long now = System.currentTimeMillis();
				String expireTimeString;
				expireTimeString = SimDate.getDateTimeFormatter_Day().format(now - this.interval * 1000l);
				deleterDao.Delete_XmExpireResource("XmAggregatedResourceDay", expireTimeString);
			}
		};
		
		expireTran.setActionName("Expire Transactions");
		expireActionSec.setActionName("Expire action seconds");
		expireActionMin.setActionName("Expire action minutes");
		expireActionHour.setActionName("Expire action hours");
		expireActionDay.setActionName("Expire action days");

		expireResourceSec.setActionName("Expire Resource seconds");
		expireResourceMin.setActionName("Expire Resource minutes");
		expireResourceHour.setActionName("Expire Resource hours");
		expireResourceDay.setActionName("Expire Resource days");

	}
	
	private synchronized  void aggregateActionSec(){
		// Delay insert!! : insert a buffer to table
		AmassServer server = AmassServer.getInstance();
		ConcurrentMap<String, ItemMap_XmAmassTrans> allTrans = server.getAmassTrans();
		for (String key : allTrans.keySet()) {
			try {
				ItemMap_XmAmassTrans trans = allTrans.get(key);
				int second = (int)(System.currentTimeMillis() / 1000L);
				long now = second * 1000L;
				if (trans.getTimeToInsert() != 0L && trans.getTimeToInsert() < now) {
//					// NOTYET 이면 complete만 오고 init이 오지 않은 것임
					if (!"NOTYET".equals(trans.GetActionName())) {
						boolean success = server.insertTrans(key, trans);
					}
					//불필요한 로그 제거 yys
					//LOG.info("insert trans: " + "size : " + allTrans.size() + "," + trans.toString());
					allTrans.remove(key, trans);
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		
		// 집계 시작!
		int intSize = actionInserterDao.Insert_XmAggregatedActionSecGroup();
		//집계량 출력 로그이므로 제거 YYS
		//LOG.debug("aggregateActionSec : " + intSize);
	}
	
	private JsonGeneratorAmass jsongen = new JsonGeneratorAmass(); 
	/*
	 * 단위 초 마다 리소스를 Agent에 리소스 요청 
	 */
	private void requestResouce(){
		
		RequestAgentShort Sender_ReqOrderAgent = new RequestAgentShort();
		
		Iterator<String> agentNames = xmPropertiesXml.htAgentList.keySet().iterator();
		try {
			String tempAgentName 				= "";
			while(agentNames.hasNext()) {
				tempAgentName 	= agentNames.next();
				if(tempAgentName.equals("")) continue;				
				int ret = Sender_ReqOrderAgent.SendRequestCmd(tempAgentName, jsongen.RequestAgentShort(tempAgentName));
			}
		}
		catch (CharacterCodingException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
	

	public static AggregatorThread getInstance() {
		if(instance == null)
			instance = new AggregatorThread();
		
		return instance;
	}
	
	// init
	public void startup(){
		Thread threadHolder = new Thread(this, "AggregatorThread");
		threadHolder.start();
		LOG.debug("AggregatorThread has been started");
	}

	// Close and exit
	public synchronized void shutdown(){
		shutdown = true;
		notify();
	}

	// Entry for thread
	public void run(){
			// Big loop
		while (!shutdown){
			pause();
			if (!shutdown){
				for (ActionItem item : actionItems) {
					item.increaseTimer();
				}
				for (ActionItem item : actionItems) {
					item.process();
				}
			}
		}
		
		LOG.debug("AggregatorThread has been shutdowned");
	}
	
	// Waits
	private synchronized void pause(){
		try{
			if(!shutdown)
				wait(1000l);
		}
		catch (Exception e){}
	}

	// item
	abstract class ActionItem implements Runnable{
		private int timer = 86400;
		private int repeatTime;
		private String actionName;
		
		protected int interval;
		
		public void setActionName(String actionName) {
			this.actionName = actionName;
		}
		
		protected abstract void work();
		
		public void run() {
			LOG.info("[Aggregator] actionName: " + actionName + " has been executed. (" + Thread.currentThread().getName() + ")");
			work();
		};
		
		public ActionItem(int repeatTime, int interval) {
			this.repeatTime = repeatTime;
			this.interval = interval;
			actionItems.add(this);
		}
		
		public int getTimer() {
			return timer;
		}
		public void setTimer(int timer) {
			this.timer = timer;
		}
		public void increaseTimer() {
			timer ++;
		}
		public int getInterval() {
			return interval;
		}
		public void setInterval(int interval) {
			this.interval = interval;
		}
		public void process() {
			if (repeatTime != 0 && timer > repeatTime){
				timer = 0;
//				run();
				try {
					LOG.debug("[Aggregator] actionName: " + actionName + " has been fired.");
					workerThreadPool.execute(this);
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
		}
	}

}
