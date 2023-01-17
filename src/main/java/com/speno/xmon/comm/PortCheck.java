package com.speno.xmon.comm;

import java.io.IOException;
import java.net.Socket;

public class PortCheck {
	Socket client_socket = null;
	
	public PortCheck() {	}
	public boolean PortCheckRun(String ip, int port)
	{
		if (ip == null || ip.length() < 1)
			return false;
		
		try {
		      client_socket = new Socket(ip, port);
		      
		      if(client_socket.isConnected()) {
		    	  return true;
		      }
		}
		catch (Exception e) {
			return false;
		}
		finally {
		    if(client_socket != null)
		    	try { client_socket.close(); }
				catch (IOException e) { e.printStackTrace(); }	
		}
		return false;
	}
}
