package com.ef;

import com.ef.entities.LogEntry;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides functions for parsing a single entry from a log file into a LogEntry instance.
 * @author Andres Jaimes
 */
public class LogEntryParser {
    /**
     * The field separator found in the log file.
     */
    private static final String SEPARATOR = "\\|";

    /**
     * The date format for date fields in the log file.
     */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Internal regex pattern to remove surrounding quotes from strings.
     */
    private static final Pattern PATTERN = Pattern.compile("^\"|\"$");

    /**
     * Parses a log entry string into a LogEntry instance.
     * @param line a log entry from a log file.
     * @return An Optional instance containing a LogEntry (if parsing went fine),
     *         or an empty value if an exception occurred during the parsing process.
     */
    public static Optional<LogEntry> mapToLogEntry(String line) {
        try {
            Matcher m;
            String[] p = line.split(SEPARATOR);

            LogEntry logEntry = new LogEntry();
            logEntry.setDate(ZonedDateTime.parse(p[0], DATE_FORMAT.withZone(ZoneOffset.UTC)));
            logEntry.setIp(p[1]);
            m = PATTERN.matcher(p[2]); // Remove surrounding quotes for request and user agent
            logEntry.setRequest(m.replaceAll(""));
            logEntry.setStatus(Integer.parseInt(p[3]));
            logEntry.setUserAgent(m.reset(p[4]).replaceAll(""));

            return Optional.ofNullable(logEntry);
        } catch (DateTimeParseException | NumberFormatException e) {
            System.err.println("Parser error: " + e.getMessage() + " at line: " + line);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Parser error: Non-existing field " + e.getMessage() + " at line: " + line);
        }
        return Optional.empty();
    }

}
