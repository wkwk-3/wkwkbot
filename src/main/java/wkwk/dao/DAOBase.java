package wkwk.dao;

import wkwk.paramater.DAOParameters;

import java.sql.*;

public class DAOBase {
    Connection con;
    PreparedStatement prestmt;

    protected void open() {
        try {
            prestmt = null;
            con = DriverManager.getConnection(DAOParameters.CONNECT_STRING, DAOParameters.USERID, DAOParameters.PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void close(Statement stmt) {
        try {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}