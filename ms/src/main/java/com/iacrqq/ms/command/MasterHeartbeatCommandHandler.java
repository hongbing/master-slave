package com.iacrqq.ms.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.Node;
import com.iacrqq.ms.master.MasterState;
import com.iacrqq.ms.slave.SlaveState;

/**
 * 
 * @author raoqiang
 *
 */
public class MasterHeartbeatCommandHandler implements CommandHandler {
	
	private static final Log log = LogFactory.getLog(MasterHeartbeatCommandHandler.class);
	
	private Node master;

	@Override
	public Command handle(Command command) {
		log.info(new StringBuilder("Master receive heartbeat from slave : ").append(command.getSession().getRemoteIP().toString()));
		command.getSession().alive();
		master.acceptStatistics(SlaveState.fromByteBuffer(command.getPayLoad()));
		return CommandFactory.createHeartbeatCommand(((MasterState)master.gatherStatistics()).toByteBuffer());
	}
	
	public void setMaster(Node master) {
		this.master = master;
	}
}
