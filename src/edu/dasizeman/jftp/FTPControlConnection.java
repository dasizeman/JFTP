package edu.dasizeman.jftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class FTPControlConnection extends Connection implements Runnable {
	private static FTPControlConnection instance;
	private static final String MODULE_NAME = "ControlConnection";
	public static final int DEFAULT_PORT = 21;
	private static Logger logger;
	
	static {
		logger = Logger.getGlobal();
	}
	
	private ProtocolManager manager;
	private UncaughtExceptionHandler handler;
	private String nextCommand;
	private BufferedWriter writer;
	private BufferedReader reader;
	
	
	public static FTPControlConnection getInstance(String host) throws Exception {
		// If no port is given, append the default port
		if (!host.contains(":")) {
			host = host + ":" + Integer.toString(DEFAULT_PORT);
		}
		
		if (instance == null) {
			instance = new FTPControlConnection(host);
		}
		
		// If the host address or port have changed, we must re-connect
		String previousHost = instance.host;
		int previousPort = instance.port;
		
		// Set's the instance's host and port
		instance.parseHostString(host);
		
		if (!instance.host.equals(previousHost) 
				|| instance.port != previousPort
				|| !instance.socket.isConnected()) {
			instance.close();
			instance = new FTPControlConnection(host);
		}
		

		return instance;
	}

	public FTPControlConnection(String host) throws Exception {
		super(host);
		this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
		this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		logger.log(Level.INFO, MODULE_NAME + ":connecting to " + host);
	}
	
	public void SendCommand(String command) throws Exception {
		if (this.manager == null || this.handler == null) {
			throw new Exception(MODULE_NAME + " Connect(): handler or protocol manager not set");
		}
		this.nextCommand = command;
		
		Thread t = new Thread(this);
		t.setUncaughtExceptionHandler(this.handler);
		t.start();
	}
	
	public void SetProtocolManager(ProtocolManager manager) {
		this.manager = manager;
	}
	
	public void SetExceptionHandler(UncaughtExceptionHandler handler) {
		this.handler = handler;
	}
	
	// Handles the possibility of multiline responses
	private String ReadFTPResponse() {
		StringBuffer response = new StringBuffer();
		
		try {
			// Read the first line
			response.append(this.reader.readLine());
			
			// Look for the code followed by a '-', this means the response is multiline
			String firstLine = response.toString();
			
			if (!Pattern.matches(firstLine, "^\\d+-")) {
				return firstLine;
			}
			
			// Read until we find the next line beginning with a number
			String thisLine;
			do {
				thisLine = this.reader.readLine();
				response.append(thisLine);
			}while(!Pattern.matches(thisLine, "^\\d+"));
			
			
		} catch (IOException e) {
			throw new RuntimeException(MODULE_NAME + ":failed to read response");
		} 
		
		return response.toString();
		
	}
	
	@Override
	public void run() {
		
		// Send the command
		try {
			this.writer.write(this.nextCommand);
			this.writer.flush();
			logger.log(Level.INFO, MODULE_NAME + ":sending \"" + this.nextCommand + "\"");
		} catch (IOException e) {
			throw new RuntimeException(MODULE_NAME + ":failed to send command");
		}
		
		// Receive the response
		String response = ReadFTPResponse();
		logger.log(Level.INFO, MODULE_NAME + ": received \"" + response + "\"" );
		this.manager.ControlDataReceived(response);
		
		
	}

}
