package com.ef.dao;

import com.ef.entities.LogEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * DAO for log entries. Insertion is performed one row at a time
 * to avoid rolling back full batches if only one item fails to be
 * inserted. Still prepared statements
 * are used to avoid unnecessary query recompilations.
 * @author Andres Jaimes
 */
public class LogEntryDAO {

    private final String INSERT =
            "insert into log_entries "
            + "(ip, request_date, request, status, user_agent) "
            + "values(?, ?, ?, ?, ?)";

    private final String INSERT_BANNED_IP =
            "insert into banned_ips "
            + "(ip, description) "
            + "values(?, ?)";

    private final PreparedStatement insertLogEntry;
    private final PreparedStatement insertBannedIP;

    public LogEntryDAO(Connection conn) throws SQLException {
        this.insertLogEntry = conn.prepareStatement(INSERT);
        this.insertBannedIP = conn.prepareStatement(INSERT_BANNED_IP);
    }

    /**
     * Inserts a log entry into the database.
     * @param logEntry
     */
    public void insert(LogEntry logEntry) {
        try {
            insertLogEntry.setString(1, logEntry.getIp());
            insertLogEntry.setTimestamp(2, Timestamp.from(logEntry.getDate().toInstant()));
            insertLogEntry.setString(3, logEntry.getRequest());
            insertLogEntry.setInt(4, logEntry.getStatus());
            insertLogEntry.setString(5, logEntry.getUserAgent());
            insertLogEntry.executeUpdate();
        } catch (SQLException sqle) {
            // Ignore if already in the database...
        }
    }

    /**
     * Inserts a banned ip into the database.
     * @param ip the ip to ban
     * @param message explaining the reason for banning the given ip
     */
    public void insert(String ip, String message) {
        try {
            insertBannedIP.setString(1, ip);
            insertBannedIP.setString(2, message);
            insertBannedIP.executeUpdate();
        } catch (SQLException sqle) {
            System.err.println("Error while inserting into the database: " + message);
        }
    }

}
