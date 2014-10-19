package com.iacrqq.ms.slave.failure.slave;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.ms.master.MasterState;

public class DefaultMasterFailureHandler implements MasterFailureHandler {

	private static final Log log = LogFactory.getLog(DefaultMasterFailureHandler.class);
	
	@Override
	public void handle(List<MasterState> masterStateList) {
		log.warn("Master down");
		dump(masterStateList);
	}
	
	private void dump(List<MasterState> masterStateList) {
		log.debug("Start to dump masterStateList in MasterFailureMonitor:");
		for(MasterState masterState : masterStateList) {
			log.debug(masterState);
		}
	}

}
