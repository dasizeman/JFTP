package edu.sizetron.jftp;

public interface ProtocolManager {
	
	public void Reset();
	
	public void Transiton(ProtocolTransition<?, ?> transition) throws ProtocolException; 

}
