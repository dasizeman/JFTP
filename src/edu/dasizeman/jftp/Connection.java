package edu.dasizeman.jftp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection {
	protected Socket socket;
	protected InputStream socketInputStream;
	protected OutputStream socketOutputStream;
	protected String host;
	protected int port;
	
	public Connection(String host) throws Exception {
		if (!parseHostString(host)) {
			throw new Exception("Could not parse connection host: " + host);
		}
		Connect();
	}
	
	protected void Connect() throws Exception {
		
		this.socket = new Socket(this.host, this.port);
		this.socketInputStream = this.socket.getInputStream();
		this.socketOutputStream = this.socket.getOutputStream();
	}
	
	// TODO make this a utility, don't modify members directly
	protected boolean parseHostString(String str) {
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
