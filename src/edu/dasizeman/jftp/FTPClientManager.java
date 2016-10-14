package edu.dasizeman.jftp;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FTPClientManager implements ProtocolManager {
	static private FTPClientManager instance;
	
	// The state diagrams that represent behavior of the DFA from a wait state 
	// according to the FTP RFC
	static private StateDiagram[] stateDiagrams;
	static {
		// TODO initialize each diagram here
	}
	

	private FTPState currentState;
	private StateDiagram currentDiagram;
	private Map<FTPCommand, FTPClientCommandHandler> FTPCmdMap;
	private Map<FTPInterfaceCommand, FTPClientCommandHandler> FTPInterfaceCmdMap;
	private FTPExceptionHandler exHandler;
	private Throwable unhandledException;
	private Logger logger;

	
	
	public static FTPClientManager getInstance() {
		if (instance == null) {
			instance = new FTPClientManager();
		}
		
		return instance;
	}
	
	public FTPClientManager() {
		this.currentState = FTPState.BEGIN;
		this.unhandledException = null;
		this.exHandler = new FTPExceptionHandler();
		this.exHandler.setFTPManager(this);
		this.logger = Logger.getLogger(this.getClass().getName());
		
		// Use a dirty reflection trick to build our handler maps
		FTPCmdMap = new HashMap<FTPCommand, FTPClientCommandHandler>();
		FTPInterfaceCmdMap = new HashMap<FTPInterfaceCommand, FTPClientCommandHandler>();
		// TODO do this
		
		// TODO initiate a connection that can call back here

		
	}
	
	private class FTPExceptionHandler implements UncaughtExceptionHandler {
		private FTPClientManager manager;
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			manager.setException(e);
			
		}
		
		public void setFTPManager(FTPClientManager manager) {
			this.manager = manager;
		}
	}
	
	private void setException(Throwable e) {
		this.unhandledException = e;
	}
	
	
	
	public void CheckException() throws Throwable {
		if (this.unhandledException == null) {
			return;
		}
		throw this.unhandledException;
	}
	
	public boolean IsReady() {
		return this.currentState == FTPState.BEGIN;
	}
	
	
	private void parseAndExecuteCommand(String command) {
		// TODO implement command parsing and handing off to the correct handler
		// each handler must set the current state diagram appropriately
	}


	// Our control connection thread will call back here
	public void controlResponseReceived(String response) {
		
	}
	
	private FTPResponse parseControlResponse(String response) throws ProtocolException {
		// TODO Parse to an FTPResponse
		return null;
	}

	@Override
	public void Transiton(Object data) throws ProtocolException {
		try {
			switch (this.currentState) {
			case BEGIN:
				String commandString = (String)data;
				// We are being passed a command string
				this.currentState = FTPState.WAIT;
				
				// Parse and act on the command that will bring us to waiting state
				break;
				
			case WAIT:
				// TODO Receipt of a FTPResponse, called back from a connection thread
				FTPResponse response = (FTPResponse) data;
				handleFTPResponse(response);
				break;
				
			case SUCCESS:
			case ERROR:
				String message = (String)data;
				// Either everything is good or is good or some not so bad error happened,
				// just log and print the message and go back to begin
				
				this.currentState = FTPState.BEGIN;
				break;
			
			case FAILURE:
				ProtocolException ex = (ProtocolException)data;
				// Ruh roh.  Let's throw our exception so it will be picked up by the shell. 
				throw ex;
			}
		} catch (ClassCastException e) {
			throw e;
		}
		
	}
	
	private void handleFTPResponse(FTPResponse response) {
		// 
		switch (response) {
			
		}
	}
	
	@Override
	public void Reset() {
		this.currentState = FTPState.BEGIN;
	}
	
	
	
	// Define the handlers for different FTP shell commands
	
	
	public interface FTPClientCommandHandler {
		public void handle(String command);
	}
	
	public class USERhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class PASShandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class CWDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class CDUPhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class QUIThandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class PASVhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class EPSVhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class PORThandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class EPRThandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class RETRhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class PWDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class LISThandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class HELPhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String command) {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Override
	public void ControlDataReceived(String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void DataReceived(byte[] data) {
		// TODO Auto-generated method stub
		
	}
	
	

}


