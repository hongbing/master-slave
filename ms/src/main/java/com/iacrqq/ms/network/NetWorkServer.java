package com.iacrqq.ms.network;

/**
 * 网络服务器，运行在Master节点
 * 
 * @author raoqiang
 *
 */
public interface NetWorkServer {
	
	String NET_WORK_SERVER_THREAD_NAME = "MS-NetWorkServer-Thread";
	
	/**
	 * 
	 * @param port
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
	 * @return
	 */
	String getIp();
	
	/**
	 * 
	 */
	int getPort();
}
