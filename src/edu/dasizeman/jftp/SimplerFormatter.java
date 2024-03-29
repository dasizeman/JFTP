package edu.dasizeman.jftp;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimplerFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        StringBuffer log = new StringBuffer();
        log.append("[" + 
        		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(record.getMillis()))
        		+ "]");
        log.append(" ");
        log.append("<" + record.getLevel().getName() + ">");
        log.append(" ");
        //log.append("[").append(record.getSourceClassName() ?: record.getLoggerName())
        //log.append(" ")
        //log.append(record.getSourceMethodName() ?: " - ").append("]")
        log.append(": ");
        log.append(record.getMessage());
        log.append(System.getProperty("line.separator"));
        return log.toString();
    }
}