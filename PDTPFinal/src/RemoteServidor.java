

import java.rmi.Remote;

public interface RemoteServidor extends Remote {

	
	public boolean getFile(String fileName, RemoteClient cli) throws java.rmi.RemoteException;
	public boolean getFileServer(String fileName, RemoteServidor cli) throws java.rmi.RemoteException;
	boolean writeFileChunk(byte [] fileChunk, int nbytes) throws java.rmi.RemoteException;
    
    public void addObserver(RemoteClient observer) throws java.rmi.RemoteException;
    public void removeObserver(RemoteClient observer) throws java.rmi.RemoteException;
    public String[] getFileList() throws java.rmi.RemoteException;
}
