package edu.dasizeman.jftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ServerSocket;
import java.nio.CharBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class FTPConnection extends Connection implements Runnable {
	private static FTPConnection controlInstance, dataInstance;
	public static final String CRLF = "\r\n";
	public static final int DEFAULT_PORT = 21;
	private static Logger logger;
	
	static {
		logger = Logger.getGlobal();
	}
	
	private enum Mode {
		CONTROL, 
		DATA
	}
	
	private ProtocolManager manager;
	private UncaughtExceptionHandler handler;
	private String nextCommand;
	private BufferedWriter writer;
	private BufferedReader reader;
	private Mode mode;
	private String MODULE_NAME;
	private String filePath;
	
	
	// Data connection
	private String localHostIPV4, localHostIPV6;
	
	
	public static FTPConnection getControlInstance(String host) throws Exception {
		// If no port is given, append the default port
		if (!host.contains(":")) {
			host = host + ":" + Integer.toString(DEFAULT_PORT);
		}
		
		if (controlInstance == null) {
			controlInstance = new FTPConnection(host);
			controlInstance.mode = Mode.CONTROL;
			controlInstance.MODULE_NAME = "ControlConnection";
		}
		
		// If the host address or port have changed, we must re-connect
		String previousHost = controlInstance.host;
		int previousPort = controlInstance.port;
		
		// Set's the instance's host and port
		controlInstance.parseHostString(host);
		
		if (!controlInstance.host.equals(previousHost) 
				|| controlInstance.port != previousPort
				|| !controlInstance.socket.isConnected()) {
			controlInstance.close();
			controlInstance = new FTPConnection(host);
		}
		

		return controlInstance;
	}
	
	public static FTPConnection getDataInstance(String host, FTPCommand type) throws Exception {
		if (controlInstance == null) {
			throw new Exception("Cannot create a data connection without a control connection.");
		}
		if(dataInstance == null) {
			dataInstance = new FTPConnection(host, type);
			dataInstance.mode = Mode.DATA;
			dataInstance.MODULE_NAME = "DataConnection";
		}
		
		switch (type) {
		case PORT:
		case EPRT:
			// For PORT connections, make sure the connection is open
			if (!dataInstance.socket.isConnected()) {
				dataInstance = new FTPConnection(host,type);
			}
			break;
		case PASV:
		case EPSV:
			// If this is for a passive connection it's just like a control connection, we care about updating if the host
			// changes
			// If the host address or port have changed, we must re-connect
			String previousHost = dataInstance.host;
			int previousPort = dataInstance.port;
			
			// Set's the instance's host and port
			dataInstance.parseHostString(host);
			
			if (!dataInstance.host.equals(previousHost) 
					|| dataInstance.port != previousPort
					|| !dataInstance.socket.isConnected()) {
				dataInstance.close();
				dataInstance = new FTPConnection(host);
			}
			break;
			
		default:
			throw new Exception("FTPConnection must be passed one of PORT, EPRT, PASV, or EPSV");
				
		}
		
		
		
		return dataInstance;
	}

	// Used for control connections
	public FTPConnection(String host) throws Exception {
		super(host);
		Connect();
		this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
		this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		logger.log(Level.INFO, MODULE_NAME + ":connecting to " + host);
		
		// TODO Figure out our host strings for active
	}
	
	// Used for data connections
	public FTPConnection(String host, FTPCommand type) throws Exception {
		this.filePath = "";
		if (type == FTPCommand.PASV || type == FTPCommand.EPSV) {
			// Copied from other constructor because I don't have time
			if (!parseHostString(host)) {
				throw new Exception("Could not parse connection host: " + host);
			}
			Connect();
			logger.log(Level.INFO, MODULE_NAME + ":connecting to " + host);
			
		} else if (type == FTPCommand.PORT || type == FTPCommand.EPRT) {
			int dataPort = this.socket.getLocalPort() + 1;
			ServerSocket serverSocket = new ServerSocket(dataPort);
			this.socket = serverSocket.accept();
			serverSocket.close();
		} else {
			throw new Exception("FTPConnection must be passed one of PORT, EPRT, PASV, or EPSV");
		}

		this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
		this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
	}
	
	public String GetIPV4Host() {
		//TODO
		return "";
	}
	
	public String GetIPV6Host() {
		//TODO
		return "";
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
	
	public void ReadData(String filePath) throws Exception {
		if (this.manager == null || this.handler == null) {
			throw new Exception(MODULE_NAME + " Connect(): handler or protocol manager not set");
		}
		this.filePath = filePath;
		
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
	
	// Reads text from a data connection, like for LS
	private String readASCIIData() {
		//TODO
		StringBuffer data = new StringBuffer();
		
		// Read until the connection is closed on us
		while(true) {
			try {
				data.append(this.reader.readLine());
			} catch (IOException e) {
				break;
			}
		}
		
		return data.toString();
		
	}
	
	// Keeps reading the socket and dumping to a file until the connection is closed
	private void dumpToFile(String path) {
		//TODO
	}
	
	
	
	// Handles the possibility of multiline responses
	private String ReadFTPResponse() {
		StringBuffer response = new StringBuffer();
		
		
		try {
			
			// Read the first line
			response.append(this.reader.readLine());
			
			// Look for the code followed by a '-', this means the response is multiline
			String firstLine = response.toString();
			
			if (!Pattern.matches("^\\d+-.*", firstLine)) {
				return firstLine;
			}
			
			response.append(CRLF);
			
			// Read until we find the next line beginning with a number
			String thisLine;
			do {
				thisLine = this.reader.readLine();
				response.append(thisLine + CRLF);
			}while(!Pattern.matches("^\\d+.*", thisLine));
			
			
		} catch (IOException e) {
			throw new RuntimeException(MODULE_NAME + ":failed to read response");
		} 
		
		return response.toString();
		
	}
	
	@Override
	public void run() {
		switch(this.mode) {
		case CONTROL:
			runControl();
			break;
		case DATA:
			runData();
			break;
		}
		
	}
	
	private void runControl() {
		String response = "";
		try {
			// Special case, if our next command is blank, it means we're waiting for a greeting
			// This will block
			if (this.nextCommand.equals("")) {
				response = ReadFTPResponse();
			} else if (this.socket.getInputStream().available() > 0) {
				
				// See if there is a message already waiting on the socket.  This can happen 
				// if the server sends us a timeout notification, etc.  We don't want to block 
				// if this isn't the case, so we check if there are available bytes on the socket
				response = ReadFTPResponse();
			}
			
			
			// If there wasn't a pending response, we can send our command and receive the its response
			if (response.equals("")) {
				// Send the command
					logger.log(Level.INFO, MODULE_NAME + ":sending \"" + this.nextCommand + "\"");
					this.writer.write(this.nextCommand + CRLF);
					this.writer.flush();
				response = ReadFTPResponse();
			}
		} catch (IOException e) {
			throw new RuntimeException(MODULE_NAME + ":failed to send command");
		}
		
		logger.log(Level.INFO, MODULE_NAME + ": received \"" + response + "\"" );
		this.manager.ControlDataReceived(response);
		
	}
	
	private void runData() {
		if (filePath.equals("")) {
			this.manager.TextDataReceived(readASCIIData());
		}
	}

}
