package edu.dasizeman.jftp;

public class FTPResponseData {
	public FTPResponse response;
	public String responseMessage;
	
	public FTPResponseData(FTPResponse response, String message) {
		this.response = response;
		this.responseMessage = message;
	}

}
