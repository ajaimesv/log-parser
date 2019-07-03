package com.ef.dao;

import com.ef.Configuration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Andres Jaimes
 */
public class ConnectionManager {

    /**
     * Opens a connection to the database.
     * @param config
     * @return An active database connection
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public static Connection getConnection(Configuration config) throws ClassNotFoundException, SQLException {
        Properties props = new Properties();

        props.put("user", config.getDatabaseUser());
        props.put("password", config.getDatabasePassword());

        String connString = "jdbc:mysql://"
                + config.getDatabaseHost() + ":"
                + config.getDatabasePort() + "/"
                + config.getDatabaseName()
                + "?useSSL=false&serverTimezone=UTC";

        Class.forName(config.getDatabaseDriver());
        Connection conn = DriverManager.getConnection(connString, props);
        return conn;
    }

}
