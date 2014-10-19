package com.iacrqq.ms.command;

import java.nio.ByteBuffer;

import com.iacrqq.ms.network.NetWorkConstants;
import com.iacrqq.ms.session.Session;

/**
 * Command工厂，负责创建Command
 * 
 * 0号Command保留给HeartbeatCommand
 * 
 * @author raoqiang
 *
 */
public class CommandFactory {
	/**
	 * 
	 * @param session
	 * @param type
	 * @param payLoad
	 * @return
	 */
	public static Command createCommand(Session session, Long type, ByteBuffer payLoad) {
		Command command = new Command(session, type, payLoad);
		return command;
	}
	
	/**
	 * 
	 * @param type
	 * @param payLoad
	 * @return
	 */
	public static Command createCommand(Long type, ByteBuffer payLoad) {
		Command command = new Command(type, payLoad);
		return command;
	}
	
	/**
	 * 
	 * @param session
	 * @return
	 */
	public static Command createHeartbeatCommand(Session session) {
		byte[] bytes = NetWorkConstants.MS_HEART_BEAT_MSG.getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		return CommandFactory.createCommand(session, Command.HEART_BEAT_COMMAND, buffer);
	}
	
	public static Command createHeartbeatCommand(ByteBuffer payLoad) {
		return CommandFactory.createCommand(Command.HEART_BEAT_COMMAND, payLoad);
	}
	
	public static Command createHeartbeatCommand() {
		byte[] bytes = NetWorkConstants.MS_HEART_BEAT_MSG.getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		return CommandFactory.createCommand(Command.HEART_BEAT_COMMAND, buffer);
	}
	
}
