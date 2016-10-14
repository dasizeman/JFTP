package edu.dasizeman.jftp;

import java.lang.Thread.UncaughtExceptionHandler;

public class FTPControlConnection extends Connection implements Runnable {
	private ProtocolManager manager;
	private String nextCommandString;
	private UncaughtExceptionHandler handler;

	public FTPControlConnection(String host, ProtocolManager manager, UncaughtExceptionHandler handler) throws Exception {
		super(host);
		this.manager = manager;
		this.handler = handler;
	}
	
	
	public void SendCommand(String command) {
		this.nextCommandString = command;
		
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
