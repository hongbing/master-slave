package com.iacrqq.ms.command;

import java.util.HashMap;
import java.util.Map;

public class SampleCommandDispatcher implements CommandDispatcher {
	
	/**
	 * 命令路由表
	 */
	private Map<Long, CommandHandler> commandRoutingTable = new HashMap<Long, CommandHandler>();
	
	
	@Override
	public void init() {
		// Heartbeat command 
		//commandRoutingTable.put(Command.HEART_BEAT_COMMAND, new MasterHeartbeatCommandHandler());
	}
	
	@Override
	public Command dispatch(Command command) {
		CommandHandler commandHandler = commandRoutingTable.get(command.getType());
		return commandHandler.handle(command);
	}
	
	public void setCommandRoutingTable(Map<Long, CommandHandler> commandRoutingTable) {
		this.commandRoutingTable = commandRoutingTable;
	}

}
