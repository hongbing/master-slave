package com.iacrqq.ms.session;

import java.nio.channels.SocketChannel;

/**
 * 
 * @author raoqiang
 *
 */
public abstract class SessionFactory {
	
	public static Session createSession(Long id, SocketChannel channel) {
		return new Session(id, channel);
	}
}
