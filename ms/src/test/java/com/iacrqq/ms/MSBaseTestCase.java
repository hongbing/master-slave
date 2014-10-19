package com.iacrqq.ms;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class MSBaseTestCase extends
		AbstractDependencyInjectionSpringContextTests {

	@Override
	protected String[] getConfigLocations() {
		return new String[]{"classpath*:/spring-test/spring-ms-test.xml"};
	}
}
