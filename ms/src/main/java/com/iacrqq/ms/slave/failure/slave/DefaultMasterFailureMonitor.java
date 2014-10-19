package com.iacrqq.ms.slave.failure.slave;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.master.MasterState;

public class DefaultMasterFailureMonitor implements MasterFailureMonitor {
	
	private static final Log log = LogFactory.getLog(DefaultMasterFailureMonitor.class);
	
	private MasterFailureHandler masterFailureHandler;
	
	private Timer timer;							// 计时器
	private TimerTask monitorTask;					// 
	private long	lastReceivedMasterStateTime;	// 
	private long    monitorInterval = DEFAULT_FAILURE_MONITOR_INTERVAL;
	private long    waitMasterStateTimeout = DEFAULT_FAILURE_MONITOR_WAIT_MASTER_STATE_TIME_OUT;

	/**
	 * 
	 */
	private List<MasterState> masterStateList = new ArrayList<MasterState>(MasterFailureMonitor.DEFAULT_STORE_MASTER_STATE_SIZE);
	
	
	@Override
	public void init() {
		timer = new Timer();
		monitorTask = new TimerTask(){
			public void run() {
				try{
					monitor();
				}catch(Exception e){
					log.error(e);
				}
			}
		};
	}

	@Override
	public void start() {
		timer.schedule(monitorTask, new Date(System.currentTimeMillis() + monitorInterval), monitorInterval);
	}

	@Override
	public void stop() {
		monitorTask.cancel();
	}

	@Override
	public void process(MasterState masterState) {
		log.info("Get MasterState");
		if(masterStateList.size() == MasterFailureMonitor.DEFAULT_STORE_MASTER_STATE_SIZE) {
			masterStateList.remove(MasterFailureMonitor.DEFAULT_STORE_MASTER_STATE_SIZE - 1);
		}
		
		masterStateList.add(masterState);
		
		lastReceivedMasterStateTime = System.currentTimeMillis();
	}
	
	
	private void monitor() {
		long now = System.currentTimeMillis();
		if(now - lastReceivedMasterStateTime > waitMasterStateTimeout) {
			masterFailureHandler.handle(masterStateList);
		} else {
			lastReceivedMasterStateTime = now;
		}
	}
	
	public void setMasterFailureHandler(MasterFailureHandler masterFailureHandler) {
		this.masterFailureHandler = masterFailureHandler;
	}
	
	public void setMonitorInterval(long monitorInterval) {
		this.monitorInterval = monitorInterval < DEFAULT_FAILURE_MONITOR_INTERVAL ? DEFAULT_FAILURE_MONITOR_INTERVAL : monitorInterval;
	}
	
	public void setWaitMasterStateTimeout(long waitMasterStateTimeout) {
		this.waitMasterStateTimeout = waitMasterStateTimeout < DEFAULT_FAILURE_MONITOR_WAIT_MASTER_STATE_TIME_OUT ? DEFAULT_FAILURE_MONITOR_WAIT_MASTER_STATE_TIME_OUT : waitMasterStateTimeout;
	}
}
