package com.oracle.labs.mlrg.olcut.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 */
public class CheckOJDBC {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");

        String url = args[0];

        Connection conn = DriverManager.getConnection(url, args[1], args[2]);

        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("select BANNER from SYS.V_$VERSION");
        while(rset.next()) {
            System.out.println(rset.getString(1));
        }
        stmt.close();

        System.out.println("Success!");
    }

}
