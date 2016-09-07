package directory;
import java.rmi.Remote;
import java.rmi.RemoteException;

import server.RemoteServidor;
import utils.User;

public interface DirService extends Remote {
	
	public String doAuthentication(User s) throws RemoteException;
	public RemoteServidor getRemoteServerInterface() throws RemoteException;

} 
