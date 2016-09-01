package directory;
import java.rmi.Remote;
import java.rmi.RemoteException;

import utils.User;

public interface DirService extends Remote {
	
	public String getIpServer(User s) throws RemoteException;

} 
