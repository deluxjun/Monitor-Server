package com.speno.xmon.codedic;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.core.session.IoSession;

public class DicCommands {
	public final static String SessionAgent					= "SessionAgent";
	
	public final static String Trans_Stats						= "Trans_Stats";
	public final static String Trans_Unit						= "Trans_Unit";
	
	public static final String Set_AgentNames 			= "Set_AgentNames";
	public static final String Set_AgentHealth 				= "Set_AgentHealth";
	public static final String Set_AgentHealthPort 		= "Set_AgentHealthPort";
	
	public static final String Set_Commands 				= "Set_Commands";
	public static final String Set_ActionNames 			= "Set_ActionNames";
	
	public static final String Set_LogIds 						= "Set_LogIds";
	
	public static final String Set_EnvProperties_Main 	= "Set_EnvProperties_Main";
	public static final String Set_EnvProperties_Sch 	= "Set_EnvProperties_Sch";
	
	public static final String Set_InitSession 				= "Set_InitSession";
	
	public static final String Set_EnvAmass 				= "Set_EnvAmass";
	public static final String Set_EnvAgent 					= "Set_EnvAgent";
	
	public static final String Log_Persist						= "Log_Persist";
	public static final String Log_LogText					= "Log_LogText";
	public static final String Log_Event						= "Log_Event";
	
	public static final String Event_Level					= "Event_Level";
	
	// 20141029, junsoo, error codes
	public static final String GENERAL_ERROR		= "-1";
	public static final String NODATA				= "-100";
	public static final String OK					= "0";
	
	public  static final ConcurrentMap<String, IoSession> htConsoleCmdSessionFromAmassServer 
																					= new ConcurrentHashMap<String, IoSession>();

	/*
	 * key: Req_JavaHeap 
	 * value: USE, TOTAL 
	 */
	public static ConcurrentMap<String, HashMap<String,List<String>>> ResourceCommand	= new ConcurrentHashMap<String, HashMap<String,List<String>>>();

	public static ConcurrentMap<String, HashMap<String,String>> AgentMyActionsList		= new ConcurrentHashMap<String, HashMap<String,String>>();

	
}
