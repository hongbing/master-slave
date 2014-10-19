package com.iacrqq.ms.network;

import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.command.Command;

/**
 * 网络通信数据包
 * 
 * @author raoqiang
 *
 */
public class Packet {
	
	private static final Log log = LogFactory.getLog(Packet.class);
	
	/**
	 * header
	 */
	private String magic = NetWorkConstants.MS_MATIC;
	
	private Long type;
	
	private Long length;		// 整个报文的长度
	
	/**
	 * 
	 */
	
	private ByteBuffer payLoad;
	
	public static int getHeaderSize() {
		int size = 0;
		size += NetWorkConstants.MS_MATIC.getBytes().length;
		size += Long.SIZE / Byte.SIZE;
		size += Long.SIZE / Byte.SIZE;
		
		return size;
	}
	
	public int getSize() {
		int size = 0;
		size += getHeaderSize();
		size += payLoad.remaining();
		
		return size;
	}
	
	public static int getLengthIndex() {
		int size = 0;
		size += NetWorkConstants.MS_MATIC.getBytes().length;
		size += Long.SIZE / Byte.SIZE;
		
		return size;
	}

	public ByteBuffer marshall(){
		ByteBuffer buffer = ByteBuffer.allocate(getSize());
		buffer.put(magic.getBytes());
		buffer.putLong(type);
		buffer.putLong(getSize());
		buffer.put(payLoad);
		buffer.flip();
		
		return buffer;
	}
	
	public static Packet unmarshall(ByteBuffer buffer) {
		if(buffer.remaining() < getHeaderSize()) {
			throw new RuntimeException("Wrong packet.");
		}
		
		Packet packet = new Packet();
		byte[] str = new byte[NetWorkConstants.MS_MATIC.getBytes().length];
		buffer.get(str);
		packet.setMagic(new String(str));
		
		if(!NetWorkConstants.MS_MATIC.equals(packet.getMagic())){
			throw new RuntimeException("Wrong packet.");
		}
		
		packet.setType(buffer.getLong());
		packet.setLength(buffer.getLong());
		
		byte[] buf = new byte[(int)(packet.getLength() - getHeaderSize())];
		buffer.get(buf);
		packet.setPayLoad(ByteBuffer.wrap(buf));
		
		return packet;
	}
	
	public Long getType() {
		return type;
	}

	public void setType(Long type) {
		this.type = type;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}
	
	public String getMagic() {
		return magic;
	}

	public void setMagic(String magic) {
		this.magic = magic;
	}
	
	public ByteBuffer getPayLoad() {
		return payLoad;
	}

	public void setPayLoad(ByteBuffer payLoad) {
		this.payLoad = payLoad;
	}
	
	public static Packet newPacket(Command command) {
		Packet packet = new Packet();
		packet.setType(command.getType());
		packet.setLength(Long.valueOf(getHeaderSize() + command.getPayLoad().remaining()));
		packet.setPayLoad(command.getPayLoad());
		return packet;
	}
	
	public static void main(String[] args) {
		System.out.println("[DEBUG] Packet Header size = " + getHeaderSize());
	}
}
