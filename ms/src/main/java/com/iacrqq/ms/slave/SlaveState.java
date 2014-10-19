package com.iacrqq.ms.slave;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.State;
import com.iacrqq.ms.master.MasterState;
import com.iacrqq.ms.session.SessionState;

/**
 * 
 * Slave 端统计信息
 * 
 * @author raoqiang
 *
 */
public class SlaveState extends State{
	
	private static final Log log = LogFactory.getLog(SlaveState.class);
	
	private static final long serialVersionUID = 9106821624690079506L;

	private Boolean isMasterCandidate = false;
	

	public Boolean getIsMasterCandidate() {
		return isMasterCandidate;
	}
	public void setIsMasterCandidate(Boolean isMasterCandidate) {
		this.isMasterCandidate = isMasterCandidate;
	}
	
	public static SlaveState fromByteBuffer(ByteBuffer buffer) {
		ByteArrayInputStream bin = null;
		ObjectInputStream oin = null;
		try {
			bin = new ByteArrayInputStream(buffer.array(), buffer.position(), buffer.remaining());
			oin = new ObjectInputStream(bin);
			return (SlaveState)oin.readObject();
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
		sb.append(", isMasterCandidate:");
		sb.append(isMasterCandidate);
		sb.append("}");
		
		return sb.toString();
	}
}
