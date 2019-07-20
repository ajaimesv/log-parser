Web Log Parser
=================================================

Parses web log entries looking for IP addresses that have made at least N requests in a given period of time.
Useful for banning IP's.

Database setup
-------------------------------------------------

A database has to be created in order to run the application.
Please run the following script on a MySQL instance to create the required tables:

```sql
CREATE DATABASE `parser`;
USE `parser`;

CREATE TABLE `log_entries` (
  `ip` char(15) NOT NULL,
  `request_date` datetime(6) NOT NULL,
  `request` varchar(30) NOT NULL,
  `status` int(11) NOT NULL,
  `user_agent` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`ip`,`request_date`)
) ENGINE=InnoDB;

CREATE TABLE `banned_ips` (
  `ip` char(15) NOT NULL,
  `description` varchar(200) NOT NULL
) ENGINE=InnoDB;

ALTER TABLE `banned_ips`
ADD INDEX `ip_idx` (`ip` ASC);
```

Application setup
-------------------------------------------------

The application assumes that the database connection parameters are set like the following:

```properties
db.host=localhost
db.port=3306
db.name=parser
db.user=user
db.password=password
```

The application includes a default properties file in the jar file. If your MySQL settings match,
the previous ones, great you don't have to do anything else and can skip now to the next point.

If not, then you have to create a properties file in your home directory called `.parser.properties`
that overrides the previous values (note the initial dot in the file name). For example:

```properties
db.host=127.0.0.1
db.port=3306
db.name=my_parser
db.user=some-user
db.password=some-password
```

Running the application
-------------------------------------------------

You can run the application like this for a daily check up:

```sh
java -cp "parser.jar" com.ef.Parser --startDate=2017-01-01.00:00:00 --duration=daily --threshold=500 --accesslog=/path/to/access.log
```

Or like this for an hourly one:

```sh
java -cp "parser.jar" com.ef.Parser --startDate=2017-01-01.15:00:00 --duration=hourly --threshold=200 --accesslog=/path/to/access.log
```


Source code
-------------------------------------------------

The source code can be found in the `source` directory.
The project follows the maven standard.


SQL Queries for Testing
-------------------------------------------------

The following queries can help you review the previous executions.

Find IPs that made more than a certain number of requests for a given time period:

```sql
SELECT ip, count(*)
FROM parser.log_entries
WHERE request_date BETWEEN '2017-01-01 00:00:00' AND '2017-01-02 00:00:00'
GROUP BY ip HAVING count(*) > 500;
```

Find requests made by a given IP

```sql
SELECT * FROM parser.log_entries WHERE ip = '192.168.102.136'
```


Sample output for selected executions
-------------------------------------------------

For a daily execution:

```
Parsing done, 116484 total rows.
192.168.143.177 has 729 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.51.205 has 610 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.33.16 has 584 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.185.164 has 528 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.162.248 has 623 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.129.191 has 747 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.31.26 has 591 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.38.77 has 743 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.219.10 has 533 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.52.153 has 541 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.102.136 has 513 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.199.209 has 640 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.203.111 has 601 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.62.176 has 582 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
192.168.206.141 has 536 requests between 2017-01-01T00:00Z and 2017-01-02T00:00Z (duration: daily).
```

For an hourly execution:

```
192.168.106.134 has 232 requests between 2017-01-01T15:00Z and 2017-01-01T16:00Z (duration: hourly).
192.168.11.231 has 211 requests between 2017-01-01T15:00Z and 2017-01-01T16:00Z (duration: hourly).
```
