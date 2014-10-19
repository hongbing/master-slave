package com.iacrqq.ms.command;

import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.session.Session;

/**
 * 
 * @author raoqiang
 *
 */
public class MasterGreetCommandHandler implements CommandHandler {
	
	private static final Log log = LogFactory.getLog(MasterGreetCommandHandler.class);
	
	@Override
	public Command handle(Command command) {
		log.info(new StringBuilder("Command type = ").append(command.getType()).toString());
		String hello = new String(command.getPayLoad().array());
		log.info(new StringBuilder("Master receive greet from slave : ").append(command.getSession().getRemoteIP().toString()).append(", say:").append(hello));
		command.getSession().alive();
		return makeGreetCommand(command.getSession());
	}
	
	private Command makeGreetCommand(Session session) {
		String hello = new StringBuilder("Hello slave:").append(session.getRemoteIP()).toString();
		ByteBuffer buffer = ByteBuffer.allocate(hello.getBytes().length);
		buffer.put(hello.getBytes());
		buffer.flip();
		
		Command command = new Command(1L, buffer);
		
		return command;
	}

}
