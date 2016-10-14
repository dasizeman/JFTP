package edu.dasizeman.jftp;

public interface ProtocolManager {
	
	public void Reset();
	
	public void Transiton(Object data) throws ProtocolException; 
	
	public void ControlDataReceived(String data);
	
	public void DataReceived(byte[] data);
	
	public boolean IsReady();
	
	public void CheckException() throws Throwable;
	
}