package com.iacrqq.ms.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.Node;
import com.iacrqq.ms.master.MasterState;

/**
 * 
 * @author raoqiang
 *
 */
public class SlaveHeartbeatCommandHandler implements CommandHandler {
	
	private static final Log log = LogFactory.getLog(SlaveHeartbeatCommandHandler.class);
	
	private Node slave;
	
	
	@Override
	public Command handle(Command command) {
		log.info("Slave received heartbeat from master");
		slave.acceptStatistics(MasterState.fromByteBuffer(command.getPayLoad()));
		return null;
		//return CommandFactory.createHeartbeatCommand(((SlaveState)slave.gatherStatistics()).toByteBuffer());
	}
	
	public void setSlave(Node slave) {
		this.slave = slave;
	}
}
