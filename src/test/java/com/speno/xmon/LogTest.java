package com.speno.xmon;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.slf4j.LoggerFactory;


public class LogTest {
	private void init() {
		DailyRollingFileAppender fa = new DailyRollingFileAppender();
		fa.setName("FileLogger1");

		fa.setFile("log/logtest1.log");
		fa.setLayout(new PatternLayout("%d{[yyyy.MM.dd,HH:mm:ss.SSS]} %M -- %m%n"));
		fa.setThreshold(Level.INFO);
		fa.setAppend(true);
		fa.activateOptions();

		Logger.getLogger("FileLogger1").addAppender(fa);

		fa = new DailyRollingFileAppender();
		fa.setName("FileLogger2");

		fa.setFile("log/logtest2.log");
		fa.setLayout(new PatternLayout("%d{[yyyy.MM.dd,HH:mm:ss.SSS]} %M -- %m%n"));
		fa.setThreshold(Level.INFO);
		fa.setAppend(true);
		fa.activateOptions();

		  //add appender to any Logger (here is root)
		Logger.getLogger("FileLogger2").addAppender(fa);
	}
	
	public static void main(String[] args) {
		LogTest log = new LogTest();
		log.init();
		
		org.slf4j.Logger logger1 = LoggerFactory.getLogger("FileLogger1");
		logger1.info("test11111111111111111");

		org.slf4j.Logger logger2 = LoggerFactory.getLogger("FileLogger2");
		logger2.info("test22222222222222222");
		
	}
}
