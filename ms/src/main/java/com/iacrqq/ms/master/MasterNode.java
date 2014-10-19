package com.iacrqq.ms.master;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.Node;
import com.iacrqq.ms.State;
import com.iacrqq.ms.network.NetWorkServer;
import com.iacrqq.ms.session.SessionManager;
import com.iacrqq.ms.slave.SlaveState;

public class MasterNode implements Node {
	
	private static final Log log = LogFactory.getLog(MasterNode.class);
	
	private volatile boolean runningFlag = true;
	
	private SessionManager sessionManager;
	
	private NetWorkServer netWorkServer;
	
	private AtomicLong stateIDGenrator = new AtomicLong(0);

	@Override
	public void run() {
		try{
			while(runningFlag){
				Thread.currentThread().sleep(60000);
			}
		}catch(Exception e) {
			e.printStackTrace();
			stop();
		}
	}

	@Override
	public void init() {
		netWorkServer.init();
	}

	@Override
	public void start() {
		netWorkServer.start();
	}

	@Override
	public void stop() {
		runningFlag = false;
		sessionManager.destroy();
		netWorkServer.stop();
	}

	@Override
	public void reset() {
		
	}
	
	@Override
	public State gatherStatistics() {
		MasterState masterState = new MasterState();
		masterState.setId(stateIDGenrator.addAndGet(1));
		masterState.setIp(netWorkServer.getIp());
		masterState.setPort(netWorkServer.getPort());
		masterState.setSessionStateList(sessionManager.generateSessionState());
		return masterState;
	}
	
	@Override
	public void acceptStatistics(State state) {
		log.info(new StringBuilder("Master Accept Slave state : ").append(((SlaveState)state).toString()).toString());
	}

	public void setNetWorkServer(NetWorkServer netWorkServer) {
		this.netWorkServer = netWorkServer;
	}
	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}


}
