package com.iacrqq.ms.command;

/**
 * 
 * @author raoqiang
 *
 */
public interface CommandDispatcher {
	
	/**
	 * 
	 */
	void init();
	
	/**
	 * 
	 * @param command
	 * @return
	 */
	Command dispatch(Command command);
}
