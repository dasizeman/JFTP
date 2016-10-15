package edu.dasizeman.jftp;

public interface ProtocolManager {
	
	public void Reset();
	
	public void Transition(Object data) throws Exception; 
	
	public void ControlDataReceived(String data) throws Exception;
	
	public void DataReceived(byte[] data) throws Exception;
	
	public boolean IsReady();
	
	public void CheckException() throws Throwable;
	
}
