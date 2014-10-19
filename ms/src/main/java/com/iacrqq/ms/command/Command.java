package com.iacrqq.ms.command;

import java.nio.ByteBuffer;

import com.iacrqq.ms.session.Session;

/**
 * 
 * 
 * @author raoqiang
 *
 */
public class Command {
	
	public static Long HEART_BEAT_COMMAND = 0L;
	
	/**
	 * 
	 */
	private Long type;
	
	/**
	 * 
	 */
	private ByteBuffer payLoad;
	
	/**
	 * 
	 */
	private Session session;
	

	public Command(Long type, ByteBuffer payLoad){
		this(null, type, payLoad);
	}
	
	/**
	 * 
	 * @param type
	 * @param payLoad
	 */
	public Command(Session session, Long type, ByteBuffer payLoad){
		this.session = session;
		this.type = type;
		this.payLoad = payLoad;
	}
	
	/**
	 * 
	 * @param type
	 */
	public void setType(Long type){
		this.type = type;
	}
	
	/**
	 * 
	 * @return
	 */
	public Long getType(){
		return type;
	}
	
	/**
	 * 
	 * @param payLoad
	 */
	public void setPayLoad(ByteBuffer payLoad){
		this.payLoad = payLoad;
	}
	
	/**
	 * 
	 * @return
	 */
	public ByteBuffer getPayLoad(){
		return payLoad;
	}
	
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
}
