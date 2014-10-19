package com.iacrqq.ms.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author raoqiang
 *
 */
public class SlaveGreetCommandHandler implements CommandHandler {
	
	private static final Log log = LogFactory.getLog(SlaveGreetCommandHandler.class);
	
	@Override
	public Command handle(Command command) {
		log.info(new StringBuilder("Command type = ").append(command.getType()).toString());
		String hello = new String(command.getPayLoad().array());
		log.info("Slave received greet from master:" + hello);
		return null;
	}

}
