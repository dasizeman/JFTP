package edu.dasizeman.jftp;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FTPClientManager implements ProtocolManager {
	static private FTPClientManager instance;
	private static Logger logger;
	
	// The state diagrams that represent behavior of the DFA from a wait state 
	// according to the FTP RFC
	static private Map<FTPCommand, StateDiagram> stateDiagrams;
	static private Map<FTPCommand, FTPClientCommandHandler> FTPCmdMap;
	static private Map<FTPInterfaceCommand, FTPClientCommandHandler> FTPInterfaceCmdMap;
	static {
		logger = Logger.getGlobal();
		
		// ABOR, ALLO, DELE, CWD, CDUP, SMNT, HELP, MODE, NOOP, PASV,
		//QUIT, SITE, PORT, SYST, STAT, RMD, MKD, PWD, STRU, and TYPE.
		// RFC 959 pp 54
		StateDiagram diagramOne = new StateDiagram();
				
		// APPE, LIST, NLST, REIN, RETR, STOR, and STOU.
		// RFC 959 pp 55 top
		StateDiagram diagramTwo = new StateDiagram();
		
		// USER, PASS, ACCT
		// RFC 959 pp 57
		StateDiagram diagramThree = new StateDiagram();
		
		// Assign the appropriate transition states based on the first digit
		// of the response code, for all diagrams
		for (FTPResponse response: FTPResponse.values()) {
			//1xx
			if (response.code >= 100 && response.code < 200) {
				// Map for all commands
				// On D1, D3, all 1xx responses go to error state regardless of command
				for (FTPCommand cmd : FTPCommand.values()) {
					diagramOne.put(new FTPDiagramState(response, cmd), FTPState.ERROR);
					diagramThree.put(new FTPDiagramState(response, cmd), FTPState.ERROR);
				}
				
				// On D2, all 1xx responses go to wait, regardless of command
				for (FTPCommand cmd : FTPCommand.values()) {
					diagramTwo.put(new FTPDiagramState(response, cmd), FTPState.WAIT);
				}
				
			}
			
			//2xx
			// On all diagrams, all 2xx responses go to success regardless of command
			if (response.code >= 200 && response.code < 300) {
				for (FTPCommand cmd : FTPCommand.values()) {
					diagramOne.put(new FTPDiagramState(response, cmd), FTPState.SUCCESS);
					diagramTwo.put(new FTPDiagramState(response, cmd), FTPState.SUCCESS);
					diagramThree.put(new FTPDiagramState(response, cmd), FTPState.SUCCESS);
				}
				
			}
			
			//3xx
			if (response.code >= 300 && response.code < 400) {
				// On D1, D2, all 3xx responses go to error regardless of command
				for (FTPCommand cmd : FTPCommand.values()) {
					diagramOne.put(new FTPDiagramState(response, cmd), FTPState.ERROR);
					diagramTwo.put(new FTPDiagramState(response, cmd), FTPState.ERROR);
				}
				
				// On D3, the transition on a 3xx response is dependent on the current command
				diagramThree.put(new FTPDiagramState(response, FTPCommand.USER), FTPState.BEGIN);
				diagramThree.put(new FTPDiagramState(response, FTPCommand.PASS), FTPState.BEGIN);
				
				//...but we don't support the ACCT command right now
				//diagramThree.put(new FTPDiagramState(response, FTPCommand.ACCT), FTPState.BEGIN);
				
				
			}
			
			//On all diagrams, all 4xx, 5xx responses go to failure, regardless of command
			if (response.code >= 400 && response.code < 600) {
				for (FTPCommand cmd : FTPCommand.values()) {
					diagramOne.put(new FTPDiagramState(response, cmd), FTPState.FAILURE);
					diagramTwo.put(new FTPDiagramState(response, cmd), FTPState.FAILURE);
					diagramThree.put(new FTPDiagramState(response, cmd), FTPState.FAILURE);
				}
				
			}
			
		}
		
		// Now we map each command to a diagram
		stateDiagrams = new HashMap<FTPCommand, StateDiagram>();
		
		// ABOR, ALLO, DELE, CWD, CDUP, SMNT, HELP, MODE, NOOP, PASV,
		//QUIT, SITE, PORT, SYST, STAT, RMD, MKD, PWD, STRU, and TYPE.
		stateDiagrams.put(FTPCommand.CWD, diagramOne);
		stateDiagrams.put(FTPCommand.CDUP, diagramOne);
		stateDiagrams.put(FTPCommand.HELP, diagramOne);
		stateDiagrams.put(FTPCommand.PASV, diagramOne);
		stateDiagrams.put(FTPCommand.QUIT, diagramOne);
		stateDiagrams.put(FTPCommand.PORT, diagramOne);
		stateDiagrams.put(FTPCommand.PWD, diagramOne);
		
		// APPE, LIST, NLST, REIN, RETR, STOR, and STOU.
		stateDiagrams.put(FTPCommand.LIST, diagramTwo);
		stateDiagrams.put(FTPCommand.RETR, diagramTwo);
		
		// USER, PASS, ACCT
		stateDiagrams.put(FTPCommand.USER, diagramThree);
		stateDiagrams.put(FTPCommand.PASS, diagramThree);
		
		// Use a dirty reflection trick to build our handler maps
		FTPCmdMap = new HashMap<FTPCommand, FTPClientCommandHandler>();
		FTPInterfaceCmdMap = new HashMap<FTPInterfaceCommand, FTPClientCommandHandler>();
		// TODO do this
		
		
		// TODO initiate a connection that can call back here
	}
	

	private FTPDiagramState currentDiagramState;
	private FTPState currentState;
	private StateDiagram currentDiagram;
	private FTPExceptionHandler exHandler;
	private Throwable unhandledException;
	private String currentHost;

	
	
	public static FTPClientManager getInstance() {
		if (instance == null) {
			instance = new FTPClientManager();
		}
		
		return instance;
	}
	
	public FTPClientManager() {
		this.currentDiagramState = new FTPDiagramState();
		this.currentState = FTPState.BEGIN;
		this.unhandledException = null;
		this.exHandler = new FTPExceptionHandler();
		this.exHandler.setFTPManager(this);
		
		
		// Interface commands
		for (FTPInterfaceCommand cmd : FTPInterfaceCommand.values()) {
			FTPClientManager.FTPInterfaceCmdMap.put(cmd, getHandlerInstanceForCommand(cmd.name()));
		}
		
		// Protocol commands
		for (FTPCommand cmd : FTPCommand.values()) {
			FTPClientManager.FTPCmdMap.put(cmd, getHandlerInstanceForCommand(cmd.name()));
		}
		

		
	}
	
	private FTPClientCommandHandler getHandlerInstanceForCommand(String commandName) {
			// Build the name of the inner handler class
			FTPClientCommandHandler handlerInstance = null;
			try {
			String handlerClassName = FTPClientManager.class.getName() + "$" + commandName + "handler";
			
			// Get a reference to its class object
			Class<?> handlerClass = Class.forName(handlerClassName);
			
			// Now we have to get at its constructor
			Constructor<?> handlerClassConstructor = handlerClass.getConstructor(new Class[]{this.getClass()});
			
			// And instantiate it
			handlerInstance = (FTPClientCommandHandler)handlerClassConstructor.newInstance(new Object[]{this});

			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException 
					| IllegalArgumentException | InvocationTargetException | NoSuchMethodException 
					| SecurityException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			return handlerInstance;
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
	
	
	private void parseAndExecuteCommand(String command) throws Exception {
		String[] tokens = command.split(" ");
		if (tokens.length < 1) {
			throw new Exception("Command parsing got 0 tokens...what?");
		}
		
		String baseCommandStr = tokens[0];
		
		if(FTPInterfaceCommand.getByAlias(baseCommandStr) == null) {
			System.out.println("Unsupported command: " + baseCommandStr);
			return;
		}
		
		String[] cmdArgs = Arrays.copyOfRange(tokens, 1, tokens.length);
		
		FTPInterfaceCommand cmd = FTPInterfaceCommand.getByAlias(baseCommandStr);
		FTPClientManager.FTPInterfaceCmdMap.get(cmd).handle(cmdArgs);

		// TODO implement command parsing and handing off to the correct handler
		// each handler must set the current state diagram appropriately
		
		//TODO set current diagram state's command section to this command so we can map
		// state diagrams correctly
	}

	@Override
	public void ControlDataReceived(String data) {
		FTPResponse response;
		try {
			response = parseControlResponse(data);
			Transition(response);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void DataReceived(byte[] data) {
		// TODO Auto-generated method stub
		
	}
	
	private FTPResponse parseControlResponse(String response) throws ProtocolException {
		// TODO Parse to an FTPResponse
		// Set the current diagram state's response
		
		return null;
	}
	
	private void handleFTPResponse(FTPResponse response) {
		// 
		switch (response) {
			
		}
	}
	

	@Override
	public void Transition(Object data) throws Exception {
		try {
			switch (this.currentState) {
			case BEGIN:
				String commandString = (String)data;
				// We are being passed a command string
				//TODO just for testing
				//this.currentState = FTPState.WAIT;
				
				// Parse and act on the command that will bring us to waiting state
				parseAndExecuteCommand(commandString);
				
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
				ProtocolException e = (ProtocolException)data;
				// Ruh roh.  Let's throw our exception so it will be picked up by the shell. 
				throw e;
			}
		} catch (ClassCastException e) {
			throw e;
		}
		
	}
	
	
	@Override
	public void Reset() {
		this.currentState = FTPState.BEGIN;
		this.currentDiagramState = new FTPDiagramState();
	}
	
	
	
	
	
	public interface FTPClientCommandHandler {
		public void handle(String[] commandArgs) throws Exception;
	}

	private void sendControlMessage(String message) throws Exception {
		FTPControlConnection connection = FTPControlConnection.getInstance(this.currentHost);
		connection.SetProtocolManager(this);
		connection.SetExceptionHandler(this.exHandler);
		connection.SendCommand(message);
	}
	
	// Handlers for the shell/interface commands.  I chose to have this separate command
	// layer so that the user interface could be more intuitive than just entering raw FTP commands.
	public class CONNECT_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Exception {
			// TODO For now we will send a NO-OP command as the first message
			
			// Argument must be the <hostname>:<port> string
			if(command.length < 1) {
				throw new Exception("connect: no host provided");
			}
			currentHost = command[0];
			sendControlMessage("NOOP");
		}
		
	}
	public class LOGIN_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			logger.log(Level.SEVERE, "LOGIN_CMD");
			
		}
		
	}
	public class CD_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			logger.log(Level.SEVERE, "CD_CMD");
			
		}
		
	}
	public class CDUP_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			logger.log(Level.SEVERE, "CDUP_CMD");
			
		}
		
	}
	public class QUIT_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			logger.log(Level.SEVERE, "QUIT_CMD");
			
		}
		
	}
	public class PASV_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			logger.log(Level.SEVERE, "PASV_CMD");
			
		}
		
	}
	public class ACTV_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			logger.log(Level.SEVERE, "ACTV_CMD");
			
		}
		
	}
	public class GET_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			logger.log(Level.SEVERE, "GET_CMD");
			
		}
		
	}
	public class PWD_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			logger.log(Level.SEVERE, "PWD_CMD");
			
		}
		
	}
	public class LS_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			logger.log(Level.SEVERE, "LS_CMD");
			
		}
		
	}
	public class SERVERHELP_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "SERVERHELP_CMD");
			
		}
		
	}
	public class HELP_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			System.out.println(FTPInterfaceCommand.GetHelpString());
		}
		
	}
	
	

	// Actual protocol handlers
	public class USERhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class PASShandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class CWDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class CDUPhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class QUIThandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class PASVhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class EPSVhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class PORThandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class EPRThandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class RETRhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class PWDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class LISThandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public class HELPhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			
		}
		
	}

}


