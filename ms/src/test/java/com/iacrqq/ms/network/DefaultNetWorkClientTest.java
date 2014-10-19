package com.iacrqq.ms.network;

import javax.annotation.Resource;

import org.junit.Test;

import com.iacrqq.ms.MSBaseTestCase;


public class DefaultNetWorkClientTest extends MSBaseTestCase{

	@Resource
	private NetWorkClient netWorkClient;
	

	@Test
	public void testNetWorkClient() throws Exception {
		netWorkClient.init();
		netWorkClient.start();
		Thread.currentThread().join();
	}
	
	public void setNetWorkClient(NetWorkClient netWorkClient) {
		this.netWorkClient = netWorkClient;
	}
}
