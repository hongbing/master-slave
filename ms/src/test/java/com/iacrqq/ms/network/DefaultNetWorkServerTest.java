package com.iacrqq.ms.network;

import javax.annotation.Resource;

import org.junit.Test;

import com.iacrqq.ms.MSBaseTestCase;


public class DefaultNetWorkServerTest extends MSBaseTestCase{
	
	@Resource
	private NetWorkServer netWorkServer;
	
	@Resource
	private NetWorkClient netWorkClient;
	

	@Test
	public void testNetWorkServer() throws Exception {
		netWorkServer.init();
		netWorkClient.init();
		netWorkServer.start();
		netWorkClient.start();
		
		Thread.currentThread().join();
	}
	
	public void setNetWorkServer(NetWorkServer netWorkServer) {
		this.netWorkServer = netWorkServer;
	}
	
	public void setNetWorkClient(NetWorkClient netWorkClient) {
		this.netWorkClient = netWorkClient;
	}

}
