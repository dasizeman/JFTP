package edu.dasizeman.jftp;

public class FTPDiagramState {
	public FTPResponse response;
	public FTPCommand cmd;
	
	public FTPDiagramState() {
		this.response = null;
		this.cmd = null;
	}
	
	public FTPDiagramState(FTPResponse response, FTPCommand command) {
		this.response = response;
		this.cmd = command;
	}

}
