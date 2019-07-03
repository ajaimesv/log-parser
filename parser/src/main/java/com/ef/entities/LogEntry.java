package com.ef.entities;

import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an entry in a log file.
 * @author Andres Jaimes
 */
@Getter
@Setter
public class LogEntry {
    private String ip;
    private ZonedDateTime date;
    private String request;
    private int status;
    private String userAgent;
}
