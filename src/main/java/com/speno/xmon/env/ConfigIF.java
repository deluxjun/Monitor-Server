 
package com.speno.xmon.env;

// Java stuff
import java.util.Hashtable;

// EIMS

// Interface to component logger
public interface ConfigIF{
    public void startParms(String szName, Hashtable attrs);
    public void endParms(String szName);
	
	
}