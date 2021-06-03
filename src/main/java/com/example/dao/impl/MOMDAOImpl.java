package com.example.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.example.dao.MOMDAO;

public class MOMDAOImpl implements MOMDAO {
    
	private Connection getConnection() {
		String connectionUrl = "jdbc:mysql://localhost:3306/momdb";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(connectionUrl, "momtest","momtest");
		} catch (SQLException e) {
			System.out.println("Error in getting SQL Connection! Message: " + e.getMessage());
        	throw new RuntimeException(e);
		}
		
		return conn;
	}
	
	private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
            conn.close();
            } catch (SQLException e) {
            	throw new RuntimeException(e);
            }
        }
	}
	
    public void getTestTable() {
        String sql = "SELECT * FROM CUSTOMER";
        Connection conn = getConnection();
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
//            ps.setInt(1, custId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
            	System.out.println("{" + rs.getInt("CUST_ID") + ", " + 
            			rs.getString("NAME") + ", " + rs.getInt("AGE") + "}");
            }
            
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getConnection()" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }
    }
    
}
