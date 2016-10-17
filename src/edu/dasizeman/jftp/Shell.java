package edu.dasizeman.jftp;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public abstract class Shell {
	
	private Logger logger;
	private FileHandler logFile;
	private StreamHandler consoleLog;
	private SimplerFormatter simpleFormatter;
	
	public Shell(String logPath) {
		
		LogManager.getLogManager().reset();
		// Set up our logger
		logger = Logger.getGlobal();
		
		try {
			logFile = new FileHandler(logPath, true);
		} catch (SecurityException | IOException e) {
			System.out.println("Could not open log file.");
			System.exit(1);
		}
		
		simpleFormatter = new SimplerFormatter();
		consoleLog = new StreamHandler(System.out, simpleFormatter) {
	        @Override
	        public synchronized void publish(final LogRecord record) {
	            super.publish(record);
	            flush();
	        }
	    };
		consoleLog.setLevel(Level.ALL);
		logFile.setFormatter(simpleFormatter);
		logFile.setLevel(Level.ALL);
		
		
		logger.addHandler(logFile);
		logger.addHandler(consoleLog);
		
		logger.setLevel(Level.ALL);
	}
	
	protected abstract void init();
	
	public abstract void doCommand(String commandStr) throws Throwable;
	
	protected abstract String welcomeMessage();
	
	public void run() {
		Scanner inputScanner = new Scanner(System.in);
		String inputString = "";

		System.out.println(welcomeMessage());
		System.out.println("Type 'help' for commands.");
		do {
			System.out.print(">");
			inputString = inputScanner.nextLine(); 
			if (inputString.equals("")) {
				continue;
			}

			try {
				doCommand(inputString);
			} catch (Throwable e) {
				logger.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}while (!inputString.equals("quit"));
		
		inputScanner.close();
		System.out.println("...bye :(");
	}
	
}
