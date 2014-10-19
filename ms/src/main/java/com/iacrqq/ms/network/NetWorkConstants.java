package com.iacrqq.ms.network;

/**
 * 
 * @author sihai
 *
 */
public interface NetWorkConstants
{

	/**
	 * 
	 */
	int DEFAULT_NETWORK_SERVER_PORT = 8206;
	/**
	 * 
	 */
	long DEFAULT_NETWORK_TIME_OUT = 1000;
	/**
	 * 
	 */
	int DEFAULT_NETWORK_BUFFER_SIZE = 4096;

	/**
	 * 心跳检测间隔
	 */
	long DEFAULT_MS_HEART_BEAT_INTERVAL = 60L;

	String MS_MATIC = "ms@iac-rqq.com";

	String MS_HEART_BEAT_MSG = "Hi, I am still alive.";

}