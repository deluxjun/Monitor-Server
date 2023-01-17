package com.speno.xmon.db.collectLog;

//Import the Java classes
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

import com.speno.xmon.env.xmPropertiesXml;


/**
* Implements console-based log file tailing, or more specifically, tail following:
* it is somewhat equivalent to the unix command "tail -f"
*/
public class Tail implements LogFileTailerListener
{
/**
* The log file tailer
*/
private LogFileTailer tailer;
private Queue<String> logQueue = new LinkedList<String>();
public boolean bSystemOut = false;
public boolean bFileExists = true;;
/**
* Creates a new Tail instance to follow the specified file
*/
public Tail( String filename, String threadName )
{
	
	if(new File( filename ).exists())
		bFileExists = true;
	else
	{
		bFileExists = false;
		return;
	}
	 tailer = new LogFileTailer( new File( filename ), 2000, xmPropertiesXml.startAtLog);
	 tailer.addLogFileTailerListener( this );	 
	 tailer.setName("LogTailer_" + threadName);
	 tailer.start();
}

public void stopTail(){
	if (tailer != null)
		tailer.stopTailing();
}
/**
* A new line has been added to the tailed log file
* 
* @param line   The new line that has been added to the tailed log file
*/
public void newLogFileLine(String line)
{
	if(bSystemOut)  System.out.println( line );
	else
	{
		logQueue.offer(line+ "\n");
		//20150909 로그 사이즈가 너무 큰 경우 지움
		if(logQueue.size() > 10000){
			logQueue.poll();
		}
	}
}
public String getQueueLogFileLine(int i)
{
	String text="";
	if(i == 0)
	{
		int maxLine = 1000;
		while(!logQueue.isEmpty())
		{
			text += logQueue.poll() ;
			maxLine--;
			if(maxLine <0) break;
		}
	}
	
	for(int line=0;line<i;line++)
	{
		if(logQueue.isEmpty()) break;
		text += logQueue.poll() ;
	}
	return text;
}
/**
* Command-line launcher
*/
public static void main( String[] args )
{
	String fn  = "D:\\DEV_ENV\\workspace3\\xMonitor_test\\Log_xMonAgent.log";
 
	/*
	if( args.length < 1 )
 {
   System.out.println( "Usage: Tail <filename>" );
   System.exit( 0 );
 }
 */
	
 Tail tail = new Tail( fn, "threadName");
 tail.bSystemOut = false;
 tail.getQueueLogFileLine(1);
 
}
}