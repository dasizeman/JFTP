package edu.sizetron.jftp;

public enum FTPResponse {
	COMMAND_OK(200, "Command okay.");

	
	
	public final int code;
	public final String message;
	
	FTPResponse(int code, String message) {
		this.code = code;
		this.message = message;
	}

}
