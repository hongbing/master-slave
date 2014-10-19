package com.iacrqq.ms.slave;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.Node;
import com.iacrqq.ms.State;
import com.iacrqq.ms.command.Command;
import com.iacrqq.ms.command.CommandProvider;
import com.iacrqq.ms.config.MSConfig;
import com.iacrqq.ms.master.MasterState;
import com.iacrqq.ms.network.NetWorkClient;
import com.iacrqq.ms.slave.failure.slave.MasterFailureMonitor;

public class SlaveNode implements Node{
	
	private static final Log log = LogFactory.getLog(SlaveNode.class);

	private volatile boolean runningFlag = true;
	
	private NetWorkClient netWorkClient;
	
	private AtomicLong stateIDGenrator = new AtomicLong(0);
	
	private CommandProvider commandProvider;
	
	private Thread slaveThread;
	
	private Boolean isMasterCandidate;
	
	private MasterFailureMonitor masterFailureMonitor;
	
	private MSConfig msConfig;

	@Override
	public void init() {
		netWorkClient.init();
		masterFailureMonitor.init();
		slaveThread = new Thread(this, "MS-Slave-Thread");
	}

	@Override
	public void start() {
		runningFlag = true;
		netWorkClient.start();
		masterFailureMonitor.start();
		slaveThread.start();
	}

	@Override
	public void stop() {
		runningFlag = false;
		netWorkClient.stop();
		masterFailureMonitor.stop();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void run() {
		try{
			while(runningFlag){
				// sleep
				Thread.sleep(msConfig.getSlaveCommandProduceInterval() * 1000);
				
				Command command = commandProvider.produce();
				if(command != null) {
					netWorkClient.send(command);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			stop();
		}
	}
	
	@Override
	public State gatherStatistics() {
		SlaveState slaveState = new SlaveState();
		slaveState.setId(stateIDGenrator.addAndGet(1));
		slaveState.setIsMasterCandidate(isMasterCandidate);
		
		return slaveState;
	}
	
	
	@Override
	public void acceptStatistics(State state) {
		log.info(new StringBuilder("Slave Accept Master state : ").append(((MasterState)state).toString()).toString());
		masterFailureMonitor.process((MasterState)state);
	}

	public void setNetWorkClient(NetWorkClient netWorkClient) {
		this.netWorkClient = netWorkClient;
	}
	
	public void setCommandProvider(CommandProvider commandProvider) {
		this.commandProvider = commandProvider;
	}
	
	public void setIsMasterCandidate(Boolean isMasterCandidate) {
		this.isMasterCandidate = isMasterCandidate;
	}
	
	public void setMasterFailureMonitor(MasterFailureMonitor masterFailureMonitor) {
		this.masterFailureMonitor = masterFailureMonitor;
	}

	public void setMsConfig(MSConfig msConfig) {
		this.msConfig = msConfig;
	}
}
