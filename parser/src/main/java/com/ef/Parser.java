package com.ef;

import com.ef.dao.ConnectionManager;
import com.ef.dao.LogEntryDAO;
import com.ef.entities.LogEntry;
import com.ef.exceptions.ConfigurationException;
import com.ef.exceptions.ParserException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * The main parser class.
 * @author Andres Jaimes
 */
public class Parser {

    /**
     * The application configuration.
     */
    private final Configuration config;

    /**
     * Instantiates and loads the configuration instance required to
     * run the application.
     * @param args the command line arguments passed to the application.
     * @throws ConfigurationException if the configuration cannot be
     *         instantiated.
     */
    public Parser(String[] args) throws ConfigurationException {
        config = new Configuration(args);
    }

    /**
     * Parses the log file and saves it into the database.
     * @param dao the related database layer for saving the log entries.
     * @return A list of ip's that match the date criteria.
     */
    private Map<String, Integer> parseLogFile(LogEntryDAO dao) throws ParserException {
        Map<String, Integer> ips = new HashMap<>();
        int i = 0;

        try(BufferedReader br = new BufferedReader(new FileReader(config.getAccessLog()))) {
            System.out.print("Parsing...\r");
            for(String line; (line = br.readLine()) != null; ) {
                Optional<LogEntry> logEntry = LogEntryParser.mapToLogEntry(line);
                if (logEntry.isPresent()) {
                    dao.insert(logEntry.get());

                    // If the current entry matches the given date criteria, then add
                    // to the list of ip's...
                    String ip = logEntry.get().getIp();
                    long date = logEntry.get().getDate().toInstant().toEpochMilli();
                    if (date >= config.getStartDateMillis() && date < config.getEndDateMillis()) {
                        if (ips.containsKey(logEntry.get().getIp())) {
                            ips.put(ip, ips.get(ip) + 1);
                        } else {
                            ips.put(ip, 1);
                        }
                    }
                }
                if (i % 5000 == 0) {
                    System.out.print("Parsing... " + i + " rows          \r");
                }
                i++;
            }
            System.out.println("Parsing done, " + i + " total rows.");
        } catch (IOException ioe) {
            throw new ParserException(ioe);
        }

        return ips;
    }

    /**
     * Filters the list of resulting ip's and prints them out.
     * @param dao the related database layer for saving the banned ip's.
     * @param ips
     */
    private void filterIPs(Map<String, Integer> ips, LogEntryDAO dao) {
        int results = 0;
        for (Entry<String, Integer> e: ips.entrySet()) {
            if (e.getValue() > config.getThreshold()) {
                String msg = String.format(
                        "%s has %d requests between %s and %s (duration: %s).",
                        e.getKey(), e.getValue(),
                        config.getStartDate(), config.getEndDate(),
                        config.getDuration());
                System.out.println(msg);
                dao.insert(e.getKey(), msg);
                results++;
            }
        }
        if (results == 0) {
            System.out.println("No ip's found that match the given parameters.");
        }
    }

    /**
     * Executes the main application process.
     * @throws com.ef.exceptions.ParserException
     */
    public void start() throws ParserException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection(config);
            LogEntryDAO dao = new LogEntryDAO(conn);

            // Parse the log file and save it into the database
            Map<String, Integer> ips = parseLogFile(dao);
            // Filter IP list and print results
            filterIPs(ips, dao);

        } catch (ClassNotFoundException | SQLException e) {
            throw new ParserException(e.getMessage());
        } finally {
            try { if (conn != null && !conn.isClosed()) conn.close();
            } catch (Exception e) {}
        }
    }

    /**
     * The main application function.
     * @param args
     */
    public static void main(String[] args) {
        try {
            Parser parser = new Parser(args);
            parser.start();
        } catch (ConfigurationException ce) {
            System.err.println("Configuration error: " + ce.getMessage());
        } catch (ParserException pe) {
            System.err.println("Error: " + pe.getMessage());
        }
    }

}
