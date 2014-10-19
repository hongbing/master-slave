package com.iacrqq.ms.network;

import java.io.IOException;

import com.iacrqq.ms.command.Command;

/**
 * 网络客户端，运行在Slave节点
 * 
 * @author raoqiang
 *
 */
public interface NetWorkClient {
	
	String NET_WORK_CLIENT_THREAD_NAME = "MS-NetWorkClient-Thread";
	
	String SLAVE_COMMAND_DISPATCHE_THREAD_NAME = "MS-Slave-Command-Dispatche-Thread";
	
	String SLAVE_HEART_BEAT_THREAD = "MS-Slave-Heartbeat-Thread";
	
	/**
	 * 
	 * @throws IOException
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
	 * @param command
	 */
	void send(Command command);

}
