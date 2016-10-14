package edu.dasizeman.jftp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection {
	private Socket socket;
	private InputStream socketInputStream;
	private OutputStream socketOutputStream;
	private String host;
	private int port;
	
	public Connection(String host) throws Exception {
		this.connect(host);
	}
	
	private void connect(String host) throws Exception {
		if (!parseHostString(host)) {
			throw new Exception("Could not parse connection host: " + host);
		}
		
		this.socket = new Socket(this.host, this.port);
		this.socketInputStream = this.socket.getInputStream();
		this.socketOutputStream = this.socket.getOutputStream();
	}
	
	private boolean parseHostString(String str) {
		if (!str.contains(":")) {
			return false;
		}
		
		String[] tokens = str.split(":");
		if (tokens.length <= 1) {
			return false;
		}
		
		this.host = tokens[0];
		this.port = Integer.parseInt(tokens[1]);
		
		return true;
	}
	
	public InputStream getInputStream() {
		return this.socketInputStream;
	}
	
	public OutputStream getOutputStream() {
		return this.socketOutputStream;
	}
	
	public void close() throws Exception {
		this.socket.close();
	}
	
}
