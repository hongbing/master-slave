package com.iacrqq.ms.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.command.Command;
import com.iacrqq.ms.command.CommandDispatcher;
import com.iacrqq.ms.command.CommandFactory;
import com.iacrqq.ms.slave.SlaveNode;
import com.iacrqq.ms.slave.SlaveState;

/**
 * 
 * @author raoqiang
 *
 */
public class DefaultNetWorkClient implements NetWorkClient, Runnable {
	
	private static final Log log = LogFactory.getLog(DefaultNetWorkClient.class);

	private String hostName;
	private int    port;
	
	private SocketChannel socketChannel;
	private Selector selector;
	private Thread thread;
	private Thread commandDispatchThread;
	private CommandDispatchTask commandDispatchTask;
	private ScheduledExecutorService heartbeatScheduler;
	private volatile boolean runningFlag = true;
	
	private BlockingQueue<ByteBuffer> inputBuffer;
	private BlockingQueue<Command> outputBuffer;
	
	private CommandDispatcher commandDispatcher;
	
	private SlaveNode slave;
	
	public DefaultNetWorkClient() {
		this(null, NetWorkConstants.DEFAULT_NETWORK_SERVER_PORT);
	}
	
	public DefaultNetWorkClient(String hostName) {
		this(hostName, NetWorkConstants.DEFAULT_NETWORK_SERVER_PORT);
	}
	
	public DefaultNetWorkClient(String hostName, int port){
		this.hostName = hostName;
		this.port = port;
	}

	@Override
	public void init() {
		inputBuffer = new LinkedBlockingQueue<ByteBuffer>();
		outputBuffer = new LinkedBlockingQueue<Command>();
	}

	@Override
	public void start() {
		try{
			socketChannel = SocketChannel.open(new InetSocketAddress(hostName, port));
			socketChannel.configureBlocking(false);
			selector = Selector.open();
			socketChannel.register(selector, SelectionKey.OP_READ);
		}catch(IOException e){
			e.printStackTrace();
			log.error(e);
			throw new RuntimeException(e);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		thread = new Thread(this, NET_WORK_CLIENT_THREAD_NAME);
		thread.setDaemon(true);
		thread.start();
		
		commandDispatchTask = new CommandDispatchTask();
		commandDispatchThread = new Thread(commandDispatchTask, SLAVE_COMMAND_DISPATCHE_THREAD_NAME);
		commandDispatchThread.setDaemon(true);
		commandDispatchThread.start();
		
		// heart beat
		heartbeatScheduler = Executors.newScheduledThreadPool(1, new ThreadFactory()
		{
			public Thread newThread(Runnable r)
			{
				return new Thread(r, SLAVE_HEART_BEAT_THREAD);
			}
		});
		
		heartbeatScheduler.scheduleAtFixedRate(new HeartbeatTask(), NetWorkConstants.DEFAULT_MS_HEART_BEAT_INTERVAL, NetWorkConstants.DEFAULT_MS_HEART_BEAT_INTERVAL, TimeUnit.SECONDS);
	}

	@Override
	public void stop() {
		heartbeatScheduler.shutdown();
		runningFlag = false;
		commandDispatchTask.stop();
		
	}
	
	@Override
	public void run() {
		log.info(new StringBuilder("MS-NetWork-Clent started, connected to Server: ").append(hostName == null ? "" : hostName + ":").append(port).toString());
		while(runningFlag) {
			dispatch();
		}
		log.info(new StringBuilder("MS-NetWork-Clent stoped ").toString());
	}
	
	private void dispatch() {
		try{
			write();
			if (selector.select(NetWorkConstants.DEFAULT_NETWORK_TIME_OUT) <= 0){
				return;
			}
		}
		catch(IOException e) {
			log.error(e);
		}
		
		Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
			
		while (iterator.hasNext()) {
			SelectionKey key = (SelectionKey) iterator.next();
			iterator.remove();
			try{
				if (!key.isValid()) {
					key.cancel();
					continue;
				}
	
				if (key.isReadable()) {
					onRead();
				}
			}catch(Exception e){
				key.cancel();
				try{
					socketChannel.close();
				}catch(Exception ex){
					log.error(ex);
				}
			}
		}

	}
	
	private void onRead() throws Exception{
		ByteBuffer readByteBuffer = ByteBuffer.allocate(NetWorkConstants.DEFAULT_NETWORK_BUFFER_SIZE);
		int ret = 0;
		do {
			ret = socketChannel.read(readByteBuffer);
		}while(ret > 0);
		
		readByteBuffer.flip();
		inputBuffer.put(readByteBuffer);
	}
	
	private void write() throws IOException{
		Command command = outputBuffer.poll();
		if(command != null) {
			Packet packet = Packet.newPacket(command);
			ByteBuffer data = packet.marshall();
			
			while(data.remaining() > 0){
				socketChannel.write(data);
			}
		}
	}
	
	
	// parse network data, convert to command
	private Command parse() throws Exception{
		int size = 0;
		int oldSize = size;
		long length = Packet.getHeaderSize();
		ByteBuffer buffer = ByteBuffer.allocate(NetWorkConstants.DEFAULT_NETWORK_BUFFER_SIZE);
		ByteBuffer currentBuffer = null;
		
		while(size < length) {
			while((currentBuffer = inputBuffer.peek()) == null)
			{
				Thread.sleep(1000);
			}
			oldSize = size;
			size += currentBuffer.remaining();
			buffer.put(currentBuffer);
		
			if(size >= Packet.getHeaderSize()) {
				length = buffer.getLong(Packet.getLengthIndex());
			}
			
			if(size <= length){
				inputBuffer.remove();
			} else {
				currentBuffer.position(0);
				byte[] buf = new byte[(int)(length - oldSize)];
				buffer.put(currentBuffer.get(buf));
			}
		}
		
		//buffer.position(0);
		buffer.flip();
		Packet packet = Packet.unmarshall(buffer);
		
		return CommandFactory.createCommand(packet.getType(), packet.getPayLoad());
	}
	
	private void process() throws Exception {
		Command command = parse();
		
		if(command != null) {
			Command resultCommand = commandDispatcher.dispatch(command);
			if(resultCommand != null) {
				outputBuffer.put(resultCommand);
			}
		}
	}
	
	private void heartbeat() throws Exception{
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream objOutputStream = new ObjectOutputStream(bout);
			SlaveState slaveState  = (SlaveState)slave.gatherStatistics();
			objOutputStream.writeObject(slaveState);
			objOutputStream.flush();
			outputBuffer.put(CommandFactory.createHeartbeatCommand(ByteBuffer.wrap(bout.toByteArray())));
		}catch(IOException e) {
			throw new RuntimeException(e);
		}finally{
			
		}
	}
	
	@Override
	public void send(Command command) {
		outputBuffer.add(command);
	}
	
	
	private class CommandDispatchTask implements Runnable{
		private volatile boolean runningFlag = true;
		
		@Override
		public void run() {
			log.info("MS-Slave-Command-Dispatche-Thread started");
			while(runningFlag){
				try{
					process();
				}catch(Exception e){
					log.error(e);
				}
			}
			log.info("MS-Slave-Command-Dispatche-Thread stoped");
		}
		
		public void stop(){
			runningFlag = false;
		}
	}
	
	private class HeartbeatTask implements Runnable
	{
		public void run()
		{
			try 
			{
				heartbeat();
			}
			catch(Exception e)
			{
				log.error(e);
				throw new RuntimeException(e);
			}
		}
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setCommandDispatcher(CommandDispatcher commandDispatcher) {
		this.commandDispatcher = commandDispatcher;
	}
	
	public void setSlave(SlaveNode slave) {
		this.slave = slave;
	}
}
