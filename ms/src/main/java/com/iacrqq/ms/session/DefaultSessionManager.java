package com.iacrqq.ms.session;

import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.command.CommandDispatcher;

/**
 * 后面可以考虑做一个SessionPool
 * 
 * @author raoqiang
 *
 */
public class DefaultSessionManager implements SessionManager {
	
	private static final Log log = LogFactory.getLog(DefaultSessionManager.class);
	
	private AtomicLong sessionCount = new AtomicLong(0);
	private AtomicLong sessionId = new AtomicLong(0);
	private int preInitSessionCount = DEFAULT_PRE_INIT_SESSION_COUNT;
	private int increaseSessionCount = DEFAULT_INCREASE_SESSION_COUNT;
	
	private long sessionRecycleInterval = DEFAULT_SESSION_RECYCLE_INTERVAL;

	private int maxSessionCount = DEFAULT_MAX_SESSION_COUNT;
	private CommandDispatcher commandDispatcher;
	private CopyOnWriteArrayList<Session> idleSessionList;
	private ConcurrentHashMap<Long, Session> sessionMap;
	
	private ScheduledExecutorService sessionRecycleScheduler;
	
	@Override
	public void init() {
		idleSessionList = new CopyOnWriteArrayList<Session>();
		sessionMap = new ConcurrentHashMap<Long, Session>();
		log.info(new StringBuilder("Pre init ").append(preInitSessionCount).append(" sessions.").toString());
		increment(preInitSessionCount);
		
		sessionRecycleScheduler = Executors.newScheduledThreadPool(1, new ThreadFactory()
		{
			public Thread newThread(Runnable r)
			{
				return new Thread(r, SESSION_RECYCLE_THREAD_NAME);
			}
		});
		
		sessionRecycleScheduler.scheduleAtFixedRate(new SessionRecycleTask(), sessionRecycleInterval, sessionRecycleInterval, TimeUnit.SECONDS);
	}
	
	private void increment() {
		if(sessionCount.get() >= maxSessionCount) {
			log.warn(new StringBuilder("Reach max session supported.").toString());
			return;
		}
		
		long count = maxSessionCount - sessionCount.get();
		count = count < increaseSessionCount ? count :  preInitSessionCount;
		
		increment(count);
	}
	
	/**
	 * 调用本函数的肯定只有一个线程
	 */
	private void increment(long count) {
		
		log.warn(new StringBuilder("Increase ").append(count).append(" sessions").toString());
		for(long i = 0; i < count; i++) {
			Session session = SessionFactory.createSession(sessionId.addAndGet(1), null);
			session.setCommandDispatcher(commandDispatcher);
			session.init();
			idleSessionList.add(session);
		}
	}
	

	@Override
	public Session newSession(SocketChannel channel) {
		log.info(new StringBuilder("New session for ").append(channel.socket().getRemoteSocketAddress().toString()).toString());

		if(idleSessionList.isEmpty()){
			increment();
		}
	
		Session session = idleSessionList.remove(0);
		session.setChannel(channel);
		session.start();
		sessionMap.put(session.getId(), session);
		return session;
	}

	@Override
	public void freeSession(Session session) {
		log.warn(new StringBuilder("Free session : id = ").append(session.getId()).append(", for : ").append(session.getRemoteIP()).toString());
		sessionMap.remove(session.getId());
		session.free();
		idleSessionList.add(session);
		
	}
	
	private void sessionRecycle() {
		log.debug("Begin session recycle.");
		dump();
		Iterator<Long> iterator = sessionMap.keySet().iterator();
		Session session = null;
		while(iterator.hasNext()) {
			Long sessionId = iterator.next();
			session = sessionMap.get(sessionId);
			if(session == null) {
				continue;
			}
			
			if(session.isDead()) {
				iterator.remove();
				session.free();
				idleSessionList.add(session);
				continue;
			}
			
			session.transitState();
		}
		log.debug("End session recycle.");
		dump();
	}
	
	private void dump() {
		Long key = null;
		Session session = null;
		Iterator<Long> iterator = sessionMap.keySet().iterator();
		log.debug("Dump session state:");
		while(iterator.hasNext()) {
			key = iterator.next();
			session = sessionMap.get(key);
			if(session != null) {
				log.debug(new StringBuilder("Session: ").append(session.getId()).append(" , State: ").append(session.getState().desc()).toString());
			}
		}
	}
	
	@Override
	public List<SessionState> generateSessionState() {
		SessionState sessionState = null;
		List<SessionState> sessionStateList = new LinkedList<SessionState>();
		Long key = null;
		Session session = null;
		Iterator<Long> iterator = sessionMap.keySet().iterator();
		while(iterator.hasNext()) {
			key = iterator.next();
			session = sessionMap.get(key);
			if(session != null) {
				sessionState = new SessionState();
				sessionState.setIp(session.getRemoteIP());
				sessionState.setPriority(computePriority(session));
				sessionState.setIsMasterCandidate(session.getIsMasterCandidate());
				sessionStateList.add(sessionState);
			}
		}
		
		return sessionStateList;
	}
	
	@Override
	public void destroy() {
		sessionRecycleScheduler.shutdown();
		idleSessionList.clear();
		idleSessionList = null;
		Long key = null;
		Session session = null;
		Iterator<Long> iterator = sessionMap.keySet().iterator();
		while(iterator.hasNext()) {
			key = iterator.next();
			session = sessionMap.get(key);
			if(session != null) {
				session.close();
			}
		}
		sessionMap.clear();
		sessionMap = null;
	}
	
	/**
	 * 计算Slave的转换成Master的优先级
	 * @return
	 */
	private Long computePriority(Session session) {
		return 0L;
	}
	
	public void setMaxSessionCount(int maxSessionCount) {
		this.maxSessionCount = maxSessionCount;
	}
	
	public void setCommandDispatcher(CommandDispatcher commandDispatcher) {
		this.commandDispatcher = commandDispatcher;
	}

	public void setPreInitSessionCount(int preInitSessionCount) {
		this.preInitSessionCount = preInitSessionCount;
	}

	public void setIncreaseSessionCount(int increaseSessionCount) {
		this.increaseSessionCount = increaseSessionCount;
	}

	public void setSessionRecycleInterval(long sessionRecycleInterval) {
		this.sessionRecycleInterval = sessionRecycleInterval;
	}
	
	
	private class SessionRecycleTask implements Runnable
	{
		public void run() {
			try{
				sessionRecycle();
			}catch(Exception e){
				log.error(e);
			}
		}
	}
}
