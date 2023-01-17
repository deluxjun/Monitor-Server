drop TABLE XmAggregatedResourceDay;
CREATE TABLE XmAggregatedResourceDay(
AgentName varchar2(255)
,CommandName varchar2(255)
,ResourceID varchar(30)
,PropertyName varchar2(255)
,AggregatedTime varchar2(64)
,PropertyMaxValue NUMERIC
,PropertyMinValue NUMERIC
,PropertyAvgValue NUMERIC
,PropertyValueUnit varchar2(255)
,PropertyValueCnt NUMERIC
);

drop TABLE XmAggregatedResourceHour;
CREATE TABLE XmAggregatedResourceHour(
AgentName varchar2(255)
,CommandName varchar2(255)
,ResourceID varchar(30)
,PropertyName varchar2(255)
,AggregatedTime varchar2(64)
,PropertyMaxValue NUMERIC
,PropertyMinValue NUMERIC
,PropertyAvgValue NUMERIC
,PropertyValueUnit varchar2(255)
,PropertyValueCnt NUMERIC
);

drop TABLE XmAggregatedResourceMin;
CREATE TABLE XmAggregatedResourceMin(
AgentName varchar2(255)
,CommandName varchar2(255)
,ResourceID varchar(30)
,PropertyName varchar2(255)
,AggregatedTime varchar2(64)
,PropertyMaxValue NUMERIC
,PropertyMinValue NUMERIC
,PropertyAvgValue NUMERIC
,PropertyValueUnit varchar2(255)
,PropertyValueCnt NUMERIC
);

drop TABLE XmAggregatedResourceSec;
CREATE TABLE XmAggregatedResourceSec(
AgentName varchar2(255)
,CommandName varchar2(255)
,ResourceID varchar(30)
,PropertyName varchar2(255)
,AggregatedTime varchar2(64)
,PropertyValue NUMERIC
,PropertyValueUnit varchar2(255)
,ExtMap varchar2(255)
);
CREATE INDEX XmResourceSec_Time ON XmAggregatedResourceSec(AggregatedTime);

drop TABLE XmCommandList;
CREATE TABLE XmCommandList(
AgentName varchar(30) NOT NULL
,CommandName varchar(50) NOT NULL
,ValueUnit varchar(10)
,CommandDesc varchar(100)
,CommandTitle varchar(50)
,AggreUseYN char(1) NOT NULL
,HealthUseYN char(1) NOT NULL
);

drop TABLE XmCommandSubList;
CREATE TABLE XmCommandSubList(
AgentName varchar(30) NOT NULL
,CommandName varchar(30) NOT NULL
,ResourceID varchar(30) NOT NULL
,PropertyName varchar(30) NOT NULL
);

drop TABLE XmEventLevel;
CREATE TABLE XmEventLevel(
AgentName varchar2(255)
,EventName varchar2(255)
,EventText varchar2(2000)
,EventLevel int
,EventDateTime varchar2(64)
,ReceviedTime varchar2(64)
);

drop TABLE XmActionList;
CREATE TABLE XmActionList(
AgentName varchar2(255)
,ActionName varchar2(255)
,ActionDesc varchar2(1024)
,ActionTitle varchar2(255)
,AggreUseYN char(1) NOT NULL
,HealthUseYN char(1) NOT NULL
);

drop TABLE XmAggregatedActionDay;
CREATE TABLE XmAggregatedActionDay(
AggregatedTime varchar2(64)
,AgentName varchar2(255)
,ActionName varchar2(255)
,ResponseAvgTime NUMERIC
,ResponseMaxTime NUMERIC
,ResponseMinTime NUMERIC
,SuccessCount NUMERIC
,ErrorCount NUMERIC
,TimeOutCount NUMERIC
);

drop TABLE XmAggregatedActionHour;
CREATE TABLE XmAggregatedActionHour(
AggregatedTime varchar2(64)
,AgentName varchar2(255)
,ActionName varchar2(255)
,ResponseAvgTime NUMERIC
,ResponseMaxTime NUMERIC
,ResponseMinTime NUMERIC
,SuccessCount NUMERIC
,ErrorCount NUMERIC
,TimeOutCount NUMERIC
);

drop TABLE XmAggregatedActionMin;
CREATE TABLE XmAggregatedActionMin(
AggregatedTime varchar2(64)
,AgentName varchar2(255)
,ActionName varchar2(255)
,ResponseAvgTime NUMERIC
,ResponseMaxTime NUMERIC
,ResponseMinTime NUMERIC
,SuccessCount NUMERIC
,ErrorCount NUMERIC
,TimeOutCount NUMERIC
);

drop TABLE XmAggregatedActionSec;
CREATE TABLE XmAggregatedActionSec(
AggregatedTime varchar2(64)
,AgentName varchar2(255)
,ActionName varchar2(255)
,ResponseAvgTime NUMERIC
,ResponseMaxTime NUMERIC
,ResponseMinTime NUMERIC
,SuccessCount NUMERIC
,ErrorCount NUMERIC
,TimeOutCount NUMERIC
);
CREATE INDEX XmActionSec_Time ON XmAggregatedActionSec(AggregatedTime);

drop TABLE XmTransAction;
CREATE TABLE XmTransAction(
AgentName varchar2(255)
,ActionName varchar2(255)
,TransResult varchar2(255)
,TransID varchar2(255)
,TransInitTime varchar2(64)
,TransCompTime varchar2(64)
,ResponseTime numeric
);
CREATE INDEX XmTransAction_InitTime ON XmTransAction(TransCompTime);

--create index XmTransAction_transcomptime on XmTransAction (es_templateid, es_name, es_stringvalue);

