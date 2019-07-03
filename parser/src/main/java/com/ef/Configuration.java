package com.ef;

import com.ef.exceptions.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;
import lombok.Getter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Initializes the application parameters.
 * @author Andres Jaimes
 */
public class Configuration {
    @Getter private String accessLog;
    @Getter private ZonedDateTime startDate;
    @Getter private ZonedDateTime endDate;
    @Getter private long startDateMillis;
    @Getter private long endDateMillis;
    @Getter private String duration;
    @Getter private int threshold;

    @Getter private String databaseHost;
    @Getter private String databasePort;
    @Getter private String databaseName;
    @Getter private String databaseUser;
    @Getter private String databasePassword;
    @Getter private String databaseDriver;

    /**
     * Default configuration file.
     */
    private final String DEFAULT_CONFIG_FILE = "parser.properties";

    /**
     * Custom properties file. Users can use this to override any default values.
     */
    private final String CUSTOM_CONFIG_FILE_PROP = System.getProperty("user.home") + "/.parser.properties";

    /**
     * Keeps the configuration values read from the properties file.
     */
    protected Properties appProps;

    /**
     * Keeps a list of the available command line options (arguments).
     */
    private Options options;

    /**
     * Creates a new instance of this class.
     * @param args the arguments passed to the application on the command line.
     * @throws com.ef.exceptions.ConfigurationException
     */
    public Configuration(String[] args) throws ConfigurationException {
        initCmdLineArguments();
        readCmdLineArguments(args);
        initPropFile();
    }

    /**
     * Reads the database connection parameters from the properties file.
     * A user may override values in his/her home directory.
     * @throws ConfigurationException
     */
    private void initPropFile() throws ConfigurationException {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE);
            Properties defaultProps = new Properties();
            defaultProps.load(in);
            in.close();

            appProps = new Properties(defaultProps);
            File appPropsFile = new File(CUSTOM_CONFIG_FILE_PROP);
            if (appPropsFile.exists() && appPropsFile.isFile()) {
                in = new FileInputStream(appPropsFile);
                appProps.load(in);
                in.close();
            }

            databaseHost = appProps.getProperty("db.host");
            databasePort = appProps.getProperty("db.port");
            databaseName = appProps.getProperty("db.name");
            databaseUser = appProps.getProperty("db.user");
            databasePassword = appProps.getProperty("db.password");
            databaseDriver = appProps.getProperty("db.driver");

        } catch (IOException ioe) {
            throw new ConfigurationException(ioe);
        }
    }

    /**
     * Initializes the command line arguments.
     */
    private void initCmdLineArguments() {
        options = new Options();
        options.addOption(Option.builder()
                                .longOpt("accesslog")
                                .desc("path to log file")
                                .hasArg()
                                .argName("path-to-file")
                                .build());
        options.addOption(Option.builder()
                                .longOpt("startDate")
                                .desc("the start date in the following format: yyyy-MM-dd.HH:mm:ss")
                                .hasArg()
                                .argName("start-date")
                                .build());
        options.addOption(Option.builder()
                                .longOpt("duration")
                                .desc("the duration value: hourly|daily")
                                .hasArg()
                                .argName("duration")
                                .build());
        options.addOption(Option.builder()
                                .longOpt("threshold")
                                .desc("the minimum number of incidents to look for")
                                .hasArg()
                                .argName("n")
                                .build());
    }

    /**
     * Displays a help text to aid users to run this application.
     */
    private void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -cp \"parser.jar\" com.ef.Parser",
                "\nParses a web server access log file, loads the log to MySQL and looks for IPs that make more than a certain number of requests for the given duration.\n\n",
                options,
                "\nDatabase connectivity options can be overriden at ~/.parser.properties file.\n\n", true);
    }

    /**
     * Parses the list of arguments passed in the command line.
     * @param args
     * @throws ConfigurationException
     */
    private void readCmdLineArguments(String args[]) throws ConfigurationException {
        try {
            if (args == null || args.length == 0) {
                throw new IllegalArgumentException();
            }

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss");
            accessLog = cmd.getOptionValue("accesslog");
            startDate = ZonedDateTime.parse(cmd.getOptionValue("startDate"), dtf.withZone(ZoneOffset.UTC));
            duration = cmd.getOptionValue("duration").matches("hourly|daily") ? cmd.getOptionValue("duration") : null;
            threshold = Integer.parseInt(cmd.getOptionValue("threshold"));
            if(accessLog == null || !Files.isRegularFile(Paths.get(accessLog))) {
                throw new ConfigurationException("Cannot read file: " + accessLog);
            }
            if (duration == null) {
                showHelp();
                throw new ConfigurationException("Duration has an invalid value.");
            }
            if ("hourly".equals(duration)) {
                endDate = startDate.plusHours(1);
            } else if ("daily".equals(duration)) {
                endDate = startDate.plusDays(1);
            }
            startDateMillis = startDate.toInstant().toEpochMilli();
            endDateMillis = endDate.toInstant().toEpochMilli();
        } catch (IllegalArgumentException | ParseException | DateTimeParseException ex) {
            showHelp();
            throw new ConfigurationException("The command has some invalid or missing arguments. Please review them and run it again.");
        }
    }

}
