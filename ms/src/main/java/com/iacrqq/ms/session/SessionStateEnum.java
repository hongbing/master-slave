package com.iacrqq.ms.session;

/**
 * 
 * @author raoqiang
 *
 */
public enum SessionStateEnum {
	MS_SESSION_STATE_ALIVE(0, "alive"),
	MS_SESSION_STATE_WAITING_0(1, "waiting_0"),
	MS_SESSION_STATE_WAITING_1(2, "waiting_1"),
	MS_SESSION_STATE_WAITING_2(3, "waiting_2"),
	MS_SESSION_STATE_DEAD(4, "dead");
	
	private int value;
	private String desc;
	
	private SessionStateEnum(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}
	
	public int value() {
		return this.value;
	}
	
	public String desc() {
		return this.desc;
	}
}
