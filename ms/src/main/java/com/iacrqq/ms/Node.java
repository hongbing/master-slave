package com.iacrqq.ms;

import com.iacrqq.ms.master.MasterState;

/**
 * 分布式节点，Master节点MasterNodel和Slave节点SlaveNode实现本接口
 * 
 * @author raoqiang
 *
 */
public interface Node extends Runnable {
	/**
	 * 节点初始化
	 */
	void init();
	
	/**
	 * 节点启动
	 */
	void start();
	
	/**
	 * 节点停止
	 */
	void stop();
	
	/**
	 * 节点重启
	 */
	void reset();
	
	/**
	 * 收集统计信息
	 * 
	 * @return
	 */
	State gatherStatistics();
	
	/**
	 * 
	 * @param state
	 */
	void acceptStatistics(State state);

}
