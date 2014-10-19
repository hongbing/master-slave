package com.iacrqq.ms.command;


/**
 * 
 * @author raoqiang
 *
 */
public interface CommandHandler {
	
	/**
	 * 命令处理器
	 * 
	 * @param command
	 * @return
	 */
	Command handle(Command command);
	
	/**
	 * 
	 * @param command
	 * @param callback
	 * @return
	 */
	//Command handle(Command command, CommandCompletedCallback callback);
	
}
