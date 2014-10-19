package com.iacrqq.ms.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.session.Session;
import com.iacrqq.ms.session.SessionManager;

/**
 * 	Master端网络服务器, NIO
 * 
 * @author raoqiang
 * 
 */
public class DefaultNetWorkServer implements NetWorkServer, Runnable
{

	private static final Log log = LogFactory.getLog(DefaultNetWorkServer.class);

	private String hostName; //

	private int port; //

	private ServerSocketChannel serverSocketChannel; //
	private Selector selector; //
	private Thread netWorkServerThread; //

	private SessionManager sessionManager;

	private volatile boolean runningFlag = true;

	public DefaultNetWorkServer()
	{
		this(null, NetWorkConstants.DEFAULT_NETWORK_SERVER_PORT);
	}

	public DefaultNetWorkServer(String hostName, int port)
	{
		this.hostName = hostName;
		this.port = port;
	}

	@Override
	public void init()
	{
		try
		{
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(
					hostName == null ? new InetSocketAddress(port) : new InetSocketAddress(hostName, port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void start()
	{
		netWorkServerThread = new Thread(this, NET_WORK_SERVER_THREAD_NAME);
		netWorkServerThread.setDaemon(true);
		netWorkServerThread.start();
	}

	@Override
	public void stop()
	{
		runningFlag = false;
	}

	@Override
	public String getIp()
	{
		return serverSocketChannel.socket().getLocalSocketAddress().toString();
	}

	@Override
	public int getPort()
	{
		return port;
	}

	private void dispatch()
	{
		try
		{
			if (selector.select(NetWorkConstants.DEFAULT_NETWORK_TIME_OUT) <= 0)
			{
				return;
			}
		}
		catch (IOException e)
		{
			log.error(e);
		}

		Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

		while (iterator.hasNext())
		{
			SelectionKey key = (SelectionKey) iterator.next();
			iterator.remove();
			try
			{
				if (!key.isValid())
				{
					key.cancel();
					continue;
				}

				if (key.isAcceptable())
				{
					onAccept(key);
				}
				else if (key.isReadable())
				{
					onRead(key);
				}
				else if (key.isWritable())
				{
					onWrite(key);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				log.error(e);
				Session session = (Session) key.attachment();
				//
				sessionManager.freeSession(session);
			}
		}

	}

	@Override
	public void run()
	{
		log.info(new StringBuilder("MS-NetWork-Server started listen on ")
				.append(hostName == null ? "" : hostName + ":").append(port).toString());
		while (runningFlag)
		{
			dispatch();
		}
		log.info(new StringBuilder("MS-NetWork-Server stoped ").toString());
	}

	private void onAccept(SelectionKey key) throws IOException
	{
		SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
		Session session = sessionManager.newSession(channel);
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ, session);

		if (log.isWarnEnabled())
			log.warn("Accept client from : " + channel.socket().getRemoteSocketAddress());
	}

	private void onRead(SelectionKey key) throws Exception
	{
		Session session = (Session) key.attachment();
		session.onRead();
	}

	private void onWrite(SelectionKey key) throws Exception
	{
		key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
		Session session = (Session) key.attachment();
		session.onWrite();
	}

	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}
}
