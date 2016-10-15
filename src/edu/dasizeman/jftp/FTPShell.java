package edu.dasizeman.jftp;

public class FTPShell extends Shell {
	
	private ProtocolManager manager;
	
	public FTPShell(String logPath) {
		super(logPath);
		manager = FTPClientManager.getInstance();
	}
	@Override
	public void doCommand(String commandStr) throws Throwable {
		try {
		// TODO Call for a state transition from begin, with the command line
			manager.Transition(commandStr);
			
		// Wait for the state machine to get back to the begin state.
		while(!manager.IsReady()) { // totally spinlocking lol
			manager.CheckException();
			Thread.sleep(100);
		}
			
		} catch (Exception e) { // TODO something out-of-protocol happened here, so we probably need to just reset everything and throw up
			manager.Reset();
			throw e;
		}
		
	}

	@Override
	protected String welcomeMessage() {
		// TODO Write a welcome message
		return null;
	}

	@Override
	protected String helpMessage() {
		// TODO Write a message explaining available commands
		return null;
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

}
