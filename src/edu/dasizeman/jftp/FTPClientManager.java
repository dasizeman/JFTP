package edu.dasizeman.jftp;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTPClientManager implements ProtocolManager {
	
	/* Static */
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
		
		// TODO Add EPRT and EPSV
		// ABOR, ALLO, DELE, CWD, CDUP, SMNT, HELP, MODE, NOOP, PASV,
		//QUIT, SITE, PORT, SYST, STAT, RMD, MKD, PWD, STRU, and TYPE.
		stateDiagrams.put(FTPCommand.CWD, diagramOne);
		stateDiagrams.put(FTPCommand.CDUP, diagramOne);
		stateDiagrams.put(FTPCommand.HELP, diagramOne);
		stateDiagrams.put(FTPCommand.PASV, diagramOne);
		stateDiagrams.put(FTPCommand.QUIT, diagramOne);
		stateDiagrams.put(FTPCommand.PORT, diagramOne);
		stateDiagrams.put(FTPCommand.PWD, diagramOne);
		stateDiagrams.put(FTPCommand.NOOP, diagramOne);
		
		// APPE, LIST, NLST, REIN, RETR, STOR, and STOU.
		stateDiagrams.put(FTPCommand.LIST, diagramTwo);
		stateDiagrams.put(FTPCommand.RETR, diagramTwo);
		
		// USER, PASS, ACCT
		stateDiagrams.put(FTPCommand.USER, diagramThree);
		stateDiagrams.put(FTPCommand.PASS, diagramThree);
		
		// Use a dirty reflection trick to build our handler maps
		FTPCmdMap = new HashMap<FTPCommand, FTPClientCommandHandler>();
		FTPInterfaceCmdMap = new HashMap<FTPInterfaceCommand, FTPClientCommandHandler>();
		
	}
	
	public static FTPClientManager getInstance() {
		if (instance == null) {
			instance = new FTPClientManager();
		}
		
		return instance;
	}
	
	/* Instance */

	private FTPDiagramState currentDiagramState;
	private FTPState currentState;
	private StateDiagram currentDiagram;
	private FTPExceptionHandler exHandler;
	private Throwable unhandledException;
	private String currentControlHost, currentDataHost;
	private boolean controlFinished, dataFinished;
	private FTPCommand dataMode;
	private FTPClientManager selfPtr;

	
	
	
	public FTPClientManager() {
		this.currentDiagramState = new FTPDiagramState();
		this.currentState = FTPState.BEGIN;
		this.unhandledException = null;
		this.exHandler = new FTPExceptionHandler();
		this.exHandler.setFTPManager(this);
		this.controlFinished = true;
		this.dataFinished = true;
		this.dataMode = FTPCommand.PASV;
		this.selfPtr = this;
		
		
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
	
	/* Exception handling */
	
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
	
	
	

	/* Receiving data */
	@Override
	public void DataReceived(byte[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void TextDataReceived(String data) {
		for (String line : data.split("\r\n")) {
			logger.log(Level.INFO, line);
		}
		this.dataFinished = true;
		
	}

	@Override
	public void ControlDataReceived(String data) {
		FTPResponseData responseData;
		try {
			responseData = parseControlResponse(data);
			
			// Transition from WAIT based on our diagram state
			transition(FTPState.WAIT);
			
			// Handle whatever terminal state we ended up at
			String message = responseData.responseMessage;
			if (this.currentState == FTPState.ERROR || this.currentState == FTPState.SUCCESS) {
				// Either everything is good or is good or some not so bad error happened,
				// just log and print the message and go back to begin
				Level level;
				if (this.currentState == FTPState.SUCCESS) {
					level = Level.INFO;
				} else {
					level = Level.WARNING;
				}
				
				logger.log(level, message);
				
				this.currentState = FTPState.BEGIN;
			} else if (this.currentState == FTPState.FAILURE) {
				// Ruh roh.  Let's throw our exception so it will be picked up by the shell. 
				// This will also cause the state machine to be reset 
				throw new ProtocolException(message);
			} else if (this.currentState == FTPState.WAIT) {
				// If we ended up in WAIT again, receive the command we are waiting for
				doControlReceive();
			}
			
			// State is now set from this response, we can unlock
			this.controlFinished = true;
			
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	
	private FTPResponseData parseControlResponse(String responseStr) throws ProtocolException {
		// TODO Parse to an FTPResponse
		// Set the current diagram state's response
		
		// First let's separate the code and the message by using our old friend Mr. Reginald Xavior
		Pattern responsePattern = Pattern.compile("^(\\d+)(.*)", Pattern.DOTALL);
		Matcher responseMatcher = responsePattern.matcher(responseStr);
		
		if (!responseMatcher.find() || responseMatcher.groupCount() < 1) {
			throw new ProtocolException("Got response from server without a response code");
		}
		
		int responseCode = Integer.parseInt(responseMatcher.group(1));
		
		// Look if we have a specific response for this code
		FTPResponse response = FTPResponse.getByCode(responseCode);
		
		
		if (response == null) {
			throw new ProtocolException("Received unknown repsonse code");
		}

		// Set the diagram state's response field
		this.currentDiagramState.response = response;
		
		String responseMessage = "";
		if (responseMatcher.groupCount() > 1) {
			responseMessage = responseMatcher.group(2);
		}
		
		// TODO this is where we check if this response has any side effects (like setting our data connection for PASV),
		// and do them
		if (response == FTPResponse.ENTERING_PASV) {
			this.currentDataHost = parsePASVResponse(responseMessage);
			this.dataMode = FTPCommand.PASV;
		}
		
		
		return new FTPResponseData(response, responseMessage);
	}
	
	private String parsePASVResponse(String response) throws ProtocolException {
		Pattern pasvPattern = Pattern.compile("\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)\\)");
		Matcher pasvMatcher = pasvPattern.matcher(response);
		if (!pasvMatcher.find() || pasvMatcher.groupCount() < 6) {
			throw new ProtocolException("Could not parse PASV reponse");
		}
		
		StringBuffer hostStr = new StringBuffer();
		// Build IPv4 segment
		for (int i = 1; i <= 4; i++) {
			hostStr.append(pasvMatcher.group(i));
			if (i == 4) {
				hostStr.append(":");
			} else {
				hostStr.append(".");
			}
		}
		
		// Build port string
		String portString = Integer.toString((256 * Integer.parseInt(pasvMatcher.group(5)) + Integer.parseInt(pasvMatcher.group(6))));
		hostStr.append(portString);
		
		return hostStr.toString();
	}
	
	/* State machine */

	public boolean IsReady() {
		return this.controlFinished && this.dataFinished;
	}
	

	private void transition(FTPState expected) throws Throwable {
		if (this.currentState != expected) {
			throw new ProtocolException("State machine expected " + expected.name() + ", got " + this.currentState.name());
		}
		switch (this.currentState) {
		case BEGIN:
			this.currentState = FTPState.WAIT;
			break;
			
		case WAIT:
			// Change state based on the current state diagram
			this.currentState = this.currentDiagram.get(this.currentDiagramState);
			break;
			
		default:
			throw new ProtocolException("Transition() called with invalid state.");
			
			
		}
		
	}
	
	
	// Waits until the state machine is ready for a new command and throws any
	// exceptions that occur.
	private void waitForReady() throws Throwable {
		while(!IsReady()) {
			CheckException();
			Thread.sleep(100);
		}
	}
	
	
	@Override
	public void Reset() {
		this.currentState = FTPState.BEGIN;
		this.currentDiagramState = new FTPDiagramState();
		this.controlFinished = true;
		this.dataFinished = true;
		this.unhandledException = null;
	}
	
	
	
	/* Sending commands */
	
	public void ParseAndExecuteInterfaceCommand(String command) throws Throwable {
		if (this.currentState != FTPState.BEGIN) {
			throw new ProtocolException("State machine not in BEGIN when sending command");
		}
		
		String[] tokens = command.split(" ");
		if (tokens.length < 1) {
			throw new Exception("Command parsing got 0 tokens...what?");
		}
		
		String baseCommandStr = tokens[0].toLowerCase();
		
		if(FTPInterfaceCommand.getByAlias(baseCommandStr) == null) {
			System.out.println("Unsupported command: " + baseCommandStr);
			Reset();
			return;
		}
		
		String[] cmdArgs = Arrays.copyOfRange(tokens, 1, tokens.length);
		
		FTPInterfaceCommand cmd = FTPInterfaceCommand.getByAlias(baseCommandStr);
		FTPClientManager.FTPInterfaceCmdMap.get(cmd).handle(cmdArgs);
	}

	private void sendControlMessage(String message) throws Exception {
		FTPConnection connection = FTPConnection.getControlInstance(this.currentControlHost);
		connection.SetProtocolManager(this);
		connection.SetExceptionHandler(this.exHandler);
		connection.SendCommand(message);
	}
	
	private void receiveData(String file) throws Exception {
		if (file == null)
			file = "";
		// Set up a data connection
		FTPConnection dataConnection = FTPConnection.getDataInstance(currentDataHost, dataMode);
		dataConnection.SetProtocolManager(this);
		dataConnection.SetExceptionHandler(this.exHandler);
		dataConnection.ReadData(file);
		
	}
	
	// Wrapper that calls the correct handler for an FTP Protocol command and sets state diagram info appropriately
	private void doProtocolCommand(FTPCommand cmd, String[] args) throws Throwable {
		// "Lock" the state machine thread until we've received a response and set state, or failed.
		this.controlFinished= false;

		// Set the current state diagram
		this.currentDiagram = stateDiagrams.get(cmd);
		
		// Set the diagram state's command field
		this.currentDiagramState.cmd = cmd;
		
		// We should be in BEGIN, go to WAIT
		transition(FTPState.BEGIN);
		
		// Invoke the handler with the arguments
		FTPCmdMap.get(cmd).handle(args);
		
		// Wait for the state machine to be ready 
		waitForReady();
	}
	
	// Special case here, we will "send" a blank message to initiate the connection
	// and read the greeting. We'll make it look like a NOOP command to the state machine, 
	// so that everything works.  A little hacky.
	// For intermediate reposnes, and the greeting
	private void doControlReceive() throws Throwable {
		controlFinished = false;
		currentDiagram = stateDiagrams.get(FTPCommand.NOOP);
		currentDiagramState.cmd = FTPCommand.NOOP;
		sendControlMessage("");
		waitForReady();
	}
	
	private void badCommand() throws Exception {
		throw new Exception("Incorrect command syntax.  See 'help' for details");
	}
	
	/* Handlers */

	public interface FTPClientCommandHandler {
		public void handle(String[] commandArgs) throws Throwable;
	}
	
	// Handlers for the shell/interface commands.  I chose to have this separate command
	// layer so that the user interface could be more intuitive than just entering raw FTP commands.
	public class CONNECT_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			// TODO For now we will send a NO-OP command as the first message
			
			// Argument must be the <hostname>:<port> string
			if(command.length < 1) {
				throw new Exception("connect: no host provided");
			}
			currentControlHost = command[0];
			
			transition(FTPState.BEGIN);
			doControlReceive();
		}
		
	}
	public class LOGIN_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			// TODO Auto-generated method stub
			String[] flags = new String[]{"-u", "-p"};
			ParseMap parsed = Parser.Parse(command, flags);
			
			if (parsed == null) {
				badCommand();
				return;
			}
			
			
			// Send a USER FTP command
			doProtocolCommand(FTPCommand.USER, new String[]{parsed.get("-u")});
			
			// Send a PASS FTP command
			doProtocolCommand(FTPCommand.PASS, new String[]{parsed.get("-p")});
		}
		
	}
	public class CD_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			if (command.length < 1) {
				badCommand();
				return;
			}
			
			// Send a CWD FTP command
			doProtocolCommand(FTPCommand.CWD, command);
			
			
		}
		
	}
	public class CDUP_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			
			// Send a CDUP FTP command
			doProtocolCommand(FTPCommand.CDUP, command);
			
		}
		
	}
	public class QUIT_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Call server quit, for now just RESET
			Reset();
			
		}
		
	}
	public class PASV_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			if (command.length > 1 && command[1].equals("-e")) {
				// EPSV
				doProtocolCommand(FTPCommand.EPSV, command);
			}else {
				// PASV
				doProtocolCommand(FTPCommand.PASV, command);
			}
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
		public void handle(String[] command) throws Throwable {
			if (command.length != 1) {
				badCommand();
			}
			
			// Do  a RETR FTP command
			doProtocolCommand(FTPCommand.RETR, command);
			
		}
		
	}
	public class PWD_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			doProtocolCommand(FTPCommand.PWD, command);
			
		}
		
	}
	public class LS_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			
			// Send a LIST FTP command
			doProtocolCommand(FTPCommand.LIST, command);
		}
		
	}
	public class SERVERHELP_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			// Call the HELP protocol handler
			doProtocolCommand(FTPCommand.HELP, command);
			
		}
		
	}
	public class HELP_CMDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) {
			// TODO Auto-generated method stub
			System.out.println(FTPInterfaceCommand.GetHelpString());
			Reset();
		}
		
	}
	
	

	// Actual protocol handlers
	
	public class NOOPhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] commandArgs) throws Throwable {
			sendControlMessage(FTPCommand.NOOP.name());
			
		}
		
	}
	
	public class USERhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			if (command.length < 1) {
				badCommand();
			}
			
			// Send a USER FTP command
			sendControlMessage(FTPCommand.USER.name() + " " + command[0]);
			
			
		}
		
	}
	public class PASShandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			if (command.length < 1) {
				badCommand();
			}
			
			// Send a PASS FTP command
			sendControlMessage(FTPCommand.PASS.name() + " " + command[0]);
			
		}
		
	}
	public class CWDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			sendControlMessage(FTPCommand.CWD.name() + " " + command[0]);
			
		}
		
	}
	public class CDUPhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			sendControlMessage(FTPCommand.CDUP.name());
			
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
		public void handle(String[] command) throws Throwable {
			sendControlMessage(FTPCommand.PASV.name());
			
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
		public void handle(String[] command) throws Throwable {
			receiveData(command[0]);
			sendControlMessage(FTPCommand.RETR.name() + " " + command[0]);
			
		}
		
	}
	public class PWDhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			sendControlMessage(FTPCommand.PWD.name());
			
		}
		
	}
	public class LISThandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			String commandStr = FTPCommand.LIST.name();
			if (command.length > 0) {
				commandStr += " " + String.join(" ", command);
			}
			
			// Connect to the data port and start receiving.  A blank
			// argument means text data will be read.
			dataFinished = false;
			receiveData("");
			sendControlMessage(commandStr);
			
		}
		
	}
	public class HELPhandler implements FTPClientCommandHandler {

		@Override
		public void handle(String[] command) throws Throwable {
			String commandStr = FTPCommand.HELP.name();
			if (command.length > 0) {
				commandStr += " " + String.join(" ", command);
			}
			sendControlMessage(commandStr);
		}
		
	}


}


