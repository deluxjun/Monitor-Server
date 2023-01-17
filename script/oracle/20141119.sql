Select * From MAIN.[XmAggregatedActionSec] 
order by 1 desc;

Create  TABLE MAIN.[XmAggregatedActionMin](
[AggregatedTime] TEXT PRIMARY KEY UNIQUE
,[AgentName] TEXT
,[ActionName] TEXT
,[ResponseAvgTime] TEXT
,[ResponseMaxTime] NUMERIC
,[ResponseMinTime] NUMERIC
,[SuccessCount] NUMERIC
,[ErrorCount] NUMERIC
,[TimeOutCount] NUMERIC
) ;

Create  TABLE MAIN.[XmAggregatedActionHour](
[AggregatedTime] TEXT PRIMARY KEY UNIQUE
,[AgentName] TEXT
,[ActionName] TEXT
,[ResponseAvgTime] TEXT
,[ResponseMaxTime] NUMERIC
,[ResponseMinTime] NUMERIC
,[SuccessCount] NUMERIC
,[ErrorCount] NUMERIC
,[TimeOutCount] NUMERIC
) ;

Create  TABLE MAIN.[XmAggregatedActionDay](
[AggregatedTime] TEXT PRIMARY KEY UNIQUE
,[AgentName] TEXT
,[ActionName] TEXT
,[ResponseAvgTime] TEXT
,[ResponseMaxTime] NUMERIC
,[ResponseMinTime] NUMERIC
,[SuccessCount] NUMERIC
,[ErrorCount] NUMERIC
,[TimeOutCount] NUMERIC
) ;

Select * From MAIN.[XmAggregatedActionMin] 
order by 1 desc;

delete from MAIN.[XmAggregatedActionMin] 
where aggregatedTime = '2014-11-19 14:22';

update MAIN.[XmAggregatedActionMin]
set successCount = 0 
where aggregatedTime = '2014-11-19 14:22';

Select * From MAIN.[XmTransAction] 
order by transinittime desc;

 Select AgentName								
        , ActionName 					
        , TransResult     					
        , SUBSTR(TransCompTime,1, 19)   as AggregatedTime					
        , count(AgentName)           		as cnt  					
        , round(AVG(ResponseTime),1) 	as ResponseAvgTime 					
        , MIN(ResponseTime)          		as ResponseMinTime 					
        , MAX(ResponseTime)          		as ResponseMaxTime					
        From XmTransAction					
     WHERE  TransCompTime > (select (CASE WHEN Max(AggregatedTime) IS NULL THEN '0' ELSE Max(AggregatedTime) END) from XmAggregatedActionSec ) || 'z'					
 GROUP BY AgentName, ActionName, TransResult, SUBSTR(TransCompTime,1, 19);					

 select (CASE WHEN Max(AggregatedTime) IS NULL THEN '0' ELSE Max(AggregatedTime) END) from XmAggregatedActionSec;

  Select AgentName								
        , ActionName 					
        , TransResult     					
        , SUBSTR(TransInitTime,1, 19)   as AggregatedTime					
        , count(AgentName)           		as cnt  					
        , round(AVG(ResponseTime),1) 	as ResponseAvgTime 					
        , MIN(ResponseTime)          		as ResponseMinTime 					
        , MAX(ResponseTime)          		as ResponseMaxTime					
        From XmTransAction					
     WHERE  TransCompTime > '2014-11-19 10:26:51'					
 GROUP BY AgentName, ActionName, TransResult, SUBSTR(TransInitTime,1, 19);

delete From MAIN.[XmAggregatedActionSec]
where aggregatedtime > '2014-11-19 13:33:04'; 



Select * From MAIN.[XmAggregatedActionSec] 
where responseavgtime > 70000;


MERGE INTO XmAggregatedActionMin TT
USING (
Select	AgentName									
    			,	ActionName							
    			,	round(avg(ResponseAvgTime))	as ResponseAvgTime	
    			,	round(avg(ResponseMaxTime))	as ResponseMaxTime	
	    		,	round(avg(ResponseMinTime))	as ResponseMinTime	
	    		,	sum(SuccessCount) as SuccessCount				
	    		,	sum(ErrorCount) as ErrorCount					
	    		,	sum(TimeOutCount) as TimeOutCount				
     			,	substr(AggregatedTime,1,16) as AggregatedTimeMin
From XmAggregatedActionSec 									
WHERE	AggregatedTime > (select (CASE WHEN Max(AggregatedTime) IS NULL THEN '0' ELSE Max(AggregatedTime) END) from XmAggregatedActionMin ) || 'z'	
GROUP BY	AgentName,ActionName,substr(AggregatedTime,1,16)						
) ST
ON (TT.AggregatedTime = ST.AggregatedTimeMin)
WHEN MATCHED THEN
UPDATE SET
TT.AgentName = ST.AgentName
,TT.ActionName		= ST.ActionName
,TT.ResponseAvgTime	= ST.ResponseAvgTime
,TT.ResponseMaxTime	= ST.ResponseMaxTime				
,TT.ResponseMinTime	= ST.ResponseMinTime
,TT.SuccessCount	= ST.SuccessCount
,TT.ErrorCount		= ST.ErrorCount
,TT.TimeOutCount	= ST.TimeOutCount
,TT.AggregatedTime	= ST.AggregatedTimeMin
WHEN NOT MATCHED THEN
INSERT (AgentName						
    			,	ActionName							
	    		,	ResponseAvgTime						
	    		,	ResponseMaxTime						
	    		,	ResponseMinTime						
	    		,	SuccessCount						
	    		,	ErrorCount						
	    		,	TimeOutCount						
	    		,	AggregatedTime)
VALUES (
ST.AgentName,ST.ActionName,ST.ResponseAvgTime,
ST.ResponseMaxTime,ST.ResponseMinTime,ST.SuccessCount,
ST.ErrorCount,ST.TimeOutCount,ST.AggregatedTimeMin
);



insert or replace into   XmAggregatedActionMin (AgentName						
    			,	ActionName							
	    		,	ResponseAvgTime						
	    		,	ResponseMaxTime						
	    		,	ResponseMinTime						
	    		,	SuccessCount						
	    		,	ErrorCount						
	    		,	TimeOutCount						
	    		,	AggregatedTime)							
Select	AgentName									
    			,	ActionName							
    			,	round(avg(ResponseAvgTime))	as ResponseAvgTime	
    			,	round(avg(ResponseMaxTime))	as ResponseMaxTime	
	    		,	round(avg(ResponseMinTime))	as ResponseMinTime	
	    		,	sum(SuccessCount) as SuccessCount				
	    		,	sum(ErrorCount) as ErrorCount					
	    		,	sum(TimeOutCount) as TimeOutCount				
     			,	substr(AggregatedTime,1,16)     	
From XmAggregatedActionSec 									
WHERE	AggregatedTime >= (select (CASE WHEN Max(AggregatedTime) IS NULL THEN '0' ELSE Max(AggregatedTime) END) from XmAggregatedActionMin )	
GROUP BY	AgentName,ActionName,substr(AggregatedTime,1,16)