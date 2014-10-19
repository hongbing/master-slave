package com.iacrqq.ms.slave.failure.slave;

import com.iacrqq.ms.master.MasterState;
import com.iacrqq.ms.network.NetWorkConstants;

/**
 * Slave端监控Master故障，并做响应的处理
 * 
 * @author raoqiang
 *
 */
public interface MasterFailureMonitor {
	
	int 	DEFAULT_STORE_MASTER_STATE_SIZE = 10;
	long 	DEFAULT_FAILURE_MONITOR_INTERVAL = NetWorkConstants.DEFAULT_MS_HEART_BEAT_INTERVAL * 4;
	long 	DEFAULT_FAILURE_MONITOR_WAIT_MASTER_STATE_TIME_OUT = NetWorkConstants.DEFAULT_MS_HEART_BEAT_INTERVAL * 10;
	/**
	 * 
	 */
	void init();
	
	/**
	 * 
	 */
	void start();
	
	/**
	 * 
	 */
	void stop();
	
	/**
	 * 
	 * @param masterState
	 */
	void process(MasterState masterState);
}
