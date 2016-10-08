package edu.sizetron.jftp;

public class FTPManager implements ProtocolManager {

	static private FTPManager instance;
	public static FTPManager getInstance() {
		if (instance == null) {
			instance = new FTPManager();
		}
		
		return instance;
	}
	
	public FTPManager() {
		
	}

	@Override
	public void Reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Transiton(ProtocolTransition<?, ?> transition) throws ProtocolException {
		// TODO Auto-generated method stub
		
	}

}
