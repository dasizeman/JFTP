package edu.sizetron.jftp;

import java.util.HashMap;
import java.util.Map;

public class FTPClientManager implements ProtocolManager {
	static private FTPClientManager instance;
	
	// The state diagrams that represent behavior of the DFA from a wait state 
	// according to the FTP RFC
	static private Map<ProtocolTransition<FTPState, Integer>,FTPState>[] stateDiagrams;
	static {
		// TODO initialize each diagram here
	}
	

	private FTPState currentState;
	private Map<FTPCommand, FTPClientCommandHandler> commandMap;
	
	
	public static FTPClientManager getInstance() {
		if (instance == null) {
			instance = new FTPClientManager();
		}
		
		return instance;
	}
	
	public FTPClientManager() {
		this.currentState = FTPState.BEGIN;
		
		// Use a dirty reflection trick to build our handler map
		commandMap = new HashMap<FTPCommand, FTPClientCommandHandler>();
		// TODO do this
		
		// TODO initiate a connection that can call back here

		
	}
	
	private void parseAndExecuteCommand(String command) {
		// TODO implement command parsing and handing off to the correct handler
	}

	@Override
	public void Reset() {
		this.currentState = FTPState.BEGIN;
	}

	@Override
	public void Transiton(ProtocolTransition<?, ?> transition) throws ProtocolException {
		FTPState lastState = (FTPState)transition.lastState;
		
		if (lastState == FTPState.BEGIN) {
			// We are being passed a command string
			this.currentState = FTPState.WAIT;
			
			// Parse and act on the command that will bring us to waiting state
			
		} else {
			// TODO Receipt of a response code, called back from a connection thread 
		}
		
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
	
	

}


