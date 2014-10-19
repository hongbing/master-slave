package com.iacrqq.ms.master;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.State;
import com.iacrqq.ms.session.SessionState;

/**
 * 
 * Master端统计信息
 * 
 * @author raoqiang
 *
 */
public class MasterState extends State {
	
	private static final Log log = LogFactory.getLog(MasterState.class);
	
	private static final long serialVersionUID = 7412463722733033955L;
	
	private String 				ip;						//
	private Integer				port;					//
	private List<SessionState>	sessionStateList;		//
	
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	
	public List<SessionState> getSessionStateList() {
		return sessionStateList;
	}
	public void setSessionStateList(List<SessionState> sessionStateList) {
		this.sessionStateList = sessionStateList;
	}
	
	public static MasterState fromByteBuffer(ByteBuffer buffer) {
		ByteArrayInputStream bin = null;
		ObjectInputStream oin = null;
		try {
			bin = new ByteArrayInputStream(buffer.array(), buffer.position(), buffer.remaining());
			oin = new ObjectInputStream(bin);
			return (MasterState)oin.readObject();
		} catch(IOException e) {
			log.error(e);
			throw new RuntimeException(e);
		} catch(ClassNotFoundException e) {
			log.error(e);
			throw new RuntimeException(e);
		} finally {
			if(oin != null) {
				try	{
					oin.close();
				} catch(IOException e){
					log.error(e);
				}
			}
		}
	}
	
	public ByteBuffer toByteBuffer() {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream objOutputStream = new ObjectOutputStream(bout);
			objOutputStream.writeObject(this);
			objOutputStream.flush();
			return ByteBuffer.wrap(bout.toByteArray());
		}catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{id:");
		sb.append(id);
		sb.append(", ip:");
		sb.append(ip);
		sb.append(", port:");
		sb.append(port);
		sb.append(", sessionStateList:{");
		for(SessionState sessionState : sessionStateList) {
			sb.append("sessionState:");
			sb.append(sessionState);
			sb.append(",");
		}
		if(sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("}");
		sb.append("}");
		
		return sb.toString();
	}
}
