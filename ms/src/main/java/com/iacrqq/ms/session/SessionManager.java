package com.iacrqq.ms.session;

import java.nio.channels.SocketChannel;
import java.util.List;

import com.iacrqq.ms.master.MasterState;
import com.iacrqq.ms.network.NetWorkConstants;

/**
 * MS session manager
 * 
 * @author raoqiang
 *
 */
public interface SessionManager {
	
	String SESSION_RECYCLE_THREAD_NAME = "MS-Sesion-Recycle-Thread";
	
	int DEFAULT_PRE_INIT_SESSION_COUNT = 8;
	int DEFAULT_INCREASE_SESSION_COUNT = DEFAULT_PRE_INIT_SESSION_COUNT;
	int DEFAULT_MAX_SESSION_COUNT = 1024;
	
	long DEFAULT_SESSION_RECYCLE_INTERVAL = NetWorkConstants.DEFAULT_MS_HEART_BEAT_INTERVAL * 2;
	
	/**
	 * 
	 */
	void init();
	
	/**
	 * 
	 * @param channel
	 * @return
	 */
	Session newSession(SocketChannel channel);
	
	/**
	 * 
	 * @param session
	 */
	void freeSession(Session session);
	
	/**
	 * 
	 * @return
	 */
	List<SessionState> generateSessionState();
	
	/**
	 * 
	 */
	void destroy();
}
