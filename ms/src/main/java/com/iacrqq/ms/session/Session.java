package com.iacrqq.ms.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.command.Command;
import com.iacrqq.ms.command.CommandDispatcher;
import com.iacrqq.ms.command.CommandFactory;
import com.iacrqq.ms.network.NetWorkConstants;
import com.iacrqq.ms.network.Packet;

/**
 * 
 * Session
 * 
 * @author raoqiang
 * 
 */
public class Session implements Runnable
{
	private static final Log log = LogFactory.getLog(Session.class);

	private static final String SESSION_THREAD_NAME_PREFIX = "MS-Session-Thread-";
	private Long id;
	private volatile SessionStateEnum state = SessionStateEnum.MS_SESSION_STATE_ALIVE;
	private Object _state_lock_ = new Object();

	private SocketChannel channel;

	private Thread thread;
	private volatile boolean runningFlag = true;			// 
	private volatile boolean idleFlag = true;				// idle

	// slave info
	private String remoteIP;								// 
	private int remotePort;									// 

	//
	private Boolean isMasterCandidate; // 是不是Master的候选者

	private BlockingQueue<ByteBuffer> inputBuffer;
	private BlockingQueue<Command> outputBuffer;

	private CommandDispatcher commandDispatcher;
	

	public Session(Long id, SocketChannel channel)
	{
		this.id = id;
		this.channel = channel;
	}

	public void init()
	{
		// this.remoteIP = channel.socket().getRemoteSocketAddress().toString();
		// this.remotePort = channel.socket().getPort();
		runningFlag = true;
		inputBuffer = new LinkedBlockingQueue<ByteBuffer>(1024);
		outputBuffer = new LinkedBlockingQueue<Command>(1024);

		thread = new Thread(this);
		thread.setDaemon(true);
		thread.setName(new StringBuilder(SESSION_THREAD_NAME_PREFIX).append(id).toString());
		thread.start();
	}

	/**
	 * 
	 */
	public void start()
	{
		synchronized (_state_lock_)
		{
			state = SessionStateEnum.MS_SESSION_STATE_ALIVE;
		}
		
		idleFlag = false;
	}

	/**
	 * 
	 */
	public void close()
	{
		runningFlag = false;
		idleFlag = true;
		try
		{
			channel.close();
		}
		catch (IOException e)
		{
			log.error(e);
		}
	}

	private void reset()
	{
		idleFlag = true;
		inputBuffer.clear();
		outputBuffer.clear();

		try
		{
			channel.close();
		}
		catch (IOException e)
		{
			log.error(e);
		}

		channel = null;
		remoteIP = null;
		remotePort = 0;
	}

	/**
	 * 
	 */
	private void idle()
	{
		reset();
	}

	public void free()
	{
		log.warn(new StringBuilder("Free one session id = ").append(id).toString());
		idle();
	}

	public void transitState()
	{
		SessionStateEnum oldState = state;
		synchronized (_state_lock_)
		{
			if (state == SessionStateEnum.MS_SESSION_STATE_ALIVE)
			{
				state = SessionStateEnum.MS_SESSION_STATE_WAITING_0;
			}
			else if (state == SessionStateEnum.MS_SESSION_STATE_WAITING_1)
			{
				state = SessionStateEnum.MS_SESSION_STATE_WAITING_2;
			}
			else if (state == SessionStateEnum.MS_SESSION_STATE_WAITING_2)
			{
				state = SessionStateEnum.MS_SESSION_STATE_DEAD;
			}
		}

		if (oldState != state)
		{
			log.warn(new StringBuilder("Session id = ").append(id).append(" transit state from : ")
					.append(oldState.desc()).append(" to : ").append(state.desc()));
		}
	}

	public void alive()
	{
		synchronized (_state_lock_)
		{
			state = SessionStateEnum.MS_SESSION_STATE_ALIVE;
		}
		log.warn(new StringBuilder("Session id = ").append(id).append(" is alived."));
	}

	public boolean isDead()
	{
		synchronized (_state_lock_)
		{
			return state == SessionStateEnum.MS_SESSION_STATE_DEAD;
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void onRead() throws Exception
	{
		ByteBuffer readByteBuffer = ByteBuffer.allocate(NetWorkConstants.DEFAULT_NETWORK_BUFFER_SIZE);
		int ret = 0;
		do
		{
			ret = channel.read(readByteBuffer);
		} while (ret > 0);

		readByteBuffer.flip();
		inputBuffer.put(readByteBuffer);
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void onWrite() throws IOException
	{
		Command command = outputBuffer.poll();
		if (command != null)
		{
			Packet packet = Packet.newPacket(command);
			ByteBuffer data = packet.marshall();

			while (data.remaining() > 0)
			{
				channel.write(data);
			}
		}
	}

	// parse network data, convert to command
	private Command parse() throws Exception
	{	
		int size = 0;
		int oldSize = size;
		long length = Packet.getHeaderSize();
		ByteBuffer buffer = ByteBuffer.allocate(NetWorkConstants.DEFAULT_NETWORK_BUFFER_SIZE);
		ByteBuffer currentBuffer = null;

		while (size < length)
		{
			while ((currentBuffer = inputBuffer.peek()) == null) {
				Thread.sleep(1000);
			}
			oldSize = size;
			size += currentBuffer.remaining();
			buffer.put(currentBuffer);

			if (size >= Packet.getHeaderSize())
			{
				length = buffer.getLong(Packet.getLengthIndex());
			}

			if (size <= length)
			{
				inputBuffer.remove();
			}
			else
			{
				currentBuffer.position(0);
				byte[] buf = new byte[(int) (length - oldSize)];
				buffer.put(currentBuffer.get(buf));
			}
		}

		// buffer.position(0);
		buffer.flip();
		Packet packet = Packet.unmarshall(buffer);

		return CommandFactory.createCommand(this, packet.getType(), packet.getPayLoad());
	}

	private void process() throws Exception
	{
		onWrite();

		Command command = parse();
		if (command != null)
		{
			Command resultCommand = commandDispatcher.dispatch(command);
			if (resultCommand != null)
			{
				outputBuffer.put(resultCommand);
			}
		}
	}

	@Override
	public void run()
	{
		while (runningFlag)
		{
			if (idleFlag)
			{
				Thread.yield();
			}
			
			try
			{
				process();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				log.error(e);
			}
		}
	}

	public void setCommandDispatcher(CommandDispatcher commandDispatcher)
	{
		this.commandDispatcher = commandDispatcher;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public SessionStateEnum getState()
	{
		return state;
	}

	public void setState(SessionStateEnum state)
	{
		this.state = state;
	}

	public void setChannel(SocketChannel channel)
	{
		this.channel = channel;
	}

	public String getRemoteIP()
	{
		return channel.socket().getRemoteSocketAddress().toString();
	}

	public void setRemoteIP(String remoteIP)
	{
		this.remoteIP = remoteIP;
	}

	public int getRemotePort()
	{
		return channel.socket().getPort();
	}

	public void setRemotePort(int remotePort)
	{
		this.remotePort = remotePort;
	}

	public Boolean getIsMasterCandidate()
	{
		return isMasterCandidate;
	}

	public void setIsMasterCandidate(Boolean isMasterCandidate)
	{
		this.isMasterCandidate = isMasterCandidate;
	}

	public static void main(String[] args)
	{

		BlockingQueue<ByteBuffer> inputBuffer = new LinkedBlockingQueue<ByteBuffer>();

		for (int i = 0; i < 10; i++)
		{
			inputBuffer.add(makePacket().marshall());
		}

		int size = 0;
		int oldSize = size;
		long length = Packet.getHeaderSize();
		ByteBuffer buffer = ByteBuffer.allocate(NetWorkConstants.DEFAULT_NETWORK_BUFFER_SIZE);
		ByteBuffer currentBuffer = null;

		while (size < length)
		{
			while ((currentBuffer = inputBuffer.peek()) == null)
				;
			oldSize = size;
			size += currentBuffer.remaining();
			buffer.put(currentBuffer);

			if (size >= Packet.getHeaderSize())
			{
				length = buffer.getLong(Packet.getLengthIndex());
			}

			if (size <= length)
			{
				inputBuffer.remove();
			}
			else
			{
				currentBuffer.position(0);
				byte[] buf = new byte[(int) (length - oldSize)];
				buffer.put(currentBuffer.get(buf));
			}
		}

		// buffer.position(0);
		buffer.flip();
		Packet packet = Packet.unmarshall(buffer);

		Command command = CommandFactory.createCommand(packet.getType(), packet.getPayLoad());

		String str = new String(command.getPayLoad().array());

		System.out.println(str);

	}

	public static Packet makePacket()
	{
		return Packet.newPacket(makeGreetCommand());
	}

	public static Command makeGreetCommand()
	{
		String HELLO = "Hello Master";
		ByteBuffer buffer = ByteBuffer.allocate(HELLO.getBytes().length);
		buffer.put(HELLO.getBytes());
		buffer.flip();

		Command command = new Command(1L, buffer);

		return command;
	}
}
