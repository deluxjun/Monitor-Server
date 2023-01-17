package com.speno.xmon.agent;

import java.util.Hashtable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;


public class Log4jLogger implements ILogger {
	Logger	m_log=null;

//	log4j.appender.file.MaxFileSize=100MB
//			log4j.appender.file.MaxBackupIndex=50
//			log4j.appender.file.layout=org.apache.log4j.PatternLayout
//			log4j.appender.file.layout.ConversionPattern=[%5p] %d{dd,HH:mm:ss.SSS} (%t:%c:%M:%L) -- %m%n

	private Logger getLogger(String logFile, String fileSize, int backupIndex, String strLevel) {
		RollingFileAppender fa = new RollingFileAppender();
		fa.setName("XmonAgent");
//		fa.setDatePattern(".yyyy-MM-dd");
		fa.setMaxFileSize(fileSize);
		fa.setMaxBackupIndex(backupIndex);
		fa.setFile(logFile);
		fa.setLayout(new PatternLayout("[%5p] %d{[yyyy.MM.dd,HH:mm:ss.SSS]} -- %m%n"));
		Level level = Level.toLevel(strLevel);
		fa.setThreshold(level);
		fa.setAppend(true);
		fa.activateOptions();

		org.apache.log4j.Logger.getLogger("XmonAgent").addAppender(fa);
		return Logger.getLogger("XmonAgent");
	}

	
	public void init(Hashtable attrs) throws Exception{
		// Only one should be labeled "file"
		String file = (String)attrs.get("NAME");
		if (file != null){

    		try{
//    	    	PropertyConfigurator.configure(file);
//                m_log = Logger.getLogger((String)attrs.get("SECTION"));		
    			
    			String fileSize = (String)attrs.get("FILESIZE");
    			int backupIndex = 50;
    			try {
        			backupIndex = Integer.parseInt((String)attrs.get("BACKUPINDEX"));
				} catch (Exception e) {}
    			if (fileSize == null || fileSize.length() < 1)
    				fileSize = "10MB";
    			
    			String strLevel = (String)attrs.get("LEVEL");
    			if (strLevel == null || strLevel.length() < 1) {
    				strLevel = "INFO";
    			}
    			m_log = getLogger(file, fileSize, backupIndex, strLevel);
    			
	    		// Opening line
				info("***** Log Started *****");
    			return;
    		}
    		catch (Exception e){
    			// Oops 
    			throw e;
    		}
		}
		else{
			throw new Exception("File 'name' parameter not specified");
		}

	}
//	
//	public void info(String defMsg, String ins1, String ins2){
//    	if(ins1 == null) ins1 = "";
//    	if(ins2 == null) ins2 = "";
//    	defMsg = defMsg.replaceFirst("%1", ins1);
//    	defMsg = defMsg.replaceFirst("%2", ins2);
//
//   		m_log.info(defMsg);
//	}
//	public void debug(String defMsg, String ins1, String ins2){
//    	if(ins1 == null) ins1 = "";
//    	if(ins2 == null) ins2 = "";
//    	defMsg = defMsg.replaceFirst("%1", ins1);
//    	defMsg = defMsg.replaceFirst("%2", ins2);
//
//   		m_log.debug(defMsg);
//	}
//	public void error(String defMsg, String ins1, String ins2){
//    	if(ins1 == null) ins1 = "";
//    	if(ins2 == null) ins2 = "";
//    	defMsg = defMsg.replaceFirst("%1", ins1);
//    	defMsg = defMsg.replaceFirst("%2", ins2);
//
//   		m_log.error(defMsg);
//	}

	public void warn(String message){
		m_log.warn(message);
	}

	public void debug(String message){
		m_log.debug(message);
	}
	public void error(String message){
		m_log.error(message);
	}
	public void info(String message){
		m_log.info(message);
	}
}
