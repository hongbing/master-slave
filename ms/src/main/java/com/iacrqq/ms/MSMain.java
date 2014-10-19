package com.iacrqq.ms;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * start class
 * 
 * java MSMain -mode master
 * java MSMain -mode slave
 * 
 * 
 * @author raoqiang
 *
 */
public class MSMain {
	
	private static final String MASTER_BEAN_NAME = "master";
	private static final String SLAVE_BEAN_NAME = "slave";
	
	private static final String MASTER_MODE = "master";
	private static final String SLAVE_MODE = "slave";
	
	private static final String ARG_MODE_KEY = "-mode";
	
	private static final int MIN_ARGS_LENGTH = 2;
	
	private static final String APPLICATION_CONTEXT_FILE = "spring/spring-ms.xml";
	private static ApplicationContext applicationContext = null;
	
	public static void init() {
		applicationContext = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT_FILE);
	}
	
	public static void showUsage() {
		System.out.println("MS");
		System.out.println("	Usage:");
		System.out.println("			Run as master mode:");
		System.out.println("				java -jar ms.jar MSMain -mode master");
		System.out.println("			Run as slave mode:");
		System.out.println("				java -jar ms.jar MSMain -mode slave");
	}
	
	public static void main(String[] args) {
		if(args.length < MIN_ARGS_LENGTH) {
			showUsage();
			return;
		}
		
		if(!ARG_MODE_KEY.equals(args[0])) {
			showUsage();
			return;
		}
		
		if(MASTER_MODE.equals(args[1])) {
			runAsMaster();
		} else if(SLAVE_MODE.equals(args[1])) {
			runAsSlave();
		} else {
			showUsage();
			return;
		}
		
		try{
			Thread.currentThread().join();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void runAsMaster() {
		System.out.println("Run as master model ......");
		init();
		Node node = (Node)applicationContext.getBean(MASTER_BEAN_NAME);
		node.init();
		node.start();
	}
	
	public static void runAsSlave() {
		System.out.println("Run as slave model ......");
		init();
		Node node = (Node)applicationContext.getBean(SLAVE_BEAN_NAME);
		node.init();
		node.start();
	}
}
