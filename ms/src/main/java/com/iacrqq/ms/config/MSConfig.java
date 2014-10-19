package com.iacrqq.ms.config;

/**
 * 
 * 系统配置
 * 
 * @author sihai
 *
 */
public class MSConfig {
	
	public Long DEFAULT_SLAVE_COMMAND_PRODUCE_INTERVAL = 60L;		// unit second
	
	private Long slaveCommandProduceInterval = DEFAULT_SLAVE_COMMAND_PRODUCE_INTERVAL;

	public Long getSlaveCommandProduceInterval() {
		return slaveCommandProduceInterval;
	}

	public void setSlaveCommandProduceInterval(Long slaveCommandProduceInterval) {
		this.slaveCommandProduceInterval = slaveCommandProduceInterval;
	}
}
