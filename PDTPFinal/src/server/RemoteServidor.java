package server;

import java.rmi.Remote;

import client.RemoteClient;

public interface RemoteServidor extends Remote {

	
	public boolean getFile(String fileName, RemoteClient cli) throws java.rmi.RemoteException;
    
    public void addObserver(RemoteClient observer) throws java.rmi.RemoteException;
    public void removeObserver(RemoteClient observer) throws java.rmi.RemoteException;
    public String getFileList() throws java.rmi.RemoteException;
}
