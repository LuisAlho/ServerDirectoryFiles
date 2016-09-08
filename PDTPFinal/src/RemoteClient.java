

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteClient extends Remote {
	
	
	public void notifyFileList(String msg) throws RemoteException;
	boolean writeFileChunk(byte [] fileChunk, int nbytes) throws java.rmi.RemoteException;

}
