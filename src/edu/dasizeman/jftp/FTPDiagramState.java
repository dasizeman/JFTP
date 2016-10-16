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
	
	@Override
	public int hashCode()
	{
	    return (this.response.name() + this.cmd.name()).hashCode();
	}	
	
	@Override
	public boolean equals(Object obj) {
		FTPDiagramState other = (FTPDiagramState)obj;
		return (this.response.equals(other.response) && this.cmd.equals(other.cmd));
	}

}
