package edu.dasizeman.jftp;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class Shell {
	
	private Logger logger;
	private FileHandler logFile;
	private ConsoleHandler consoleLog;
	private SimpleFormatter simpleFormatter;
	
	public Shell(String logPath) {
		// Set up our logger
		logger = Logger.getGlobal();
		
		try {
			logFile = new FileHandler(logPath);
		} catch (SecurityException | IOException e) {
			System.out.println("Could not open log file.");
			System.exit(1);
		}
		
		consoleLog = new ConsoleHandler();
		
		simpleFormatter = new SimpleFormatter();
		logFile.setFormatter(simpleFormatter);
		consoleLog.setFormatter(simpleFormatter);
		
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
		while (inputString.toLowerCase() != "exit") {
			System.out.print(">");
			inputString = inputScanner.nextLine(); 
			
			try {
				doCommand(inputString);
			} catch (Throwable e) {
				System.out.println(e.getMessage());
			}
		}
		
		inputScanner.close();
		System.out.println("...bye :(");
	}
	
}
