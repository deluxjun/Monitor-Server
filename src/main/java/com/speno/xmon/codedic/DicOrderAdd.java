package com.speno.xmon.codedic;

import com.speno.xmon.env.xmPropertiesXml;

public class DicOrderAdd {
	
	/****************************************************************************************/
	/************************************** ��û�ʼ� ***************************************/
	/****************************************************************************************/
	public static final String TargetAgent			="TargetAgent";
	public static final String AgentName				="AgentName";    
	public static final String ConsoleCmdID		="ConsoleCmdID";
	public static final String RequestDateTime	="RequestDateTime";
	public static final String RequestOrderType	="OrderType";
	
	/****************************************************************************************/
	/****************************** �󼼿�û RequestType*********************************/
	/****************************************************************************************/
	public static final String ConsoleId			= "ConsoleId";
	public static final String SendTime			= "SendTime";
	public static final String Command 			="Command";
	public static final String ResourceID			="ResourceID";
	public static final String PerUnit				= "PerUnit";
	public static final String ValueUnit			= "ValueUnit";
	public static final String PropertyValueUnit= "PropertyValueUnit";
	public static final String AggreUnit			= "AggreUnit";
	public static final String RangeStart			= "RangeStart";
	public static final String RangeEnd			= "RangeEnd";
	public static final String AggregatedTime	= "AggregatedTime";
	public static final String ReturnMsg			= "ReturnMsg";
	public static final String ReturnCode			= "ReturnCode";
	
	public static final String PropertyName 	= "PropertyName";
	public static final String PropertyNames 	= "PropertyNames";
	public static final String ActionName 		= "ActionName";
	public static final String CommandList 		= "CommandList";

	public static final String ValuesComma 			="ValuesComma";
	
	public static final String DUMMY 			="DUMMY";

	
	public static final String RequestedID                    ="RequestedID";
	
	public static final String AgentTransactionID				="TransactionID";
	
	public static final String AgentTransDateTime			="TransDateTime";
	public static final String AgentValueUnit					="ValueUnit";
	public static final String TransTime 							= "TransInitTime";
	
	public static final String TransactionID		="TransactionID";
	public static final String TransDateTime	="TransDateTime";

	//public static final String AgentValue							="Value";
	public static final String Exten									= "Ext" + xmPropertiesXml.Sep;
	
	public static final String LogID 					= "LogID";
	public static final String LogText 				= "LogText";

	public static final String Title									= "Title";
	public static final String Description							= "Description";
	public static final String AggreUseYN 						="AggreUseYN";
	public static final String HealthUseYN 						="HealthUseYN";
	
	
	public static String AmassPreFix_Log						="Log";
	public static String AmassSufFix_Log						="Persist";
	 
	public static String AmassPreFix_Trans					="Trans";
	public static String AmassPreFix_Resource				="Req";
	
	public static String AmassSufFix_ActionInit				="Init";
	public static String AmassSufFix_ActionCompletion	="Comp";
	public static String AmassSufFix_ActionCompSuc		="Success";
	public static String AmassSufFix_ActionCompError	="Error";
	public static String AmassSufFix_ActionCompTimeout	="Timeout";
	
	public static final String TransID								= "TransID";
	public static final String TransResult_Success			= "Suc";
	public static final String TransResult_Error               = "Err";
	public static final String TransResult_TimeOut			= "Out";
	
	public static final String AgentLogID 						= "LogID";
	public static final String AgentLogText 						= "LogText";
	
	
	public static final String EventName							= "EventName";
	public static final String EventText							= "EventText";
	public static final String EventLevel          				= "EventLevel";
	public static final String EventDateTime					= "EventDateTime";
	
	
	public static final String StatisticsResource = "StatisticsResource";
	public static final String StatisticsTPS = "StatisticsTPS";
	public static final String StatisticsResponse = "StatisticsResponse";
	
	public static final String ResourceGroupID		= "ResourceGroupID";
	
}
