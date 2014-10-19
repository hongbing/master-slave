package com.iacrqq.ms;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.iacrqq.ms.master.MasterNode;
import com.iacrqq.ms.slave.SlaveNode;

public class MSTest extends MSBaseTestCase{
	
	@Autowired
	@Qualifier("master")
	private MasterNode master;

	@Autowired
	@Qualifier("slave")
	private SlaveNode slave;
	
	@Test
	public void test() throws Exception {
		master.init();
		slave.init();
		master.start();
		slave.start();
		
		Thread.currentThread().sleep(30000);
		
		//master.stop();
		
		Thread.currentThread().join();
	}
	
	public void setMaster(MasterNode master) {
		this.master = master;
	}

	public void setSlave(SlaveNode slave) {
		this.slave = slave;
	}
	
}
