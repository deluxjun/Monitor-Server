package com.speno.xmon.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class SqliteConn {

	SqliteConn()
	{
		try {
			InitialContext ctx = new InitialContext();
			Object obj = ctx.lookup("java:comp/env/jdbc/MyDB");  
			if(obj!=null)
			 {
			  javax.sql.DataSource ds = (javax.sql.DataSource)obj;
			  
			  Connection con = ds.getConnection();
			  Statement stmt = con.createStatement();
			  
			  String sql = "select * from board";
			  ResultSet rs = stmt.executeQuery(sql);
			  
			  //out.println("커넥션 완료!!!");
			  
			  while(rs.next()){
			   //out.println(rs.getString(1) + "<br/>");
			  }
			  
			  stmt.close();
			  con.close();
			 }
			 else
			 {
			  //out.println("검색 실패!!!");
			 }



		}
		catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
	}
}
