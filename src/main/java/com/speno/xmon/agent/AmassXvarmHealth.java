package com.speno.xmon.agent;

import java.util.HashMap;



public class AmassXvarmHealth  implements IxMon 
{
	public long getValue(String resourceID, String propertyName) 
	{
		if(propertyName.equals("USE"))
			return 0;
		else if(propertyName.equals("TOTAL"))
			return 0;
		
		return 0;
	}

	public HashMap<String, String> getExtMap() {
		// TODO Auto-generated method stub
		return null;
	}

}
