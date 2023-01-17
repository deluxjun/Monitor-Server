package com.speno.xmon.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataCounter {
	private DBProperties db;

	public DataCounter() {
		this.db = DBProperties.getInstance();
	}

	public int Count_Acc_Concurrent(String CON_DATE) {
		if (this.db.open(DBProperties.dbType_Action) == false) {
			return -1;
		}

		String query = "SELECT COUNT(*) FROM TBL_ACC_CONCURRENT"
				+ "           WHERE CON_DATE=? ;";

		PreparedStatement prep = null;
		Connection con = null;
		ResultSet row = null;
		int count = 0;
		
		try {
			con = this.db.getConnection(DBProperties.dbType_Action);			
			prep = con.prepareStatement(query);
			prep.setString(1, CON_DATE);

			row = prep.executeQuery();
			if (row.next()) {
				count = row.getInt(1); // index 로 가져오기
			}			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(prep != null ){ 	
					prep.close();
					prep = null;
				}
				if(row != null){
					row.close();
					row = null;
				}
			} catch (SQLException e) {				
				e.printStackTrace();
			}			
			if (con != null)
				this.db.releaseConnection(DBProperties.dbType_Action, con);
		}
		return count;
	}

}
