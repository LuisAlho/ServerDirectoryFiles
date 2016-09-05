package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteClient extends Remote {
	
	public void notifyClient() throws RemoteException;

}
