package com.speno.xmon.sms;

import java.util.Map;

public interface SendSMS {	
	int sendSms();
	int addSmsMgs(String ReciverNumber,String senderNumber,String msg);
	int setUser(String id, String pass);
	int connectSms();
	int disConnectSms();
	int setMultiUser(Map<String, String> map);	
}
