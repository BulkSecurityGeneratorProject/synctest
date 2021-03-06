package com.oc.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.postgresql.util.PSQLException;

public class OcConnect {

	public Connection c = null;
	public Statement s = null;
	public ResultSet rs = null;

	public static OcConnect getConnection(String query) throws SQLException,
			ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		OcConnect result = new OcConnect();
		result.c = DriverManager.getConnection(
				"jdbc:postgresql://127.0.0.1:5432/openclinica", "clinica",
				"clinica");
		result.s = result.c.createStatement();
		try {
			result.rs = result.s.executeQuery(query);
		} catch (PSQLException e) {
		}
		return result;
	}

	public static void close(OcConnect oc) throws SQLException {
		oc.s.close();
		oc.c.close();
	}
}
