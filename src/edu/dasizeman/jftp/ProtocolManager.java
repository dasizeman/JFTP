package edu.dasizeman.jftp;

public interface ProtocolManager {
	
	public void Reset();
	
	public void Transition(Object data) throws ProtocolException; 
	
	public void ControlDataReceived(String data) throws ProtocolException;
	
	public void DataReceived(byte[] data) throws ProtocolException;
	
	public boolean IsReady();
	
	public void CheckException() throws Throwable;
	
}
