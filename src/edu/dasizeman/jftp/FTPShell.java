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
			manager.ParseAndExecuteInterfaceCommand(commandStr);
			
		} catch (Exception e) { // TODO something out-of-protocol happened here, so we probably need to just reset everything and throw up
			manager.Reset();
			throw e;
		}
		
	}

	@Override
	protected String welcomeMessage() {
		return "Welcome to JFTP";
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

}
