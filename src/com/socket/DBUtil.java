package com.socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DBUtil {

	private static final String USERNAME = "root";
	private static final String PASSWORD = "";
	private static final String CONN_STRING = "jdbc:mysql://localhost/duttadb";
//	private static final String USERNAME = "dbuser";
//	private static final String PASSWORD = "dbpassword";
//	private static final String CONN_STRING = "jdbc:mysql://localhost/duttadb";


/*	 private static final String USERNAME = "root";
	 private static final String PASSWORD = "admin4al";
	 private static final String CONN_STRING =
	 "jdbc:mysql://103.230.62.158/duttadb";*/

	public static Connection getConnection() throws SQLException {

		return DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);

	}
}
