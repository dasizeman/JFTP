package edu.sizetron.jftp;

public class FTPShell extends Shell {
	
	private ProtocolManager manager;
	@Override
	public void doCommand(String commandStr) throws Exception {
		try {
		// TODO Call for a state transition from begin, with the command line
			
		// Wait for the state machine to get back to the begin state.
		// TODO get the manager to catch network thread exceptions and set them
			// while (!ready && checkException)
			
		} catch (Exception e) { // TODO something out-of-protocol happened here, so we probably need to just reset everything and throw up
			
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

}
